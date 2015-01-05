
import java.awt.event.ItemEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.crawl.Crawl;
import org.apache.nutch.indexer.solr.SolrIndexer;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.core.CoreContainer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


class LogReader implements Runnable {

    private BufferedReader reader;

    public LogReader(InputStream is) {
        this.reader = new BufferedReader(new InputStreamReader(is));
    }

    public void run() {
        try {
            String line = reader.readLine();
            while (line != null) {
                System.out.println(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


class DocumentResult {
    
    public int sno;
    public String title;
    public String url;
    public int cluster;
    public double score;
    
    public DocumentResult(int sno, String title, String url, int cluster, double score) {
        
        this.sno = sno;
        this.title = title;
        this.url = url;
        this.cluster = cluster;
        this.score = score;
    }
}

/**
 *
 * @author anant
 */
public class MainGUI extends javax.swing.JFrame {

    private int threshold;
    private int wiggle;
    private int clusterCount;
    private String[] keywordList;
    private Boolean needsCrawl;
    private Boolean needsClustering;
    private Boolean isKMeans;
    private Boolean clusterChecked;
    private Boolean solrChosen;
    private Boolean javaChosen;
    private Boolean solrTested;
    private Boolean needToPose;
    private String javaPath;
    private String solrPath;
    private String examplePath;
    private String nutchPath;
    private String queryString;
    private HttpSolrServer solr;
    private HashMap<String, Integer> docIdMap;
    private HashMap<Integer, String> idUrlMap;
    private KMeansClusterer KMClusterer;
    private SLinkClusterer SLINKClusterer;
    private ArrayList< ArrayList<Integer> > clusters;
    private ArrayList<DocumentResult> lastResult;
    private static final int SLINK = 0;
    private static final int KMEANS = 1;
    
    public MainGUI() {
        needsCrawl = false;
        needsClustering = true;
        isKMeans = true;
        clusterChecked = false;
        solrChosen = false;
        javaChosen = false;
        solrTested = false;
        needToPose = true;
        KMClusterer = new KMeansClusterer();
        SLINKClusterer = new SLinkClusterer();
        initComponents();
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        solrBrowse = new javax.swing.JButton();
        javaBrowse = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        startSolr = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        testSolr = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        keywordsArea = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        shouldCluster = new javax.swing.JCheckBox();
        startCrawling = new javax.swing.JButton();
        clusterData = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        clusteringAlgorithm = new javax.swing.JList();
        thresholdValue = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        numClusters = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        wiggleRoom = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        crawlProgress = new javax.swing.JProgressBar();
        jPanel6 = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        poseQuery = new javax.swing.JButton();
        queryField = new javax.swing.JTextField();
        jPanel10 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        resultsTable = new javax.swing.JTable();
        jPanel12 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        sortClusterNumber = new javax.swing.JButton();
        computeSSE = new javax.swing.JButton();
        computedSSE = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        sortSerialNumber = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Focused IR Tool");
        setPreferredSize(new java.awt.Dimension(900, 800));
        setResizable(false);
        getContentPane().setLayout(new java.awt.FlowLayout());

        jPanel1.setLayout(new java.awt.GridLayout(1, 0));

        jLabel1.setFont(new java.awt.Font("Lucida MAC", 0, 16)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Crawling and Indexing");
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel1.setAlignmentY(0.0F);
        jLabel1.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(228, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(205, 205, 205))
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addGap(0, 412, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 196, Short.MAX_VALUE)))
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addGap(0, 407, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 191, Short.MAX_VALUE)))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addGap(0, 18, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 7, Short.MAX_VALUE)))
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addGap(0, 13, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 2, Short.MAX_VALUE)))
        );

        getContentPane().add(jPanel3);

        jPanel8.setPreferredSize(new java.awt.Dimension(850, 10));

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 850, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel8);

        jPanel14.setPreferredSize(new java.awt.Dimension(115, 300));

        solrBrowse.setText("Solr (Browse)");
        solrBrowse.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                solrBrowseMouseClicked(evt);
            }
        });

        javaBrowse.setText("Java (Browse)");
        javaBrowse.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                javaBrowseMouseClicked(evt);
            }
        });

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("Set Solr Path");

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("Set Java Path");

        startSolr.setText("Start Server");
        startSolr.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                startSolrMouseClicked(evt);
            }
        });

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("Start Solr");

        testSolr.setText("Test Server");
        testSolr.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                testSolrMouseClicked(evt);
            }
        });

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("Test Solr");

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(testSolr, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(javaBrowse, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(solrBrowse, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(startSolr, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 21, Short.MAX_VALUE))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(solrBrowse)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(javaBrowse)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(startSolr)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(testSolr)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel14);

        jPanel13.setPreferredSize(new java.awt.Dimension(500, 325));

        keywordsArea.setColumns(55);
        keywordsArea.setLineWrap(true);
        keywordsArea.setRows(15);
        keywordsArea.setTabSize(4);
        keywordsArea.setText("Enter keywords to use for crawling over here, each one on a new line. The Threshold value and Wiggle Room can be entered on the right to determine the minimum frequency of the keywords on a web page.");
        keywordsArea.setWrapStyleWord(true);
        keywordsArea.setPreferredSize(new java.awt.Dimension(45, 325));
        keywordsArea.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                keywordsAreaFocusLost(evt);
            }
        });
        jScrollPane2.setViewportView(keywordsArea);

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 480, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel13Layout.createSequentialGroup()
                .addContainerGap(13, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        getContentPane().add(jPanel13);

        jPanel4.setLayout(new java.awt.BorderLayout());

        shouldCluster.setText("Cluster Results");
        shouldCluster.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                shouldClusterItemStateChanged(evt);
            }
        });

        startCrawling.setText("Start Crawling");
        startCrawling.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                startCrawlingMouseClicked(evt);
            }
        });

        clusterData.setText("Cluster Data");
        clusterData.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                clusterDataMouseClicked(evt);
            }
        });

        clusteringAlgorithm.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "SLINK Hierarchical", "K-Means Flat" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        clusteringAlgorithm.setNextFocusableComponent(numClusters);
        clusteringAlgorithm.setSelectedIndex(1);
        clusteringAlgorithm.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                clusteringAlgorithmMousePressed(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                clusteringAlgorithmMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(clusteringAlgorithm);

        thresholdValue.setText("0");
        thresholdValue.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                thresholdValueFocusLost(evt);
            }
        });

        jLabel3.setText("Threshold");

        numClusters.setText("0");
        numClusters.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                numClustersFocusLost(evt);
            }
        });

        jLabel4.setText("No. of Clusters");

        wiggleRoom.setText("0");
        wiggleRoom.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                wiggleRoomFocusLost(evt);
            }
        });

        jLabel6.setText("Wiggle Room");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(numClusters, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(startCrawling, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1)
                    .addComponent(shouldCluster, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(clusterData, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(wiggleRoom, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 51, Short.MAX_VALUE)
                            .addComponent(thresholdValue, javax.swing.GroupLayout.Alignment.TRAILING)))))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3)
                    .addComponent(thresholdValue, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(wiggleRoom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(startCrawling, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(shouldCluster, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(numClusters, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(clusterData, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel4.add(jPanel5, java.awt.BorderLayout.LINE_START);

        getContentPane().add(jPanel4);

        crawlProgress.setPreferredSize(new java.awt.Dimension(300, 20));
        getContentPane().add(crawlProgress);

        jPanel6.setPreferredSize(new java.awt.Dimension(835, 20));

        jSeparator1.setBackground(javax.swing.UIManager.getDefaults().getColor("Separator.background"));
        jSeparator1.setForeground(javax.swing.UIManager.getDefaults().getColor("Separator.foreground"));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        getContentPane().add(jPanel6);

        jLabel2.setFont(new java.awt.Font("Lucida MAC", 0, 16)); // NOI18N
        jLabel2.setText("Querying and Clustering");
        getContentPane().add(jLabel2);

        jPanel7.setPreferredSize(new java.awt.Dimension(800, 5));

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 800, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 5, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel7);

        jPanel9.setPreferredSize(new java.awt.Dimension(805, 40));

        poseQuery.setText("Pose Query");
        poseQuery.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                poseQueryMouseClicked(evt);
            }
        });

        queryField.setText("Enter your query here. Example: \" ( cat AND fish ) OR ( dog AND bone ) \"");
        queryField.setPreferredSize(new java.awt.Dimension(700, 33));
        queryField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                queryFieldFocusLost(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addComponent(queryField, javax.swing.GroupLayout.PREFERRED_SIZE, 688, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(poseQuery, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(poseQuery)
                    .addComponent(queryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 3, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel9);

        jPanel10.setPreferredSize(new java.awt.Dimension(800, 5));

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 800, Short.MAX_VALUE)
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 5, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel10);

        jScrollPane3.setPreferredSize(new java.awt.Dimension(625, 260));

        resultsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "S. No.", "Document Name", "URL", "Cluster"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        resultsTable.setPreferredSize(new java.awt.Dimension(550, 5000));
        resultsTable.getTableHeader().setResizingAllowed(false);
        resultsTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane3.setViewportView(resultsTable);

        getContentPane().add(jScrollPane3);

        jPanel12.setPreferredSize(new java.awt.Dimension(15, 5));

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 5, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel12);

        sortClusterNumber.setText("Sort by Cluster No.");
        sortClusterNumber.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sortClusterNumberMouseClicked(evt);
            }
        });

        computeSSE.setText("Compute SSE");
        computeSSE.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                computeSSEMouseClicked(evt);
            }
        });

        computedSSE.setEditable(false);

        jLabel5.setText("Computed SSE Value");

        sortSerialNumber.setText("Sort by Serial No.");
        sortSerialNumber.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sortSerialNumberMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(computedSSE)
                    .addComponent(sortClusterNumber, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(computeSSE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sortSerialNumber, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addComponent(sortClusterNumber)
                .addGap(18, 18, 18)
                .addComponent(sortSerialNumber)
                .addGap(18, 18, 18)
                .addComponent(computeSSE)
                .addGap(18, 18, 18)
                .addComponent(jLabel5)
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(computedSSE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        getContentPane().add(jPanel11);

        pack();
    }

    private void thresholdValueFocusLost(java.awt.event.FocusEvent evt) {
        try {
            threshold = Integer.parseInt(thresholdValue.getText());
        } catch (Exception e) {
            thresholdValue.setText("0");
            JOptionPane.showMessageDialog(this, "Please enter a valid Integer value for Threshold!");
        }
    }

    private void wiggleRoomFocusLost(java.awt.event.FocusEvent evt) {
        try {
            wiggle = Integer.parseInt(wiggleRoom.getText());
        } catch (Exception e) {
            wiggleRoom.setText("0");
            JOptionPane.showMessageDialog(this, "Please enter a valid Integer value for Wiggle Room!");
        }
    }

    private void clusteringAlgorithmMousePressed(java.awt.event.MouseEvent evt) {
        if (clusteringAlgorithm.getSelectedIndex() == SLINK)
            isKMeans = false;
        else
            isKMeans = true;
    }

    private void clusteringAlgorithmMouseClicked(java.awt.event.MouseEvent evt) {
        if (clusteringAlgorithm.getSelectedIndex() == SLINK)
            isKMeans = false;
        else
            isKMeans = true;
        
        needToPose = true;
    }

    private void numClustersFocusLost(java.awt.event.FocusEvent evt) {
        try {
            clusterCount = Integer.parseInt(numClusters.getText());
        } catch (Exception e) {
            numClusters.setText("0");
            JOptionPane.showMessageDialog(this, "Please enter a valid Integer value for the number of Clusters!");
        }
        
        needToPose = true;
    }

    private void clusterDataMouseClicked(java.awt.event.MouseEvent evt) {
        if (!solrTested) {
            JOptionPane.showMessageDialog(this, "Please test the server at-least once before attempting operations.");
            return;
        }
        
        if (clusterCount <= 0) {
            JOptionPane.showMessageDialog(this, "Please enter a non-zero number of clusters.");
            return;
        }
        
        SolrQuery query = new SolrQuery();
        query.setQuery("*:*");
        query.setRows(Integer.MAX_VALUE);
        query.setFields("title", "url", "content");
        
        docIdMap = new HashMap<String, Integer>();
        idUrlMap = new HashMap<Integer, String>();
        String[] data;
        
        try {
            QueryResponse response = solr.query(query);
            SolrDocumentList results = response.getResults();
            data = new String[results.size()];
            for (int i = 0; i < results.size(); i++) {
                docIdMap.put(results.get(i).getFieldValue("url").toString().trim(), i);
                idUrlMap.put(i, results.get(i).getFieldValue("url").toString());
                data[i] =  results.get(i).getFieldValue("url") + " ";
                if (results.get(i).getFieldValue("content") != null || results.get(i).getFieldValue("title") != null) {
                    if (results.get(i).getFieldValue("title") != null) data[i] = results.get(i).getFieldValue("title").toString() + " ";
                    if (results.get(i).getFieldValue("content") != null) data[i] += " " + results.get(i).getFieldValue("content").toString();
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        if (isKMeans) {
            clusters = KMClusterer.KMeansClustering(data, clusterCount);
        } else {
            clusters = SLINKClusterer.SLinkClustering(data, clusterCount);
        }
        
        needsClustering = false;
        
        JOptionPane.showMessageDialog(this, "Clustering complete!");
    }

    private void keywordsAreaFocusLost(java.awt.event.FocusEvent evt) {
        keywordList = keywordsArea.getText().split("\n");
    }

    private void shouldClusterItemStateChanged(java.awt.event.ItemEvent evt) {
        if (evt.getStateChange() == ItemEvent.SELECTED)
            clusterChecked = true;
        else
            clusterChecked = false;
        
        needToPose = true;
    }

    private void startCrawlingMouseClicked(java.awt.event.MouseEvent evt) {
        if (!solrTested) {
            JOptionPane.showMessageDialog(this, "Please test the server once before attempting any operation.");
            return;
        }
        
        crawlProgress.setValue(0);
        
        JOptionPane.showMessageDialog(this, "Select the runtime folder of Nutch.");
        
        JFileChooser jf = new JFileChooser();
        jf.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jf.setDialogTitle("Select 'runtime/local' folder.");
        
        int returnVal = jf.showOpenDialog(this);
        
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = jf.getCurrentDirectory();
            if (file.getAbsolutePath().indexOf("runtime") >= 0) {
                if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0)
                    nutchPath = file.getAbsolutePath() + "\\local";
                else 
                    nutchPath = file.getAbsolutePath() + "/local";
            } else {
                JOptionPane.showMessageDialog(this, "Incorrect folder chosen!");
                return;
            }
            
        } else {
            return;
        }
        
        String keywordspath;
        if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0)
            keywordspath = nutchPath + "\\conf\\keywords.txt";
        else
            keywordspath = nutchPath + "/conf/keywords.txt";        
        
        try {
            PrintWriter pw = new PrintWriter(keywordspath);
            pw.println("[threshold]");
            pw.println(threshold + "\n");
            
            pw.println("[wiggle]");
            pw.println(wiggle + "\n");
            
            pw.println("[keyword list]");
            for (String keyword : keywordList)
                pw.println(keyword);
            pw.close();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error manipulating keywords file.");
        }
        
        crawlProgress.setValue(10);
        
        System.out.println("Done editing keywords file.");
        
        String urlsfile;
        
        String crawlargs;
        if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
            crawlargs = nutchPath + "\\urls -dir crawl -threads 5 -depth 3 -topN 20";
            urlsfile = nutchPath + "\\urls\\urls";
        }
        else {
            crawlargs = nutchPath + "/urls -dir crawl -threads 5 -depth 3 -topN 20";
            urlsfile = nutchPath + "/urls/urls";
        }
        
        
        String google = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=";
        String charset = "UTF-8";
        String search = null;
        JSONParser jsonParser = new JSONParser();
        PrintWriter pw;
        
        try {
            pw = new PrintWriter(urlsfile);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error accessing seeds URLs file. Check Nutch directory and try again.");
            return;
        }
        
        for (String keyword : keywordList) {
            search = keyword;
            try {
                URL url = new URL(google + URLEncoder.encode(search, charset));
                Reader reader = new InputStreamReader(url.openStream(), charset);
                JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
        
                JSONArray results = (JSONArray)((JSONObject) jsonObject.get("responseData")).get("results");
                
                int cur = 10;
                
                for (int i = 0; i < results.size(); i++) {
                    String temp = ((JSONObject) results.get(i)).get("unescapedUrl").toString();
                    pw.println(temp);
                    cur = 10 + (i * 20) / (results.size());
                    crawlProgress.setValue(cur);
                }
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Crawling aborted. Failed to get seeds.");
                e.printStackTrace();
                return;
            }
            
        }
        
        pw.close();
        
        
        try {
            ToolRunner.run(NutchConfiguration.create(), new Crawl(), crawlargs.split("\\s+"));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        System.out.println("Done running Nutch");
        
        String solrhome;
        if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0)
            solrhome = examplePath + "\\solr";
        else
            solrhome = examplePath + "/solr";
        
        System.out.println(solrhome);
        
        crawlProgress.setValue(60);
        
        System.setProperty("solr.solr.home", solrhome);
        CoreContainer.Initializer initializer = new CoreContainer.Initializer();
        CoreContainer coreContainer;
        
        try {
            coreContainer = initializer.initialize();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        crawlProgress.setValue(80);
        
        EmbeddedSolrServer server = new EmbeddedSolrServer(coreContainer, "");
        String indexargs = "local crawl/crawldb -linkdb crawl/linkdb crawl/segments/*";
        
        try {
            ToolRunner.run(NutchConfiguration.create(), new SolrIndexer(), indexargs.split("\\s+"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        crawlProgress.setValue(100);
        
        System.out.println("Done giving data to Solr");
        
    }

    private void solrBrowseMouseClicked(java.awt.event.MouseEvent evt) {
        if (!javaChosen) {
            JOptionPane.showMessageDialog(this, "Please choose Java path first!");
            return;
        }
        
        final JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            solrPath = file.getAbsolutePath();
            System.out.println(solrPath);
            solrChosen = true;
        }
        
        if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
            examplePath = solrPath.substring(0, solrPath.lastIndexOf("\\"));
            
        } else {
            examplePath = solrPath.substring(0, solrPath.lastIndexOf("/"));
        }
        
    }

    private void javaBrowseMouseClicked(java.awt.event.MouseEvent evt) {
        final JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            javaPath = file.getAbsolutePath();
            System.out.println(javaPath);
            javaChosen = true;
        }
    }

    private void startSolrMouseClicked(java.awt.event.MouseEvent evt) {
        if (!javaChosen) {
            JOptionPane.showMessageDialog(this, "Please choose the Java path, then the Solr JAR Path.");
            return;
        }
        if (!solrChosen) {
            JOptionPane.showMessageDialog(this, "Please choose the Solr JAR path first.");
            return;
        }
        
        String exampleFolder;
        String execCommand;
        if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
            exampleFolder = solrPath.substring(0, solrPath.lastIndexOf("\\"));
            execCommand = javaPath + " -Djetty.home=" + exampleFolder
                          + " -Djetty.logs=tmp -Dsolr.solr.home=" + exampleFolder + "\\solr"
                          + " -jar " + solrPath;
        } else {
            exampleFolder = solrPath.substring(0, solrPath.lastIndexOf("/"));
            execCommand = javaPath + " -Djetty.home=" + exampleFolder
                          + " -Djetty.logs=tmp -Dsolr.solr.home=" + exampleFolder + "/solr"
                          + " -jar " + solrPath;
        }
        
        examplePath = exampleFolder;
        System.out.println(exampleFolder);
        System.out.println(execCommand);
        
        try {
            Process proc = Runtime.getRuntime().exec(execCommand);
            LogReader lsr = new LogReader(proc.getInputStream());
            LogReader err = new LogReader(proc.getErrorStream());
            Thread thread = new Thread(lsr, "LogReader");
            System.out.println("Reached here");
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    private void testSolrMouseClicked(java.awt.event.MouseEvent evt) {
        
        solr = new HttpSolrServer("http://localhost:8983/solr");
        SolrQuery query = new SolrQuery();
        query.setQuery("*:*");
        
        try {
            QueryResponse response = solr.query(query);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Server is not working. :(");
            return;
        }
        
        JOptionPane.showMessageDialog(this, "Server seems to be working! :)");
        solrTested = true;
        
    }

    private void queryFieldFocusLost(java.awt.event.FocusEvent evt) {
        if (isInvalid(queryField.getText())) {
            JOptionPane.showMessageDialog(this, "Please enter a valid query!");
            queryField.setText("( cat AND dog ) OR ( shark AND fish )");
        } else {
            queryString = queryField.getText();
        }
    }

    private void poseQueryMouseClicked(java.awt.event.MouseEvent evt) {
        
        if (!solrTested) {
            JOptionPane.showMessageDialog(this, "Please test the server at least once before you begin!");
            return;
        }
        
        if (clusterChecked && needsClustering) {
            JOptionPane.showMessageDialog(this, "Clustering has been checked, but data hasn't been clustered."
                                          + "\nPlease Cluster the data and try again.");
            return;
        }
        
        SolrQuery query = new SolrQuery();
        String queryToPose = getTransformedQuery(queryString);
        System.out.println(queryToPose);
        query.setQuery(queryToPose);
        query.setRows(Integer.MAX_VALUE);
        query.setFields("title", "url", "score");
        QueryResponse response;
        
        if (queryString.trim().equals("*")) {
            query.setQuery("*:*");
        }
        
        try {
            response = solr.query(query);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to execute query! Please check the server logs / the logical consistency of the query.");
            return;
        }
        
        
        SolrDocumentList results = response.getResults();
        ArrayList<DocumentResult> docres = new ArrayList<DocumentResult>();
        HashMap<Integer, Boolean> clustersToGet = new HashMap<Integer, Boolean>();
        
       
        for (int i = 0; i < results.size(); i++) {
            Object titleObj = results.get(i).getFieldValue("title");
            String titleVal = titleObj == null ? "<No Title>" : titleObj.toString();
            docres.add(new DocumentResult(i+1, titleVal, results.get(i).getFieldValue("url").toString(),
                                          -1, Double.parseDouble(results.get(i).getFieldValue("score").toString())));
        }
        
         if (clusterChecked) {
            for (int i = 0; i < results.size(); i++) {
                clustersToGet.put(getCluster(docIdMap.get(results.get(i).getFieldValue("url").toString())), true);
            }

            for (Map.Entry<Integer, Boolean> pair : clustersToGet.entrySet()) {
                int clusterno = pair.getKey();
                for (int j = 0; j < clusters.get(clusterno).size(); j++) {
                    int docno = clusters.get(clusterno).get(j);
                    if (!alreadyFetched(results, docno)) {
                        try {
                            SolrQuery q = new SolrQuery();
                            q.setQuery("url:\"" + idUrlMap.get(docno) + "\"");
                            q.setFields("url", "title", "score");
                            QueryResponse r = solr.query(q);
                            SolrDocumentList fetched = r.getResults();
                            if (fetched.size() <= 0) continue;
                            Object titleObj = fetched.get(0).getFieldValue("title");
                            String titleVal = null;
                            if (titleObj == null) {
                                titleVal = "<No Title>";
                                continue;
                            }
                            else
                                titleVal = titleObj.toString();
                            String urlVal = fetched.get(0).getFieldValue("url").toString();
                            double scoreVal = Double.parseDouble(fetched.get(0).getFieldValue("score").toString());
                            docres.add(new DocumentResult(0, titleVal, urlVal, clusterno, scoreVal));
                        } catch (Exception e) {
                            e.printStackTrace();
                            JOptionPane.showMessageDialog(this, "Invalid document encountered. Please recrawl.");
                            return;
                        }
                    }
                }

            }
        }
         
        if (clusterChecked) {
            for (int i = 0; i < docres.size(); i++) {
                if (docIdMap.get(docres.get(i).url) != null)
                    docres.get(i).cluster = getCluster(docIdMap.get(docres.get(i).url.trim()));
                else {
                    // Invalid document, according to index
                    JOptionPane.showMessageDialog(this, "Invalid document in query results! Please recrawl.");
                    return;
                }
            }
        }
        
        Collections.sort(docres, new Comparator<DocumentResult>() {
            
            public int compare (DocumentResult a, DocumentResult b) {
                if (a.score > b.score) return -1;
                else if (a.score < b.score) return 1;
                return 0;
            }
        });
        
        for (int i = 0; i < docres.size(); i++)
            docres.get(i).sno = i+1;
        
        needToPose = false;
        lastResult = docres;
        displayResults();
       
    }

    private void sortClusterNumberMouseClicked(java.awt.event.MouseEvent evt) {
        
        if (lastResult == null || lastResult.size() == 0) {
            JOptionPane.showMessageDialog(this, "No results to sort!");
            return;
        }
        
        if (!clusterChecked) {
            JOptionPane.showMessageDialog(this, "Please select 'Cluster Results' first.");
            return;
        }
        
        if (needsClustering) {
            JOptionPane.showMessageDialog(this, "Data needs to be clustered. Please press 'Cluster Data', then try again.");
            return;
        }
        
        if (needToPose) {
            JOptionPane.showMessageDialog(this, "Please refresh the results first by posing a new query.");
            return;
        }
        
        Collections.sort(lastResult, new Comparator<DocumentResult>() {
            
            public int compare (DocumentResult a, DocumentResult b) {
                if (a.cluster < b.cluster) return -1;
                else if (a.cluster > b.cluster) return 1;
                return 0;
            }
        });
        
        displayResults();
    }

    private void sortSerialNumberMouseClicked(java.awt.event.MouseEvent evt) {
        
        if (lastResult == null || lastResult.size() == 0) {
            JOptionPane.showMessageDialog(this, "Nothing to sort.");
            return;
        }        
        
        Collections.sort(lastResult, new Comparator<DocumentResult>() {
            
            public int compare (DocumentResult a, DocumentResult b) {
               if (a.sno < b.sno) return -1;
               else if (a.sno > b.sno) return 1;
               return 0;
            }
        });
        
        displayResults();
        
    }

    private void computeSSEMouseClicked(java.awt.event.MouseEvent evt) {
        
        if (needsClustering) {
            JOptionPane.showMessageDialog(this, "Data inconsistent. Please run Clustering again.");
            return;
        }
        
        if (isKMeans) {
            computedSSE.setText(Double.toString(KMClusterer.lastSSE));
        } else {
            computedSSE.setText(Double.toString(SLINKClusterer.lastSSE));
        }
    }

    /**
     * 
     * @param query The query to convert to Solr syntax
     * @return The converted query
     */
    
    public String getTransformedQuery (String query) {
        String[] splitted = query.split("\\s+");
        String answer = "";
        for (String word : splitted) {
            if (word.equals("(") || word.equals(")") || word.equals("NOT") || word.equals("AND") || word.equals("OR"))
                answer += word + " ";
            else {
                word = "(title:\"" + word + "\" OR content:\"" + word + "\") ";
                answer  += word;
            }
        }
        return answer;
    }
    
    /**
     *  Function to check whether a document with a given id
     *  has already been fetched.
     * @param results The document list to compare against
     * @param docid The document id to check for presence
     * @return True if it is present, False otherwise
     */
    
    public Boolean alreadyFetched(SolrDocumentList results, int docid) {
        
        for (int i = 0; i < results.size(); i++) {
            if (docIdMap.get(results.get(i).getFieldValue("url").toString()) == docid)
                return true;
        }
        return false;
    }
    
    
    /**
     *  Prints the retrieved results on the JTable resultsTable
     */
    
    public void displayResults() {
        
        DefaultTableModel model = (DefaultTableModel) resultsTable.getModel();
        model.setRowCount(lastResult.size());
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(5);
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        resultsTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        resultsTable.getColumnModel().getColumn(3).setPreferredWidth(5);
        
        
        for (int i = 0; i < lastResult.size(); i++) {
            
            resultsTable.setValueAt(lastResult.get(i).sno, i, 0);
            resultsTable.setValueAt(lastResult.get(i).title, i, 1);
            resultsTable.setValueAt(lastResult.get(i).url, i, 2);
            resultsTable.setValueAt(lastResult.get(i).cluster, i, 3);
        }
    }
    
    /**
     * 
     * @param docid The docid to get the Cluster number for
     * @return The Cluster number
     */
    public int getCluster (int docid) {
        
        for (int i = 0; i < clusters.size(); i++) {
            for (int j = 0; j < clusters.get(i).size(); j++) {
                if (clusters.get(i).get(j) == docid) return i;
            }
        }
        return -1;
    }
    
    /**
     * 
     * @param queryText The query to be tested for validity
     * @return {Boolean} True if the query is invalid, false otherwise
     */
    public Boolean isInvalid (String queryText) {
        String[] splitQuery = queryText.split("\\s+");
        int opencount = 0;
        Stack<String> s = new Stack<String>();
        for (int i = 0; i < splitQuery.length; i++) {
            if (splitQuery[i].equalsIgnoreCase(")")) {
                do {
                    if (s.empty()) return true;
                    String top = s.pop();
                    if (top.equals("(")) { opencount--; break; }
                } while (true);
            } else {
                s.push(splitQuery[i]);
                if (splitQuery[i].equals("(")) opencount++;
            }
        }
        if (opencount > 0) return true;
        return false;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainGUI().setVisible(true);
            }
        });
    }

    private javax.swing.JButton clusterData;
    private javax.swing.JList clusteringAlgorithm;
    private javax.swing.JButton computeSSE;
    private javax.swing.JTextField computedSSE;
    private javax.swing.JProgressBar crawlProgress;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton javaBrowse;
    private javax.swing.JTextArea keywordsArea;
    private javax.swing.JTextField numClusters;
    private javax.swing.JButton poseQuery;
    private javax.swing.JTextField queryField;
    private javax.swing.JTable resultsTable;
    private javax.swing.JCheckBox shouldCluster;
    private javax.swing.JButton solrBrowse;
    private javax.swing.JButton sortClusterNumber;
    private javax.swing.JButton sortSerialNumber;
    private javax.swing.JButton startCrawling;
    private javax.swing.JButton startSolr;
    private javax.swing.JButton testSolr;
    private javax.swing.JTextField thresholdValue;
    private javax.swing.JTextField wiggleRoom;
}
