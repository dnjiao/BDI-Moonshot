package org.mdacc.rists.bdi;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class PushSingleToTRA {
	final static String URL_STRING = "http://10.113.241.55:8099/bdi/serviceingestion?domain=";
	final static String USERNAME = "ristsvc";
	final static String PASSWORD = "CH!M@321";
	static List <String> TYPES = Arrays.asList("vcf", "cnv", "exon", "gene", "splice");
	
	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Invalid arguments.Usage: PushSingleTest [type] [filepath]");
			System.exit(1);
		}
		String prefix = URL_STRING  + args[0] + "&fileName=";
		File file = new File(args[1]);
		if (!file.exists()) {
			System.err.println("File does not exist, " + args[1]);
			System.exit(1);
		}
		PushToTRA.pushSingle(prefix, USERNAME, PASSWORD, args[1], "true");
	}
}