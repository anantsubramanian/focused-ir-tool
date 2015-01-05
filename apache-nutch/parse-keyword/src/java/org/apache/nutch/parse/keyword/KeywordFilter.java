package org.apache.nutch.parse.keyword;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.parse.HTMLMetaTags;
import org.apache.nutch.parse.HtmlParseFilter;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.ParseResult;
import org.apache.nutch.protocol.Content;
import org.w3c.dom.DocumentFragment;

public class KeywordFilter implements HtmlParseFilter{

	private static final Log LOG = LogFactory.getLog(KeywordFilter.class
		      .getName());

	private Configuration conf;

	private Set<String> keywordset = new HashSet<String>();
	private int threshold = 2;
	private int wiggle = 200;
	private ArrayList< ArrayList<Integer> > positions;
	
	public KeywordFilter() {
		super();
		readConfiguration();		
	}

	private void readConfiguration() {
		InputStream is = null;
		positions = new ArrayList< ArrayList<Integer> >();
		
		try {
			is = new FileInputStream("conf/keywords.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String s;
			boolean in_keyword_list = false;
			while((s = br.readLine())!=null){
				if(in_keyword_list){
					keywordset.add(s.toLowerCase());
					continue;
				}
				
				if(s.toLowerCase().equals("[threshold]")){
					s = br.readLine();
					if(s !=  null){
						threshold = Integer.parseInt(s);
					}
				}
				
				if(s.toLowerCase().equals("[wiggle]")) {
					s = br.readLine();
					if (s != null) {
						wiggle = Integer.parseInt(s);
					}
				}
				
				if(s.toLowerCase().equals("[keyword list]")){
					in_keyword_list = true;
				}
								
			}
			
		} catch (FileNotFoundException e) {
			LOG.error("Keywords configuration file not found!");
		} catch (IOException e) {
			LOG.error("IO error occurs during reading keywords configuration!");
		} finally{
			try {
				if(is!=null){
					is.close();
				}
			} catch (IOException e) {
				LOG.error("IO error occurs during closing keywords configuration!");
			}
		}
		
	}

	@Override
	public Configuration getConf() {
		return this.conf;
	}

	@Override
	public void setConf(Configuration conf) {
		this.conf = conf;
	}

	@Override
	public ParseResult filter(Content content, ParseResult parseResult,
			HTMLMetaTags metaTags, DocumentFragment doc) {
		
		Parse parse = parseResult.get(content.getUrl());
		String text = parse.getText().toLowerCase();
		
		if(keywordset.isEmpty()){
			return parseResult;
		}
		
		String[] words = text.split("[\\s,\\.<>\\[\\]\\{\\}\\(\\)@#%^&\\*-\\+/'\"!]\\s*");
		
	  int i = 0;
		for (String keyword : keywordset) {
  	  positions.add(new ArrayList<Integer>());
      for (int j = 0; j < words.length; j++) {
  	    if (words[j].equalsIgnoreCase(keyword))
  	      positions.get(i).add(j);
      }
      i++;
  	}
  	
  	int overlay = 0;
    Boolean donej;
    
    for(i = 0; i < positions.size(); i++) {
    	for (int j = i+1; j < positions.size(); j++) {
    	  donej = false;
        for (int k = 0 ; k < positions.get(i).size(); k++) {
    	    for (int m = 0; m < positions.get(j).size(); m++) {
    	      if (Math.abs(positions.get(i).get(k) - positions.get(j).get(m)) <= wiggle) {
    	        overlay++;
    	        if (overlay >= threshold)
      	        return parseResult;
              donej = true;
              break;
          	}
					}	
        	if (donej) break;
      	}
    	}
		}
		
		return null;
	}

}

