/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.redirect.RedirectConsultantFactory;
import edu.tigers.sumatra.skillsystem.skills.util.PositionValidator;
import lombok.Getter;
import lombok.Setter;

import java.awt.Color;


public class ApproachBallLineSkill extends AMoveToSkill
{
	@Setter
	private double maximumReasonableRedirectAngle = 1.2;

	private final PositionValidator positionValidator = new PositionValidator();

	@Configurable(defValue = "100.0", comment = "When distance is below, the ball is considered catched up")
	private static double minDistanceToBallLine = 100;

	@Getter
	@Setter
	private IVector2 desiredBallCatchPos;
	@Setter
	private IVector2 target;
	@Setter
	private double marginToTheirPenArea;


	@Override
	public void doUpdate()
	{
		getMoveCon().setBallObstacle(false);

		positionValidator.update(getWorldFrame(), getMoveCon());
		positionValidator.getMarginToPenArea().put(ETeam.OPPONENTS, marginToTheirPenArea);
		if (getBall().getVel().getLength2() > 0.1)
		{
			var catchPosition = ballCatchPosition();
			catchPosition = positionValidator.movePosInsideFieldWrtBallPos(catchPosition);
			catchPosition = positionValidator.movePosOutOfPenAreaWrtBall(catchPosition);

			var targetAngle = RedirectConsultantFactory.createDefault().getTargetAngle(
					getBall(),
					catchPosition,
					target,
					RuleConstraints.getMaxKickSpeed());

			double redirectAngle = getBall().getVel().multiplyNew(-1).angleToAbs(Vector2.fromAngle(targetAngle))
					.orElse(0.0);

			getShapes().get(ESkillShapesLayer.APPROACH_BALL_LINE_SKILL)
					.add(new DrawableAnnotation(catchPosition,
							String.format("angle: %.2f", AngleMath.rad2deg(redirectAngle)), Vector2.fromY(100)));

			if (redirectAngle > maximumReasonableRedirectAngle)
			{
				updateLookAtTarget(getBall());
				targetAngle = getBall().getVel().multiplyNew(-1).getAngle();
			} else
			{
				updateTargetAngle(targetAngle);
			}

			var center2DribblerDist = getBot().getCenter2DribblerDist() + Geometry.getBallRadius();
			var botToKickerPos = Vector2.fromAngle(targetAngle).scaleToNew(center2DribblerDist);
			var dest = catchPosition.subtractNew(botToKickerPos);

			updateDestination(dest);
			updatePrimaryDirection();
		}

		if (desiredBallCatchPos != null)
		{
			getShapes().get(ESkillShapesLayer.APPROACH_BALL_LINE_SKILL)
					.add(new DrawableCircle(desiredBallCatchPos, 30, Color.magenta));
		}
		super.doUpdate();

		updateSkillState();
	}


	private void updateSkillState()
	{
		if (getBall().getVel().getLength2() < 0.1 || ballMovesAwayFromMe())
		{
			setSkillState(ESkillState.FAILURE);
		} else if (ballLineIsApproached())
		{
			setSkillState(ESkillState.SUCCESS);
		} else
		{
			setSkillState(ESkillState.IN_PROGRESS);
		}
	}


	private boolean ballLineIsApproached()
	{
		return getDestination().distanceTo(getPos()) < minDistanceToBallLine;
	}


	private boolean ballMovesAwayFromMe()
	{
		if (getBall().getPos().distanceTo(getPos()) < 200)
		{
			// Do not switch if close to ball
			return false;
		}
		var ballDir = getBall().getVel();
		var ballToBotDir = getPos().subtractNew(getBall().getPos());
		var angle = ballDir.angleToAbs(ballToBotDir).orElse(0.0);
		return angle > AngleMath.deg2rad(120);
	}


	private void updatePrimaryDirection()
	{
		if (Boolean.TRUE.equals(ballMovesInTheSameDirectionInWhichTheRobotAlsoWantsToMove()))
		{
			getMoveConstraints().setPrimaryDirection(Vector2.zero());
		} else
		{
			getMoveConstraints().setPrimaryDirection(getBall().getVel());
		}
	}


	private Boolean ballMovesInTheSameDirectionInWhichTheRobotAlsoWantsToMove()
	{
		return getBall().getVel().angleToAbs(getDestination().subtractNew(getPos())).map(a -> a < AngleMath.PI_HALF)
				.orElse(true);
	}


	private IVector2 ballCatchPosition()
	{
		if (desiredBallCatchPos != null)
		{
			return desiredBallCatchPos;
		}
		IVector2 catchPos = getTBot().getBotKickerPos(Geometry.getBallRadius());
		return getBall().getTrajectory().closestPointTo(catchPos);
	}
}
