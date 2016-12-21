package org.mdacc.rists.bdi;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.mdacc.rists.bdi.utils.EmailNotificationClient;
import org.mdacc.rists.bdi.utils.EmailVO;
import org.mdacc.rists.bdi.vo.FileQueueVO;

public class SendEmail {
	static String fromAddress = "djiao@mdanderson.org";
	
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("Usage: SendEmail [to] [file]");
			System.exit(1);
		}
		sendReportEmail(args[0], args[1]);

	}
	
	public static void sendReportEmail(String toAddress, String path) throws Exception {
		DateTimeFormatter FORMAT = DateTimeFormat.forPattern("MM-dd-yyyy");
		DateTime dt = new DateTime();
		String subject = "RIStore Report " + FORMAT.print(dt);
		String message = "RIStore Report " + FORMAT.print(dt);
		EmailNotificationClient client = new EmailNotificationClient();
		client.sendEmailNotification(createEmail(toAddress, subject, message, path));
	}
	
	private static EmailVO createEmail(String toAddress, String subject, String messageBody, String path) throws IOException	{
		EmailVO email = new EmailVO();
		email.setFromAddress(fromAddress);
		email.setToAddresses(toAddress);
		email.setCcAddresses(fromAddress);
		email.setSubject(subject);
		email.setMessageBody(messageBody);
		File file = new File(path);
		email.setAttachmentFileName(file.getName());
		byte[] data = FileUtils.readFileToByteArray(file);
		email.setAttachmentData(data);
		return email;
	}
	

}
