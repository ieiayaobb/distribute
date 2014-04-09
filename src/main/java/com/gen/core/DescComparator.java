package com.gen.core;

import java.util.Comparator;

import com.gen.bean.Point;

public class DescComparator implements Comparator<Point> {
	@Override
	public int compare(Point o1, Point o2) {
		if(o1.getNum() < o2.getNum()){
			return 1;
		}else if(o1.getNum() > o2.getNum()){
			return -1;
		}
		return 0;
	}
}
