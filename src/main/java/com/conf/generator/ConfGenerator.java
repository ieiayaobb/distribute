package com.conf.generator;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.core.bean.Node;
import com.core.handler.impl.NodeHandlerImpl;

public class ConfGenerator {
	private static final Logger log = LoggerFactory.getLogger(ConfGenerator.class);
	
	public ConfGenerator(int num){
		this.generateNode(num);
	}
	private Map<String,Node> generateNode(int num){
		Map<String,Node> nodePool = new HashMap<String,Node>();
		Node p1Node = new Node("p1");
		nodePool.put("p1", p1Node);
		
		for(int i = 1;i < num;i++){
			String newKey = "p" + (i + 1);
			
			Node node = new Node(newKey);
			int relationNum = (int) Math.ceil(Math.random() * nodePool.size());
			log.info("relationNum : " + relationNum);
			for(int j = 0;j < relationNum;j++){
				int index = (int) Math.ceil(Math.random() * nodePool.size());
				if(!node.getLink().contains("p" + index)){
					log.info("index : " + index);
					node.addLinkNodeId("p" + index);
					log.info("node : " + node);
					Node poolNode = nodePool.get("p" + index);
					log.info("poolNode : " + poolNode);
					poolNode.addLinkNodeId(newKey);
				}
			}
			nodePool.put(newKey, node);
		}
		log.info(nodePool + "");
		return nodePool;
	}
	public static void main(String[] args){
		new ConfGenerator(10);
	}
}
