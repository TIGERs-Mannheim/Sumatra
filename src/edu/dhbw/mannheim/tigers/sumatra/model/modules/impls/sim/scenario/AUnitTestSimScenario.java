/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 9, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim.scenario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee.RefereeCaseMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim.stopcrit.ASimStopCriterion;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim.stopcrit.SimStopTimeCrit;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * All scenarios that should act as a unit test should implement this class and
 * put all errors in the list, so that the test can be evaluated
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class AUnitTestSimScenario extends ASimulationScenario
{
	private static final Logger	log					= Logger.getLogger(AUnitTestSimScenario.class.getName());
	private final List<String>		errorMessages		= new ArrayList<>();
	
	@Configurable
	/** [ms] set to 10min */
	private static long				timeout				= 10 * 60 * 1000;
	
	@Configurable(comment = "[s]")
	private static float				preparationTime	= 5;
	
	
	/**
	 * 
	 */
	public AUnitTestSimScenario()
	{
		setEnableBlue(true);
		setEnableYellow(true);
	}
	
	
	protected void addErrorMessage(final String msg)
	{
		log.warn(msg);
		errorMessages.add(msg);
	}
	
	
	/**
	 * @return the errorMessages
	 */
	public final List<String> getErrorMessages()
	{
		return Collections.unmodifiableList(errorMessages);
	}
	
	
	@Override
	protected void setupStopCriteria(final List<ASimStopCriterion> criteria)
	{
		criteria.add(new SimStopTimeoutCrit(timeout));
	}
	
	
	@Override
	public void onUpdate(final AIInfoFrame aiFrame, final List<RefereeCaseMsg> caseMsgs)
	{
		List<String> errors = getRuleViolationsForAI(aiFrame, caseMsgs);
		for (String msg : errors)
		{
			addErrorMessage(msg);
		}
		if (!errors.isEmpty())
		{
			if (isStopAfterCompletition())
			{
				stopSimulation();
			} else
			{
				pause();
			}
		}
	}
	
	
	private class SimStopTimeoutCrit extends SimStopTimeCrit
	{
		/**
		 * @param timeout
		 */
		public SimStopTimeoutCrit(final long timeout)
		{
			super(timeout);
		}
		
		
		@Override
		protected boolean checkStopSimulation()
		{
			if (super.checkStopSimulation())
			{
				addErrorMessage("Test case timed out after " + SimStopTimeoutCrit.this.getTimeout()
						+ ". Check your stop criteria!");
				return true;
			}
			return false;
		}
	}
	
	
	/**
	 * @return the timeout
	 */
	protected final long getScenarioTimeout()
	{
		return timeout;
	}
	
	
	/**
	 * @return the preparationTime
	 */
	protected final float getPreparationTime()
	{
		return preparationTime;
	}
}
