package transfer;

import java.io.File;

import org.junit.Test;


public class ConvertTest {
	@Test
	public void testConversion() {
		String path = "/Users/djiao/Box Sync/Work/Projects/IMT/immunopath/tsv";
//		String path = "/Users/djiao/Box Sync/Work/Projects/IMT/immunopath/tsv";
//		File input = new File(path, "final_summary.xls");
//		File output = new File(path, "final_summary.csv");
		File input = new File(path, "tsv_Summary_08032015162913.xls");
		File output = new File(path, "Test Summary.tsv");
		FileConvert.immunoTsv(input, output);
		
	}
}