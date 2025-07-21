/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.move;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;


public interface DestinationAdjuster
{
	IVector2 adjustDestination(AthenaAiFrame aiFrame, IVector2 destination, BotID botID);
}
