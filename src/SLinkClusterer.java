import java.util.ArrayList;  
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.Comparator;

/**
 * Auxiliary class used to store the data of the edgelists.
 *
 */

class Edge {
	
	public int v1, v2;
	public double distance;
	
	public Edge (int v1, int v2, double distance) {
		
		this.distance = distance;
		this.v1 = v1;
		this.v2 = v2;
	}
}

public class SLinkClusterer {

	private ArrayList <HashMap <String, Integer> > documents;
	private HashMap<String, Integer> allTerms;
	private ArrayList< ArrayList<Integer> > clusters;
	private ArrayList<Integer> head;	// Stores the link to the group head for Union-Find
	private String splitRegex;
	private ArrayList<Edge> edgelist;
        public double lastSSE;
	
	/**
	 * The path compression function for the Union Find Data Structure
	 */
	
	public int pathCompress(int u) {
	
                if (u == head.get(u)) return u;
		else {
			head.set(u, pathCompress(head.get(u)));
			return head.get(u);
		}
	}
	
	/**
	 * 
	 * Method that computes the distances between two document vectors
	 * 
	 * @param a The first HashMap
	 * @param b The second HashMap
	 * @return The distance value as a double
	 */
	
	public double distance(HashMap<String, Integer> a, HashMap<String, Integer> b) {
		
		double dist = 0.0;
		for (Map.Entry<String, Integer> pair : a.entrySet()) {
			if (b.containsKey(pair.getKey())) {
				dist += Math.abs(pair.getValue() - b.get(pair.getKey()));
			} else {
				dist += pair.getValue();
			}
		}
		for (Map.Entry<String, Integer> pair : b.entrySet()) {
			if (!a.containsKey(pair.getKey())) {
				dist += pair.getValue();
			}
		}
		return dist;
	}
	
        /**
         *  Computes the distance between a centroid and a document
         * 
         * @param a The centroid
         * @param b A Document
         * @return  The distance between them
         */
        
        public double distanceCentroid(HashMap<String, Double> a, HashMap<String, Integer> b) {
                
                double dist = 0.0;
		for (Map.Entry<String, Double> pair : a.entrySet()) {
			if (b.containsKey(pair.getKey())) {
				dist += Math.abs(pair.getValue() - b.get(pair.getKey()));
			} else {
				dist += pair.getValue();
			}
		}
		for (Map.Entry<String, Integer> pair : b.entrySet()) {
			if (!a.containsKey(pair.getKey())) {
				dist += pair.getValue();
			}
		}
		return dist;
            
        }
        
	/**
	 * Computes the pair-wise distances between all documents. These represent the
	 * edges of the Graph for which the MST must be found.
	 */
	
	public void computePairwiseDistances () {
		
		for (int i = 0; i < documents.size(); i++) {
			for (int j = i+1; j < documents.size(); j++) {
				edgelist.add(new Edge(i, j, distance(documents.get(i), documents.get(j))));
			}
		}
	}
	
	/**
	 * 
	 * Clusters the data given to it using the Single-Linkage Clustering algorithm
	 * and returns the clusters as an array of arrays of integers.
	 *
	 * @param data The documents' text, one per index of the array
	 * @param numClusters The required number of clusters
	 * @return The clusters, with indices according to the original array 'data' 
	 */
	
	public ArrayList< ArrayList<Integer> > SLinkClustering(String [] data, int numClusters) {
		
		// ArrayList of integers that store the results of the KMeans Clustering
		// where the K'th index contains the indices from 'data' that belong to
		// the K'th cluster.
		clusters = new ArrayList< ArrayList<Integer> >();
		
		for (int i = 0; i < numClusters; i++)
			clusters.add(new ArrayList<Integer>());
		
		// Refresh documents every time new data is given for clustering
		documents = new ArrayList< HashMap<String, Integer> >();
		
		int termindex = 0;
		
		// Populate the HashMaps for the documents
		// The HashMaps compactly represent the document vectors
		
		for (int i = 0; i < data.length; i++) {
			String[] splitdoc = data[i].split(splitRegex);
			documents.add(new HashMap<String, Integer>());
			for (int j = 0; j < splitdoc.length; j++) {
				if (documents.get(i).containsKey(splitdoc[j])) {
					int value = documents.get(i).get(splitdoc[j]);
					documents.get(i).put(splitdoc[j],  value+1);
				} else {
					documents.get(i).put(splitdoc[j],  1);
				}
				if (!documents.get(i).containsKey(splitdoc[j]))
					allTerms.put(splitdoc[j], termindex++);
			}
		}

		head = new ArrayList<Integer>();
		for (int i = 0; i < documents.size(); i++) {
			head.add(i);
		}
		
		edgelist = new ArrayList<Edge>();
		computePairwiseDistances();
		
		Collections.sort(edgelist, new Comparator<Edge>() {
			
			public int compare (Edge e1, Edge e2) {
				if (e1.distance < e2.distance) return -1;
				else if (e1.distance > e2.distance) return 1;
				else return 0;
			}
		});

		// Main SLink clustering loop. Merges the edge with the smallest distance
		// after checking whether this merge will combine two clusters.
		// Terminates when the no. of clusters have reduced to the required no. of
		// clusters.
		
		int curclusters = documents.size();
		int i = 0;
		
		while (curclusters >= numClusters) {
                    
                        System.out.println(curclusters);
			Edge next = edgelist.get(i);
                        i++;
			
			int u = next.v1, v = next.v2;
                        System.out.println(u + " " + v + " " + next.distance);
			
			head.set(head.get(u),  pathCompress(u));
			head.set(head.get(v),  pathCompress(v));
			
			if (head.get(head.get(u)) == head.get(head.get(v))) continue;
			
                        if (head.get(u) < head.get(v))
                            head.set(head.get(head.get(v)), head.get(u));
                        else
                            head.set(head.get(head.get(u)), head.get(v));
			curclusters--;
			
		}
		
		// Map the indices down to the [0, numClusters) range
		
		int curindx = 0;
		HashMap<Integer, Integer> indexMap = new HashMap<Integer, Integer>();
		for (i = 0; i < head.size(); i++) {
                        head.set(i, pathCompress(head.get(i)));
			if (!indexMap.containsKey(head.get(i))) {
				indexMap.put(head.get(i),  curindx);
                                curindx++;
                        }
		}
				
		for (i = 0; i < head.size(); i++) {
                        System.out.println(i + " " + indexMap.get(head.get(i)));
			clusters.get(indexMap.get(head.get(i))).add(i);
		}
				
                calculateSSE(numClusters);
                
		return clusters;
	}
	
        /**
         * Calculates the SSE of the clusters
         * 
         * @param numClusters The no. of clusters
         */
        
        public void calculateSSE(int numClusters) {
            
            ArrayList<HashMap<String, Double>> centroids = new ArrayList<HashMap<String,Double>>();
            ArrayList<Double> clusterDistances = new ArrayList<Double>();
            
            for (int i = 0; i < numClusters; i++) {
                centroids.add(new HashMap<String, Double>());
                clusterDistances.add(0.0);
            }
            
            
            for (int i = 0; i < clusters.size(); i++) {
                for (int j = 0; j < clusters.get(i).size(); j++) {
                    for (Map.Entry<String, Integer> pair : documents.get(clusters.get(i).get(j)).entrySet()) {
                        if (!centroids.get(i).containsKey(pair.getKey()))
                            centroids.get(i).put(pair.getKey(), (double) pair.getValue());
                        else {
                            double prevval = centroids.get(i).get(pair.getKey());
                            centroids.get(i).put(pair.getKey(), prevval + pair.getValue());
                        }
                            
                    }
                }
            }
            
            for (int i = 0; i < centroids.size(); i++)
                for (Map.Entry<String, Double> pair : centroids.get(i).entrySet())
                    centroids.get(i).put(pair.getKey(), pair.getValue() / clusters.get(i).size());
            
            lastSSE = 0.0;
            
            for (int i = 0; i < clusters.size(); i++)
                for (int j = 0; j < clusters.get(i).size(); j++)
                    lastSSE += distanceCentroid(centroids.get(i), documents.get(clusters.get(i).get(j)));
            
        }
        
	/**
	 * Constructor that initializes the regex required for parsing the input documents.
	 */
	
	public SLinkClusterer() {
		
		splitRegex = "[\\s,\\.<>\\[\\]\\{\\}\\(\\)@#%^&\\*-\\+/'\"!]\\s*";
                lastSSE = 0.0;
	}
	
}
