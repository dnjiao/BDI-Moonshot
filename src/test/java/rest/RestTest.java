package rest;

import java.io.File;

import org.junit.Test;

import transfer.PushFiles;

public class RestTest {
	@Test
	public void testPush() {
		final String TYPE = "splice";
		final String EXT = ".txt";
		String url = "http://10.111.100.207:8098/bdi/serviceingestion?domain=" + TYPE + "&fileName=";
		String path = "/Users/djiao/Work/moonshot/dest/" + TYPE;
		File dir = new File(path);
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isFile() && file.getName().endsWith(EXT)) {
				PushFiles.pushSingle(url, file.getAbsolutePath());
			}
		}
	}
}