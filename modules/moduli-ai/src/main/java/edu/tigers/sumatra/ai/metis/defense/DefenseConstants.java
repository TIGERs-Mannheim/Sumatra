/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.rectangle.IRectangle;


/**
 * Global constants for the defense. Try to keep this class small and only put constants here, if they
 * really must used at different places.
 */
public final class DefenseConstants
{
	@Configurable(comment = "Minimum lookahead used for bots [s]", defValue = "0.1")
	private static double minLookaheadBotThreats = 0.1;

	@Configurable(comment = "Maximum lookahead used for bots [s]", defValue = "0.2")
	private static double maxLookaheadBotThreats = 0.2;

	@Configurable(comment = "Velocity [m/s] which uses the maximum lookahead for the opposing teams bots", defValue = "4.0")
	private static double maxLookaheadBotThreatsVelocity = 4.0;

	@Configurable(comment = "DistToPenAreaCorner + this = max distance from goal center to go out", defValue = "500.0")
	private static double maxGoOutDistance = 500;

	@Configurable(comment = "Min distance from penArea to go out", defValue = "400.0")
	private static double minGoOutDistance = 400;

	static
	{
		ConfigRegistration.registerClass("metis", DefenseConstants.class);
	}
	
	
	private DefenseConstants()
	{
	}
	
	
	/**
	 * @param velocity velocity of the tracked object
	 * @return lookahead in dependency on the velocity
	 */
	public static double getLookaheadBotThreats(final double velocity)
	{
		final double factor = SumatraMath.relative(velocity, 0, maxLookaheadBotThreatsVelocity);
		final double linearPart = maxLookaheadBotThreats - minLookaheadBotThreats;
		final double constantPart = minLookaheadBotThreats;

		return (factor * linearPart) + constantPart;
	}
	
	
	public static double getMaxGoOutDistance()
	{
		var distToFarthestPenArea = Geometry.getGoalOur().getCenter()
				.distanceTo(Geometry.getPenaltyAreaOur().getRectangle().getCorner(
						IRectangle.ECorner.TOP_RIGHT));
		return distToFarthestPenArea + maxGoOutDistance;
	}
	
	
	public static double getMinGoOutDistance()
	{
		return minGoOutDistance;
	}
}
