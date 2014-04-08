package com.core.handler;

import com.core.bean.Node;

public interface INodeHandler {
	
	void startListen();
	
	void business(Node node);
	
	void fireRequest(Node node);
	
	void start(Node node);
}
