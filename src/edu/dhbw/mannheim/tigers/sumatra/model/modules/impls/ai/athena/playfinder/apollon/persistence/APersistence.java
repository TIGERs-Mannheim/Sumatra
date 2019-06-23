/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 12, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;

import org.apache.log4j.Logger;


/**
 * Abstract Persistence class
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public abstract class APersistence
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger	log			= Logger.getLogger(APersistence.class.getName());
	
	private final String				basePath;
	/** file extension for database files */
	public static final String		DB_FILE_EXT	= ".odb";
	private final String				dbName;
	
	private EntityManagerFactory	emf;
	private EntityManager			em;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	static
	{
		// Set config file for ObjectDB access, needed for LearningPlayFinder's KnowledgeBase
		System.setProperty("objectdb.conf", "config/objectdb/objectdb.conf");
	}
	
	
	/**
	 * @param basePath
	 * @param dbName
	 */
	public APersistence(String basePath, String dbName)
	{
		String fileSeperator = System.getProperty("file.separator");
		if (!basePath.endsWith(fileSeperator))
		{
			this.basePath = basePath + fileSeperator;
		} else
		{
			this.basePath = basePath;
		}
		this.dbName = dbName;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Do not use this in this implementation. it will be called automatically.
	 */
	public void open()
	{
		if ((em == null) || !em.isOpen())
		{
			log.debug("Opening odb connection");
			emf = Persistence.createEntityManagerFactory(getDbFullPath());
			em = emf.createEntityManager();
			em.setFlushMode(FlushModeType.AUTO);
			log.debug("odb connection opened");
		}
	}
	
	
	/**
	 * Do not use this in this implementation. it will be called automatically.
	 */
	public void close()
	{
		if (em.isOpen())
		{
			log.debug("Closing odb connection");
			em.close();
			emf.close();
			log.debug("odb connection closed");
		}
	}
	
	
	/**
	 * @return
	 */
	public boolean isOpen()
	{
		return (em != null) && em.isOpen();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	protected String getDbFullPath()
	{
		return basePath + dbName + DB_FILE_EXT;
	}
	
	
	/**
	 * @return the emf
	 */
	public final EntityManagerFactory getEmf()
	{
		return emf;
	}
	
	
	/**
	 * @return the em
	 */
	public final EntityManager getEm()
	{
		return em;
	}
	
	
	protected final String getBasePath()
	{
		return basePath;
	}
}
