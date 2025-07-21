/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.dribble;

import edu.tigers.sumatra.math.vector.IVector2;
import lombok.AllArgsConstructor;
import lombok.Value;


@Value
@AllArgsConstructor
public class DribbleToPos
{
	IVector2 protectFromPos;
	IVector2 dribbleToDestination;
	EDribblingCondition dribblingCondition;
	DribbleKickData dribbleKickData;
}
