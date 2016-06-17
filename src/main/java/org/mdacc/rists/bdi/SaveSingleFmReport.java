package org.mdacc.rists.bdi;

import java.io.File;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.mdacc.rists.bdi.fm.dao.SpecimenDao;
import org.mdacc.rists.bdi.fm.models.SpecimenTb;

public class SaveSingleFmReport {
	public static void main(String[] args) {
		String filepath = args[0];
		File file = new File(filepath);
		BigDecimal flId = new BigDecimal(args[1]);
		boolean status = SaveFmReports.insertReportTb(file, new BigDecimal(9999), flId);
		System.out.println(status);
		return;
	}
	
	public static void saveOrUpdateSpecimen(String sid) {
		SpecimenTb specimen = new SpecimenTb();
		Date date = new Date();
		String source = "FM";
		BigDecimal etl = new BigDecimal(9999);
		specimen.setSpecimenNo(sid);
		specimen.setEtlProcId(etl);
		specimen.setSpecimenSource(source);
		specimen.setInsertTs(date);
		specimen.setUpdateTs(date);
		EntityManagerFactory emFactory = Persistence.createEntityManagerFactory("RIStore_Flow");
		EntityManager em = emFactory.createEntityManager();
		SpecimenDao specimenDao = new SpecimenDao(em);
		SpecimenTb spec = specimenDao.getSpecimenBySpecno(sid);
		boolean success = false;

		em.close();
		emFactory.close();
	}
}
