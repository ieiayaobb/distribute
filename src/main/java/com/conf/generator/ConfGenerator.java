package com.conf.generator;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.core.bean.Node;
import com.core.handler.impl.NodeHandlerImpl;

public class ConfGenerator {
	private static final Logger log = LoggerFactory.getLogger(ConfGenerator.class);
	
	public ConfGenerator(){
	}
	public Map<String,Node> generateNode(int num, int valueNum){
		Map<String,Node> nodePool = new HashMap<String,Node>();
		Node p1Node = new Node("p1");
		for(int k = 0; k < valueNum; k ++){
			p1Node.pushTop((int) Math.round(Math.random() * 99));
		}
		p1Node.saveValue();
		p1Node.setPort(1);
		p1Node.setStock();
		nodePool.put("p1", p1Node);
		
		for(int i = 1;i < num;i++){
			String newKey = "p" + (i + 1);
			
			Node node = new Node(newKey);
			int relationNum = (int) Math.ceil(Math.random() * nodePool.size());
//			log.debug("relationNum : " + relationNum);
			for(int j = 0;j < relationNum;j++){
				int index = (int) Math.ceil(Math.random() * nodePool.size());
				if(!node.getLink().contains("p" + index)){
//					log.debug("index : " + index);
					node.addLinkNodeId("p" + index);
//					log.debug("node : " + node);
					Node poolNode = nodePool.get("p" + index);
//					log.debug("poolNode : " + poolNode);
					poolNode.addLinkNodeId(newKey);
					
				}
			}
			for(int k = 0; k < valueNum; k ++){
				node.pushTop((int) Math.round(Math.random() * 100));
			}
			node.saveValue();
			nodePool.put(newKey, node);
			node.setPort(i + 1);
			node.setStock();
		}
		log.debug(nodePool + "");
		return nodePool;
	}
	public static void main(String[] args){
		new ConfGenerator().generateNode(10, 15);
	}
}
