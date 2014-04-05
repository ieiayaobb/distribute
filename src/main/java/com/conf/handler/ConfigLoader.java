package com.conf.handler;

import java.io.File;

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
				log.info("node : " + node);
				conf.setNode(node.getId(), node);
			}
			log.info("generate link-relations");
			for(Object obj : root.elements("node")){
				Element nodeEle = (Element)obj;
				String key = nodeEle.elementText("id");
				Node node = conf.getNode(key);
				
				for(Object linkObj : nodeEle.element("link").elements("node")){
					Node linkNode = conf.getNode(((Element)linkObj).getText());
					node.addNode(linkNode);
				}
			}
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conf;
	}
}
