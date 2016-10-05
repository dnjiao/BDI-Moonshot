package org.mdacc.rists.bdi.db.utils;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;


public class FileTransferAuditUtilTest {

	static Connection con;
	
	@Before
	public void getDBCon() {
		con = DBConnection.getConnection();
	}
	
	@Test
	public void test() {
		
		int fileQueueId = 94290;
		String file = "/rsrch1/rists/moonshot/data/dev/vcf/83995_all_variant_020220161454428833_02162016153131.vcf";
		List<String> list = new ArrayList<String>();
		list.add(file);
		String error = FileTransferAuditUtil.updateFileQueueId(con, list, fileQueueId);
		assertNull(error);
	
	}

}
