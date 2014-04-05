package com.core.executor;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.conf.bean.Configuration;
import com.conf.handler.ConfigLoader;
import com.core.bean.Node;
import com.core.handler.INodeHandler;
import com.core.handler.impl.NodeHandlerImpl;

public class Executor {

	/**
	 * @param args
	 */
	private static final Logger log = LoggerFactory.getLogger(Executor.class);
	
	public static void main(String[] args) {
		log.info("====== begin ======");
		Configuration conf = new ConfigLoader().load("target/classes/conf.xml");
		
		Map<String,Node> allNodes = conf.getAllNodes();
		log.info("allNodes : " + allNodes);
		
		Map<String,INodeHandler> nodeHandlerMap = new HashMap<String,INodeHandler>();
		log.info("------ init all lisnter -------");
		for(String nodeKey : allNodes.keySet()){
			Node node  = allNodes.get(nodeKey);
			
			INodeHandler nodeHandler = new NodeHandlerImpl(node);
			nodeHandlerMap.put(nodeKey, nodeHandler);
			nodeHandler.startListen();
		}
		
		log.info("pick one handler ");
		INodeHandler p1Handler = nodeHandlerMap.get("p2");
		p1Handler.business();
		
	}

}
