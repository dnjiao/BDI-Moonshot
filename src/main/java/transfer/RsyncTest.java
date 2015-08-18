package transfer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RsyncTest {
	public static void main(String[] args) {
		String cmd = "/rsrch2/rists/djiao/apps/sshpass/bin/sshpass -p 'b#gd#123' rsync -auv --delete --include=*.txt --include=*.csv --exclude=* "
				+ "bdiuser@dcprpinformat1.mdanderson.edu:/inform/flatfiles/ipct/ /rsrch1/rists/moonshot/data/dev/mapping";
		System.out.println(cmd);
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
