/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.AIInfoFrame;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;


/**
 * Crucial offenders may not be taken by defense. But there is no guaranty, that a crucial offender is actually
 * becoming an offender!
 *
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class CrucialOffenderCalc extends ACalculator
{
	protected static final Logger log = Logger
			.getLogger(CrucialOffenderCalc.class.getName());
	
	
	/**
	 * Calculates the crucial offenders.
	 */
	public CrucialOffenderCalc()
	{
		// nothing here
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		AIInfoFrame prevFrame = baseAiFrame.getPrevFrame();
		Set<BotID> crucialOffender = new HashSet<>(prevFrame.getTacticalField().getDesiredBotMap()
				.getOrDefault(EPlay.OFFENSIVE, Collections.emptySet()));
		newTacticalField.setCrucialOffender(crucialOffender);
	}
}
