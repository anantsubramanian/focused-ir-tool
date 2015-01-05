focused-ir-tool
===============

A Focused Information Retrieval Tool

A tool written in Java that allows for Focused Crawling and Boolean information retrieval on the crawled and indexed pages. It uses Apache Nutch (Lucene) for crawling, with a plugin that enforces the focus, and Apache Solr for indexing and Boolean retrieval.

The jar_dependencies folder contains a list of the JAR files required to compile the project. The project can be compiled using ant (versions 1.8.0 and above).

The Apache Nutch source code needs to be modified to add the plugin to the parsing pipeline. The instructions are given in the apache-nutch folder.
The schema of Apache Solr needs to be set-up to correctly index web page data. The instructions for the same are given in the apache-solr folder.

How to compile and run the tool:
1. Set-up Apache Nutch (version 1.x) and Solr (version 3.x or 4.x) by downloading the source code for Nutch and the executables for Solr from the Apache website and following the instructions mentioned in the respective folders.
2. Download the dependency files mentioned in the jar_dependencies folder and put them in the same folder.
3. Use ant (version 1.8.0+) to compile the tool and run the JAR file from the dist folder.
4. Use the "Java (Browse)" button to select the java executable file on your system.
5. Use the "Solr (Browse)" button to select the start.jar file in the example folder of Solr.
6. Use the "Start Server" and "Test Server" buttons to start and test the working of the server respectively.
7. Enter the keywords to use to focus the crawler, one on each line, in the space provided. Set the Threshold and Wiggle values in the spaces provided. Only pages that have at least 'Threshold' number of occurances of pairs of keywords within 'Wiggle' words of each other are indexed and their outlinks examined. The seed URLs are selected as the top 5 URLs listed by a Google search for each of the keywords.
8. Press the "Start Crawling" button to run Nutch using these parameters. You can monitor the progress through stdout.
9. Clustering and Searching using the tool are self-explanatory.
