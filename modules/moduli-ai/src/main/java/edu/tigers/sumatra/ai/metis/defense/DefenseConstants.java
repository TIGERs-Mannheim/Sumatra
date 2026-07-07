package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.ai.metis.defense.data.IDefenseThreat;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.rectangle.IRectangle;


/**
 * Global constants for the defense. Try to keep this class small and only put constants here, if they
 * really must used at different places.
 */
public final class DefenseConstants
{
	@Configurable(comment = "Minimum lookahead used for bots", defValue = "0.1", unit = EConfigUnit.TIME_S)
	private static double minLookaheadBotThreats = 0.1;

	@Configurable(comment = "Maximum lookahead used for bots", defValue = "0.2", unit = EConfigUnit.TIME_S)
	private static double maxLookaheadBotThreats = 0.2;

	@Configurable(comment = "Velocity which uses the maximum lookahead for the opposing teams bots", defValue = "4.0", unit = EConfigUnit.VELOCITY_M)
	private static double maxLookaheadBotThreatsVelocity = 4.0;

	@Configurable(comment = "DistToPenAreaCorner + this = max distance from goal center to go out when defending a bot", defValue = "500.0", unit = EConfigUnit.DISTANCE_MM)
	private static double maxGoOutDistanceBot = 500;
	@Configurable(comment = "DistToPenAreaCorner + this = max distance from goal center to go out when defending the ball", defValue = "1000.0", unit = EConfigUnit.DISTANCE_MM)
	private static double maxGoOutDistanceBall = 1000;

	@Configurable(comment = "Min distance from penArea to go out", defValue = "300.0", unit = EConfigUnit.DISTANCE_MM)
	private static double minGoOutDistance = 300;

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


	public static double getMaxGoOutDistance(IDefenseThreat threat)
	{
		return threat.getObjectId().isBall() ?
				getMaxGoOutDistanceBall() :
				getMaxGoOutDistanceBot();
	}


	public static double getMaxGoOutDistanceBot()
	{
		var distToFarthestPenArea = Geometry.getGoalOur().getCenter()
				.distanceTo(Geometry.getPenaltyAreaOur().getRectangle().getCorner(
						IRectangle.ECorner.TOP_RIGHT));
		return distToFarthestPenArea + maxGoOutDistanceBot;
	}


	public static double getMaxGoOutDistanceBall()
	{
		var distToFarthestPenArea = Geometry.getGoalOur().getCenter()
				.distanceTo(Geometry.getPenaltyAreaOur().getRectangle().getCorner(
						IRectangle.ECorner.TOP_RIGHT));
		return distToFarthestPenArea + maxGoOutDistanceBall;
	}


	public static double getMinGoOutDistance()
	{
		return minGoOutDistance;
	}
}
