package org.mdacc.rists.bdi.db.utils;

import static org.junit.Assert.*;

import java.sql.Connection;

import org.junit.Before;
import org.junit.Test;

public class FileTypeUtilTest {
	Connection con;
	
//	System.out.println(getFileTypeId(con, "FM"));
	@Before
	public void before() {
		con = DBConnection.getConnection();
	}
	@Test
	public void testGetFileTypeId() {
		int fileTypeId = FileTypeUtil.getFileTypeId(con, "FM");
		System.out.println("@Test fileTypeId = " + fileTypeId);
		assertEquals(fileTypeId, 10);
	}

}
