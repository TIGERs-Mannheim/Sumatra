/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.support;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.common.PointChecker;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveActionType;
import edu.tigers.sumatra.ai.metis.support.behaviors.ESupportBehavior;
import edu.tigers.sumatra.ai.metis.support.behaviors.SupportBehaviorPosition;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.triangle.Triangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;


/**
 * Highly "coachable" supporter role, trigger different support behavior with different states.
 */
@Log4j2
public class SupportRole extends ARole
{
	@Configurable(defValue = "true", comment = "Keep the current position during stop, as long as it is valid to reduce movement and the ball is in their half")
	private static boolean keepPositionInStopIfBallInTheirHalf = true;

	@Configurable(defValue = "true", comment = "Keep the current position during stop, as long as it is valid to reduce movement and the ball is in our half")
	private static boolean keepPositionInStopIfBallInOurHalf = true;

	@Configurable(defValue = "500", comment = "Distance [mm] to the ball to keep all the time")
	private static double distanceToBall = 500;
	@Configurable(comment = "[rad/s] maximal angular velocity while relaxing", defValue = "3")
	private static double relaxStateMaxVelW = 3;
	@Configurable(comment = "[rad/s2] maximal angular acceleration while relaxing", defValue = "10")
	private static double relaxStateMaxAccW = 10;
	private final PointChecker pointChecker = new PointChecker()
			.checkBallDistances()
			.checkPointFreeOfBots()
			.checkNotInPenaltyAreas()
			.checkConfirmWithKickOffRules()
			.checkInsideField()
			.checkCustom(this::isNotBetweenFreeKickAndBall);
	@Setter
	private SupportBehaviorPosition target;
	@Setter
	private ESupportBehavior behavior;
	@Setter
	private boolean avoidPassesFromOffensive = true;


	public SupportRole()
	{
		super(ERole.SUPPORT);
		var moveToTargetState = new MoveToTargetState();
		var relaxState = new RelaxState();

		moveToTargetState.addTransition("startRelaxing", relaxState::startRelaxing, relaxState);
		relaxState.addTransition("stopRelaxing", relaxState::stopRelaxing, moveToTargetState);

		setInitialState(moveToTargetState);
	}


	private boolean isNotBetweenFreeKickAndBall(IVector2 position)
	{
		var state = getAiFrame().getGameState();
		if (!state.isBallPlacementForUs() && !state.isFreeKickForUs() && !state.isNextStandardSituationForUs())
		{
			return true;
		}

		if (Geometry.getFieldHalfOur().isPointInShape(position))
		{
			return true;
		}

		var blockedTriangle = Triangle.fromCorners(
						Geometry.getGoalOur().getLeftPost(),
						Geometry.getGoalOur().getRightPost(),
						getBall().getPos()
				)
				.withMargin(500);
		return !blockedTriangle.isPointInShape(position);
	}


	private class MoveToTargetState extends RoleState<MoveToSkill>
	{
		public MoveToTargetState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			skill.getMoveCon().setDistanceToBall(distanceToBall);
			skill.getMoveCon().setCustomObstacles(createCustomObstacle());

			if (target == null)
			{
				return;
			}

			skill.updateDestination(target.getPosition());
			target.getLookAt().ifPresent(skill::updateLookAtTarget);
		}


		private List<IObstacle> createCustomObstacle()
		{
			return Stream.concat(
					getTacticalField().getAllPassObstacles().entrySet().stream()
							.filter(entry -> avoidKickFromAction(entry.getKey()))
							.map(Map.Entry::getValue)
							.flatMap(List::stream),
					Optional.ofNullable(getTacticalField().getOngoingPassObstacle())
							.filter(obstacle -> avoidPassesFromOffensive)
							.stream()
			).toList();
		}


		private boolean avoidKickFromAction(EOffensiveActionType type)
		{
			return switch (type)
			{
				case KICK, DRIBBLE_KICK, REDIRECT_KICK -> true;
				case PASS -> avoidPassesFromOffensive;
				default -> false;
			};
		}


		@Override
		public String toString()
		{
			if (behavior != null)
			{
				return super.toString() + "<" + behavior.toString() + ">";
			}
			return super.toString();
		}
	}

	private class RelaxState extends RoleState<MoveToSkill>
	{
		private RelaxState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			skill.updateTargetAngle(getBot().getAngleByTime(0) + 0.5);
			skill.getMoveConstraints().setVelMaxW(relaxStateMaxVelW);
			skill.getMoveConstraints().setAccMaxW(relaxStateMaxAccW);
		}


		private boolean startRelaxing()
		{
			var gameState = getAiFrame().getGameState();
			return gameState.isStoppedGame() &&
					!gameState.isNextStandardSituationForThem() &&
					keepPositionInStop() &&
					isCurrentPosValid();
		}


		private boolean isCurrentPosValid()
		{
			return pointChecker.allMatch(getAiFrame().getBaseAiFrame(), getPos(), getBotID());
		}


		private boolean stopRelaxing()
		{
			return !startRelaxing();
		}


		private boolean keepPositionInStop()
		{
			return (keepPositionInStopIfBallInTheirHalf && isBallOnTheirHalf()) ||
					(keepPositionInStopIfBallInOurHalf && isBallOnOurHalf());
		}


		private boolean isBallOnTheirHalf()
		{
			return getBall().getPos().x() >= 0;
		}


		private boolean isBallOnOurHalf()
		{
			return getBall().getPos().x() <= 0;
		}
	}
}
