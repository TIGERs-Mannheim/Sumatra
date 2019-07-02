/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import java.awt.Color;

import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.redirect.ARedirectConsultant;
import edu.tigers.sumatra.skillsystem.skills.redirect.RedirectConsultantFactory;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.skillsystem.skills.util.PositionValidator;


public class ApproachBallLineSkill extends AMoveSkill
{
	private IVector2 desiredApproachingPos;
	private IVector2 target = null;
	private ARedirectConsultant consultant;
	private double marginToTheirPenArea = 0;


	public ApproachBallLineSkill()
	{
		super(ESkill.APPROACH_BALL_LINE);
		setInitialState(new DefaultState());
	}


	public void setMarginToTheirPenArea(final double marginToTheirPenArea)
	{
		this.marginToTheirPenArea = marginToTheirPenArea;
	}

	private class DefaultState extends MoveToState
	{
		private final PositionValidator positionValidator = new PositionValidator();


		private DefaultState()
		{
			super(ApproachBallLineSkill.this);
		}


		@Override
		public void doEntryActions()
		{
			getMoveCon().setBallObstacle(false);
			super.doEntryActions();
		}


		@Override
		public void doUpdate()
		{
			if (getBall().getTrajectory().getTravelLineRolling().distanceTo(getPos()) < 100)
			{
				// do not respect other bots, when on ball line
				getMoveCon().setBotsObstacle(false);
			}

			positionValidator.update(getWorldFrame(), getMoveCon());
			positionValidator.getMarginToPenArea().put(ETeam.OPPONENTS, marginToTheirPenArea);
			if (getBall().getVel().getLength2() > 0.1)
			{
				updatePrimaryDirection();

				IVector2 bot2Target = getBall().getPos().subtractNew(getPos());
				double targetAngle = bot2Target.getAngle();
				if (target != null)
				{
					targetAngle = calcTargetAngle();
					getMoveCon().updateTargetAngle(targetAngle);
				} else if (!bot2Target.isZeroVector())
				{
					getMoveCon().updateTargetAngle(bot2Target.getAngle());
				}

				IVector2 dest = findDestination();
				IVector2 botToKickerPos = Vector2.fromAngle(targetAngle).scaleToNew(getBot().getCenter2DribblerDist());
				dest = dest.subtractNew(botToKickerPos);
				dest = positionValidator.movePosInsideFieldWrtBallPos(dest);
				dest = positionValidator.movePosOutOfPenAreaWrtBall(dest);
				getMoveCon().updateDestination(dest);
			}

			if (desiredApproachingPos != null)

			{
				getShapes().get(ESkillShapesLayer.APPROACH_BALL_LINE_SKILL)
						.add(new DrawableCircle(desiredApproachingPos, 100, Color.magenta));
			}
			super.doUpdate();
		}


		private void updatePrimaryDirection()
		{
			if (ballAndRobotMoveInTheSameDirection()
					|| getBall().getTrajectory().getTravelLineRolling().distanceTo(getPos()) < 300)
			{
				getMoveCon().getMoveConstraints().setPrimaryDirection(getBall().getVel());
			} else
			{
				getMoveCon().getMoveConstraints().setPrimaryDirection(Vector2.zero());
			}
		}


		private Boolean ballAndRobotMoveInTheSameDirection()
		{
			return getBall().getVel().angleToAbs(getVel()).map(a -> a < AngleMath.PI_HALF).orElse(true);
		}


		private IVector2 findDestination()
		{
			if (desiredApproachingPos != null)
			{
				return getBall().getTrajectory().getTravelLineRolling().closestPointOnLine(desiredApproachingPos);
			}
			return getBall().getTrajectory().getTravelLineRolling().closestPointOnLine(getPos());
		}


		private double calcTargetAngle()
		{
			updateConsultant();
			return consultant.getTargetAngle();
		}


		private void updateConsultant()
		{
			IVector2 ballVelAtCollision = getBall().getTrajectory()
					.getVelByTime(getBall().getTrajectory().getTimeByPos(getTBot().getBotKickerPos())).getXYVector();
			if (ballVelAtCollision.isZeroVector() || getBall().getVel().isZeroVector())
			{
				ballVelAtCollision = getPos().subtractNew(getBall().getPos()).scaleTo(3.0);
			}

			IVector2 desiredBallDir = target.subtractNew(getTBot().getBotKickerPos());
			IVector2 desiredBallVel = desiredBallDir.scaleToNew(KickParams.maxStraight().getKickSpeed());
			consultant = RedirectConsultantFactory.createDefault(ballVelAtCollision, desiredBallVel);
		}

	}


	public void setDesiredApproachingPos(final IVector2 desiredApproachingPos)
	{
		this.desiredApproachingPos = desiredApproachingPos;
	}


	public IVector2 getDesiredApproachingPos()
	{
		return desiredApproachingPos;
	}


	public void setTarget(final IVector2 target)
	{
		this.target = target;
	}
}
