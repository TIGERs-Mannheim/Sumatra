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

import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.GeneralKnowledgeBase;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.IKnowledgeBase;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.KnowledgePlay;


/**
 * This class provides the interface for loading and saving the knowledgeBase in a ObjectDB DB with JPA 2.0.
 * 
 * It saves the each knowledgebase in a seperate database and the knowledgebase object is not saved directly. instead
 * only the plays are saved...
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 */
public final class ObjectDBPlaysPersistence extends APersistence implements IKnowledgeBasePersistence
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log	= Logger.getLogger(ObjectDBPlaysPersistence.class.getName());
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param basePath base Path to folder for database file
	 * @param dbName name of the database = knowledgebase
	 */
	public ObjectDBPlaysPersistence(String basePath, String dbName)
	{
		super(basePath, dbName);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Save a knowledge base in DB. Note only Plays are save. A Database File represents a knowledge base. <br />
	 * @param kb
	 * @param merge if true the data will be merged with existing data, otherwise the existing data is deleted and the
	 *           new written
	 */
	@Override
	public void saveKnowledgeBase(IKnowledgeBase kb, boolean merge)
	{
		try
		{
			saveKnowledgeBaseNoExHandling(kb, merge);
		} catch (PersistenceException e1)
		{
			String tempName = System.nanoTime() + "-" + new Random(System.nanoTime()).nextInt();
			log.error("Persistence Exception occurred. Data will be saved in temp database: " + tempName + ".odb", e1);
			ObjectDBPlaysPersistence tempPers = new ObjectDBPlaysPersistence(getBasePath(), tempName);
			try
			{
				tempPers.saveKnowledgeBaseNoExHandling(kb, merge);
			} catch (PersistenceException e2)
			{
				log.fatal("Persistence Exception occurred. Data is lost", e2);
				return;
			}
		}
		log.info("KnowledgeBase saved in odb");
	}
	
	
	private void saveKnowledgeBaseNoExHandling(IKnowledgeBase kb, boolean merge)
	{
		if (kb != null)
		{
			log.info(kb.getKbStatistics());
			log.debug("Saving KnowledgeBase in odb");
			EntityManager em = getEm();
			if (em != null)
			{
				final EntityTransaction t = em.getTransaction();
				
				t.begin();
				doSave(kb, merge);
				t.commit();
				
				log.info("KnowledgeBase saved in odb");
			} else
			{
				log.warn("KnowledgeBase could not be saved cause the entity manager is null, maybe it was not opened");
			}
		}
	}
	
	
	private void doSave(IKnowledgeBase kb, boolean merge)
	{
		for (KnowledgePlay kPlay : kb.getKnowledgePlays())
		{
			if (merge)
			{
				getEm().merge(kPlay);
				log.debug("Merged");
			} else
			{
				getEm().persist(kPlay);
			}
		}
	}
	
	
	/**
	 * Load a specific knowledge base from a DB with the given name. If no knowledge base with this name exists it will
	 * be created (but not saved in DB at this point). <br />
	 * 
	 * The name must be euqal to the db file name for this knowledge base.
	 * 
	 * @param name
	 * @return
	 */
	@Override
	public IKnowledgeBase loadKnowledgeBase(String name)
	{
		IKnowledgeBase kb = new GeneralKnowledgeBase(name);
		try
		{
			final EntityTransaction t = getEm().getTransaction();
			t.begin();
			final TypedQuery<KnowledgePlay> q = getEm().createQuery("SELECT kPlay FROM KnowledgePlay kPlay",
					KnowledgePlay.class);
			try
			{
				for (KnowledgePlay kPlay : q.getResultList())
				{
					kb.addKnowledgePlay(kPlay);
				}
			} catch (final NoResultException e)
			{
				// Nothing to do, because Knowledge Base was empty
			}
			t.commit();
		} catch (final PersistenceException e)
		{
			log.error("Could not load DB: " + e.getMessage(), e);
		}
		return kb;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
