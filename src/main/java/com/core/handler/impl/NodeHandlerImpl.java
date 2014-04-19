package com.core.handler.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.conf.bean.Configuration;
import com.core.bean.Node;
import com.core.executor.Executor;
import com.core.handler.INodeHandler;

public class NodeHandlerImpl implements INodeHandler {

	private static final Logger log = LoggerFactory.getLogger(NodeHandlerImpl.class);
	
	private static List<Node> leafResult = new ArrayList<Node>(); 
	
	public static int traceCount = 0;
	
	private Node node;
	
	private static Node startNode;
	
	private ServerSocket listenServer;
	private ServerSocket msgServer;
	
	private static int totalK = 0;
	
//	private static boolean listenFlag = false;
	
//	private boolean msgFlag = true;
//	
//	private boolean callbackFlag = false;
//	
//	private static boolean isListening = false;
//	
//	public static boolean isListening() {
//		return isListening;
//	}
//
//	public static void setListening(boolean isListening) {
//		NodeHandlerImpl.isListening = isListening;
//	}
//
	
//	public static void resetLeafResult(){
//		leafResult = new ArrayList<Node>(); 
//	}
	
	public NodeHandlerImpl(Node node){
		this.node = node;
	}
	
	@Override
	public void startListen() {
		int port = node.getPort();
		int listenPort = 20000 + port;
		int msgPort = 30000 + port;
		try {
			
//			listenFlag = true;
			
			listenServer = new ServerSocket(listenPort);
			msgServer = new ServerSocket(msgPort);
			new listenThread();
			new msgThread();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	class listenThread extends Thread{
		public listenThread(){
			start();
		}
		public void run(){
			while(true){
				try {
					Socket socket = listenServer.accept();
					new ClientContainer(socket);
//					listenServer.close();
//					log.debug("------ connect -------");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		class ClientContainer extends Thread{
			private Socket socket;
			private DataInputStream dis;
			public ClientContainer(Socket socket){
				this.socket = socket;
				this.start();
			}
			public void run(){
				try {
					
					dis = new DataInputStream(socket.getInputStream());
					String messageStr = dis.readUTF();
					Map<String,Object> message = JSON.parseObject(messageStr, Map.class);
					
					String sourceId = (String)message.get("sourceId");
					String targetId = (String)message.get("targetId");
					
					String threadName = "==== Thread " + targetId + " Listen     ==== ";
					this.setName(threadName);
					
					
//					log.debug(threadName + "previous nodeId : " + sourceId);
					
					Node sourceNode = Configuration.getNode(sourceId);
					Node targetNode = Configuration.getNode(targetId);
					
					int k = (Integer)message.get("k");
					
//					log.debug(threadName + " k : " + k);
					targetNode.setK(k);
					
					targetNode.setLevel(sourceNode.getLevel() + 1);
					targetNode.setParentNode(sourceNode);
					
//					log.debug(threadName + "sourceNode : " + sourceNode.getId() + " responseNumIncrese");
					sourceNode.responseNumIncrese();
					
//					log.debug(threadName + "showLinkStartedStatus : " + targetNode.showLinkStartedStatus());
					business(targetNode);
//					waitUntilFinish(targetNode,sourceNode);
					
//					log.debug(threadName + "showLinkStartedStatus : " + targetNode.showLinkStartedStatus());
					
//					if(targetNode.getId().equals("p4")){
//						log.debug("");
//					}
//					
//					while(sourceNode.getResponseNum() > 0){
//						log.debug(threadName + "======= wait =======");
//					}
//					log.debug(threadName + "======= pass =======");
					
					if(--traceCount == 1){
						callback();
					}
					
//					send(sourceId, targetId);
		            
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	private void response(String sourceId,String targetId){
		
//		log.debug(" targetId : " + targetId + " ===> " + " sourceId : " + sourceId);
		
		Node sourceNode = Configuration.getNode(sourceId);
		Node targetNode = Configuration.getNode(targetId);
		int msgPort = 30000 + sourceNode.getPort();
		Socket socket;
		try {
			socket = new Socket("127.0.0.1",msgPort);
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());  
			
			Map<String,Object> request = new HashMap<String,Object>();
			request.put("sourceId", sourceId);
			request.put("targetId", targetId);
			String threadName = "==== Thread " + targetId + " response     ==== ";
			log.debug(threadName + "topk : " + targetNode.getK());
			request.put("topK", targetNode.getSortedTopK(targetNode.getK()));
			if(Executor.isFirst){
				request.put("stock", targetNode.getStock());
			}
			
			dos.writeUTF(JSON.toJSONString(request));
			dos.flush();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private static List<Node> savedLeafResult = new ArrayList<Node>();
	
	class NodeComparator implements Comparator<Node>{

		@Override
		public int compare(Node o1, Node o2) {
			int o1level = o1.getLevel();
			int o2level = o2.getLevel();
			if(o1level >= o2level){
				return -1;
			}else if(o1level < o2level){
				return 1;
			}
			return 0;
		}
		
	}
	
	private void callback(){
		log.debug("======== callback ========");
		
		Collections.sort(leafResult, new NodeComparator());
		log.debug("leafResult" + leafResult);
		
		if(Executor.isFirst){
			for(Node leaf : leafResult){
				savedLeafResult.add(leaf);
			}
		}
		
		int maxLevel = leafResult.get(0).getLevel();
		
//		int i = 0;
//		while(!leafResult.isEmpty()){
//			Node node = leafResult.get(i);
//			Node pNode = node.getParentNode();
//			
//			if((pNode != null)){
//				if(pNode.getLevel() == maxLevel - 1){
//					response(pNode.getId(), node.getId());
//					if(!leafResult.contains(pNode)){
//						leafResult.set(i,pNode);
//					}else{
//						leafResult.remove(i);
//					}
//				}
//			}else{
//				leafResult.remove(i);
//			}
//			i = i == leafResult.size() - 1? 0 : i + 1;
//			maxLevel --;
//		}
		
//		log.debug("maxLevel : " + maxLevel);
		for(int i = maxLevel;i >= 0;i--){
//			log.debug("i : " + i);
			for(int j = 0;j < leafResult.size();){
//				log.debug("leafResult : " + leafResult);
				Node node = leafResult.get(j);
				Node pNode = node.getParentNode();
				if(node.getLevel() == i){
					if((pNode != null)){
						while(node.getResponseNum() != 0){
						}
						
						response(pNode.getId(), node.getId());
						if(!leafResult.contains(pNode)){
							leafResult.set(j,pNode);
							j = 0;
							continue;
						}else{
							leafResult.remove(j);
							j--;
						}
					}
				}
				j++;
			}
		}
		finish();
	}
	private void finish(){
		
		while(startNode.getResponseNum() != 0){
//			log.debug("startNode.getResponseNum() : " + startNode.getResponseNum());
		}
		log.debug("======== finish ========");
		log.debug(startNode.getSortedTopK(startNode.getK()) + "");
		
		if(Executor.isFirst){
			log.debug(startNode.getStock() + "");
		}
		Executor.isFirst = false;
//		msgFlag = false;
		log.info("================================================================================================");
		log.debug("==================== " + "begin : " + begin + " ====================");
		log.debug("==================== " + "now : " + System.currentTimeMillis() + " ====================");
		log.info("==================== " + "cost : " + (System.currentTimeMillis() - begin) + " ms " + " ====================");
		log.info("==================== " + "total k : " + totalK * 4 + "B " + " ====================");
		log.info("================================================================================================");
	}
	
	class msgThread extends Thread{
		public msgThread(){
			start();
		}
		public void run(){
			while(true){
				try {
					Socket socket = msgServer.accept();
					new msgContainer(socket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
				}
			}
		}
		class msgContainer extends Thread{
			private Socket socket;
			private DataInputStream dis;
			public msgContainer(Socket socket){
				this.socket = socket;
				this.start();
			}
			public void run(){
				try {
					dis = new DataInputStream(socket.getInputStream());
					String messageStr = dis.readUTF();
					Map<String,Object> message = JSON.parseObject(messageStr, Map.class);
					
					String sourceId = (String) message.get("sourceId");
					String targetId = (String) message.get("targetId");
					List<Integer> topK =  (List<Integer>)message.get("topK");
					Map<String,Integer> stock = null;
					if(Executor.isFirst){
						stock = (Map<String,Integer>)message.get("stock");
					}
					
					Node sourceNode = Configuration.getNode(sourceId);
					Node targetNode = Configuration.getNode(targetId);
					
//					thisNode.getTopK().addAll(topK);
//					Collections.sort(node.getTopK());
					
					
					String threadName = "==== Thread " + sourceId + " MsgHandler ==== ";
					this.setName(threadName);
					
//					log.debug(threadName + "sourceNode : " + sourceNode.getId() + " responseNumDecrese");
					
					log.debug(threadName + "topK : " + topK + " , " + targetId + " ==> " + sourceId);
					if(Executor.isFirst){
						sourceNode.putStockPair(targetId, stock);
						log.debug(threadName + targetId + " ==> " + sourceId);
						log.debug(threadName + "sourceNode.getStock() : " + sourceNode.getStock());
//						log.debug(threadName + "sourceNode.getStockPair() : " + sourceNode.getStockPair());
					}
					sourceNode.merge(topK);
					sourceNode.responseNumDecrese();
					
					targetNode.setFinished(true);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private static long begin;
	public void start(Node node, int topValue){
		totalK = 0;
		begin = System.currentTimeMillis();
		log.debug("==== Thread " + node.getId() + " Main       ==== " + "visiting node : " + node);
		
		startNode = node;
		node.setK(topValue);
		node.setStarted(true);
		node.setFinished(true);
		fireRequest(node);
	}
	@Override
	public void business(Node node) {
//		if(!node.isVisited()){
			String threadName = "==== Thread " + node.getId() + " Main       ==== ";
			log.debug(threadName + "visiting node : " + node);
//			node.setVisited(true);
			
			fireRequest(node);
//		}
	}
	
	@Override
	public void fireRequest(Node sourceNode) {
		
		String threadName = "==== Thread " + sourceNode.getId() + " Main       ==== ";
		
		log.debug(threadName + "showLinkStartedStatus : " + sourceNode.showLinkStartedStatus());
		log.debug(threadName + "stockPair : " + sourceNode.getStockPair());
		log.debug(threadName + "topk : " + sourceNode.getK());
		
		for(String targetNodeId : sourceNode.getLink()){
				Node targetNode = Configuration.getNode(targetNodeId);
//				nodeLock.lock();
				sourceNode.setLeaf(targetNode.isStarted());
				
				if(Executor.isFirst){
					if(!targetNode.isStarted()){
						send(targetNode, sourceNode);
					}
				}else{
					if(targetNode.getParentNode() != null){
						if(targetNode.getParentNode().getId() == sourceNode.getId()){
							send(targetNode, sourceNode);
						}
					}
				}
				
		}
		if(Executor.isFirst){
			if(sourceNode.isLeaf()){
				leafResult.add(sourceNode);
			}
		}else{
			leafResult = savedLeafResult;
		}
	}
	private void send(Node targetNode, Node sourceNode){
		targetNode.setStarted(true);
		int linkPort = 20000 + targetNode.getPort();
		Socket socket;
		try {
			socket = new Socket("127.0.0.1",linkPort);
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());  
			
			/**
			 * 			1)	source ===> target
			 * 			2)	target ===> source
			 */
			
			Map<String,Object> request = new HashMap<String,Object>();
			request.put("sourceId", sourceNode.getId());
			request.put("targetId", targetNode.getId());
			int k;
			if(Executor.isFirst){
				k = sourceNode.getK();
			}else{
				k = sourceNode.getNumFromStockValue(targetNode.getId(), sourceNode.getK());
			}
			totalK += k;
			request.put("k", k);
			
			dos.writeUTF(JSON.toJSONString(request));
			dos.flush();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
//	@Override
//	public void closeListen() {
//		while(msgFlag){
//			
//		}
//		try {
//			listenServer.close();
//			msgServer.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
