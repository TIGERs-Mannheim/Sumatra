/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import edu.tigers.sumatra.math.vector.IVector2;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


/**
 * Common methods for the Offensive
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OffensiveMath
{
	/**
	 * Get the redirect angle of a pass
	 *
	 * @param passSenderPos      sender position, which most of the time is the ball position
	 * @param passReceiverPos    receiving robot position
	 * @param passReceiverTarget redirecting target of the receiving robot.
	 * @return the absolute redirect angle [rad]
	 */
	public static double getRedirectAngle(
			final IVector2 passSenderPos,
			final IVector2 passReceiverPos,
			final IVector2 passReceiverTarget)
	{
		IVector2 botToBall = passSenderPos.subtractNew(passReceiverPos);
		IVector2 botToTarget = passReceiverTarget.subtractNew(passReceiverPos);
		return botToBall.angleToAbs(botToTarget).orElse(Math.PI);
	}
}
