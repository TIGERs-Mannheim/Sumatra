/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.PositionValidator;
import edu.tigers.sumatra.wp.data.KickedBall;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.awt.Color;
import java.util.List;
import java.util.Optional;


/**
 * Base class for skills waiting for an incoming ball.
 */
public abstract class ABallArrivalSkill extends AMoveToSkill
{
	@Configurable(defValue = "200.0", comment = "Distance bot pos to ball [mm] to fix the target orientation of the bot.")
	private static double distThresholdToFixOrientation = 200.0;

	@Configurable(defValue = "1000.0", comment = "If the receiving pos is further than this away from the rolling travel line, the ball can not reach the receiving pos")
	private static double maxDistanceToReceivingPosition = 1000.0;

	@Configurable(defValue = "110.0", comment = "Margin between penalty area and bot destination [mm] (should be larger than botRadius + Geometry#getPenaltyAreaMargin()")
	private static double marginBetweenDestAndPenArea = 110.0;

	@Configurable(defValue = "0.1", comment = "[s] Maximum expected time the vision might deviate from the real world")
	private static double maxExpectedVisionTimeDeviation = 0.1;

	@Configurable(defValue = "0.0", comment = "[s] offset to the ballArrivalTime to allow tuning the timing of overshooting trajectories")
	private static double ballArrivalTimeOffset = 0;

	private final PositionValidator positionValidator = new PositionValidator();
	@Setter
	protected IVector2 ballReceivingPosition;
	@Setter
	private Hysteresis ballSpeedHysteresis = new Hysteresis(0.1, 0.6);
	@Setter
	private double maxReceptionHeight = 0;
	@Setter
	private boolean useOvershoot = false;
	private IVector2 currentBallReceivingPosition;
	@Getter(AccessLevel.PROTECTED)
	@Setter(AccessLevel.PROTECTED)
	private double desiredTargetAngle;


	private boolean receivingPositionIsReachableByBall(IVector2 pos)
	{
		return getBall().getTrajectory().closestPointToBelow(pos, maxReceptionHeight).distanceTo(pos)
				< maxDistanceToReceivingPosition;
	}


	protected final boolean ballIsMoving()
	{
		return ballSpeedHysteresis.isUpper()
				&& (getTBot().getBallContact().getContactDuration() < 0.1
				|| !getTBot().getBallContact().hadContact(0.2));
	}


	private boolean ballNearKicker(double dist)
	{
		return getBall().getPos().distanceTo(getTBot().getBotKickerPos()) < dist;
	}


	protected boolean ballIsMovingTowardsMe()
	{
		return ballIsMoving() && getBall().getTrajectory().getTravelLine().isPointInFront(getPos());
	}


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		getMoveCon().setBallObstacle(false);

		// init target orientation
		updateTargetAngle(getBall().getPos().subtractNew(getPos()).getAngle());
		currentBallReceivingPosition = getTBot().getBotKickerPos();
	}


	@Override
	public void doUpdate()
	{
		ballSpeedHysteresis.update(getBall().getVel().getLength2());
		positionValidator.update(getWorldFrame(), getMoveCon());

		setCurrentBallReceivingPosition(determineBallReceivingPosition());

		updateTargetAngle(calcTargetAngle(currentBallReceivingPosition));
		updateDestination(calcDest());

		drawShapes();

		super.doUpdate();
	}


	private IVector2 projectIdealBallReceivingPosOnBallTrajectory(IVector2 previousInterceptionPoint)
	{
		var distance = getBall().getTrajectory().getDistByTime(maxExpectedVisionTimeDeviation);
		var ballLineProjectedBack = Lines.segmentFromPoints(getBall().getPos(),
				getBall().getVel().scaleToNew(-distance).add(getBall().getPos()));

		return previousInterceptionPoint.nearestTo(List.of(
				getBall().getTrajectory().closestPointToBelow(previousInterceptionPoint, maxReceptionHeight),
				ballLineProjectedBack.closestPointOnPath(previousInterceptionPoint)
		));
	}


	private IVector2 determineBallReceivingPosition()
	{
		var center2Dribbler = getTBot().getCenter2DribblerDist() + Geometry.getBallRadius();
		var kickPos = BotShape.getKickerCenterPos(
				Pose.from(getPos(), getTargetAngle()), // using target angle from last frame intentionally here
				center2Dribbler
		);
		var idealBallReceivingPosition = Optional.ofNullable(ballReceivingPosition).orElse(kickPos);
		var closestPointToIdealPos = projectIdealBallReceivingPosOnBallTrajectory(idealBallReceivingPosition);
		var kickAge = getWorldFrame().getKickedBall()
				.map(KickedBall::getKickTimestamp)
				.map(ts -> (getWorldFrame().getTimestamp() - ts) / 1e9)
				.orElse(Double.POSITIVE_INFINITY);

		if (ballIsMoving() &&
				receivingPositionIsReachableByBall(closestPointToIdealPos) &&
				ballIsMovingTowardsBot() &&
				kickAge > 0.1)
		{
			return closestPointToIdealPos;
		} else if (!ballNearKicker(1500))
		{
			return idealBallReceivingPosition;
		}
		return currentBallReceivingPosition;
	}


	private void setCurrentBallReceivingPosition(final IVector2 receivingPosition)
	{
		IVector2 dest = receivingPosition;
		dest = positionValidator.movePosInFrontOfOpponent(dest);
		dest = positionValidator.movePosOutOfPenAreaWrtBall(dest, Geometry.getBallRadius(),
				getMoveCon().getConsideredPenAreas());
		dest = positionValidator.movePosInsideFieldWrtBallPos(dest);
		currentBallReceivingPosition = dest;
	}


	private boolean ballIsMovingTowardsBot()
	{
		return getBall().getTrajectory().getTravelLine().isPointInFront(getPos());
	}


	private IVector2 calcDest()
	{
		IVector2 dest = BotShape.getCenterFromKickerPos(currentBallReceivingPosition, getTargetAngle(),
				getTBot().getCenter2DribblerDist() + Geometry.getBallRadius());

		// the bot may drive through the penArea, but it should not have a destination inside,
		// because touching the ball while being partially inside the penArea is a foul.
		var finalDest = positionValidator.movePosOutOfPenAreaWrtBall(dest, marginBetweenDestAndPenArea,
				getMoveCon().getConsideredPenAreas());

		if (!useOvershoot)
		{
			return finalDest;
		}

		var ballTime = getBall().getTrajectory().getTimeByPos(currentBallReceivingPosition);
		return TrajectoryGenerator.generateVirtualPositionToReachPointInTime(getTBot(), finalDest,
				ballTime + ballArrivalTimeOffset);
	}


	private double calcTargetAngle(final IVector2 kickerPos)
	{
		double distBallBot = getBall().getPos().distanceTo(kickerPos);
		if (distBallBot < distThresholdToFixOrientation)
		{
			// just keep last position -> this is probably most safe to not push ball away again
			return getTargetAngle();
		}

		return desiredTargetAngle;
	}


	private void drawShapes()
	{
		Optional.ofNullable(ballReceivingPosition).ifPresent(pos ->
				getShapes().get(ESkillShapesLayer.BALL_ARRIVAL_SKILL)
						.add(new DrawableCircle(Circle.createCircle(pos, 30), Color.magenta)));
		getShapes().get(ESkillShapesLayer.BALL_ARRIVAL_SKILL)
				.add(new DrawableAnnotation(getPos(), ballIsMoving() ? "ballMoving" : "ballNotMoving",
						Vector2.fromX(200)));

		getShapes().get(ESkillShapesLayer.BALL_ARRIVAL_SKILL)
				.add(new DrawableLine(Lines.segmentFromOffset(getPos(), Vector2.fromAngle(desiredTargetAngle).scaleTo(200)))
						.setColor(Color.magenta));
	}
}
