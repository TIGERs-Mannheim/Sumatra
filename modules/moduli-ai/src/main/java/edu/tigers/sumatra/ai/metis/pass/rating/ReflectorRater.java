package edu.tigers.sumatra.ai.metis.pass.rating;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRangeRater;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.Collection;
import java.util.Comparator;

import static edu.tigers.sumatra.math.SumatraMath.relative;


/**
 * A nice rater that rates a pass by the ability to score
 * a goal from the pass target, respecting the redirect angle.
 */
public class ReflectorRater extends APassRater
{
	@Configurable(defValue = "0.9", unit = EConfigUnit.ANGLE_RAD)
	private static double minCriticalAngle = 0.9;

	@Configurable(defValue = "1.2", unit = EConfigUnit.ANGLE_RAD)
	private static double maxCriticalAngle = 1.2;

	@Configurable(defValue = "0.5", unit = EConfigUnit.PERCENTAGE)
	private static double bestImprovement = 0.5;

	@Configurable(defValue = "0.5", unit = EConfigUnit.FACTOR)
	private static double timeToKickFactor = 0.5;

	static
	{
		ConfigRegistration.registerClass("metis", ReflectorRater.class);
	}

	private final Collection<ITrackedBot> obstacles;
	private final BotID opponentKeeperId;


	public ReflectorRater(Collection<ITrackedBot> obstacles, BotID opponentKeeperId)
	{
		this.obstacles = obstacles;
		this.opponentKeeperId = opponentKeeperId;
	}


	@Override
	public double rate(Pass pass)
	{
		AngleRangeRater rater = AngleRangeRater.forGoal(Geometry.getGoalTheir());
		rater.setKeeperID(opponentKeeperId);
		rater.setObstacles(this.obstacles.stream()
				.filter(bot -> !bot.getBotId().equals(pass.getShooter()) && !bot.getBotId().equals(pass.getReceiver()))
				.toList());
		rater.setTimeToKick(pass.getOpponentPreparationTime() + pass.getDuration() * timeToKickFactor);

		var sourceScore = rater.rate(pass.getKick().getSource())
				.map(IRatedTarget::getScore).orElse(0.0);

		return rater.rateMultiple(pass.getKick().getTarget())
				.stream()
				.map(e -> rateTarget(sourceScore, e.getScore(), pass, e))
				.max(Comparator.comparingDouble(e -> e))
				.orElse(0.0);
	}


	private double rateTarget(double sourceScore, double targetScore, Pass pass, IRatedTarget target)
	{
		var improvement = targetScore - sourceScore;
		double improvementScore = Math.min(relative(improvement, 0, bestImprovement), targetScore);

		var angle = OffensiveMath.getRedirectAngle(
				pass.getKick().getSource(),
				pass.getKick().getTarget(),
				target.getTarget()
		);
		double reflectScore = 1 - relative(angle, minCriticalAngle, maxCriticalAngle);
		return reflectScore * improvementScore;
	}
}
