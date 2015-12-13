package de.hshannover.couchapp;

import java.util.ArrayList;
import java.util.List;

import org.ektorp.DocumentNotFoundException;
import org.ektorp.ViewQuery;
import org.ektorp.ViewResult;
import org.json.JSONArray;

import de.hshannover.reps.Node;
import de.hshannover.reps.NodeRepository;

public class CouchApp {
	
	DatabaseFetcher dbfetcher;
	
	public void fetch(String urlToSubReddit) {
		String[] tmp = urlToSubReddit.split("/");
		dbfetcher = new DatabaseFetcher(tmp[tmp.length-1]);
		
		dbfetcher.fetch();
	}
	
	public void use(String nameOfSubReddit) {
		dbfetcher = new DatabaseFetcher(nameOfSubReddit);
	}
	
	public void process() {
		System.out.println("start processing..");
		ViewQuery friendsQuery = new ViewQuery()
		.designDocId("_design/graphQueries")
		.viewName("friends")
		.groupLevel(1);
		
		ViewResult result = dbfetcher.getDb().queryView(friendsQuery);
		NodeRepository repo = new NodeRepository(Node.class, dbfetcher.getDb());
		for (ViewResult.Row row : result.getRows()) {
			Node n = new Node();
			n.setId(row.getKey());
			//TODO comments zählen.. keine Ahnung wie das moeglich sein soll
			n.setComments(0);
			
		    JSONArray jFriends = new JSONArray(row.getValue());
		    List<String> friends = new ArrayList<String>();
		    for (int i = 0; i < jFriends.length(); i++) {
				friends.add((String) jFriends.get(i));
			}
		    n.setFriends(friends);
		    addOrUpdateNode(repo, n);
		}
		System.out.println("done!");
	}
	
	private void addOrUpdateNode(NodeRepository repo, Node n) {
		Node oldn;
		try {
			oldn = repo.get(n.getId());
		} catch (DocumentNotFoundException e) {
			oldn = null;
		}
		if(oldn == null)
			repo.add(n);
		else {
			n.setRevision(oldn.getRevision());
			repo.update(n);
		}
	}
	
	public void degreeCentralityMinMax() {
		ViewQuery degreeQuery = new ViewQuery()
		.designDocId("_design/graphQueries")
		.viewName("degreeMinMax")
		.group(true)
		.descending(true)
		.limit(1);
		
		System.out.println("Max: " + dbfetcher.getDb().queryView(degreeQuery)
		.getRows().get(0).getKey());
		
		degreeQuery.descending(false);
		
		System.out.println("Min: " + dbfetcher.getDb().queryView(degreeQuery)
				.getRows().get(0).getKey());
	}
	
	public void friends(String keyToUser) {
		ViewQuery friends2DQuery = new ViewQuery()
		.designDocId("_design/graphQueries")
		.viewName("friends2D")
		.key(keyToUser);
		
		ViewResult result = dbfetcher.getDb().queryView(friends2DQuery);
		for (ViewResult.Row row : result.getRows()) {
		    System.out.println(row.getValue());
		}
	}

	public void degreeCentrality(String keyToUser) {
		ViewQuery degreeQuery = new ViewQuery()
		.designDocId("_design/graphQueries")
		.viewName("degree")
		.key(keyToUser);
		
		ViewQuery edgesQuery = new ViewQuery()
		.designDocId("_design/graphQueries")
		.viewName("allEdges");
		
		int degree = dbfetcher.getDb().queryView(degreeQuery)
				.getRows().get(0).getValueAsInt();
		
		int edges = dbfetcher.getDb().queryView(edgesQuery)
				.getRows().get(0).getValueAsInt();
		
		System.out.println((double)degree / edges);
	}
}
