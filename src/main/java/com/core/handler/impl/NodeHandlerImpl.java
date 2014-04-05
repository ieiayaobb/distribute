package com.core.handler.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.core.bean.Node;
import com.core.handler.INodeHandler;

public class NodeHandlerImpl implements INodeHandler {

	private static final Logger log = LoggerFactory.getLogger(NodeHandlerImpl.class);
	
	private Node node;
	
	private ServerSocket listenServer;
	private ServerSocket msgServer;
	
	private boolean visited = false;
	
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
		try {
			msgServer = new ServerSocket(msgPort);
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
					BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					String line;
					while(true){
						line = br.readLine();
						if(line != null){
							log.info("message : " + line);
						}
					}
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
			try {
				Socket socket = msgServer.accept();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void business() {
		if(!this.visited){
			log.info("visit node : " + node);
			visited = true;
			
			fireRequest();
		}
	}

	@Override
	public void fireRequest() {
		for(Node linkNode : this.node.getLink()){
			try {
				int linkPort = 20000 + linkNode.getPort();
				Socket socket = new Socket("127.0.0.1",linkPort);
				PrintWriter out = new PrintWriter(socket.getOutputStream());
				out.write("abc");
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
