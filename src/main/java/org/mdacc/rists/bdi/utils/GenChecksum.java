package org.mdacc.rists.bdi.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;

public class GenChecksum {
	public static void main(String[] args) {
		String fPath = args[0];
		File file = new File(fPath);
		System.out.println(getMd5(file));
	}
	
	public static String getMd5(File file) {
		FileInputStream fis;
		String md5 = null;
		try {
			fis = new FileInputStream(file);
			md5 = DigestUtils.md5Hex(fis);
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return md5;
	}
}
