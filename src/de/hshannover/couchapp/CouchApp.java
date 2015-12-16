package de.hshannover.couchapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ektorp.DocumentNotFoundException;
import org.ektorp.ViewQuery;
import org.ektorp.ViewResult;
import org.json.JSONArray;

import de.hshannover.reps.BridgeFinder;
import de.hshannover.reps.Edge;
import de.hshannover.reps.EdgeRepository;
import de.hshannover.reps.Graph;
import de.hshannover.reps.Node;
import de.hshannover.reps.NodeRepository;

public class CouchApp {

	DatabaseFetcher dbfetcher;

	public void fetch(String urlToSubReddit) {
		String[] tmp = urlToSubReddit.split("/");
		dbfetcher = new DatabaseFetcher(tmp[tmp.length - 1]);

		dbfetcher.fetch();
	}

	public void use(String nameOfSubReddit) {
		dbfetcher = new DatabaseFetcher(nameOfSubReddit);
	}

	public void process() {
		System.out.println("start processing..");
		ViewQuery friendsQuery = new ViewQuery().designDocId("_design/graphQueries").viewName("friends").groupLevel(1);

		ViewResult result = dbfetcher.getDb().queryView(friendsQuery);
		NodeRepository repo = new NodeRepository(Node.class, dbfetcher.getDb());
		for (ViewResult.Row row : result.getRows()) {
			Node n = new Node();
			n.setId(row.getKey());
			// TODO comments z�hlen.. keine Ahnung wie das moeglich sein soll
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
		if (oldn == null)
			repo.add(n);
		else {
			n.setRevision(oldn.getRevision());
			repo.update(n);
		}
	}

	public void degreeCentralityMinMax() {
		ViewQuery degreeQuery = new ViewQuery().designDocId("_design/graphQueries").viewName("degreeMinMax").group(true)
				.descending(true).limit(1);

		System.out.println("Max: " + dbfetcher.getDb().queryView(degreeQuery).getRows().get(0).getKey());

		degreeQuery.descending(false);

		System.out.println("Min: " + dbfetcher.getDb().queryView(degreeQuery).getRows().get(0).getKey());
	}

	public void bridges() {
		// EdgeRepository kp = new EdgeRepository(Edge.class,
		// dbfetcher.getDb());
		// List<Edge> kanten = kp.getAll();
		//
		//// for (Edge e: kanten){
		//// System.out.println(e.toString());
		//// }

		ViewQuery edges = new ViewQuery().designDocId("_design/graphQueries").viewName("edges");

		ViewResult result = dbfetcher.getDb().queryView(edges);
		System.out.println("RowSize: " + result.getRows().size());

		List<String> numberOfNodes = new ArrayList<String>();

		for (ViewResult.Row row : result.getRows()) {
			// Integer[] i = Integer row.getValue();
			System.out.println(row.getValue());
			
			List<String> str = Arrays.asList(row.getValue().split(":"));
			if (str.get(2).equals(str.get(5))) {
				System.out.println("Nodes identisch!");
				System.out.println();
			} else {
				if (!numberOfNodes.contains(str.get(2))) {
					numberOfNodes.add(str.get(2));
					System.out.println(str.get(2) + " hinzugefügt");
				}
				if (!numberOfNodes.contains(str.get(5))) {
					numberOfNodes.add(str.get(5));
					System.out.println(str.get(5) + " hinzugefügt");
				}
				System.out.println();

			}

			// friendship:person:DenebVegaAltair:with:person:Fautonex
		}
		BridgeFinder bf = new BridgeFinder(numberOfNodes.size());
		for (ViewResult.Row row : result.getRows()) {
			List<String> str = Arrays.asList(row.getValue().split(":"));
			if (str.get(2).equals(str.get(5))) {
			} else {
				bf.addEdgeString(str.get(2), str.get(5));
			}
		}
		bf.bridge();
	}

	public void friends(String keyToUser) {
		ViewQuery friends2DQuery = new ViewQuery().designDocId("_design/graphQueries").viewName("friends2D")
				.key(keyToUser);

		ViewResult result = dbfetcher.getDb().queryView(friends2DQuery);
		for (ViewResult.Row row : result.getRows()) {
			System.out.println(row.getValue());
		}
	}

	public void degreeCentrality(String keyToUser) {
		ViewQuery degreeQuery = new ViewQuery().designDocId("_design/graphQueries").viewName("degree").key(keyToUser);

		ViewQuery edgesQuery = new ViewQuery().designDocId("_design/graphQueries").viewName("allEdges");

		int degree = dbfetcher.getDb().queryView(degreeQuery).getRows().get(0).getValueAsInt();

		int edges = dbfetcher.getDb().queryView(edgesQuery).getRows().get(0).getValueAsInt();

		System.out.println((double) degree / edges);
	}
}
