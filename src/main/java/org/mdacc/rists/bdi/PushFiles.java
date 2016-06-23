package org.mdacc.rists.bdi;

import java.util.List;

import org.mdacc.rists.bdi.db.utils.ConsumerFileReqUtil;
import org.mdacc.rists.bdi.vo.ConsumerFileReqVO;

public class PushFiles {
	public static void main(String[] args) {
		List<ConsumerFileReqVO> consumerList = ConsumerFileReqUtil.getAllConsumer();
		for (ConsumerFileReqVO consumer : consumerList) {
			switch (consumer.getConsumer()) {
				case "TRA": PushToTRA.pushFilesByType(consumer.getFileType(), consumer.getConsumerId(), consumer.getFileTypeId());
							break;
			}		
		}
	}
}
