package org.mdacc.rists.bdi;

import java.io.IOException;

public class RunLinuxCmd {
	public static void main(String[] args) {
		modFilePerm(args[0], args[1]);
	}
	public static void modFilePerm(String cmd, String filepath) {
		try {
			Runtime.getRuntime().exec(new String[]{"bash", "-c", cmd, filepath});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
}