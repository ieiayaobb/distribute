package com.core.handler.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.core.bean.Node;
import com.core.handler.INodeHandler;

public class NodeHandlerImpl implements INodeHandler {

	private static final Logger log = LoggerFactory.getLogger(NodeHandlerImpl.class);
	
	private Node node;
	
	private ServerSocket listenServer;
	
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
			new listenThread();
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
					
					business();
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
					DataInputStream input = new DataInputStream(socket.getInputStream());
					String messageStr = input.readUTF();
					Map<String,String> message = JSON.parseObject(messageStr, Map.class);
					
					String nodeId = message.get("nodeId");
					String linkNodeId = message.get("linkNodeId");
					
					log.info("==== Thread " + linkNodeId + " ==== " + "previous nodeId : " + nodeId);
					
					DataOutputStream output = new DataOutputStream(socket.getOutputStream());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public void business() {
		if(!node.isVisited()){
			log.info("==== Thread " + node.getId() + " ==== " + "visiting node : " + node);
			
			fireRequest();
		}
	}

	@Override
	public void fireRequest() {
		for(Node linkNode : this.node.getLink()){
			try {
				int linkPort = 20000 + linkNode.getPort();
				Socket socket = new Socket("127.0.0.1",linkPort);
				OutputStream netOut = socket.getOutputStream();  
	            DataOutputStream doc = new DataOutputStream(netOut);  
	            log.info("==== Thread " + node.getId() + " ==== " + "target Node : " + "p" + linkNode.getPort() + ", visited? " + linkNode.isVisited());
	            
	            Map<String,String> message = new HashMap<String,String>();
	            message.put("nodeId", node.getId());
	            message.put("linkNodeId", linkNode.getId());
	            
	            doc.writeUTF(JSON.toJSONString(message));
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		node.setVisited(true);
	}
}
