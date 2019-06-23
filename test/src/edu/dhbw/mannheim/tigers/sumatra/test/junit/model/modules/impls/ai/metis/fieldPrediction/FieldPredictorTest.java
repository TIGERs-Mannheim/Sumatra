/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 18, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.modules.impls.ai.metis.fieldPrediction;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.Sumatra;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.fieldPrediction.FieldPredictionInformation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.fieldPrediction.FieldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.fieldPrediction.WorldFramePrediction;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.config.ConfigManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AConfigManager;


/**
 * tests if the field prediction is calculated correctly
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
public class FieldPredictorTest
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private WorldFramePrediction			wfp;
	
	private FieldPredictionInformation	fpi;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	@BeforeClass
	public static void init()
	{
		Sumatra.touch();
		SumatraModel.getInstance().setUserProperty(AAgent.KEY_AI_CONFIG, "ai_default.xml");
		SumatraModel.getInstance().setUserProperty(AAgent.KEY_GEOMETRY_CONFIG, "RoboCup_2012.xml");
		AConfigManager.registerConfigClient(AIConfig.getInstance().getAiClient());
		AConfigManager.registerConfigClient(AIConfig.getInstance().getGeomClient());
		new ConfigManager(); // Loads all registered configs (accessed via singleton)
	}
	
	
	/**
	 * before a test is started a WorldFramePrediction will be generated
	 */
	@Before
	public void generateFieldPredictionInformation()
	{
		// prepare
		Vector3 n = new Vector3(0, 0, 0);
		TrackedBall ball = new TrackedBall(n, n, n, 0f, true);
		
		IBotIDMap<TrackedBot> foes = new BotIDMap<TrackedBot>();
		IBotIDMap<TrackedTigerBot> tigers = new BotIDMap<TrackedTigerBot>();
		BotID tiger1BotID = new BotID(0);
		TrackedTigerBot tiger1 = new TrackedTigerBot(tiger1BotID, new Vector2(200, 200), new Vector2(0, 0.01), null, 0,
				0f, 0f, 0f, 0f, null);
		tigers.put(tiger1BotID, tiger1);
		
		BotID tiger2BotID = new BotID(1);
		TrackedTigerBot tiger2 = new TrackedTigerBot(tiger2BotID, new Vector2(0, 400), new Vector2(0.01, 0), null, 0, 0f,
				0f, 0f, 0f, null);
		tigers.put(tiger2BotID, tiger2);
		
		WorldFrame wf = new WorldFrame(foes, tigers, tigers, ball, 0, 0, null, 0);
		
		FieldPredictor fp = new FieldPredictor();
		
		// do
		wfp = fp.create(wf);
		
		fpi = wfp.getTiger(tiger1BotID);
	}
	
	
	/**
	 * tests if the estimation of the world frame predictor works on a part without crash
	 * 
	 */
	@Test
	public void testEstimationWithoutCrash()
	{
		if (!(new Vector2(200, 210)).equals(fpi.getPosAt(1), 0.01f))
		{
			fail(fpi.getPosAt(1) + " should equal 200,210");
		}
	}
	
	
	/**
	 * tests if the estimation of the world frame predictor works and finds a crash
	 */
	@Test
	public void testCrash()
	{
		// beware of the crash after 20 seconds
		if (!(new Vector2(200, 400)).equals(fpi.getPosAt(25), 0.01f))
		{
			fail(fpi.getPosAt(25) + " should equal 200,400");
		}
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
