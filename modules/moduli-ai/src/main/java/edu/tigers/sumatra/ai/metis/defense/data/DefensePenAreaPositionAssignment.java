/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense.data;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;

import java.util.List;


public record DefensePenAreaPositionAssignment(
		BotID botID,
		IVector2 movementDestination,
		List<IDefenseThreat> defendedThreats
)
{
}