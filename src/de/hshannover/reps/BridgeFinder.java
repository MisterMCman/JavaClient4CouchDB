package de.hshannover.reps;

//A Java program to find bridges in a given undirected graph
import java.io.*;
import java.util.*;

import com.sun.glass.events.mac.NpapiEvent;

//This class represents a undirected graph using adjacency list
//representation
public class BridgeFinder {
	public Map<String, Integer> nodeIntMap = new HashMap<String, Integer>();
	public Map<Integer, String> intNodeMap = new HashMap<Integer, String>();
	private int V; // No. of vertices
	public Integer counter = 0;
	public int nBridgesFound = 0;

	// Array of lists for Adjacency List Representation
	private LinkedList<Integer> adj[];
	int time = 0;
	static final int NIL = -1;

	// Constructor
	public BridgeFinder(int v) {
		V = v;
		adj = new LinkedList[v];
		for (int i = 0; i < v; ++i)
			adj[i] = new LinkedList();
	}

	// Function to add an edge into the graph
	void addEdge(int v, int w) {
		adj[v].add(w); // Add w to v's list.
		adj[w].add(v); // Add v to w's list
	}

	// A recursive function that finds and prints bridges
	// using DFS traversal
	// u --> The vertex to be visited next
	// visited[] --> keeps tract of visited vertices
	// disc[] --> Stores discovery times of visited vertices
	// parent[] --> Stores parent vertices in DFS tree
	void bridgeUtil(int u, boolean visited[], int disc[], int low[], int parent[]) {
		// Count of children in DFS Tree
		int children = 0;

		// Mark the current node as visited
		visited[u] = true;

		// Initialize discovery time and low value
		disc[u] = low[u] = ++time;

		// Go through all vertices aadjacent to this
		Iterator<Integer> i = adj[u].iterator();
		while (i.hasNext()) {
			int v = i.next(); // v is current adjacent of u

			// If v is not visited yet, then make it a child
			// of u in DFS tree and recur for it.
			// If v is not visited yet, then recur for it
			if (!visited[v]) {
				parent[v] = u;
				bridgeUtil(v, visited, disc, low, parent);

				// Check if the subtree rooted with v has a
				// connection to one of the ancestors of u
				low[u] = Math.min(low[u], low[v]);

				// If the lowest vertex reachable from subtree
				// under v is below u in DFS tree, then u-v is
				// a bridge
				
				if (low[v] > disc[u]) {
					// System.out.println(u+" "+v);
					nBridgesFound++;
					System.out.println(intNodeMap.get(u) + "\t <==> \t" + intNodeMap.get(v) + " " +nBridgesFound);
				}

			}

			// Update low value of u for parent function calls.
			else if (v != parent[u])
				low[u] = Math.min(low[u], disc[v]);
		}
	}
	
	// DFS based function to find all bridges. It uses recursive
	// function bridgeUtil()
	public void bridge() {
		System.out.println();
		System.out.println("---------Gefundene Bridges---------");
		// Mark all the vertices as not visited
		boolean visited[] = new boolean[V];
		int disc[] = new int[V];
		int low[] = new int[V];
		int parent[] = new int[V];

		// Initialize parent and visited, and ap(articulation point)
		// arrays
		for (int i = 0; i < V; i++) {
			parent[i] = NIL;
			visited[i] = false;
		}

		// Call the recursive helper function to find Bridges
		// in DFS tree rooted with vertex 'i'
		for (int i = 0; i < V; i++)
			if (visited[i] == false)
				bridgeUtil(i, visited, disc, low, parent);
		System.out.println("---------" + nBridgesFound +" Bridges gefunden------");
		
	}

	public void addEdgeString(String node1, String node2) {
		if (!nodeIntMap.containsKey(node1)) {
			nodeIntMap.put(node1, counter);
			intNodeMap.put(counter, node1);
			counter++;
		}
		if (!nodeIntMap.containsKey(node2)) {
			nodeIntMap.put(node2, counter);
			intNodeMap.put(counter, node2);
			counter++;
		}

		addEdge(nodeIntMap.get(node1), nodeIntMap.get(node2));

	}
}