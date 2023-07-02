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
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.redirect.RedirectConsultantFactory;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;

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
	@Configurable(defValue = "10.0", comment = "Min distance [mm] to penalty area")
	private static double minDistToPenaltyArea = 10.0;


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
		var targetAngle = redirectConsultant.getTargetAngle(getBall(), getPos(), target,
				RuleConstraints.getMaxKickSpeed());

		var targetSpeed = calculateTargetKickSpeed(targetAngle);
		if (targetSpeed > 0)
		{
			var kickSpeed = redirectConsultant.getKickSpeed(getBall(), getPos(), target, targetSpeed);
			return adaptKickSpeedToBotVel(targetAngle, kickSpeed);
		}
		return 0;
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
		var distance = intersections.get(0).distanceTo(getPos());
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

}
