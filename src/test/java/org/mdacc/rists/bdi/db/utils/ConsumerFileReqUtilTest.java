package org.mdacc.rists.bdi.db.utils;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mdacc.rists.bdi.vo.ConsumerFileReqVO;

public class ConsumerFileReqUtilTest {

	Connection con;
	
	@Before
	public void setUp() throws Exception {
		con = DBConnection.getConnection();
	}

	@After
	public void tearDown() throws Exception {
		con.close();
	}

	@Test
	public void test() {
		List<ConsumerFileReqVO> consumerList = ConsumerFileReqUtil.getAllConsumers(con);
		for (ConsumerFileReqVO consumer : consumerList) {
			if (consumer.getConsumer() == "TRA") {
				if (consumer.getFileType() == "VCF") {
					String expectedUri = "http://10.113.241.55:8099/bdi/serviceingestion?domain=vcf";
					assertEquals(consumer.getApiUri(), expectedUri);
				}
				if (consumer.getFileType() == "CNV") {
					String expectedUri = "http://10.113.241.55:8099/bdi/serviceingestion?domain=cnv";
					assertEquals(consumer.getApiUri(), expectedUri);
				}
				if (consumer.getFileType() == "FM") {
					String expectedUri = "http://10.113.241.55:8099/bdi/serviceingestion?domain=fmd";
					assertEquals(consumer.getApiUri(), expectedUri);
				}
			}	
		}
	}
}
