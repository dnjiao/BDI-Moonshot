package hibernate;

import org.joda.time.DateTime;
import org.junit.Test;
import org.mdacc.rists.bdi.transfer.PullFiles;

public class InsertFileLocationTest {
	@Test
	public void testInsertFileLoc() {
		String filetype = "vcf";
		String source = "/rsrch1/rists/djiao/vcf-top";
		DateTime current = new DateTime();
		PullFiles.insertFileLocationTB(filetype, source, current);
	}
}