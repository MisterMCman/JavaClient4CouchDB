package de.hshannover.couchapp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayDeque;
import java.util.Queue;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.DocumentNotFoundException;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hshannover.RedditOAuth;
import de.hshannover.reps.Edge;
import de.hshannover.reps.EdgeRepository;

public class DatabaseFetcher {
	
	private CouchDbConnector db;
	private CouchDbInstance dbInstance;
	private String nameOfSubReddit;
	
	private static final int MAX_ROUNDS = 10;
	
	public DatabaseFetcher(String nameOfSubReddit) {
		this.nameOfSubReddit = nameOfSubReddit;
		try {
			HttpClient httpClient = new StdHttpClient.Builder()
			.url("http://localhost:5984")
			.build();
			
			dbInstance = new StdCouchDbInstance(httpClient);
			db = new StdCouchDbConnector(nameOfSubReddit, dbInstance);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.err.println("DB Connection failed");
		}
	}
	
	public void fetch() {
		Boolean exists = dbInstance.checkIfDbExists(nameOfSubReddit);
		db.createDatabaseIfNotExists();
		
		if(!exists)
			setDesignDoc();
		System.out.println("fetching data..");
		loadFromReddit();
		System.out.println("done!"
				+ "");
	}
	
	private void setDesignDoc() {
		JSONObject designDoc = null;
		try {
			designDoc = new JSONObject(IOUtils.toString(
					DatabaseFetcher.class.
					getResourceAsStream("/de/hshannover/res/design-docs-views.json")));
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		HttpPut put = new HttpPut("http://localhost:5984/" + 
				nameOfSubReddit.toLowerCase() + 
				"/_design/graphQueries");
		put.addHeader("Content-Type", "application/json");
		put.addHeader("Accept", "application/json");
		try {
			org.apache.http.client.HttpClient httpClient = HttpClientBuilder.create().build();
			put.setEntity(new StringEntity(designDoc.toString()));
			httpClient.execute(put);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
//		httpClient.put("http://localhost:5984/" + 
//				nameOfSubReddit + 
//				"/_design/graphQueries", designDoc.toString());
	}

	private void loadFromReddit() {
		String curCode = new Long(System.currentTimeMillis()).toString();
		String restRequestPath = "/r/" + nameOfSubReddit;

		String listingArguments = "?limit=100&after=";
		String lastFullname = "";
		try {
			int round = 0;
			boolean done = false;
			Queue<String> idQueue = new ArrayDeque<String>();
			JSONObject accessToken = RedditOAuth.getToken(curCode);
			while (!done) {
				JSONObject response = RedditOAuth.getObject(
						RedditOAuth.OAUTH_API_DOMAIN + restRequestPath + listingArguments + lastFullname,
						accessToken.get("access_token").toString());
				round++;
				JSONArray posts = response.getJSONObject("data").getJSONArray("children");
				if(posts.length() < 1 || round >= MAX_ROUNDS)
					done = true;
				for (int i = 0; i < posts.length(); i++) {
					JSONObject cur = (JSONObject) posts.get(i);
					String docId = cur.getJSONObject("data").getString("id");
					lastFullname = cur.getJSONObject("data").getString("name");
					idQueue.add(docId);
				}
			}
			
			System.out.println("Fetched after "+(round-1)+" rounds, collected "+idQueue.size()+ " IDs.");
			
			EdgeRepository repo = new EdgeRepository(Edge.class, db);
			
			for (String curId : idQueue) {
				JSONArray arrResponse = RedditOAuth.getArray(RedditOAuth.OAUTH_API_DOMAIN + "/comments/" + curId,
						accessToken.get("access_token").toString());
				JSONObject author = (JSONObject) ((JSONObject) arrResponse.get(0)).getJSONObject("data").getJSONArray("children").get(0);
				for(int i = 1; i < arrResponse.length(); i++) {
					JSONArray comIt = ((JSONObject) arrResponse.get(i)).getJSONObject("data").getJSONArray("children");
					for(int k = 0; k < comIt.length(); k++) {
						JSONObject cur = (JSONObject) comIt.get(k);
						// Do something reasonable with the comment here, i.e.
						// insert into graph
						
						Edge e = new Edge();
						e.setId(getEdgeId(author.getJSONObject("data").getString("author")
								,cur.getJSONObject("data").getString("author")));
						e.setLastActive(cur.getJSONObject("data").getInt("created"));
						e.setTitle(cur.getJSONObject("data").getString("body"));
						
//						System.out.println(getEdgeId(author.getJSONObject("data").getString("author")
//								,cur.getJSONObject("data").getString("author") ));
						addEdge(repo, e);
//						repo.add(e);
						//TODO Rekusives weiterverarbeiten der "replies", momentan werden nur Top-Level Kommentare eingelesen
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	private String getEdgeId(String author1, String author2) {
		String first, second;
		if(author1.compareTo(author2) < 0) {
			first = author1;
			second = author2;
		} else {
			first = author2;
			second = author1;
		}
		return "friendship:person:" + first + ":with:person:" + second;
	}
	
	private void addEdge(EdgeRepository repo, Edge e) {
		Edge olde;
		try {
			olde = repo.get(e.getId());
		} catch (DocumentNotFoundException e2) {
			olde = null;
		}
		if(olde == null)
			repo.add(e);
		else if (olde.getLastActive() < e.getLastActive()){
			olde.setLastActive(e.getLastActive());
			olde.setTitle(e.getTitle());
			repo.update(olde);
		}
	}
	
	public CouchDbConnector getDb() {
		return db;
	}
}
