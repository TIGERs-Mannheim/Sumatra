/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 19, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.performance.hashmap;

import edu.dhbw.mannheim.tigers.sumatra.Sumatra;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.Agent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.config.ConfigManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AConfigManager;


/**
 * Tests speed for accessing values over a small HashMap or directly
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class HashMapVsDirect
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final int	RUNS	= 1000;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// Load configuration
		Sumatra.touch();
		SumatraModel.getInstance().setUserProperty(AAgent.KEY_AI_CONFIG, Agent.VALUE_AI_CONFIG);
		SumatraModel.getInstance().setUserProperty(AAgent.KEY_BOT_CONFIG, Agent.VALUE_BOT_CONFIG);
		AConfigManager.registerConfigClient(AIConfig.getInstance().getBotClient());
		AConfigManager.registerConfigClient(AIConfig.getInstance().getAiClient());
		new ConfigManager(); // Loads all registered configs (accessed via singleton)
		
		long start1 = System.nanoTime();
		float val1 = 0;
		for (int i = 0; i < RUNS; i++)
		{
			val1 = AIConfig.getGeneral(EBotType.TIGER).getBreakCurve();
		}
		System.out.println(val1);
		long end1 = System.nanoTime();
		
		long start2 = System.nanoTime();
		float val2 = 0;
		float val3 = AIConfig.getGeneral(EBotType.TIGER).getBreakCurve();
		for (int i = 0; i < RUNS; i++)
		{
			val2 = val3;
		}
		System.out.println(val2);
		long end2 = System.nanoTime();
		
		System.out.printf("runs: %d\n%d\n%d\n", RUNS, end1 - start1, end2 - start2);
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
