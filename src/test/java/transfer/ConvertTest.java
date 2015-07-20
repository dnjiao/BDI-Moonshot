package transfer;

import java.io.File;

import org.junit.Test;


public class ConvertTest {
	@Test
	public void testConversion() {
		String path = "/Users/djiao/Box Sync/Work/Projects/IMT/flowcyto/104-105";
//		String path = "/Users/djiao/Box Sync/Work/Projects/IMT/immunopath/tsv";
//		File input = new File(path, "final_summary.xls");
//		File output = new File(path, "final_summary.csv");
		File input = new File(path, "summary_mod.xls");
		File output = new File(path, "summary_mod.csv");
		FileConvert.flowTsv(input, output);
		
	}
}