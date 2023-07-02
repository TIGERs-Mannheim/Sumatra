/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.support;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.common.PointChecker;
import edu.tigers.sumatra.ai.metis.kicking.Kick;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveActionType;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.ai.metis.support.behaviors.ESupportBehavior;
import edu.tigers.sumatra.ai.metis.support.behaviors.SupportBehaviorPosition;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;
import edu.tigers.sumatra.pathfinder.obstacles.TubeObstacle;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Map;


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

	@Setter
	private SupportBehaviorPosition target;
	@Setter
	private ESupportBehavior behavior;
	@Setter
	private double maxPlannedKickObstacleLength;


	@Setter
	private boolean avoidPassesFromOffensive = true;


	private final PointChecker pointChecker = new PointChecker()
			.checkBallDistances()
			.checkPointFreeOfBots()
			.checkNotInPenaltyAreas()
			.checkConfirmWithKickOffRules()
			.checkInsideField();


	public SupportRole()
	{
		super(ERole.SUPPORT);
		var moveToTargetState = new MoveToTargetState();
		var relaxState = new RelaxState();

		moveToTargetState.addTransition("startRelaxing", relaxState::startRelaxing, relaxState);
		relaxState.addTransition("stopRelaxing", relaxState::stopRelaxing, moveToTargetState);

		setInitialState(moveToTargetState);
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
			double distanceToBall = RuleConstraints.getStopRadius() + Geometry.getBotRadius();
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
			return getTacticalField().getOffensiveActions().entrySet().stream()
					.filter(entry -> getTacticalField().getDesiredBotMap().get(EPlay.OFFENSIVE).contains(entry.getKey()))
					.map(Map.Entry::getValue)
					.map(RatedOffensiveAction::getAction)
					.filter(action -> avoidKickFromAction(action.getType()))
					.map(OffensiveAction::getKick)
					.map(this::createKickObstacle)
					.toList();
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


		private IObstacle createKickObstacle(Kick kick)
		{
			var direction = kick.getTarget().subtractNew(kick.getSource())
					.scaleTo(Math.min(kick.getTarget().distanceTo(kick.getSource()), maxPlannedKickObstacleLength));
			return new TubeObstacle("Kick",
					Tube.create(
							kick.getSource(),
							kick.getSource().addNew(direction),
							2 * Geometry.getBotRadius() + Geometry.getBallRadius()
					)
			);
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

	private class RelaxState extends RoleState<IdleSkill>
	{
		private RelaxState()
		{
			super(IdleSkill::new);
		}


		private boolean startRelaxing()
		{
			return getAiFrame().getGameState().isStoppedGame() &&
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
