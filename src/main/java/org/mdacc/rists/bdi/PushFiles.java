package org.mdacc.rists.bdi;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.mdacc.rists.bdi.db.utils.ConsumerFileReqUtil;
import org.mdacc.rists.bdi.db.utils.DBConnection;
import org.mdacc.rists.bdi.vo.ConsumerFileReqVO;

public class PushFiles {
	public static void main(String[] args) {
		
		if (args[0].equalsIgnoreCase("true")) {
			System.out.println("Sending files");
		}
		else if (args[0].equalsIgnoreCase("fake")){
			System.out.println("Fake sending files");
		} else {
			System.out.println("Not sending files.");
			return;
		}
		String ifPush = args[0];
		try {
			Connection con = DBConnection.getConnection();
			List<ConsumerFileReqVO> consumerList = ConsumerFileReqUtil.getAllConsumers(con);
			for (ConsumerFileReqVO consumer : consumerList) {
				switch (consumer.getConsumer()) {
					case "TRA": PushToTRA.pushFilesByType(consumer.getFileType(), consumer.getApiUri(),
							consumer.getApiUsername(), consumer.getApiPassword(), consumer.getConsumerId(), consumer.getFileTypeId(), ifPush);
								break;
				}		
			}
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
}
