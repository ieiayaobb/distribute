package com.gen;

import java.util.Comparator;

public class DescComparator implements Comparator<Double> {
	@Override
	public int compare(Double o1, Double o2) {
		if(o1 < o2){
			return 1;
		}else if(o1 > o2){
			return -1;
		}
		return 0;
	}
}
