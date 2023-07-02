/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;

import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Bot related calculations.
 *
 * @author nicolai.ommer
 */
public final class BotMath
{
	@SuppressWarnings("unused")
	private BotMath()
	{
	}
	
	
	/**
	 * Convert a bot-local vector to the equivalent global one.
	 *
	 * @param local Bot-local vector
	 * @param wpAngle Orientation of the bot
	 * @return Properly turned global vector
	 * @author AndreR
	 */
	public static Vector2 convertLocalBotVector2Global(final IVector2 local, final double wpAngle)
	{
		return local.turnNew(-AngleMath.PI_HALF + wpAngle);
	}
	
	
	/**
	 * Convert a global vector to a bot-local one
	 *
	 * @param global Global vector
	 * @param wpAngle Orientation of the bot
	 * @return Properly turned local vector
	 * @author AndreR
	 */
	public static Vector2 convertGlobalBotVector2Local(final IVector2 global, final double wpAngle)
	{
		return global.turnNew(AngleMath.PI_HALF - wpAngle);
	}
	
	
	/**
	 * Get front dribbler line of a robot.
	 * 
	 * @param pos Position [mm] and orientation of robot [rad]
	 * @param radius Radius [mm] or robot
	 * @param center2DribblerDist Distance from center to dribbler.
	 * @return Generated line
	 */
	public static ILineSegment getDribblerFrontLine(final IVector3 pos, final double radius,
			final double center2DribblerDist)
	{
		double theta = SumatraMath.acos((center2DribblerDist) / (radius));
		IVector2 leftBotEdge = pos.getXYVector()
				.addNew(Vector2.fromAngle(pos.z() - theta).scaleTo(radius));
		IVector2 rightBotEdge = pos.getXYVector()
				.addNew(Vector2.fromAngle(pos.z() + theta).scaleTo(radius));
		
		return Lines.segmentFromPoints(leftBotEdge, rightBotEdge);
	}
}
