package org.mdacc.rists.bdi.fm.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.mdacc.rists.bdi.models.FmReportTb;

public class FmReportDao {
	EntityManager entityManager;

	public FmReportDao() {
		super();
	}

	public FmReportDao(EntityManager entityManager) {
		super();
		this.entityManager = entityManager;
	}
	
	public void persistReport(FmReportTb report) {
		EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.persist(report);
            transaction.commit();
        } catch (Exception e) {
        	e.printStackTrace();
        	if (transaction.isActive()) {
        		transaction.rollback();
        	}
        }
	}
	
	public void mergeReport(FmReportTb report) {
		EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.merge(report);
            transaction.commit();
        } catch (Exception e) {
        	e.printStackTrace();
            transaction.rollback();
        }
	}
}
