/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.defense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.intersections.ISingleIntersection;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.skillsystem.skills.redirect.RedirectConsultantFactory;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import lombok.Value;

import java.awt.Color;


/**
 * Abstract defense role
 */
public abstract class ADefenseRole extends ARole
{
	@Configurable(comment = "The chipped ball will leave the field with the n-th touchdown", defValue = "3")
	private static int nthChipTouchdownOutOfField = 3;
	@Configurable(comment = "[mm] Activate kicker if ball is closer than this", defValue = "500.0")
	private static double kickerToBallActivationDistance = 500.0;
	@Configurable(defValue = "10.0", comment = "[mm] Min distance to penalty area")
	private static double minDistToPenaltyArea = 10.0;
	@Configurable(comment = "[mm] Margin applied to goal for goal shot detection.", defValue = "500.0")
	private static double goalShotDetectionMargin = 500.0;
	@Configurable(comment = "[s] Time difference between the idealBotTime and idealBallTime", defValue = "0.3")
	private static double idealTimeToReachDest = 0.3;


	protected ADefenseRole(final ERole type)
	{
		super(type);
	}


	protected IVector2 moveToValidDest(IVector2 dest)
	{
		IVector2 insideField = Geometry.getField().withMargin(Geometry.getBotRadius()).nearestPointInside(dest, getPos());
		return Geometry.getPenaltyAreaOur().withMargin(Geometry.getBotRadius() + minDistToPenaltyArea)
				.nearestPointOutside(insideField);
	}


	protected KickParams calcKickParams()
	{
		final double kickSpeed = calculateArmChipSpeedDuringDefense();
		return KickParams.chip(kickSpeed).withDribblerMode(kickSpeed > 0 ? EDribblerMode.DEFAULT : EDribblerMode.OFF);
	}


	private double calculateArmChipSpeedDuringDefense()
	{
		var redirectConsultant = RedirectConsultantFactory.createDefault();
		var target = getPos().addNew(Vector2.fromAngle(getBot().getOrientation()));
		var targetAngle = getBot().getOrientation();

		var targetSpeed = calculateTargetKickSpeed(targetAngle);
		if (targetSpeed > 0)
		{
			var kickSpeed = redirectConsultant.getKickSpeed(getBall(), getPos(), target, targetSpeed);
			return adaptKickSpeedToBotVel(targetAngle, kickSpeed);
		}
		return 0;
	}


	protected boolean isGoalShotInterceptNecessary(IVector2 idealProtectionDest)
	{
		var botTime = TrajectoryGenerator.generatePositionTrajectory(getBot(), idealProtectionDest).getTotalTime();
		var interceptPos = getBall().getTrajectory().getTravelLine().closestPointOnPath(idealProtectionDest);
		var ballTime = getBall().getTrajectory().getTimeByPos(interceptPos);

		if (botTime + idealTimeToReachDest > ballTime)
		{
			return false;
		}

		var ballTravelLines = getBall().getTrajectory().getTravelLineSegments();
		var goalLine = Geometry.getGoalOur().getLineSegment().withMargin(goalShotDetectionMargin);

		return ballTravelLines.stream()
				.map(goalLine::intersect)
				.anyMatch(ISingleIntersection::isPresent);
	}


	protected Destination interceptGoalShot()
	{
		return interceptGoalShot(Vector2.zero());
	}


	protected Destination interceptGoalShot(IVector2 destinationOffset)
	{
		var closestPointsToIdealPos = getBall().getTrajectory().getTravelLineSegments().stream()
				.map(line -> line.closestPointOnPath(getPos()))
				.toList();
		var interceptPos = getPos().nearestTo(closestPointsToIdealPos);
		var ballTravelTime = getBall().getTrajectory().getTimeByPos(interceptPos);
		return new Destination(interceptPos.addNew(destinationOffset), ballTravelTime);
	}


	private double calculateTargetKickSpeed(double targetAngle)
	{
		var shapes = getShapes(EAiShapesLayer.DEFENSE_DRIBBLER_KICKER);
		var ballTravel = Lines.halfLineFromDirection(getBot().getPos(), Vector2.fromAngle(targetAngle));
		var intersections = Geometry.getField().intersectPerimeterPath(ballTravel);
		if (intersections.size() != 1 || !isDirectionReasonable(targetAngle) || !isPositionReasonable())
		{
			shapes.add(new DrawableLine(ballTravel.toLineSegment(1000), Color.RED));
			return 0;
		}
		var distance = intersections.getFirst().distanceTo(getPos());
		var kickSpeed = getBall().getChipConsultant().getInitVelForDistAtTouchdown(distance, nthChipTouchdownOutOfField);
		var maxSafeDistance = getBall().getChipConsultant()
				.getMaximumDistanceToOverChip(kickSpeed, RuleConstraints.getMaxRobotHeight());
		shapes.add(new DrawableLine(ballTravel.toLineSegment(maxSafeDistance), Color.GREEN));
		return SumatraMath.min(kickSpeed, RuleConstraints.getMaxKickSpeed());
	}


	private boolean isDirectionReasonable(double targetAngle)
	{
		// Check if we aim towards enemy side
		var ballLine = Lines.halfLineFromDirection(getPos(), Vector2.fromAngle(targetAngle));
		var intersections = Geometry.getGoalTheir().getGoalLine().intersect(ballLine);
		if (!intersections.isEmpty())
		{
			return true;
		}

		// Check if we aim away from own penalty area
		var invertedBallLine = Lines.halfLineFromDirection(getPos(), Vector2.fromAngle(targetAngle).multiply(-1));
		var invertedIntersections = Geometry.getPenaltyAreaOur().getRectangle().intersectPerimeterPath(invertedBallLine);
		return !invertedIntersections.isEmpty();
	}


	private boolean isPositionReasonable()
	{
		var distance = getPos().distanceToSqr(getBall().getPos());
		return distance <= kickerToBallActivationDistance * kickerToBallActivationDistance;
	}


	private double adaptKickSpeedToBotVel(double targetAngle, double kickSpeed)
	{
		var targetVel = Vector2.fromAngleLength(targetAngle, kickSpeed);
		var adaptedTargetVel = targetVel.subtractNew(getBot().getVel());
		if (adaptedTargetVel.isZeroVector()
				|| AngleMath.diffAbs(adaptedTargetVel.getAngle(), targetAngle) > AngleMath.DEG_090_IN_RAD)
		{
			// kick speed should be <= 0
			return 0;
		}
		return adaptedTargetVel.getLength2();
	}


	@Value
	protected class Destination
	{
		IVector2 pos;
		Double time;


		public boolean comeToAStopIsFaster()
		{
			if (time == null)
			{
				return TrajectoryGenerator.isComeToAStopFaster(getBot(), pos);
			} else
			{
				return TrajectoryGenerator.isComeToAStopFasterToReachPointInTime(getBot(), pos, time);
			}
		}


		public IVector2 validPos()
		{
			var validDest = moveToValidDest(pos);
			if (time == null)
			{
				return validDest;
			}

			return TrajectoryGenerator.generateVirtualPositionToReachPointInTime(getBot(), validDest, time);
		}
	}
}
