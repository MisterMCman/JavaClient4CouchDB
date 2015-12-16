package de.hshannover.couchapp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayDeque;
import java.util.Date;
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
import de.hshannover.RedditPublic;
import de.hshannover.reps.Edge;
import de.hshannover.reps.EdgeRepository;

public class DatabaseFetcher {

	private CouchDbConnector db;
	private CouchDbInstance dbInstance;
	private String nameOfSubReddit;

	private static final int MAX_ROUNDS = 100;
	
	private static final long getDiffInMs(java.util.Date completedRequest, java.util.Date startedRequest) {
		return completedRequest.getTime() - startedRequest.getTime();
	}

	public DatabaseFetcher(String nameOfSubReddit) {
		this.nameOfSubReddit = nameOfSubReddit;
		try {
			HttpClient httpClient = new StdHttpClient.Builder().url(
					"http://localhost:5984").build();

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

		if (!exists)
			setDesignDoc();
		System.out.println("fetching data..");
		loadFromRedditPublic();
		System.out.println("done!");
	}

	private void setDesignDoc() {
		JSONObject designDoc = null;
		try {
			designDoc = new JSONObject(IOUtils.toString(DatabaseFetcher.class
					.getResourceAsStream("/de/hshannover/res/design-docs-views.json")));
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		HttpPut put = new HttpPut("http://localhost:5984/"
				+ nameOfSubReddit.toLowerCase() + "/_design/graphQueries");
		put.addHeader("Content-Type", "application/json");
		put.addHeader("Accept", "application/json");
		try {
			org.apache.http.client.HttpClient httpClient = HttpClientBuilder.create()
					.build();
			put.setEntity(new StringEntity(designDoc.toString()));
			httpClient.execute(put);
		} catch (Exception e) {
			e.printStackTrace();
		}

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
						RedditOAuth.OAUTH_API_DOMAIN + restRequestPath + listingArguments
								+ lastFullname, accessToken.get("access_token").toString());
				round++;
				JSONArray posts = response.getJSONObject("data").getJSONArray(
						"children");
				if (posts.length() < 1 || round >= MAX_ROUNDS)
					done = true;
				for (int i = 0; i < posts.length(); i++) {
					JSONObject cur = (JSONObject) posts.get(i);
					String docId = cur.getJSONObject("data").getString("id");
					lastFullname = cur.getJSONObject("data").getString("name");
					idQueue.add(docId);
				}
			}

			System.out.println("Fetched after " + (round - 1) + " rounds, collected "
					+ idQueue.size() + " IDs.");

			EdgeRepository repo = new EdgeRepository(Edge.class, db);
			idQueue.parallelStream().forEach(
					curId -> processComments(accessToken, repo, curId));
			// for (String curId : idQueue) {
			// processComments(accessToken, repo, curId);
			// }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadFromRedditPublic() {
		String restRequestPath = "/r/" + nameOfSubReddit + "/.json";

		String baseListingArguments = "?limit=100&after=";
		String listingArguments = baseListingArguments;

		int round = 0;
		boolean done = false;
		Queue<String> idQueue = new ArrayDeque<String>();
		
		Date started = new Date();

		while (!done) {
			JSONObject response = null;
			for (int t = 1; t <= 5; t++) {
				try {
					Date startedRequest = new Date();
					
					response = RedditPublic.getObject(RedditPublic.BaseDomain
							+ restRequestPath + listingArguments);
													
					System.out.println("fetch ("+getDiffInMs(new Date(), startedRequest)+"ms): " + RedditPublic.BaseDomain
							+ restRequestPath + listingArguments);
					
					break;
				} catch (Exception ex) {
					System.out.println("failed fetch (" + t + "): "
							+ RedditPublic.BaseDomain + restRequestPath + listingArguments);
				}
			}

			if (response == null) {
				System.out.println("failed to fetch 5 times... what now?");
				// just try another time...
			} else {
				round++;

				JSONObject data = response.getJSONObject("data");

				String after = data.has("after") && !data.isNull("after") ? data
						.getString("after") : null;
				if (after != null && after.length() > 0) {
					listingArguments = baseListingArguments.concat(after);
				} else {
					done = true;
				}
				
				if(!done && round >= MAX_ROUNDS) {
					done = true;
				}

				JSONArray posts = response.getJSONObject("data").getJSONArray(
						"children");

				for (int i = 0; i < posts.length(); i++) {
					JSONObject cur = (JSONObject) posts.get(i);
					String docId = cur.getJSONObject("data").getString("id");
					idQueue.add(docId);
				}
			}
		}

		System.out.println("Fetched after " + (round - 1) + " rounds, collected "
				+ idQueue.size() + " IDs. took: "+getDiffInMs(new Date(), started));

		Date startedComments = new Date();
		
		EdgeRepository repo = new EdgeRepository(Edge.class, db);
		idQueue.parallelStream().forEach(curId -> processComments(repo, curId));
		
		System.out.println("Fetching comments took: "+getDiffInMs(new Date(), startedComments));
	}

	private void processComments(JSONObject accessToken, EdgeRepository repo,
			String curId) {
		JSONArray arrResponse;
		try {
			arrResponse = RedditOAuth.getArray(RedditOAuth.OAUTH_API_DOMAIN
					+ "/comments/" + curId, accessToken.get("access_token").toString());

			String author = ((JSONObject) ((JSONObject) arrResponse.get(0))
					.getJSONObject("data").getJSONArray("children").get(0))
					.getJSONObject("data").getString("author");
			for (int i = 1; i < arrResponse.length(); i++) {
				JSONArray comIt = ((JSONObject) arrResponse.get(i)).getJSONObject(
						"data").getJSONArray("children");
				saveComments(repo, author, comIt);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void processComments(EdgeRepository repo, String curId) {
		String restRequestPath = "/comments/" + curId + "/.json";

		String baseListingArguments = "?limit=100&after=";
		String listingArguments = baseListingArguments;

		JSONArray arrResponse = null;
		for (int t = 1; t <= 5; t++) {
			try {
				System.out.println("fetch: " + RedditPublic.BaseDomain
						+ restRequestPath + listingArguments);

				arrResponse = RedditPublic.getArray(RedditPublic.BaseDomain
						+ restRequestPath + listingArguments);
				
				break;
			} catch (Exception e) {
				System.out.println("failed fetch (" + t + "): "
						+ RedditPublic.BaseDomain + restRequestPath + listingArguments);
			}
		}

		if (arrResponse == null) {
			System.out.println("failed to fetch 5 times... what now?");
			// lets try again?
		} else {
			String author = ((JSONObject) ((JSONObject) arrResponse.get(0))
					.getJSONObject("data").getJSONArray("children").get(0))
					.getJSONObject("data").getString("author");

			for (int i = 1; i < arrResponse.length(); i++) {
				JSONArray comIt = ((JSONObject) arrResponse.get(i)).getJSONObject(
						"data").getJSONArray("children");
				saveComments(repo, author, comIt);
			}
		}
	}

	private void saveComments(EdgeRepository repo, String author, JSONArray comIt) {
		for (int k = 0; k < comIt.length(); k++) {
			JSONObject curData = ((JSONObject) comIt.get(k)).getJSONObject("data");

			if (curData.has("author") && curData.has("created")
					&& curData.has("body")) {
				Edge e = new Edge();
				String newAuthor = curData.getString("author");
				e.setId(getEdgeId(author, newAuthor));
				e.setLastActive(curData.getInt("created"));
				e.setTitle(curData.getString("body"));
				addEdge(repo, e);

				// Rekusives weiterverarbeiten der "replies"
				if (curData.has("replies") && !curData.get("replies").equals("")) {
					saveComments(repo, newAuthor, curData.getJSONObject("replies")
							.getJSONObject("data").getJSONArray("children"));
				}
			}
		}
	}

	private String getEdgeId(String author1, String author2) {
		String first, second;
		if (author1.compareTo(author2) < 0) {
			first = author1;
			second = author2;
		} else {
			first = author2;
			second = author1;
		}
		return "friendship:person:" + first + ":with:person:" + second;
	}

	private void addEdge(EdgeRepository repo, Edge e) {
		Edge olde = null;
		if (repo.contains(e.getId()))
			try {
				olde = repo.get(e.getId());
			} catch (DocumentNotFoundException e2) {
				olde = null;
			}
		if (olde == null)
			repo.add(e);
		else if (olde.getLastActive() < e.getLastActive()) {
			olde.setLastActive(e.getLastActive());
			olde.setTitle(e.getTitle());
			repo.update(olde);
		}
	}

	public CouchDbConnector getDb() {
		return db;
	}
}
