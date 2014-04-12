package com.gen.bean;

import java.util.ArrayList;
import java.util.List;

public class Point {
	private int id;
	
	private double num;
	
	public double getNum() {
		return num;
	}

	public void setNum(double num) {
		this.num = num;
	}

	public Point(int id, double num){
		this.id = id;
		this.num = num;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	public int getId(){
		return this.id;
	}
	
}
