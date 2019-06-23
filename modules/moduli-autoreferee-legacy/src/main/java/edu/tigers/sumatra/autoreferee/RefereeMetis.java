/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 17, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.autoreferee;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.github.g3force.instanceables.InstanceableClass.NotCreateableException;

import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.ECalculator;


/**
 * A Metis version for the referee with only the needed calculators
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RefereeMetis
{
	private static final Logger		log			= Logger.getLogger(RefereeMetis.class.getName());
	private final List<ACalculator>	calculators	= new ArrayList<>();
																
																
	/**
	 * 
	 */
	public RefereeMetis()
	{
		List<ECalculator> calcs = new ArrayList<ECalculator>(5);
		calcs.add(ECalculator.GAME_STATE);
		calcs.add(ECalculator.BALL_LEFT_FIELD);
		calcs.add(ECalculator.BALL_POSSESSION);
		calcs.add(ECalculator.BOT_LAST_TOUCHED_BALL);
		calcs.add(ECalculator.POSSIBLE_GOAL);
		
		for (ECalculator eCalc : calcs)
		{
			try
			{
				ACalculator inst = (ACalculator) eCalc.getInstanceableClass().newDefaultInstance();
				inst.setType(eCalc);
				inst.setActive(eCalc.isInitiallyActive());
				calculators.add(inst);
			} catch (NotCreateableException e)
			{
				log.error("Could not instantiate calculator: " + eCalc, e);
			}
		}
	}
	
	
	/**
	 * @param baseAiFrame
	 * @return
	 */
	public MetisAiFrame process(final BaseAiFrame baseAiFrame)
	{
		TacticalField tacticalField = new TacticalField();
		for (ACalculator calc : calculators)
		{
			calc.calculate(tacticalField, baseAiFrame);
		}
		return new MetisAiFrame(baseAiFrame, tacticalField);
	}
}
