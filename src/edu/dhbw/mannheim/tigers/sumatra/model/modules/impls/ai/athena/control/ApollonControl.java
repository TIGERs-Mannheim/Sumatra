/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 30, 2012
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control;

import java.io.File;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.persistence.APersistence;


/**
 * This class should be used to control the sub-module apollon on runtime (e.g. from GUI). Its members represent the
 * state of the GUI.
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 */
public class ApollonControl
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log								= Logger.getLogger(ApollonControl.class.getName());
	
	/**  */
	public static final String		KB_NAME_KEY						= "kbName";
	/**  */
	public static final String		PATH_KEY							= "kbPath";
	/**  */
	public static final String		ACCEPTABLE_MATCH_KEY			= "kbPath";
	private static final double	DEFAULT_ACCEPTABLE_MATCH	= 0.8;
	private static final String	DEFAULT_DB_NAME				= "Default";
	
	private static final int		MAX_NEW_DB_RETRY				= 10;
	
	private String						knowledgeBaseName;
	private double						acceptableMatch;
	private String						databasePath;
	/**
	 * Persist Strategy: True indicates that a merge will be tried, false says that always the existing knowledgebase
	 * will be overwritten
	 */
	private boolean					persistStrategyMerge;
	private boolean					saveOnClose;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	  * 
	  */
	public ApollonControl()
	{
		knowledgeBaseName = SumatraModel.getInstance().getUserProperty(KB_NAME_KEY);
		if ((knowledgeBaseName == null) || knowledgeBaseName.isEmpty())
		{
			knowledgeBaseName = DEFAULT_DB_NAME;
		}
		
		
		try
		{
			String val = SumatraModel.getInstance().getUserProperty(ACCEPTABLE_MATCH_KEY);
			if (val == null)
			{
				throw new NumberFormatException();
			}
			acceptableMatch = Double.valueOf(val);
		} catch (NumberFormatException e)
		{
			acceptableMatch = DEFAULT_ACCEPTABLE_MATCH;
		}
		
		databasePath = SumatraModel.getInstance().getUserProperty(PATH_KEY);
		if ((databasePath == null) || databasePath.isEmpty())
		{
			File tmpFile = new File("knowledgeBase/oodb/");
			databasePath = tmpFile.getAbsolutePath();
		}
		
		
		String kbnBase = knowledgeBaseName;
		for (int i = 0; i < MAX_NEW_DB_RETRY; i++)
		{
			File dbLockFile = new File(databasePath + "/" + knowledgeBaseName + APersistence.DB_FILE_EXT + "$");
			if (!dbLockFile.exists())
			{
				break;
			}
			log.warn("Object Database " + knowledgeBaseName + " is already in use. Will fall back to a new one.");
			knowledgeBaseName = kbnBase + "_" + i;
			if (i == (MAX_NEW_DB_RETRY - 1))
			{
				throw new IllegalStateException("Tried " + MAX_NEW_DB_RETRY
						+ " times to load new database, but all were locked. Something is not correct");
			}
		}
		
		persistStrategyMerge = false;
		saveOnClose = true;
	}
	
	
	/**
	 * Copy constructor
	 * @param base
	 */
	public ApollonControl(final ApollonControl base)
	{
		knowledgeBaseName = base.knowledgeBaseName;
		acceptableMatch = base.acceptableMatch;
		databasePath = base.databasePath;
		persistStrategyMerge = base.persistStrategyMerge;
		saveOnClose = base.saveOnClose;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the knowledgeBaseName
	 */
	public synchronized String getKnowledgeBaseName()
	{
		return knowledgeBaseName;
	}
	
	
	/**
	 * @param knowledgeBaseName the knowledgeBaseName to set
	 */
	public synchronized void setKnowledgeBaseName(String knowledgeBaseName)
	{
		this.knowledgeBaseName = knowledgeBaseName;
		SumatraModel.getInstance().setUserProperty(ApollonControl.KB_NAME_KEY, knowledgeBaseName);
	}
	
	
	/**
	 * @return the acceptableMatch
	 */
	public synchronized double getAcceptableMatch()
	{
		return acceptableMatch;
	}
	
	
	/**
	 * @param acceptableMatch the acceptableMatch to set
	 */
	public synchronized void setAcceptableMatch(double acceptableMatch)
	{
		this.acceptableMatch = acceptableMatch;
		SumatraModel.getInstance().setUserProperty(ACCEPTABLE_MATCH_KEY, String.valueOf(acceptableMatch));
	}
	
	
	/**
	 * @return the databasePath
	 */
	public synchronized String getDatabasePath()
	{
		return databasePath;
	}
	
	
	/**
	 * @param databasePath the databasePath to set
	 */
	public synchronized void setDatabasePath(String databasePath)
	{
		this.databasePath = databasePath;
	}
	
	
	/**
	 * @return the persistStrategyMerge
	 */
	public synchronized boolean isPersistStrategyMerge()
	{
		return persistStrategyMerge;
	}
	
	
	/**
	 * @param persistStrategyMerge the persistStrategyMerge to set
	 */
	public synchronized void setPersistStrategyMerge(boolean persistStrategyMerge)
	{
		this.persistStrategyMerge = persistStrategyMerge;
	}
	
	
	/**
	 * @return the saveOnClose
	 */
	public synchronized boolean isSaveOnClose()
	{
		return saveOnClose;
	}
	
	
	/**
	 * @param saveOnClose the saveOnClose to set
	 */
	public synchronized void setSaveOnClose(boolean saveOnClose)
	{
		this.saveOnClose = saveOnClose;
	}
	
}
