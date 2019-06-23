/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 9, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim;

import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim.scenario.AUnitTestSimScenario;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim.scenario.RefereeDirectKickSimScenario;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim.scenario.RefereeKickoffSimScenario;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim.scenario.RefereeStopSimScenario;
import edu.dhbw.mannheim.tigers.sumatra.util.SumatraSetupHelper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SimulationTest
{
	private static final int	NUM_RUNS	= 1;
	
	static
	{
		SumatraSetupHelper.setupSumatra();
		SumatraSetupHelper.changeLogLevel(Level.INFO);
	}
	
	
	private void evaluateScenario(final AUnitTestSimScenario scenario)
	{
		Simulation.runSimulationBlocking(scenario);
		if (!scenario.getErrorMessages().isEmpty())
		{
			StringBuilder sb = new StringBuilder();
			for (String err : scenario.getErrorMessages())
			{
				sb.append(System.lineSeparator());
				sb.append(err);
			}
			
			Assert.fail("Simulation reported errors: " + sb.toString());
		}
	}
	
	
	/**
	 */
	@Test
	@Ignore
	public void testRefereeStop()
	{
		for (int i = 0; i < NUM_RUNS; i++)
		{
			AUnitTestSimScenario scenario = new RefereeStopSimScenario();
			evaluateScenario(scenario);
		}
	}
	
	
	/**
	 */
	@Test
	@Ignore
	public void testRefereeDirectKick()
	{
		for (int i = 0; i < NUM_RUNS; i++)
		{
			AUnitTestSimScenario scenario = new RefereeDirectKickSimScenario();
			evaluateScenario(scenario);
		}
	}
	
	
	/**
	 */
	@Test
	@Ignore
	public void testRefereeKickoff()
	{
		for (int i = 0; i < NUM_RUNS; i++)
		{
			AUnitTestSimScenario scenario = new RefereeKickoffSimScenario();
			evaluateScenario(scenario);
		}
	}
}
