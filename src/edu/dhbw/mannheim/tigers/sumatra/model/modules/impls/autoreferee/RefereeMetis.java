/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 17, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ECalculator;
import edu.dhbw.mannheim.tigers.sumatra.util.InstanceableClass.NotCreateableException;


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
				ACalculator inst = (ACalculator) eCalc.getImpl().newDefaultInstance();
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
		TacticalField tacticalField = new TacticalField(baseAiFrame.getWorldFrame());
		for (ACalculator calc : calculators)
		{
			calc.calculate(tacticalField, baseAiFrame);
		}
		return new MetisAiFrame(baseAiFrame, tacticalField);
	}
}
