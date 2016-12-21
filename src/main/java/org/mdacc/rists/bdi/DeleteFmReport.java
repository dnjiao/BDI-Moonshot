package org.mdacc.rists.bdi;

import java.io.File;
import java.math.BigDecimal;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.mdacc.rists.bdi.models.FmReportTb;
import org.mdacc.rists.bdi.models.SpecimenTb;
import org.mdacc.rists.bdi.fm.dao.SpecimenDao;

public class DeleteFmReport {
	
	final static String DBNAME = "RIStore_" + System.getenv("DEV_ENV").toUpperCase();

	public static void main(String[] args) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(DBNAME);
		EntityManager em = emf.createEntityManager();
		
		SpecimenDao specimenDao = new SpecimenDao(em);
		SpecimenTb specimenTb = specimenDao.findSpecimenBySpecno(args[0]);
		if (specimenTb != null) {
			EntityTransaction transaction = em.getTransaction();	
			FmReportTb report = specimenTb.getFmReportTb();
			try {
				transaction.begin();
				report.removeAllChildren();
				transaction.commit();
			} catch (Exception e) {
				e.printStackTrace();
	            transaction.rollback();
			}
		}
/*		File file = new File(args[1]);
		BigDecimal etl = new BigDecimal(99999);
		BigDecimal flId = new BigDecimal(11558);
		SaveFmReports.insertReportTb(emf, file, etl, flId);*/
		
		em.close();
		emf.close();
	}
}
