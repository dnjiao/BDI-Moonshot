package org.mdacc.rists.bdi;

import java.io.File;
import java.math.BigDecimal;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;


public class SaveSingleFmReport {
	public static void main(String[] args) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("RIStore_Flow");

		String filepath = args[0];
		File file = new File(filepath);
		BigDecimal flId = new BigDecimal(args[1]);
		char status = SaveFmReports.insertReportTb(emf, file, new BigDecimal(9999), flId);
		System.out.println(status);
		return;
	}
}
