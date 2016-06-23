package org.mdacc.rists.bdi.fm.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.mdacc.rists.bdi.models.FmReportTb;
import org.mdacc.rists.bdi.models.SpecimenTb;

public class SpecimenDao {
	EntityManager entityManager;

	public SpecimenDao() {
		super();
	}

	public SpecimenDao(EntityManager entityManager) {
		super();
		this.entityManager = entityManager;
	}
	
	public boolean persistSpecimen(SpecimenTb specimen) {
		EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.persist(specimen);
            transaction.commit();
            return true;
        } catch (Exception e) {
        	String fmId = specimen.getFmReportTbs().get(0).getFrFmId();
        	System.err.println("Loading " + fmId + " failed");
        	e.printStackTrace();
            transaction.rollback();
            return false;
        }
	}
	
	public boolean updateSpecimen(SpecimenTb specimen, FmReportTb report) {
		EntityTransaction transaction = entityManager.getTransaction();
		List<FmReportTb> reportList = new ArrayList<FmReportTb>();
		reportList.add(report);
        try {
            transaction.begin();
            specimen.setFmReportTbs(reportList);
            transaction.commit();
            return true;
        } catch (Exception e) {
        	String specNo = specimen.getSpecimenNo();
        	System.err.println("Updating " + specNo + " failed");
        	e.printStackTrace();
            transaction.rollback();
            return false;
        }
	}
	
	public SpecimenTb getSpecimenBySpecno(String sno) {
		Query q = entityManager.createQuery("SELECT s FROM SpecimenTb s WHERE specimen_no = :specno");
		q.setParameter("specno", sno);
		try {
			return (SpecimenTb) q.getSingleResult();
		} catch (NoResultException exc) {
			return null;
		}
		
	}
	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	

}
