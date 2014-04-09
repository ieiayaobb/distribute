package com.gen;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MatrixGeneratorForFloat {
	private int length;
	private int matrix;
	private BufferedOutputStream bos_original;
	private BufferedOutputStream bos_handled;
	
	private String timeStamp;
	
	public MatrixGeneratorForFloat(int length, int matrix){
		this.length = length;
		this.matrix = matrix;
		timeStamp = new Date().getTime() + "";
	}
	public String generate(Boolean isOriginal){
		StringBuffer sb = new StringBuffer();
		
		List<List<Double>> result = new ArrayList<List<Double>>();
		
		for(int i = 0 ; i < length;i++){
			List<Double> eachResult = new ArrayList<Double>();
			for(int j = 0; j < matrix;j++){
				double randomNum = Math.random() * 100;
				eachResult.add(randomNum);
				if(isOriginal){
					Collections.sort(eachResult,new DescComparator());
				}
			}
			result.add(eachResult);
		}
		
		for(int l = 0; l < matrix; l++){
			for(int k = 0; k < length; k++){
				double first = result.get(k).get(0);
				if(isOriginal){
					sb.append(String.format("%.3f",result.get(k).get(l) / first));
				}else{
					sb.append(String.format("%.3f",result.get(k).get(l)));
				}
				sb.append("\t");
			}
			sb.append("\n");
		}
		
		return sb.toString();
	}
	public void outputFile(){
		File file_original = new File("m" + length + "n" + matrix + "_ORIGINAL_ " + timeStamp + ".txt");
		File file_handled = new File("m" + length + "n" + matrix + "_HANDLED_ " + timeStamp + ".txt");
		try {
			bos_original = new BufferedOutputStream(new FileOutputStream(file_original));
			bos_handled = new BufferedOutputStream(new FileOutputStream(file_handled));
			
			bos_original.write(generate(false).getBytes("UTF-8"));
			bos_original.flush();
			bos_original.close();
			
			bos_handled.write(generate(true).getBytes("UTF-8"));
			bos_handled.flush();
			bos_handled.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String[] args){
		new MatrixGeneratorForFloat(12, 10).outputFile();;
	}
}
