package transfer;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class TransferTest {
	@Test
	public void testCpFiles() throws IOException {
		String source = "/Users/djiao/Work/moonshot/vcf/DGomez-MOON2Lung-MS2LSP-90909-T_150107-0324-150318-0206_MERGE-1CAAAAG-1CAAAAG--DGomez-MOON2Lung-MS2LSP-90909-N_150107-0324-150318-0206_MERGE-7TAGCTT-7TAGCTT_all_variant_051820151431984525.vcf";
		String dest = "/Users/djiao/Work/moonshot/vcf/dest";
		String type = "vcf";
		String mode = "update all";
		File file = new File("/Users/djiao/Work/moonshot/vcf/logs/tmp.log");
		if (!file.exists())
			file.createNewFile();
		
		PullFiles.cpFiles(source, dest, type, mode, file);
	}
	
	
}