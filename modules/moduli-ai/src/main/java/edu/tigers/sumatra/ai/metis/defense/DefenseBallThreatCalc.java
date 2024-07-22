/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseBallThreatSourceType;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.line.IHalfLine;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.KickedBall;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * Calculate the ball threat.
 * The threat line starts either at the opponent's ball receiver or at the ball position
 * It ends in the center of the goal
 */
@RequiredArgsConstructor
public class DefenseBallThreatCalc extends ADefenseThreatCalc
{
	@Configurable(comment = "Lookahead for ball position", defValue = "0.1")
	private static double ballLookahead = 0.1;

	@Configurable(comment = "Use ball direction instead of position if faster than this", defValue = "1.5")
	private static double checkBallDirectionVelThreshold = 1.5;

	@Configurable(comment = "Left/Right extension of goal line to use for shot-at-goal intersection", defValue = "200")
	private static double goalMargin = 200;

	@Configurable(comment = "Defend the closest opponent if ball is hidden", defValue = "true")
	private static boolean defendOpponentIfBallIsHidden = true;
	@Configurable(comment = "[m/s] Expected bot movement speed with the hidden ball to increase the search distance", defValue = "2.0")
	private static double hiddenBallAndBotMovementSpeed = 2.0;
	@Configurable(comment = "[m] Minimum search distance for hidden balls", defValue = "50.0")
	private static double hiddenBallMinimumSearchDistance = 50.0;

	private final Supplier<KickedBall> detectedGoalKickOpponents;
	private final Supplier<ITrackedBot> opponentPassReceiver;
	private final Supplier<BotDistance> opponentClosestToBall;

	@Getter
	private DefenseBallThreat defenseBallThreat;


	@Override
	public void doCalc()
	{
		var threatSource = threatSource();
		var threatTarget = threatTarget(threatSource.threatPos);
		ILineSegment threatLine = Lines.segmentFromPoints(threatSource.threatPos, threatTarget);
		var protectionLine = centerBackProtectionLine(threatLine, minDistanceToThreat());

		getShapes(EAiShapesLayer.DEFENSE_BALL_THREAT).add(new DrawableLine(threatLine, Color.PINK).setStrokeWidth(10));

		defenseBallThreat = new DefenseBallThreat(
				threatSource.velocity,
				threatLine,
				protectionLine.orElse(null),
				opponentPassReceiver.get(),
				threatSource.type);
		drawThreat(defenseBallThreat);
	}


	private IVector2 threatTarget(final IVector2 threatSource)
	{
		final IVector2 threatTarget = Geometry.getGoalOur().bisection(threatSource);
		if (getBall().getVel().getLength2() > checkBallDirectionVelThreshold)
		{
			IHalfLine travelLine = getBall().getTrajectory().getTravelLine();
			return Geometry.getGoalOur().withMargin(0, goalMargin).getLineSegment()
					.intersect(travelLine).asOptional().orElse(threatTarget);
		}
		return threatTarget;
	}


	private ThreatSource threatSource()
	{
		if (getAiFrame().getGameState().isFreeKick() && getBall().getVel().getLength() < 0.3)
		{
			return new ThreatSource(EDefenseBallThreatSourceType.FREE_KICK, getBall().getPos(), Vector2.zero());
		}
		if (detectedGoalKickOpponents.get() != null)
		{
			// For DirectShots, always protectBall directly
			return threatFromBall(EDefenseBallThreatSourceType.GOAL_SHOT);
		}
		if (opponentPassReceiver.get() != null)
		{
			// Prefer protecting opponentPassReceiver than protecting the ball
			return threatFromReceive(opponentPassReceiver.get());
		}
		return findOpponentHandlingTheBall()
				.map(this::threatFromBot)
				.orElseGet(() -> threatFromBall(EDefenseBallThreatSourceType.BALL));
	}


	private ThreatSource threatFromReceive(ITrackedBot opponentPassReceiver)
	{
		var posLookAhead = opponentPassReceiver.getPosByTime(ballLookahead);
		var pos = opponentPassReceiver.getPos();
		var target = Geometry.getGoalOur().bisection(posLookAhead);
		var shootLine = Lines.halfLineFromPoints(posLookAhead, target);
		var ballLine = Lines.halfLineFromDirection(getBall().getPos(), getBall().getVel());

		var receivePos = shootLine.intersect(ballLine).asOptional()
				.orElseGet(() -> ballLine.closestPointOnPath(opponentPassReceiver.getBotKickerPosByTime(ballLookahead)));
		var offset = receivePos.subtractNew(posLookAhead);
		var receiverSource = Geometry.getField().nearestPointInside(receivePos, pos.addNew(offset));

		var projectedVel = ballLine.directionVector().projectOntoThis(opponentPassReceiver.getVel());

		return new ThreatSource(EDefenseBallThreatSourceType.PASS_RECEIVE, receiverSource, projectedVel);
	}


	private ThreatSource threatFromBot(ITrackedBot opponent)
	{
		var posLookAhead = opponent.getPosByTime(ballLookahead);
		var pos = opponent.getPos();
		var target = Geometry.getGoalOur().bisection(posLookAhead);
		var offset = Vector2.fromPoints(posLookAhead, target).scaleTo(Geometry.getOpponentCenter2DribblerDist());
		var receiverSource = Geometry.getField().nearestPointInside(posLookAhead.addNew(offset), pos.addNew(offset));

		return new ThreatSource(EDefenseBallThreatSourceType.BOT_CLOSE_TO_BALL, receiverSource, opponent.getVel());
	}


	private ThreatSource threatFromBall(EDefenseBallThreatSourceType sourceType)
	{

		IVector2 predictedBallPos = getBall().getTrajectory().getPosByTime(ballLookahead).getXYVector();
		IVector2 threatSource = Geometry.getField().nearestPointInside(predictedBallPos, getBall().getPos());
		return new ThreatSource(sourceType, threatSource, getBall().getVel());
	}


	private Optional<ITrackedBot> findOpponentHandlingTheBall()
	{
		if (!defendOpponentIfBallIsHidden)
		{
			return Optional.empty();
		}
		var invisibleTime = getBall().invisibleFor();
		var opponent = getWFrame().getBot(opponentClosestToBall.get().getBotId());
		if (opponent == null || !opponent.getBallContact().hadContactFromVision(invisibleTime + 0.3))
		{
			// No ball contact registered before ball went invisible -> Do not defend it
			return Optional.empty();
		}
		var searchDistance =
				Geometry.getBallRadius() + Geometry.getBotRadius() + hiddenBallMinimumSearchDistance
						+ invisibleTime * 1_000 * hiddenBallAndBotMovementSpeed;
		if (getBall().getPos().distanceToSqr(opponent.getPos()) < searchDistance * searchDistance)
		{
			return Optional.of(opponent);
		}
		return Optional.empty();
	}


	private double minDistanceToThreat()
	{
		if (getAiFrame().getGameState().isDistanceToBallRequired())
		{
			return RuleConstraints.getStopRadius() + Geometry.getBotRadius() + Geometry.getBallRadius();
		}
		return Geometry.getBotRadius() * 2;
	}


	private record ThreatSource(EDefenseBallThreatSourceType type, IVector2 threatPos, IVector2 velocity)
	{
	}
}
