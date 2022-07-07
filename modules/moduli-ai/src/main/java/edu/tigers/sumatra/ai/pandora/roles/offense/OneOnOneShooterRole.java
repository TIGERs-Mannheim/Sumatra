/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRangeRater;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.triangle.Triangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.ApproachAndStopBallSkill;
import edu.tigers.sumatra.skillsystem.skills.ESkillState;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveWithBallSkill;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Value;

import java.awt.Color;
import java.util.Comparator;
import java.util.Optional;


public class OneOnOneShooterRole extends ARole
{
	@Configurable(comment = "Distance to dribble in one go [mm]", defValue = "1000.0")
	private static double dribbleAheadDistance = 1000.0;

	@Configurable(comment = "Distance to do a quick stop to separate from ball [mm]", defValue = "100.0")
	private static double dribbleSeparationDistance = 100.0;

	@Configurable(comment = "Time without ball contact until dribbling is continued [mm]", defValue = "0.1")
	private static double dribbleSeparationTime = 0.1;

	@Configurable(comment = "Required score to execute a flat kick (0.0 - 1.0 best)", defValue = "0.3")
	private static double requiredFlatKickScore = 0.3;

	@Configurable(comment = "Distance to target behind keeper for a chip kick [mm]", defValue = "1000.0")
	private static double chipBehindKeeperDistance = 1000.0;


	private IVector2 currentDribbleTarget;
	private ChipOpportunity chipOpportunity;
	private IRatedTarget flatRatedTarget;


	/**
	 * The penalty attacker role
	 */
	public OneOnOneShooterRole()
	{
		super(ERole.ONE_ON_ONE_SHOOTER);

		var waitAtBallState = new WaitAtBallState();
		var flatKickOnGoalState = new FlatKickOnGoalState();
		var approachBallState = new ApproachBallState();
		var dribbleForwardState = new DribbleForwardState();
		var waitForBallSeparationState = new WaitForBallSeparation();
		var chipKickOnGoalState = new ChipKickOnGoalState();

		setInitialState(waitAtBallState);

		waitAtBallState.addTransition(waitAtBallState::isPenaltyActiveAndGoalEmpty, flatKickOnGoalState);
		waitAtBallState.addTransition(waitAtBallState::isPenaltyActive, approachBallState);
		approachBallState.addTransition(approachBallState::hasBallContact, dribbleForwardState);
		approachBallState.addTransition(ESkillState.FAILURE, approachBallState);
		dribbleForwardState.addTransition(dribbleForwardState::isCloseToDribbleTarget, waitForBallSeparationState);
		dribbleForwardState.addTransition(this::isGoodGoalKickAvailable, flatKickOnGoalState);
		dribbleForwardState.addTransition(this::isChipKickTheOnlyGoodPlanWeHave, chipKickOnGoalState);
		waitForBallSeparationState.addTransition(waitForBallSeparationState::isBallSeparated, approachBallState);
		waitForBallSeparationState.addTransition(this::isGoodGoalKickAvailable, flatKickOnGoalState);
		waitForBallSeparationState.addTransition(this::isChipKickTheOnlyGoodPlanWeHave, chipKickOnGoalState);
	}


	@Override
	protected void beforeUpdate()
	{
		super.beforeUpdate();

		updateChipKickOpportunity();
		updateFlatKickOpportunity();
	}


	@Override
	protected void afterUpdate()
	{
		super.afterUpdate();

		var shapesList = getShapes(EAiShapesLayer.PENALTY_ONE_ON_ONE);

		if (flatRatedTarget != null)
		{
			shapesList.add(new DrawableAnnotation(getPos().addNew(Vector2.fromX(250.0)),
					String.format("Flat score: %.2f", flatRatedTarget.getScore())));

			ILine toTarget = Line.fromPoints(getBall().getPos(), flatRatedTarget.getTarget());
			shapesList.add(new DrawableLine(toTarget, Color.red));
		}

		if (chipOpportunity != null)
		{
			shapesList.add(new DrawableCircle(chipOpportunity.getTarget(), 50, Color.magenta));

			shapesList.add(new DrawableAnnotation(getPos().addNew(Vector2.fromX(-250.0)),
					String.format("Chip speed: %.2f", chipOpportunity.getKickSpeed())));

			shapesList.add(new DrawableLine(chipOpportunity.getBallTrajectory().getTravelLineRolling(), Color.magenta));
		}
	}


	private class WaitAtBallState extends RoleState<MoveToSkill>
	{
		public WaitAtBallState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			var waitPos = getBall().getPos()
					.subtractNew(Vector2.fromX(RuleConstraints.getStopRadius() + Geometry.getBotRadius() + 50));
			skill.updateDestination(waitPos);
			skill.updateLookAtTarget(getBall());
		}


		public boolean isPenaltyActive()
		{
			return getAiFrame().getGameState().isPenalty();
		}


		public boolean isPenaltyActiveAndGoalEmpty()
		{
			var ballGoalTriangle = Triangle.fromCorners(getBall().getPos(), Geometry.getGoalTheir().getLeftPost(),
					Geometry.getGoalTheir().getRightPost());

			boolean noOpponentInTriangle = getWFrame().getOpponentBots().values().stream()
					.noneMatch(b -> ballGoalTriangle.isPointInShape(b.getPos(), Geometry.getBotRadius() * 2.0));

			return isPenaltyActive() && noOpponentInTriangle;
		}
	}

	private class ApproachBallState extends RoleState<ApproachAndStopBallSkill>
	{
		public ApproachBallState()
		{
			super(ApproachAndStopBallSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			skill.setMarginToTheirPenArea(0.0);
		}


		public boolean hasBallContact()
		{
			return getBot().getBallContact().hadContact(0.1);
		}
	}

	private class DribbleForwardState extends RoleState<MoveWithBallSkill>
	{
		public DribbleForwardState()
		{
			super(MoveWithBallSkill::new);
		}


		@Override
		protected void onInit()
		{
			currentDribbleTarget = getPos().addNew(Vector2.fromX(dribbleAheadDistance));
		}


		@Override
		protected void onUpdate()
		{
			skill.setFinalDest(currentDribbleTarget);
			skill.setFinalOrientation(Vector2.fromPoints(getPos(), Geometry.getGoalTheir().getCenter()).getAngle());
		}


		public boolean isCloseToDribbleTarget()
		{
			return getPos().distanceTo(currentDribbleTarget) < dribbleSeparationDistance;
		}
	}

	private class WaitForBallSeparation extends RoleState<MoveToSkill>
	{
		public WaitForBallSeparation()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onInit()
		{
			skill.updateDestination(currentDribbleTarget);
			skill.updateTargetAngle(getBot().getOrientation());
		}


		public boolean isBallSeparated()
		{
			return !getBot().getBallContact().hadContact(dribbleSeparationTime);
		}
	}

	private class FlatKickOnGoalState extends RoleState<TouchKickSkill>
	{
		public FlatKickOnGoalState()
		{
			super(TouchKickSkill::new);
		}


		@Override
		protected void onInit()
		{
			skill.setTarget(getKickTarget());
			skill.setDesiredKickParams(KickParams.maxStraight());
		}
	}

	private class ChipKickOnGoalState extends RoleState<TouchKickSkill>
	{
		public ChipKickOnGoalState()
		{
			super(TouchKickSkill::new);
		}


		@Override
		protected void onInit()
		{
			if (chipOpportunity == null)
			{
				// this should not happen but it is good to have a reasonable fallback
				skill.setTarget(getKickTarget());
				skill.setDesiredKickParams(KickParams.maxStraight());
			} else
			{
				skill.setTarget(chipOpportunity.getTarget());
				skill.setDesiredKickParams(KickParams.chip(chipOpportunity.kickSpeed));
			}
		}
	}


	private IVector2 getKickTarget()
	{
		return Optional.ofNullable(flatRatedTarget).map(IRatedTarget::getTarget)
				.orElse(Geometry.getGoalTheir().getCenter());
	}


	private boolean isGoodGoalKickAvailable()
	{
		return Optional.ofNullable(flatRatedTarget).map(IRatedTarget::getScore).orElse(0.0) > requiredFlatKickScore;
	}


	private boolean isChipKickTheOnlyGoodPlanWeHave()
	{
		return chipOpportunity != null && !isGoodGoalKickAvailable();
	}


	private void updateFlatKickOpportunity()
	{
		AngleRangeRater rater = AngleRangeRater.forGoal(Geometry.getGoalTheir());
		rater.setObstacles(getWFrame().getOpponentBots().values());

		var optRatedTarget = rater.rate(getBall().getPos());

		flatRatedTarget = optRatedTarget.orElse(null);
	}


	private void updateChipKickOpportunity()
	{
		var optKeeper = getOpponentKeeper();

		if (optKeeper.isEmpty())
		{
			chipOpportunity = null;
			return;
		}

		var keeper = optKeeper.get();

		IVector2 firstTouchdownTarget = LineMath
				.stepAlongLine(keeper.getPos(), getBall().getPos(), -chipBehindKeeperDistance);

		double distanceToTouchdown = firstTouchdownTarget.distanceTo(getBall().getPos());

		double kickSpeedToOverchipKeeper = getBall().getChipConsultant()
				.getInitVelForDistAtTouchdown(distanceToTouchdown, 0);

		if (kickSpeedToOverchipKeeper > RuleConstraints.getMaxKickSpeed())
		{
			chipOpportunity = null;
			return;
		}

		double kickDirection = Vector2.fromPoints(getBall().getPos(), firstTouchdownTarget).getAngle();

		IVector3 kickVel = getBall().getChipConsultant().speedToVel(kickDirection, kickSpeedToOverchipKeeper);

		var chipTraj = Geometry.getBallFactory()
				.createTrajectoryFromKickedBallWithoutSpin(getBall().getPos(), kickVel.multiplyNew(1000.0));

		if (chipTraj.getTravelLineRolling().intersectSegment(Geometry.getGoalTheir().getLineSegment()).isPresent())
		{
			chipOpportunity = new ChipOpportunity(kickSpeedToOverchipKeeper, firstTouchdownTarget, chipTraj);
		} else
		{
			chipOpportunity = null;
		}
	}


	private Optional<ITrackedBot> getOpponentKeeper()
	{
		return getWFrame().getOpponentBots().values().stream()
				.min(Comparator.comparingDouble(a -> a.getPos().distanceToSqr(Geometry.getGoalTheir().getCenter())));
	}


	@Value
	private static class ChipOpportunity
	{
		double kickSpeed;
		IVector2 target;
		IBallTrajectory ballTrajectory;
	}
}