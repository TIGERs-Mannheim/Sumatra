/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 28, 2012
 * Author(s): andres
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.modules.impls.ai.athena.apollon;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.Sumatra;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.IKnowledgeBase;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.persistence.IKnowledgeBasePersistence;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.persistence.ObjectDBPersistence;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.persistence.ObjectDBPlaysPersistence;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.util.KnowledgeHelper;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.config.ConfigManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AConfigManager;


/**
 * Test class for testing the capabilities of the persistence class of the knowledge base.
 * 
 * @author andres
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class PersistenceTest
{
	private static final String			BASE_PATH	= "knowledgeBase/oodb/";
	private static final String			DB_FILE_EXT	= ".odb";
	private static final String			DB_NAME		= "Test";
	
	private static final int				NUM_PLAYS	= 10;
	private static final int				ROUNDS		= 2;
	private static final boolean			GENERATE		= true;
	
	private IKnowledgeBasePersistence	pers;
	
	private List<Stats>						stats			= new LinkedList<Stats>();
	
	
	/**
	 */
	@Before
	public void init()
	{
		Sumatra.touch();
		SumatraModel.getInstance().setUserProperty(AAgent.KEY_AI_CONFIG, "ai_default.xml");
		SumatraModel.getInstance().setUserProperty(AAgent.KEY_GEOMETRY_CONFIG, "RoboCup_2012.xml");
		AConfigManager.registerConfigClient(AIConfig.getInstance().getAiClient());
		AConfigManager.registerConfigClient(AIConfig.getInstance().getGeomClient());
		new ConfigManager(); // Loads all registered configs (accessed via singleton)
	}
	
	
	/**
	 */
	@After
	public void tearDown()
	{
		System.out.println(Stats.header());
		for (Stats stat : stats)
		{
			System.out.println(stat.toCSV());
		}
	}
	
	
	/**
	 */
	@Test
	public void testMultiFile()
	{
		testMultiFile(ROUNDS, DB_NAME);
	}
	
	
	/**
	 */
	@Test
	public void testOneFile()
	{
		testOneFile(ROUNDS, DB_NAME);
	}
	
	
	/**
	 * run the complete test
	 * @param numExtends
	 * @param dbName
	 */
	public void testOneFile(int numExtends, String dbName)
	{
		deleteDB(BASE_PATH + dbName + DB_FILE_EXT);
		
		pers = new ObjectDBPersistence(BASE_PATH, dbName);
		pers.open();
		for (int i = 1; i <= numExtends; i++)
		{
			extendKnowledgeBase(i);
		}
		pers.close();
	}
	
	
	/**
	 * run the complete test
	 * @param numExtends
	 * @param dbName
	 */
	public void testMultiFile(int numExtends, String dbName)
	{
		deleteDB(BASE_PATH + dbName + DB_FILE_EXT);
		
		pers = new ObjectDBPlaysPersistence(BASE_PATH, dbName);
		pers.open();
		for (int i = 1; i <= numExtends; i++)
		{
			extendKnowledgeBase(i);
		}
		pers.close();
	}
	
	
	private void deleteDB(String name)
	{
		File file = new File(name);
		if (file.exists() && !file.delete())
		{
			System.out.println("Could not delete db file: " + file.getAbsolutePath());
		}
		// tmp file
		File file2 = new File(name + "$");
		if (file2.exists())
		{
			file2.delete();
		}
	}
	
	
	/**
	 * @param runNumber
	 */
	@SuppressWarnings("null")
	public void extendKnowledgeBase(int runNumber)
	{
		Stats stat = new Stats();
		stats.add(stat);
		
		// load
		long start = System.currentTimeMillis();
		IKnowledgeBase gklb = pers.loadKnowledgeBase(DB_NAME);
		stat.load = (int) (System.currentTimeMillis() - start);
		System.out.println("Time to load [ms]: " + stat.load);
		
		// check
		assertTrue("KnowledgeBase is null", gklb != null);
		stat.loadedFields = (int) KnowledgeHelper.countKnowledgeBase(gklb);
		System.out.println("loaded field count: " + stat.loadedFields);
		
		// initialize
		if (GENERATE)
		{
			start = System.currentTimeMillis();
			gklb.initialize();
			stat.generate = (System.currentTimeMillis() - start);
			System.out.println("Time to generate [ms]: " + stat.generate);
		}
		
		// extend
		KnowledgeHelper.extendKB(gklb, NUM_PLAYS);
		stat.newFields = KnowledgeHelper.countKnowledgeBase(gklb);
		System.out.println("new field count: " + stat.newFields);
		int desiredFieldCount = (runNumber * NUM_PLAYS * (KnowledgeHelper.NUM_FAILED_FIELDS + KnowledgeHelper.NUM_SUCCESS_FIELDS));
		assertTrue(stat.newFields + " != " + desiredFieldCount, stat.newFields == desiredFieldCount);
		
		// save
		start = System.currentTimeMillis();
		pers.saveKnowledgeBase(gklb, false);
		stat.save = (System.currentTimeMillis() - start);
		System.out.println("Time to save [ms]: " + stat.save);
		
		System.out.println();
	}
	
	private static class Stats
	{
		long	load;
		long	generate;
		long	save;
		long	loadedFields;
		long	newFields;
		
		
		public static String header()
		{
			StringBuilder builder = new StringBuilder();
			builder.append("load");
			builder.append("\t");
			builder.append("generate");
			builder.append("\t");
			builder.append("save");
			builder.append("\t");
			builder.append("loadedFields");
			builder.append("\t");
			builder.append("newFields");
			return builder.toString();
		}
		
		
		public String toCSV()
		{
			StringBuilder builder = new StringBuilder();
			builder.append(load);
			builder.append("\t");
			builder.append(generate);
			builder.append("\t");
			builder.append(save);
			builder.append("\t");
			builder.append(loadedFields);
			builder.append("\t");
			builder.append(newFields);
			return builder.toString();
		}
		
		
		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			builder.append("Stats [load=");
			builder.append(load);
			builder.append(", generate=");
			builder.append(generate);
			builder.append(", save=");
			builder.append(save);
			builder.append(", loadedFields=");
			builder.append(loadedFields);
			builder.append(", newFields=");
			builder.append(newFields);
			builder.append("]");
			return builder.toString();
		}
	}
}
