package org.mdacc.rists.bdi.utils;

import java.io.Serializable;

public class EmailVO implements Serializable {
	private static final long serialVersionUID = 1L;

	private String fromAddress;
	private String toAddresses;
	private String ccAddresses;
	private String subject;
	private String messageBody;
	private byte[] attachmentData;
	private String attachmentFileName;

	public String getToAddresses() {
		return toAddresses;
	}

	public void setToAddresses(String toAddresses) {
		this.toAddresses = toAddresses;
	}

	public String getCcAddresses() {
		return ccAddresses;
	}

	public void setCcAddresses(String ccAddresses) {
		this.ccAddresses = ccAddresses;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(String message) {
		this.messageBody = message;
	}


	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public byte[] getAttachmentData() {
		return attachmentData;
	}

	public void setAttachmentData(byte[] attachmentData) {
		this.attachmentData = attachmentData;
	}

	public String getAttachmentFileName() {
		return attachmentFileName;
	}

	public void setAttachmentFileName(String attachmentFileName) {
		this.attachmentFileName = attachmentFileName;
	}


}
