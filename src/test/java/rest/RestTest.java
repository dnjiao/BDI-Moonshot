package rest;

import java.io.File;

import org.junit.Test;

public class RestTest {
	@Test
	public void testSinglePush() {
		String url = "http://10.111.100.207:8098/bdi/serviceingestion?domain=vcf";
		String path = "/Users/djiao/Work/moonshot/vcf";
		File dir = new File(path);
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isFile() && file.getName().endsWith(".vcf")) {
				PushFiles.pushSingle(url, file.getAbsolutePath());
			}
		}
	}
}