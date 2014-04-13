package com.core.executor;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.conf.bean.Configuration;
import com.conf.generator.ConfGenerator;
import com.conf.handler.ConfigLoader;
import com.core.bean.Node;
import com.core.handler.INodeHandler;
import com.core.handler.impl.NodeHandlerImpl;

public class Executor {

	/**
	 * @param args
	 */
	private static final Logger log = LoggerFactory.getLogger(Executor.class);
	
	public static boolean isFirst = true;
	
	public static void main(String[] args) {
		log.info("====== begin ======");
//		Configuration conf = new ConfigLoader().load("target/classes/conf.xml");
		Configuration conf = new ConfigLoader().load(new ConfGenerator().generateNode(10 , 15));
		
		Map<String,Node> allNodes = conf.getAllNodes();
		log.info("allNodes : " + allNodes);
		
		Map<String,INodeHandler> nodeHandlerMap = new HashMap<String,INodeHandler>();
		log.info("------ init all lisnter -------");
		for(String nodeKey : allNodes.keySet()){
			Node node  = allNodes.get(nodeKey);
			log.info(node.getId() + " stock : " + node.getStock());
			log.info(node.getId() + " top : " + node.getTopK());
			INodeHandler nodeHandler = new NodeHandlerImpl(node);
			nodeHandlerMap.put(nodeKey, nodeHandler);
			
//			if(!NodeHandlerImpl.isListening()){
				nodeHandler.startListen();
//			}
		}
//		NodeHandlerImpl.setListening(true);
		
		start(allNodes, nodeHandlerMap,TOP_K_10);
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(String nodeKey : allNodes.keySet()){
			Node node  = allNodes.get(nodeKey);
			node.resetValue();
			node.setStarted(false);
		}
		
		start(allNodes, nodeHandlerMap, TOP_K_12);
		
//		for(String nodeHanlderKey : nodeHandlerMap.keySet()){
//			nodeHandlerMap.get(nodeHanlderKey).closeListen();
//		}
		
	}
	
	private static final int TOP_K_10 = 10;
	private static final int TOP_K_12 = 12;
	
	private static void start(Map<String,Node> allNodes, Map<String,INodeHandler> nodeHandlerMap, int topValue){
		
		NodeHandlerImpl.traceCount = allNodes.size();
		
		log.info("pick one handler ");
		INodeHandler p1Handler = nodeHandlerMap.get("p1");
		p1Handler.start(allNodes.get("p1"), topValue);
	}
}
