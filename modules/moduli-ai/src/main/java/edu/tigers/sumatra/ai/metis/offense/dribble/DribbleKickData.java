/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.dribble;

import edu.tigers.sumatra.math.penarea.EDribbleKickMoveDirection;
import edu.tigers.sumatra.ai.metis.offense.dribble.finisher.FinisherMoveShape;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.AllArgsConstructor;
import lombok.Value;


@Value
@AllArgsConstructor
public class DribbleKickData
{
	FinisherMoveShape shape;
	EDribbleKickMoveDirection direction;
	boolean isViolationUnavoidable;
	boolean isViolationImminent;
	IVector2 fakePoint;
}
