focused-ir-tool
===============

A Focused Information Retrieval Tool

A tool written in Java that allows for Focused Crawling and Boolean information retrieval on the crawled and indexed pages. It uses Apache Nutch (Lucene) for crawling, with a plugin that enforces the focus, and Apache Solr for indexing and Boolean retrieval.

The jar_dependencies folder contains a list of the JAR files required to compile the project. The project can be compiled using ant (versions 1.8.0 and above).

The Apache Nutch source code needs to be modified to add the plugin to the parsing pipeline. The instructions are given in the apache-nutch folder.
The schema of Apache Solr needs to be set-up to correctly index web page data. The instructions for the same are given in the apache-solr folder.
