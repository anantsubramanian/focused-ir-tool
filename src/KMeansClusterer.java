import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class KMeansClusterer {

	private ArrayList <HashMap <String, Integer> > documents;
	private HashMap<String, Integer> allTerms;
	private String splitRegex;
	private ArrayList< ArrayList <Integer> > clusters;
	private ArrayList<Integer> documentCluster;
	private ArrayList< HashMap<String, Double> > centroids;
	private ArrayList<Integer> clustercounts;
        public double lastSSE;
	private Random rand;
	
	/**
	 * 
	 * Method that computes the distances between two document vectors
	 * 
	 * @param a The first HashMap
	 * @param b The second HashMap
	 * @return The distance value as a double
	 */
	
	public double distance(HashMap<String, Double> a, HashMap<String, Integer> b) {
		
		double dist = 0.0;
		for (Map.Entry<String, Double> pair : a.entrySet()) {
			if (b.containsKey(pair.getKey())) {
				dist += Math.pow(pair.getValue() - b.get(pair.getKey()), 2);
			} else {
				dist += pair.getValue() * pair.getValue();
			}
		}
		for (Map.Entry<String, Integer> pair : b.entrySet()) {
			if (!a.containsKey(pair.getKey())) {
				dist += pair.getValue() * pair.getValue();
			}
		}
		return Math.sqrt(dist);
	}
	
	/**
	 * 
	 * Method to initialize the clusters required for K-Means clustering, by
	 * initializing each one to be as far away from the others as possible
	 * 
	 * @param K The number of clusters
	 */
	
	public void initializeClusters(int K) {

		int randomIndex = rand.nextInt(documents.size());
		centroids = new ArrayList < HashMap<String, Double> >();
		centroids.add(new HashMap<String, Double>());
		for (Map.Entry<String, Integer> pair : documents.get(randomIndex).entrySet()) {
			centroids.get(0).put(pair.getKey(), (double) pair.getValue());
		}
		
		for (int i = 1; i < K; i++) {
			double maxdist = 0.0;
			int chosendoc = 0;
			for (int j = 0; j < documents.size(); j++) {
				double d = 0.0;
				for (int l = 0; l < centroids.size(); l++)
					d += distance(centroids.get(l), documents.get(j));
				
				if (d > maxdist) {
					maxdist = d;
					chosendoc = j;
				}
			}
			
			centroids.add(new HashMap<String, Double>());
			for (Map.Entry<String, Integer> pair : documents.get(chosendoc).entrySet()) {
				centroids.get(i).put(pair.getKey(), (double) pair.getValue());
			}
		}
		
	}
	
	/**
	 * Method that reassigns each document to its closest cluster on the
	 * basis of the centroids calculated during the previous iteration.
	 */
	
	public void reassignClusters() {
		
		clustercounts = new ArrayList<Integer>();
		for (int i = 0; i < centroids.size(); i++)
			clustercounts.add(0);
		
		for (int i = 0; i < documents.size(); i++) {
			double min = distance(centroids.get(0), documents.get(i));
			int cluster = 0;
			for (int j = 1; j < centroids.size(); j++) {
				double d = distance(centroids.get(j), documents.get(i));
				if (d < min) {
					min = d;
					cluster = j;
				}
			}
			documentCluster.set(i, cluster);
			clustercounts.set(cluster, clustercounts.get(cluster) + 1);
		}
		
                // While there exist empty clusters, randomly assign a point 
                // as a new centroid
		for (int i = 0; i < clustercounts.size(); i++)
			if (clustercounts.get(i) == 0) {
                                int randomDoc = rand.nextInt(documents.size());
                                clustercounts.set(documentCluster.get(randomDoc), clustercounts.get(documentCluster.get(randomDoc)) - 1);
				documentCluster.set(randomDoc, i);
                                clustercounts.set(i, clustercounts.get(i) + 1);
                        }
                
	}
	
	/**
	 * Method that updates the centroids of the clusters on the basis of the documents assigned
	 * to them during the previous iteration.
	 */
	
	public void updateCentroids() {
		
		for (int i = 0; i < centroids.size(); i++)
			centroids.set(i, new HashMap<String, Double>());
		
		// For each document, update the term frequencies of the occuring words in its cluster.
		for (int i = 0; i < documents.size(); i++)
			for (Map.Entry<String, Integer> pair : documents.get(i).entrySet()) {
				if (centroids.get(documentCluster.get(i)).containsKey(pair.getKey())) {
					double prevval = centroids.get(documentCluster.get(i)).get(pair.getKey());
					centroids.get(documentCluster.get(i)).put(pair.getKey(), prevval + pair.getValue());
				} else {
					centroids.get(documentCluster.get(i)).put(pair.getKey(), (double) pair.getValue());
				}
			}
		
		// Find the centroid for each cluster by dividing the term frequency by the number of
		// documents in the cluster.
		for (int i = 0; i < centroids.size(); i++) {
			for (Map.Entry<String, Double> pair : centroids.get(i).entrySet()) {
				centroids.get(i).put(pair.getKey(), pair.getValue() / clustercounts.get(i));
			}
		}
		
	}
	
	/**
	 * 	
	 * @param a The clusters counts in the 1st clustering
	 * @param b Cluster counts for the second clustering
	 * @return {int} The total change in the number of documents in clusters.
	 */
	
	public int calculateChange(ArrayList<Integer> a, ArrayList<Integer> b) {
		
		int answer = 0;
		for (int i = 0; i < a.size(); i++)
			answer += Math.abs(a.get(i) - b.get(i));
		return answer;
	}
	
	/**
	 * 
	 * Clusters the data given to it using the K-Means Clustering algorithm
	 * and returns the clusters as an array of arrays of integers.
	 *
	 * @param data The documents' text, one per index of the array
	 * @param K The required number of clusters
	 * @return The clusters, with indices according to the original array 'data' 
	 */
	
	public ArrayList<ArrayList<Integer>> KMeansClustering(String [] data, int K) {
		
		// ArrayList of integers that store the results of the KMeans Clustering
		// where the K'th index contains the indices from 'data' that belong to
		// the K'th cluster.
		clusters = new ArrayList< ArrayList<Integer> >();
		clustercounts = new ArrayList<Integer>();
		
		for(int i = 0; i < K; i++) {
			clusters.add(new ArrayList<Integer>());
			clustercounts.add(0);
		}
		
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

		initializeClusters(K);
		documentCluster = new ArrayList<Integer>();
		for (int i = 0; i < documents.size(); i++)
			documentCluster.add(0);
		
		// Main KMeans loop
		// Reassign and Update the clusters, as long as the clustering changes
		// i.e. as long as it has not stabilized.
		
		int change = 0;
                Boolean emptyCluster = false;
                int iterations = 0;
		do {
			ArrayList<Integer> oldcounts = clustercounts;
			reassignClusters();
			updateCentroids();
			change = calculateChange(oldcounts, clustercounts);
                        for (int i = 0; i < clustercounts.size(); i++)
                            if (clustercounts.get(i) == 0) {
                                emptyCluster = true;
                                break;
                            }
                        System.out.println(iterations++);
		} while (change > 0 || emptyCluster);
		
		for (int i = 0; i < documentCluster.size(); i++) {
			clusters.get(documentCluster.get(i)).add(i);
		}
	
                calculateSSE(K);
                
		return clusters;
	}
	
        /**
         * 
         * @param K The no. of clusters
         */
        
        public void calculateSSE(int K) {
            
            ArrayList<Double> clustersDists = new ArrayList<Double>();
            for (int i = 0; i < K; i++)
                clustersDists.add(0.0);
            
            
            for (int i = 0; i < documents.size(); i++) {
                clustersDists.set(documentCluster.get(i), 
                                  clustersDists.get(documentCluster.get(i)) + 
                                  distance(centroids.get(documentCluster.get(i)), 
                                           documents.get(i)));
            }
            
            for (int i = 0; i < clusters.size(); i++) {
                for (int j = 0; j < clusters.get(i).size(); j++)
                    System.out.print(clusters.get(i).get(j) + " ");
                System.out.println("");
            }
            
            for (int i = 0; i < clustercounts.size(); i++)
                System.out.println("Size of " + i + " = " + clustercounts.get(i));
                
            lastSSE = 0;
            for (int i = 0; i < clustersDists.size(); i++) {
                lastSSE += clustersDists.get(i);
                System.out.println(clustersDists.get(i));
            }
            
        }
        
	/**
	 * Constructor that initializes the Random variable required for initialization,
	 * and the regex required for parsing the input documents.
	 */
	
	public KMeansClusterer() {
		splitRegex = "[\\s,\\.<>\\[\\]\\{\\}\\(\\)@#%^&\\*-\\+/'\"!]\\s*";
		rand = new Random();
                lastSSE = -1;
	}
	
}
