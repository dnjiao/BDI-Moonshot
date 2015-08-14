package rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class SaveToPush {
	public static void main(String[] args) {
		final String TYPE = System.getenv("TYPE").toLowerCase();
		final String LOCAL_PATH = "/rsrch1/rists/moonshot/data/dev";
		String path = LOCAL_PATH + "/" + TYPE;
		String logPath = path + "/logs";
		File pullLog = PushFiles.lastPullLog(logPath);
		try {
			BufferedReader br = new BufferedReader(new FileReader(pullLog));
			File notSentLog = new File(logPath, "not_sent.log");
			PrintWriter writer = new PrintWriter(new FileWriter(notSentLog, true));
			String line;
			while ((line = br.readLine()) != null) {
				writer.println(line.split("\t")[0]);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}