package com.conf.handler;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.conf.bean.Configuration;
import com.core.bean.Node;

public class ConfigLoader {
	private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);
	
	public Configuration load(Map<String,Node> nodePool){
		Configuration conf = new Configuration();
		return conf;
	}
	
	public Configuration load(String path){
		log.info("file path : " + path);
		Configuration conf = new Configuration();
		try {
			File xmlFile = new File(path);
			
			log.info("xml file path : " + xmlFile.getAbsolutePath());
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(xmlFile);
			Element root = document.getRootElement();
			log.info("init all nodes;");
			for(Object nodeObj : root.elements("node")){
				Element nodeEle = (Element)nodeObj;
				Node node = new Node();
				node.setId(nodeEle.elementText("id"));
				node.setPort(Integer.parseInt(nodeEle.elementText("port")));
				
				for(Object top : nodeEle.element("top").elements("value")){
					Element topEle = (Element)top;
					node.pushTop(Integer.parseInt(topEle.getText()));
				}
				
				log.info("node : " + node);
				conf.setNode(node.getId(), node);
			}
			log.info("generate link-relations");
			for(Object obj : root.elements("node")){
				Element nodeEle = (Element)obj;
				String key = nodeEle.elementText("id");
				Node node = conf.getNode(key);
				
				for(Object linkObj : nodeEle.element("link").elements("node")){
					String linkNode = ((Element)linkObj).getText();
					node.addNode(linkNode);
				}
				
				List<Integer> originalTop = node.getTopK();
				Collections.sort(originalTop);
				
				node.setStock();
			}
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conf;
	}
}
