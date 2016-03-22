package org.mdacc.rists.bdi.utils;

import java.net.InetAddress;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.mdacc.hp.ris.trex.service.notification.RISNotificationManager;
import org.mdacc.hp.ris.trex.service.notification.RISNotificationManagerService;
import org.mdacc.hp.ris.trex.service.notification.RisEmailMessage;
import org.mdacc.hp.ris.trex.service.notification.RisEmailWithAttachment;

public class EmailNotificationClient {
	private static final Logger LOGGER = Logger
			.getLogger(EmailNotificationClient.class.getName());
	
	private String NOTIFICATION_URL="http://stage-researchstation/RISNotificationManager-RISNotificationManagerEJB/RISNotificationManagerService?wsdl";

	public boolean sendEmailNotification(EmailVO emailVO) throws Exception {
		if (emailVO.getAttachmentData() == null) {
			return sendEmailNotificationWithoutAttachment(emailVO);	
		} else {
		  return sendEmailNotificationWithAttachment(emailVO);	
		}
	}

	private RISNotificationManagerService intializeRISNotificationService()
			throws Exception {
		String NS_URI = "http://ejb.notificationmanager.ris.hp.mdacc.org/";
		QName serviceName = new QName(NS_URI, "RISNotificationManagerService");
		URL url = new URL(NOTIFICATION_URL);
		RISNotificationManagerService service = new RISNotificationManagerService(
				url, serviceName);
		return service;
	}
	
	private boolean sendEmailNotificationWithoutAttachment(EmailVO emailVO)	throws Exception {
		RISNotificationManagerService service = intializeRISNotificationService();
		try {
			RISNotificationManager port = service.getRISNotificationManager();
			RisEmailMessage message = new RisEmailMessage();
			message.setFromAddress(emailVO.getFromAddress());
			message.setToAddress(emailVO.getToAddresses());
			message.setCcAddress(emailVO.getCcAddresses());
			message.setMessageBody(emailVO.getMessageBody());
			message.setSubject(emailVO.getSubject());
			LOGGER.log(Level.INFO, "Before sending email");
			String mail = port.sendMail(message);
			LOGGER.log(Level.INFO, "Email status : " + mail);
		} catch (Exception e) {
			throw new Exception("Failed in sending Email Notification : "
					+ e.getMessage());
		}
		return true;
	}

	private boolean sendEmailNotificationWithAttachment(EmailVO emailVO)
			throws Exception {
		RISNotificationManagerService service = intializeRISNotificationService();
		try {
			RISNotificationManager port = service.getRISNotificationManager();
			RisEmailWithAttachment emailWithAttachment = new RisEmailWithAttachment();
			emailWithAttachment.setFromAddress(emailVO.getFromAddress());
			emailWithAttachment.setToAddress(emailVO.getToAddresses());
			emailWithAttachment.setCcAddress(emailVO.getCcAddresses());
			emailWithAttachment.setMessageBody(emailVO.getMessageBody());
			emailWithAttachment.setSubject(emailVO.getSubject());
			if (emailVO.getAttachmentData() != null) {
				emailWithAttachment.setAttachment(emailVO.getAttachmentData());
				emailWithAttachment
						.setFileName(emailVO.getAttachmentFileName());
			}
			LOGGER.log(Level.INFO, "Before sending email");
			String mailResponse = port
					.sendMailWithAttachment(emailWithAttachment);
			LOGGER.log(Level.INFO, "Email status : " + mailResponse);
		} catch (Exception e) {
			throw new Exception(
					"Failed in sending Email Notification with attachment(s) : "
							+ e.getMessage());
		}

		return true;
	}

	public String getHost(){
		String host = "\n Unspecified Host";
		
		try{
			host = "\n" + InetAddress.getLocalHost().getHostName();	
		}catch(Exception ex){
			//do nothing
		}
		
		return host;
	}

}
