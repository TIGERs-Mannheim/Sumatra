/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.11.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.modules.impls.ai.metis.fieldanalyser;


import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.Sumatra;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.fieldraster.FieldRasterGenerator.EGeneratorTyp;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.fieldanalysis.FieldAnalyser;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.config.ConfigManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.WorldFrameFactory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AConfigManager;
import edu.dhbw.mannheim.tigers.sumatra.util.StopWatch;


/**
 * Simple Benchmark for {@link FieldAnalyser}.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class Benchmak
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final WorldFrameFactory	wFrameFactory;
	private final FieldAnalyser		fieldAnalyser;
	
	private final StopWatch				watch;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public Benchmak()
	{
		// Load configuration
		Sumatra.touch();
		SumatraModel.getInstance().setUserProperty(AAgent.KEY_AI_CONFIG, "ai_default.xml");
		SumatraModel.getInstance().setUserProperty(AAgent.KEY_GEOMETRY_CONFIG, "RoboCup_2012.xml");
		AConfigManager.registerConfigClient(AIConfig.getInstance().getAiClient());
		AConfigManager.registerConfigClient(AIConfig.getInstance().getGeomClient());
		new ConfigManager(); // Loads all registered configs (accessed via singleton)
		
		wFrameFactory = new WorldFrameFactory();
		wFrameFactory.createFrames(10);
		fieldAnalyser = new FieldAnalyser(EGeneratorTyp.MAIN);
		watch = new StopWatch();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	@Test
	public void test()
	{
		AIInfoFrame aiFrame = null;
		
		for (int i = 0; i < 100000; i++)
		{
			for (final WorldFrame wFrame : wFrameFactory.getwFrames())
			{
				aiFrame = new AIInfoFrame(wFrame, null, null);
				
				final long start = System.nanoTime();
				fieldAnalyser.doCalc(aiFrame, aiFrame);
				watch.stop(start);
			}
		}
		System.out.println("Time: " + watch.mean() + "[ns]");
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
