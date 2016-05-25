package org.mdacc.rists.bdi.fm.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.mdacc.rists.bdi.fm.models.SpecimenTb;

public class SpecimenDao {
	EntityManager entityManager;

	public SpecimenDao() {
		super();
	}

	public SpecimenDao(EntityManager entityManager) {
		super();
		this.entityManager = entityManager;
	}
	
	public void persistSpecimen(SpecimenTb specimen) {
		EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.persist(specimen);
            transaction.commit();
        } catch (Exception e) {
        	e.printStackTrace();
            transaction.rollback();
        }
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	

}
