/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.dribble;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.penarea.FinisherMoveShape;
import edu.tigers.sumatra.math.penarea.EDribbleKickMoveDirection;
import lombok.AllArgsConstructor;
import lombok.Value;


@Persistent
@Value
@AllArgsConstructor
public class DribbleKickData
{
	FinisherMoveShape shape;
	EDribbleKickMoveDirection direction;
	boolean isViolationUnavoidable;
	boolean isViolationImminent;

	private DribbleKickData()
	{
		// empty constructor for Berkeley
		shape = null;
		direction = null;
		isViolationUnavoidable = false;
		isViolationImminent = false;
	}
}
