/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.dribble;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.AllArgsConstructor;
import lombok.Value;


@Value
@Persistent(version = 2)
@AllArgsConstructor
public class DribbleToPos
{
	IVector2 protectFromPos;
	IVector2 dribbleToDestination;
	EDribblingCondition dribblingCondition;


	@SuppressWarnings("unused") // berkeley
	private DribbleToPos()
	{
		protectFromPos = null;
		dribbleToDestination = null;
		dribblingCondition = EDribblingCondition.DEFAULT;
	}
}
