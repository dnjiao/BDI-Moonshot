package transfer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class SftpTest {
	public static void main(String[] args) {
		File dest = new File(args[1]);
		if (!dest.exists()) {
			System.err.println(args[1] + " does not exist.");
			return;
		}
		if (!dest.isDirectory()) {
			System.err.println(args[1] + " is not a directory.");
			return;
		}
		
		String[] cmd = new String[]{"/bin/bash", args[0], args[1]};
		Process p;
		try {
			p = Runtime.getRuntime().exec(cmd);
			String line;
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while ((line = in.readLine()) != null) {
				System.out.println(line);
			}
			p.waitFor();
			in.close();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
