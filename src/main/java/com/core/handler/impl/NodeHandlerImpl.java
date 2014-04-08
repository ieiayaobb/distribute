package com.core.handler.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.conf.bean.Configuration;
import com.core.bean.Node;
import com.core.handler.INodeHandler;

public class NodeHandlerImpl implements INodeHandler {

	private static final Logger log = LoggerFactory.getLogger(NodeHandlerImpl.class);
	
	private Node node;
	
	private ServerSocket listenServer;
	private ServerSocket msgServer;
	
	public NodeHandlerImpl(Node node){
		this.node = node;
	}
	
	@Override
	public void startListen() {
		int port = node.getPort();
		int listenPort = 20000 + port;
		int msgPort = 30000 + port;
		try {
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
//			while(true){
				try {
					Socket socket = listenServer.accept();
					new ClientContainer(socket);
//					log.info("------ connect -------");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//			}
		}
		class ClientContainer extends Thread{
			private Socket socket;
			public ClientContainer(Socket socket){
				this.socket = socket;
				this.start();
			}
			public void run(){
				try {
					DataInputStream dis = new DataInputStream(socket.getInputStream());
					String messageStr = dis.readUTF();
					Map<String,Object> message = JSON.parseObject(messageStr, Map.class);
					
					String sourceId = (String)message.get("sourceId");
					String targetId = (String)message.get("targetId");
					
					log.info("==== Thread " + targetId + " Listen ==== " + "previous nodeId : " + sourceId);
					
					Node sourceNode = Configuration.getNode(sourceId);
					Node targetNode = Configuration.getNode(targetId);
					
					int msgPort = 30000 + sourceNode.getPort();
					Socket socket = new Socket("127.0.0.1",msgPort);
					
//					waitUntilFinish(thisNode);
					
					DataOutputStream dos = new DataOutputStream(socket.getOutputStream());  
		            
		            Map<String,Object> request = new HashMap<String,Object>();
		            request.put("sourceId", sourceId);
		            request.put("targetId", targetId);
		            request.put("topK", targetNode.getSortedTopK(5));
		            
		            dos.writeUTF(JSON.toJSONString(request));
		            dos.flush();
		            
		            business(targetNode);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	class msgThread extends Thread{
		public msgThread(){
			start();
		}
		public void run(){
//			while(true){
				try {
					Socket socket = msgServer.accept();
					new ClientContainer(socket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//			}
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
					
					String sourceId = (String) message.get("sourceId");
					String targetId = (String) message.get("targetId");
					List<Integer> topK =  (List<Integer>)message.get("topK");
					
					Node sourceNode = Configuration.getNode(sourceId);
					Node targetNode = Configuration.getNode(targetId);
//					thisNode.getTopK().addAll(topK);
//					Collections.sort(node.getTopK());
					
					log.info("==== Thread " + sourceId + " MsgHandler ==== " + "topK : " + topK + " , " + targetId + " ==> " + sourceId);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public void start(Node node){
		log.info("==== Thread " + node.getId() + " Main ==== " + "visiting node : " + node);
		node.setVisited(true);
		fireRequest(node);
	}
	@Override
	public void business(Node node) {
		if(!node.isVisited()){
			log.info("==== Thread " + node.getId() + " Main ==== " + "visiting node : " + node);
			node.setVisited(true);
			
			fireRequest(node);
		}
	}
	private void waitUntilFinish(Node thisNode){
		boolean isFinished = false;
		while(!isFinished){
			isFinished = true;
			for(String targetNodeId : thisNode.getLink()){
				Node targetNode = Configuration.getNode(targetNodeId);
				isFinished = isFinished && targetNode.isVisited();
			}
			log.info("======= wait =======");
		}
	}
	
	private static Lock nodeLock = new ReentrantLock();
	@Override
	public void fireRequest(Node node) {
		for(String targetNodeId : node.getLink()){
			try {
				Node targetNode = Configuration.getNode(targetNodeId);
//				nodeLock.lock();
				log.info("==== Thread " + node.getId() + " Main ==== " + "target Node : " + targetNode.getId() + ", visited? " + targetNode.isVisited());
				if(!targetNode.isVisited()){
					
					int linkPort = 20000 + targetNode.getPort();
					Socket socket = new Socket("127.0.0.1",linkPort);
		            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());  
		            
		            /**
		             * 			1)	source ===> target
		             * 			2)	target ===> source
		             */
		            
		            
		            Map<String,Object> request = new HashMap<String,Object>();
		            request.put("sourceId", node.getId());
		            request.put("targetId", targetNodeId);
		            
		            dos.writeUTF(JSON.toJSONString(request));
		            dos.flush();
				}
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
