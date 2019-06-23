/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.AICom;
import edu.tigers.sumatra.ai.metis.IAiInfoFromPrevFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;


/**
 * Update the {@link IAiInfoFromPrevFrame} in {@link TacticalField}
 */
public class AiInfoCommunicationCalc extends ACalculator
{
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		newTacticalField.setAiInfoFromPrevFrame(
				(AICom) baseAiFrame.getPrevFrame().getTacticalField().getAiInfoForNextFrame());
	}
}
