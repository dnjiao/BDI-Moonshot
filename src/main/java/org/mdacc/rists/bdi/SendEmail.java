package org.mdacc.rists.bdi;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mdacc.hp.ris.trex.service.notification.RISNotificationManager;
import org.mdacc.hp.ris.trex.service.notification.RISNotificationManagerService;
import org.mdacc.hp.ris.trex.service.notification.RisEmailMessage;
import org.mdacc.rists.bdi.db.utils.DBConnection;
import org.mdacc.rists.bdi.db.utils.FileQueueUtil;
import org.mdacc.rists.bdi.utils.EmailNotificationClient;
import org.mdacc.rists.bdi.utils.EmailVO;
import org.mdacc.rists.bdi.vo.FileQueueVO;

public class SendEmail {
	static String fromAddress = "foundationmedicine@mdanderson.org";
	
	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			System.err.println("Usage: SendEmail [to] [type]");
			System.exit(1);
		}
		
		String toAddress = args[0];
		String type = args[1];
		
		Connection conn = DBConnection.getConnection();
		List<FileQueueVO> fqList = FileQueueUtil.getUnvalidated(conn, type);
		
		DateTimeFormatter FORMAT = DateTimeFormat.forPattern("MMddyyyy");
		DateTime dt = new DateTime();
		String subject = "Unvalidated files " + FORMAT.print(dt);
		String message = constructMessage(fqList);
		EmailNotificationClient client = new EmailNotificationClient();
		client.sendEmailNotification(createEmail(toAddress, subject, message));

	}
	private static String constructMessage(List<FileQueueVO> list) throws SQLException {
		if (list == null) {
			System.out.println("No unvalidated files.");
			System.exit(0);
		}
		String body = "Here is the list of files either failed validation or missing validation info.\n";
		for (FileQueueVO fq : list) {
			String filepath = fq.getFileUri();
			String filename = filepath.substring(filepath.lastIndexOf("/")+1, filepath.length());
			body += filename + "\n";
			
		}
		return body;
	}

	private static EmailVO createEmail(String toAddress, String subject, String messageBody)	{
		EmailVO email = new EmailVO();
		email.setFromAddress(fromAddress);
		
		email.setToAddresses(toAddress);
		email.setSubject(subject);
		email.setMessageBody(messageBody);
		return email;
	}
}
