package org.mdacc.rists.bdi.transfer;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.joda.time.DateTime;
import org.mdacc.hp.ris.trex.service.notification.RISNotificationManager;
import org.mdacc.hp.ris.trex.service.notification.RISNotificationManagerService;
import org.mdacc.hp.ris.trex.service.notification.RisEmailMessage;
import org.mdacc.rists.bdi.dbops.FileQueueUtil;
import org.mdacc.rists.bdi.utils.EmailNotificationClient;
import org.mdacc.rists.bdi.utils.EmailVO;

public class SendEmailToFoundation {
	static String fromAddress = "foundationmedicine@mdanderson.org";
	
	public static void main(String[] args) {
		EmailNotificationClient client = new EmailNotificationClient();
		String toAddress = "djiao@mdanderson.org";
		String message = "test";
		String subject = "test";
		try {
			client.sendEmailNotification(constructEmail(toAddress, subject, message));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private static EmailVO constructEmail(String toAddress, String subject, String messageBody)	{
		EmailVO email = new EmailVO();
		email.setFromAddress(fromAddress);
		
		email.setToAddresses(toAddress);
		email.setSubject(subject);
		email.setMessageBody(messageBody);
		return email;
	}
	
	private void setToMessage(ResultSet rs) {
		
		try {
			while (rs.next()) {
				int rowId = rs.getInt("ROW_ID");
				String filepath = rs.getString("FILE_URI");
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
