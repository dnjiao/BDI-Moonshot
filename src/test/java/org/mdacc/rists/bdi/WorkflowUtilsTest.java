package org.mdacc.rists.bdi;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class WorkflowUtilsTest {

	@Test
	public void testFixMappingFile() {
		File in = new File("/Users/djiao/Work/moonshot/foundation/MSBIO_SPCMN_20151016_12222015134702.txt");
		File out = new File("/Users/djiao/Work/moonshot/foundation/MSBIO_SPCMN_20151016_12222015134702_fixed.txt");
		if (out.exists()) {
			out.delete();
		}
		WorkflowUtils.fixMappingFile(in, out);
		assertTrue(out.exists());
	}
	
	@Test
	public void testIsMapping() {
		File file = new File("/Users/djiao/Work/moonshot/mapping/MSBIO_SPCMN_20160106_01072016050004.txt");
		assertTrue(WorkflowUtils.isMapping(file));
	}

}
