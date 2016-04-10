package org.mdacc.rists.bdi.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;

public class GenChecksum {
	public static void main(String[] args) {
		String fPath = args[0];
		System.out.println(getMd5(fPath));
	}
	
	public static String getMd5(String filepath) {
		FileInputStream fis;
		String md5 = null;
		try {
			fis = new FileInputStream(new File(filepath));
			md5 = DigestUtils.md5Hex(fis);
			fis.close();
		} catch (FileNotFoundException e) {
			System.err.println("Error: " + filepath + " not found.");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return md5;
	}
}
