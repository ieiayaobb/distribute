package com.gen.core;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MatrixGenerator {
	private int length;
	private int matrix;
	private String result;
	
	private String timeStamp;
	
	public MatrixGenerator(int length, int matrix){
		
		timeStamp = new Date().getTime() + "";
		
		this.length = length;
		this.matrix = matrix;
		StringBuffer sb = new StringBuffer();
		for(int i = 0 ; i < length;i++){
			for(int j = 0; j < matrix;j++){
				sb.append(Math.round(Math.random() * 100));
				if(j != matrix - 1){
					sb.append(",");
				}
			}
			sb.append("\n");
		}
		result = sb.toString();
	}
	public void outputFile(){
		File file = new File("m" + length + "n" + matrix + "_" + timeStamp + ".txt");
		try {
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
			
			bos.write(result.getBytes("UTF-8"));
			bos.flush();
			bos.close();
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
		new MatrixGenerator(78, 7).outputFile();;
	}
}
