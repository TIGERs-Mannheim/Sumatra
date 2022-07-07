/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.BallStabilizer;
import edu.tigers.sumatra.skillsystem.skills.util.PositionValidator;
import edu.tigers.sumatra.wp.data.BallKickFitState;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.awt.Color;
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

	private final Hysteresis ballSpeedHysteresis = new Hysteresis(0.3, 0.6).initiallyInUpperState();
	private final PositionValidator positionValidator = new PositionValidator();
	private final BallStabilizer ballStabilizer = new BallStabilizer();

	@Setter
	private ETeam consideredPenAreas = ETeam.BOTH;
	@Setter
	private double marginToTheirPenArea = 0;
	@Setter
	protected IVector2 ballReceivingPosition;

	@Getter
	private IVector2 currentBallReceivingPosition;
	@Setter(AccessLevel.PROTECTED)
	private double desiredTargetAngle;


	protected final boolean receivingPositionIsReachableByBall(IVector2 pos)
	{
		return getBall().getTrajectory().closestPointTo(pos).distanceTo(pos) < maxDistanceToReceivingPosition;
	}


	protected final boolean ballIsMoving()
	{
		return ballSpeedHysteresis.isUpper()
				&& (getTBot().getBallContact().getContactDuration() < 0.1
				|| !getTBot().getBallContact().hadContact(0.2));
	}


	private boolean ballNearKicker(double dist)
	{
		return ballStabilizer.getBallPos().distanceTo(getTBot().getBotKickerPos()) < dist;
	}


	protected boolean ballIsMovingTowardsMe()
	{
		return ballIsMoving()
				&& getPos().subtractNew(ballStabilizer.getBallPos())
				.angleToAbs(getBall().getVel())
				.map(a -> a < AngleMath.PI_HALF).orElse(false);
	}


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		getMoveCon().setBallObstacle(false);
		getMoveCon().setPenaltyAreaTheirObstacle(true);
		getMoveCon().setGameStateObstacle(false);

		// init target orientation
		updateTargetAngle(getBall().getPos().subtractNew(getPos()).getAngle());
		currentBallReceivingPosition = getTBot().getBotKickerPos();
	}


	@Override
	public void doUpdate()
	{
		ballSpeedHysteresis.update(getBall().getVel().getLength2());
		ballStabilizer.update(getBall(), getTBot());
		positionValidator.update(getWorldFrame(), getMoveCon());
		positionValidator.getMarginToPenArea().put(ETeam.OPPONENTS, marginToTheirPenArea);

		setCurrentBallReceivingPosition(determineBallReceivingPosition());

		getMoveCon().setBotsObstacle(!ballNearKicker(100));

		updateTargetAngle(calcTargetAngle(currentBallReceivingPosition));
		updateDestination(calcDest());
		getMoveConstraints().setPrimaryDirection(calcPrimaryDirection());

		drawShapes();

		super.doUpdate();
	}


	private IVector2 determineBallReceivingPosition()
	{
		var center2Dribbler = getTBot().getCenter2DribblerDist() + Geometry.getBallRadius();
		var kickPos = BotShape.getKickerCenterPos(
				Pose.from(getPos(), getTargetAngle()), // using target angle from last frame intentionally here
				center2Dribbler
		);
		var idealBallReceivingPosition = Optional.ofNullable(ballReceivingPosition).orElse(kickPos);
		var closestPointToIdealPos = getBall().getTrajectory().closestPointTo(idealBallReceivingPosition);
		var kickAge = getWorldFrame().getKickFitState()
				.map(BallKickFitState::getKickTimestamp)
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
		dest = positionValidator.movePosOutOfPenAreaWrtBall(dest, Geometry.getBallRadius(), consideredPenAreas);
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
		return positionValidator.movePosOutOfPenAreaWrtBall(dest, marginBetweenDestAndPenArea, consideredPenAreas);
	}


	private IVector2 calcPrimaryDirection()
	{
		if (getBall().getVel().getLength2() > 0.2)
		{
			return getBall().getVel();
		}
		return Vector2f.ZERO_VECTOR;
	}


	private double calcTargetAngle(final IVector2 kickerPos)
	{
		IVector2 ballPos = ballStabilizer.getBallPos();
		double distBallBot = ballPos.distanceTo(kickerPos);
		if (distBallBot < distThresholdToFixOrientation)
		{
			// just keep last position -> this is probably most safe to not push ball away again
			return getTargetAngle();
		}

		return desiredTargetAngle;
	}


	private void drawShapes()
	{
		getShapes().get(ESkillShapesLayer.BALL_ARRIVAL_SKILL)
				.add(new DrawablePoint(ballStabilizer.getBallPos(), Color.green));
		Optional.ofNullable(ballReceivingPosition).ifPresent(pos ->
				getShapes().get(ESkillShapesLayer.BALL_ARRIVAL_SKILL)
						.add(new DrawableCircle(Circle.createCircle(pos, 30), Color.magenta)));
		getShapes().get(ESkillShapesLayer.BALL_ARRIVAL_SKILL)
				.add(new DrawableAnnotation(getPos(), ballIsMoving() ? "ballMoving" : "ballNotMoving",
						Vector2.fromX(100)));

		getShapes().get(ESkillShapesLayer.BALL_ARRIVAL_SKILL)
				.add(new DrawableLine(Line.fromDirection(getPos(), Vector2.fromAngle(desiredTargetAngle).scaleTo(200)))
						.setColor(Color.magenta));
	}
}
