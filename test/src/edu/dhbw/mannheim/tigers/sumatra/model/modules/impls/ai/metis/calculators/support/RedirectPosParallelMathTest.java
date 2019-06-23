/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 1, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.support;

import org.junit.BeforeClass;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.WorldFrameFactory;
import edu.dhbw.mannheim.tigers.sumatra.util.SumatraSetupHelper;


/**
 * Test {@link RedirectPosGPUCalc}
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RedirectPosParallelMathTest
{
	
	/**
	 */
	@BeforeClass
	public static void init()
	{
		SumatraSetupHelper.setupSumatra();
	}
	
	
	/**
	 */
	// @Test
	public void testDefault()
	{
		RedirectPosGPUCalc rppm = new RedirectPosGPUCalc();
		WorldFrameFactory factory = new WorldFrameFactory();
		rppm.calc(factory.createWorldFrame(0));
		rppm.deinit();
	}
	
}
