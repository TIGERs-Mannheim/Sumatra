/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 19, 2012
 * Author(s): andres
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.persistence;

import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.GeneralKnowledgeBase;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.IKnowledgeBase;


/**
 * This class provides the interface for loading and saving the knowledgeBase in a ObjectDB DB with JPA 2.0.
 * @author andres
 * 
 */
public final class ObjectDBPersistence extends APersistence implements IKnowledgeBasePersistence
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log	= Logger.getLogger(ObjectDBPersistence.class.getName());
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param basePath
	 * @param dbName
	 */
	public ObjectDBPersistence(String basePath, String dbName)
	{
		super(basePath, dbName);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Save a knowledge base in DB. <br />
	 * @param kb
	 * @param merge if true the data will be merged with existing data, otherwise the existing data is deleted and the
	 *           new written
	 */
	@Override
	public void saveKnowledgeBase(IKnowledgeBase kb, boolean merge)
	{
		final EntityTransaction t = getEm().getTransaction();
		t.begin();
		
		if (merge)
		{
			getEm().merge(kb);
		} else
		{
			getEm().persist(kb);
		}
		t.commit();
		log.info("KnowledgeBase saved in DB");
		return;
	}
	
	
	/**
	 * Load a specific knowledge base from DB with the given name. If no knowledge base with this name exists it will be
	 * created (but not saved in DB at this point).
	 * 
	 * @param name
	 * @return
	 */
	@Override
	public IKnowledgeBase loadKnowledgeBase(String name)
	{
		try
		{
			final EntityTransaction t = getEm().getTransaction();
			t.begin();
			final TypedQuery<GeneralKnowledgeBase> q = getEm().createQuery(
					"SELECT kb FROM GeneralKnowledgeBase kb WHERE kb.name = :name", GeneralKnowledgeBase.class);
			
			IKnowledgeBase kb;
			try
			{
				kb = q.setParameter("name", name).getSingleResult();
			} catch (final NoResultException e)
			{
				kb = new GeneralKnowledgeBase(name);
				
				getEm().persist(kb);
			}
			getEm().flush();
			t.commit();
			return kb;
		} catch (final PersistenceException e)
		{
			log.error("Could not load DB: " + e.getMessage());
			throw e;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
