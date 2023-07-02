/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
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
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.triangle.Triangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.ESkillState;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Value;

import java.awt.Color;
import java.util.Comparator;
import java.util.Optional;


public class OneOnOneShooterRole extends ARole
{
	@Configurable(comment = "Required score to execute a flat kick (0.0 - 1.0 best)", defValue = "0.3")
	private static double requiredFlatKickScore = 0.3;

	@Configurable(comment = "Distance to target behind keeper for a chip kick [mm]", defValue = "1000.0")
	private static double chipBehindKeeperDistance = 1000.0;

	@Configurable(comment = "Poke velocity [m/s]", defValue = "1.2")
	private static double pokeVelocity = 1.2;

	@Configurable(comment = "Distance to ball when approaching (>150mm) [mm]", defValue = "150")
	private static double approachDistance = 150.0;

	@Configurable(comment = "Some tolerance when the ball is considered approached [mm]", defValue = "0")
	private static double approachDistanceTolerance = 0.0;


	private ChipOpportunity chipOpportunity;
	private IRatedTarget flatRatedTarget;


	/**
	 * The penalty attacker role
	 */
	public OneOnOneShooterRole()
	{
		super(ERole.ONE_ON_ONE_SHOOTER);

		var waitAtBallState = new WaitAtBallState();
		var approachBallState = new ApproachBallState();
		var pokeBallState = new PokeBallState();
		var flatKickOnGoalState = new FlatKickOnGoalState();
		var chipKickOnGoalState = new ChipKickOnGoalState();

		setInitialState(waitAtBallState);

		waitAtBallState.addTransition("isPenaltyActiveAndGoalEmpty", waitAtBallState::isPenaltyActiveAndGoalEmpty, flatKickOnGoalState);
		waitAtBallState.addTransition("isPenaltyActive", waitAtBallState::isPenaltyActive, approachBallState);
		approachBallState.addTransition(ESkillState.SUCCESS, pokeBallState);
		approachBallState.addTransition("isCloseToBall", approachBallState::isCloseToBall, pokeBallState);
		approachBallState.addTransition(ESkillState.FAILURE, approachBallState);
		approachBallState.addTransition("isGoodGoalKickAvailable", this::isGoodGoalKickAvailable, flatKickOnGoalState);
		approachBallState.addTransition("isChipKickTheOnlyGoodPlanWeHave", this::isChipKickTheOnlyGoodPlanWeHave, chipKickOnGoalState);
		approachBallState.addTransition("timeAlmostUp", this::timeAlmostUp, flatKickOnGoalState);

		pokeBallState.addTransition("isBallHit", pokeBallState::isBallHit, approachBallState);
		pokeBallState.addTransition("isGoodGoalKickAvailable", this::isGoodGoalKickAvailable, flatKickOnGoalState);
		pokeBallState.addTransition("isChipKickTheOnlyGoodPlanWeHave", this::isChipKickTheOnlyGoodPlanWeHave, chipKickOnGoalState);
		pokeBallState.addTransition("timeAlmostUp", this::timeAlmostUp, flatKickOnGoalState);
	}


	private boolean timeAlmostUp()
	{
		return getAiFrame().getRefereeMsg().getCurrentActionTimeRemaining() < 2;
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

			var toTarget = Lines.segmentFromPoints(getBall().getPos(), flatRatedTarget.getTarget());
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

		if (!chipTraj.getTravelLineRolling().intersect(Geometry.getGoalTheir().getLineSegment()).isEmpty())
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
					.subtractNew(Vector2.fromX(RuleConstraints.getStopRadius() + Geometry.getBotRadius() + 80));
			skill.updateDestination(waitPos);
			skill.updateLookAtTarget(getBall());
			skill.getMoveCon().setPenaltyAreaOurObstacle(false);
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
					.noneMatch(b -> ballGoalTriangle.withMargin(Geometry.getBotRadius() * 2).isPointInShape(b.getPos()));

			return isPenaltyActive() && noOpponentInTriangle;
		}
	}

	private class ApproachBallState extends RoleState<MoveToSkill>
	{
		public ApproachBallState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onInit()
		{
			skill.getMoveCon().setPenaltyAreaOurObstacle(false);
		}


		@Override
		protected void onUpdate()
		{
			IVector2 approachPos = LineMath.stepAlongLine(getBall().getPos(), Geometry.getGoalTheir().getCenter(),
					-(Geometry.getBotRadius() + approachDistance));
			skill.updateDestination(approachPos);
			skill.updateTargetAngle(
					getBall().getPos().subtractNew(getBot().getBotKickerPos()).getAngle());
		}


		public boolean isCloseToBall()
		{
			return skill.getDestination() != null && skill.getDestination().distanceTo(getBot().getPos())
					< approachDistance + approachDistanceTolerance;
		}
	}

	private class PokeBallState extends RoleState<MoveToSkill>
	{
		private IVector2 relativePosTarget;
		private double initialBallSpeed;


		public PokeBallState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onInit()
		{
			relativePosTarget = Vector2.fromPoints(getBall().getPos(), getBot().getPos()).multiply(-1.0);
			skill.getMoveCon().setBallObstacle(false);
			skill.getMoveCon().setPenaltyAreaOurObstacle(false);

			initialBallSpeed = getBall().getVel().getLength();
		}


		@Override
		protected void onUpdate()
		{
			skill.setVelMax(pokeVelocity);
			skill.updateDestination(getBall().getPos().addNew(relativePosTarget));
			skill.updateTargetAngle(
					getBall().getPos().addNew(relativePosTarget).subtractNew(getBot().getBotKickerPos()).getAngle());
		}


		public boolean isBallHit()
		{
			return getBall().getVel().getLength() > initialBallSpeed + 0.1 || getBot().hasBallContact();
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
}