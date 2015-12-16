package de.hshannover.reps;

//A Java program to find bridges in a given undirected graph
import java.io.*;
import java.util.*;

//This class represents a undirected graph using adjacency list
//representation
public class Graph
{
 private int V;   // No. of vertices

 // Array  of lists for Adjacency List Representation
 //private LinkedList<String> adj[];
 public Map<String, String> adj = new HashMap<String, String>();

 
 String time;
 static final int NIL = -1;

 // Constructor
 public Graph(int v)
 {
     V = v;
     //adj = new LinkedList[v];
//     for (int i=0; i<v; ++i)
//         adj[i] = new LinkedList();
// 
     }

 // Function to add an edge into the graph
 public void addEdge(String v, String w)
 {
	 adj.put(v, w);
	 adj.put(w, v);
	 
//     adj[v].add(w);  // Add w to v's list.
//     adj[w].add(v);  //Add v to w's list
 }

 // A recursive function that finds and prints bridges
 // using DFS traversal
 // u --> The vertex to be visited next
 // visited[] --> keeps tract of visited vertices
 // disc[] --> Stores discovery times of visited vertices
 // parent[] --> Stores parent vertices in DFS tree
 

 void bridgeUtil(String u, Map<String, Boolean> visited, Map<String, String> disc,
		 Map<String, String> low, Map<String, String> parent)
 {

     // Count of children in DFS Tree
     int children = 0;

     // Mark the current node as visited
     visited.put(u, true);
     System.out.println(u+" is visited");

     // Initialize discovery time and low value
     disc.put(u,u);
     low.put(u,u);

     // Go through all vertices aadjacent to this
     //Iterator<String> i = adj[u].iterator();
     System.out.println("adj-groesse" + adj.size());
     for (String key : adj.keySet()) {
    	 if (visited.get(key)==null || !visited.get(key))
         {
             parent.put(key, u);
             
             
             bridgeUtil(key, visited, disc, low, parent);

             // Check if the subtree rooted with v has a
             // connection to one of the ancestors of u
             String temp = low.get(u).hashCode() < low.get(key).hashCode()? low.get(u) : low.get(key);
             
             low.put(u ,temp);

             // If the lowest vertex reachable from subtree
             // under v is below u in DFS tree, then u-v is
             // a bridge
             if(low.get(key).hashCode() > disc.get(u).hashCode()) {
            	 System.out.println(u+" "+key);
             }
             else System.out.println("kleiner");
                 
         }

         // Update low value of u for parent function calls.
         else if (key != parent.get(u)){
        	 System.out.println();
        	 String temp = low.get(u).hashCode() < disc.get(key).hashCode()? low.get(u) : disc.get(key);
             low.put(u, temp);
             }
     
    	 
     }
 }


 // DFS based function to find all bridges. It uses recursive
 // function bridgeUtil()
 public void bridge()
 {
     // Mark all the vertices as not visited
    
     
     Map<String, Boolean> visited = new HashMap<String, Boolean>();
     Map<String, String> disc = new HashMap<String, String>();
	 Map<String, String> low = new HashMap<String, String>();
	 Map<String, String> parent = new HashMap<String, String>();


     // Initialize parent and visited, and ap(articulation point)
     // arrays
//     for (int i = 0; i < V; i++)
//     {
//         parent[i] = NIL;
//         visited[i] = false;
//     }

     // Call the recursive helper function to find Bridges
     // in DFS tree rooted with vertex 'i'
	 System.out.println("adj-groesse " + adj.size());
	 for (String key : adj.keySet()) {
		 System.out.println(visited.get(key));
    	 if (visited.get(key)==null||!visited.get(key))
         {
    		 bridgeUtil(key, visited, disc, low, parent);
    		 
         }
	 }
 }

 public static void main(String args[])
 {
     // Create graphs given in above diagrams
     System.out.println("Bridges in first graph ");
     Graph g1 = new Graph(5);
     g1.addEdge("1", "0");
     g1.addEdge("0", "2");
     g1.addEdge("2", "1");
     g1.addEdge("0", "3");
     g1.addEdge("3", "4");
     g1.bridge();
     System.out.println();

//     System.out.println("Bridges in Second graph");
//     Graph g2 = new Graph(4);
//     g2.addEdge(0, 1);
//     g2.addEdge(1, 2);
//     g2.addEdge(2, 3);
//     g2.bridge();
//     System.out.println();
//
//     System.out.println("Bridges in Third graph ");
//     Graph g3 = new Graph(7);
//     g3.addEdge(0, 1);
//     g3.addEdge(1, 2);
//     g3.addEdge(2, 0);
//     g3.addEdge(1, 3);
//     g3.addEdge(1, 4);
//     g3.addEdge(1, 6);
//     g3.addEdge(3, 5);
//     g3.addEdge(4, 5);
//     g3.bridge();
//     String test = "HallO";
//     Integer i = test.a;
 }
}