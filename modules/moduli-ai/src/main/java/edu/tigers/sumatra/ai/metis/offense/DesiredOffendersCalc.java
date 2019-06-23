/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>,
 *         Mark Gegier <Mark.Geiger@dlr.de>
 */
public class DesiredOffendersCalc extends ACalculator
{
	
	private static Logger log = LogManager.getLogger(DesiredOffendersCalc.class);
	private boolean invalidNumberWarned = false;
	
	
	@Override
	public void doCalc(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		int numOffenders = tacticalField.getPlayNumbers().getOrDefault(EPlay.OFFENSIVE, 0);
		Set<BotID> desiredBots = tacticalField.getOffensiveStrategy().getDesiredBots().stream().limit(numOffenders)
				.collect(Collectors.toSet());
		if (desiredBots.size() > numOffenders && !invalidNumberWarned)
		{
			invalidNumberWarned = true;
			log.error("Invalid number of offensive bots (allowed: " + numOffenders + ") actual: "
					+ Arrays.toString(desiredBots.toArray()));
		}
		tacticalField.addDesiredBots(EPlay.OFFENSIVE, desiredBots);
	}
}
