package edu.tigers.sumatra.ai.metis.pass.rating;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.dribble.finisher.FinisherMoveShape;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRangeRater;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.ai.metis.targetrater.RatedTarget;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;


/**
 * A rater that rates how well a finisher could be scored from the pass target.
 */
public class FinisherRater extends APassRater
{
	@Configurable(defValue = "0.5", unit = EConfigUnit.PERCENTAGE)
	private static double bestImprovement = 0.5;

	@Configurable(defValue = "0.8", unit = EConfigUnit.FACTOR)
	private static double finisherPenaltyFactor = 0.8;

	@Configurable(defValue = "0.5", unit = EConfigUnit.FACTOR)
	private static double timeToKickFactor = 0.5;

	private final Collection<ITrackedBot> obstacles;
	private final BotID opponentKeeperId;

	static
	{
		ConfigRegistration.registerClass("metis", FinisherRater.class);
	}


	public FinisherRater(Collection<ITrackedBot> obstacles, BotID opponentKeeperId)
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

		FinisherMoveShape shape = FinisherMoveShape.generateFinisherMoveMovementShape(pass.getKick().getTarget());
		var posOnShape = shape.stepOnShape(shape.getStepPositionOnShape(pass.getKick().getTarget()));

		if (posOnShape.distanceTo(pass.getKick().getTarget()) > Geometry.getBotRadius())
		{
			// target is far away from potential finisher movement shape. Thus, it is rated as 0
			return 0;
		}

		var ratedSource = rater.rate(pass.getKick().getSource())
				.orElseGet(() -> RatedTarget.ratedPoint(Geometry.getGoalTheir().getCenter(), 0));

		ICircle dribblingCircle = Circle.createCircle(
				pass.getKick().getTarget(), OffensiveConstants.getMaxDribblingLength());
		var samples = shape.sampleAlongFinisherMoveShape(dribblingCircle);

		drawMany(() -> samples.stream().map(e -> new DrawableCircle(Circle.createCircle(e, 100)))
				.map(IDrawableShape.class::cast).toList());

		var bestTarget = shape.sampleAlongFinisherMoveShape(dribblingCircle)
				.stream()
				.map(rater::rate)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.max(Comparator.comparingDouble(IRatedTarget::getScore))
				.orElseGet(() -> RatedTarget.ratedPoint(Geometry.getGoalTheir().getCenter(), 0));

		var improvement = bestTarget.getScore() - ratedSource.getScore();
		return Math.min(SumatraMath.relative(improvement, 0, bestImprovement), bestTarget.getScore())
				* finisherPenaltyFactor;
	}
}
