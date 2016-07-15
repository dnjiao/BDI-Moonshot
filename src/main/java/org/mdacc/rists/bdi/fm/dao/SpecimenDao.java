package org.mdacc.rists.bdi.fm.dao;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;

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
        	String fmId = specimen.getFmReportTb().getFrFmId();
        	System.err.println("Loading " + fmId + " failed");
        	e.printStackTrace();
            transaction.rollback();
            return false;
        }
	}
	
	public boolean updateSpecimen(SpecimenTb specimen, String blockId, String mrn, Date date, BigDecimal etl, FmReportTb report, BigDecimal flId) {
		EntityTransaction transaction = entityManager.getTransaction();	
        try {
            transaction.begin();
            specimen.setSpecimenNo(blockId);
            specimen.setMrn(mrn);
            specimen.setUpdateTs(date);
            specimen.setEtlProcId(etl);
            specimen.setFmReportTb(report);
            specimen.setFileLoadId(flId);
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
	
	public SpecimenTb findSpecimenBySpecno(String sno) {
		try {
			return (SpecimenTb) entityManager.createNamedQuery("SpecimenTb.findBySpecimenNo")
										.setParameter("specimenNo", sno).getSingleResult();
		} catch (NoResultException ex) {
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
