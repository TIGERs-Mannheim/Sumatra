/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.defense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.common.KeepDistanceToBall;
import edu.tigers.sumatra.ai.common.PointChecker;
import edu.tigers.sumatra.ai.metis.ballresponsibility.EBallResponsibility;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.boundary.IShapeBoundary;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.EObstacleAvoidanceMode;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.skillsystem.skills.MoveOnShapeBoundarySkill;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.statemachine.Transition;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.Setter;

import java.awt.Color;
import java.util.Set;
import java.util.stream.Collectors;

import static edu.tigers.sumatra.ai.metis.EAiShapesLayer.DEFENSE_PENALTY_AREA_ROLE;


/**
 * Defender role for protecting on the penalty area boundary.
 * It will make sure that new defenders will first move to the boundary safely and then keep moving on it
 * using a custom skill.
 * It will also handle kicking the ball away.
 */
public class DefenderPenAreaRole extends ADefenseRole
{
	@Configurable(defValue = "0.1")
	private static double requiredMinimumSlackTime = 0.1;
	@Configurable(defValue = "0.6")
	private static double slackTimeHyst = 0.6;
	@Configurable(defValue = "500.0")
	private static double moveToPenAreaMargin = 500;

	@Getter
	@Setter
	private boolean allowedToKickBall = false;

	/*
	 * destination must be initialized, because the defense group will trigger an update through switchRoles before
	 * the destination is initially set
	 */
	@Setter
	private IVector2 destination = Vector2.fromX(-(Geometry.getFieldLength() - Geometry.getPenaltyAreaDepth()));
	@Setter
	private IShapeBoundary penAreaBoundary = Geometry.getPenaltyAreaOur()
			.withMargin(Geometry.getBotRadius())
			.getShapeBoundary();


	public DefenderPenAreaRole()
	{
		super(ERole.DEFENDER_PEN_AREA);

		var moveToPenArea = new MoveToPenAreaState();
		var moveOnPenArea = new MoveOnPenAreaState();
		var kickBall = new KickBallState();
		var keepDistanceToBall = new KeepDistanceToBallState();

		var stoppedGameTransition = Transition.builder()
				.name("game stopped")
				.evaluation(() -> getAiFrame().getGameState().isStoppedGame())
				.nextState(keepDistanceToBall)
				.build();
		moveToPenArea.addTransition(stoppedGameTransition);
		moveToPenArea.addTransition("reachedPenArea", moveToPenArea::reachedPenArea, moveOnPenArea);
		moveOnPenArea.addTransition(stoppedGameTransition);
		moveOnPenArea.addTransition("canKickBallAway", this::canKickBallAway, kickBall);
		kickBall.addTransition(stoppedGameTransition);
		kickBall.addTransition("situationIsDangerous", this::situationIsDangerous, moveOnPenArea);
		kickBall.addTransition("timeRunOut", kickBall::timeRunOut, moveOnPenArea);
		keepDistanceToBall.addTransition("game not stopped", () -> !getAiFrame().getGameState().isStoppedGame(),
				moveToPenArea);

		// start with going straight to the target position
		setInitialState(moveToPenArea);
	}


	private boolean canKickBallAway()
	{
		return allowedToKickBall && enoughTimeToKickSafely(slackTimeHyst) && !situationIsDangerous();
	}


	/**
	 * @param additionalRequiredTime
	 * @return whether there is enough time left to kick the ball
	 */
	private boolean enoughTimeToKickSafely(double additionalRequiredTime)
	{
		IVector2 target = getWFrame().getBall().getPos();
		double minOpponentArrivalTime = getWFrame().getOpponentBots().values().stream()
				.mapToDouble(bot -> TrajectoryGenerator.generatePositionTrajectory(bot, target).getTotalTime())
				.min()
				.orElse(1000000);
		double myTime = TrajectoryGenerator.generatePositionTrajectory(getBot(), target).getTotalTime();
		double slackTime = minOpponentArrivalTime - myTime - requiredMinimumSlackTime - additionalRequiredTime;
		return slackTime > 0;
	}


	private IVector2 validFinalDestination(final IVector2 intermediatePos)
	{
		if (getBot().getVel().getLength2() > 0.5)
		{
			for (ITrackedBot bot : getWFrame().getOpponentBots().values())
			{
				if (bot.getPos().distanceTo(intermediatePos) < Geometry.getBotRadius() * 1.5)
				{
					var finalPos = LineMath.stepAlongLine(intermediatePos, getPos(), Geometry.getBotRadius() * 1.5);
					return penAreaBoundary.projectPoint(Geometry.getGoalOur().getCenter(), finalPos);
				}
			}

			double botBallRadius = Geometry.getBotRadius() + Geometry.getBallRadius();
			var adaptedPenArea = penAreaBoundary.withMargin(-botBallRadius);
			if (!adaptedPenArea.isPointInShape(getBall().getPos()))
			{
				final IVector2 ballProjectedToPenArea = adaptedPenArea.projectPoint(Geometry.getGoalOur().getCenter(),
						getBall().getPos());
				final double ballToPenAreaDist = ballProjectedToPenArea.distanceTo(getBall().getPos());

				if (intermediatePos.distanceTo(getBall().getPos()) < botBallRadius)
				{
					// ball is inside destination. Better not drive onto ball
					return adaptedPenArea.withMargin(ballToPenAreaDist + botBallRadius)
							.projectPoint(Geometry.getGoalOur().getCenter(), intermediatePos);
				}
			}
		}

		return intermediatePos;
	}


	private boolean situationIsDangerous()
	{
		return ballInPenArea() || lostBallResponsibility();
	}


	private boolean ballInPenArea()
	{
		final double margin = Geometry.getBotRadius() * 2 + Geometry.getBallRadius();
		return Geometry.getPenaltyAreaOur().withMargin(margin).isPointInShape(getWFrame().getBall().getPos());
	}


	private boolean lostBallResponsibility()
	{
		return getAiFrame().getTacticalField().getBallResponsibility() != EBallResponsibility.DEFENSE;
	}


	private class KeepDistanceToBallState extends MoveState
	{
		private final PointChecker pointChecker = new PointChecker()
				.checkBallDistances()
				.checkInsideField()
				.checkPointFreeOfBots()
				.checkCustom(this::canOpponentGetBallInPenArea);
		private final KeepDistanceToBall keepDistanceToBall = new KeepDistanceToBall(pointChecker);
		private IVector2 curPos;


		@Override
		protected void onInit()
		{
			skill.getMoveCon().setPenaltyAreaOurObstacle(false);
			curPos = getPos();
		}


		@Override
		protected void onUpdate()
		{
			if (curPos.distanceTo(getPos()) > 50)
			{
				curPos = getPos();
			}
			if (Geometry.getPenaltyAreaOur().isPointInShapeOrBehind(getBall().getPos()))
			{
				skill.updateDestination(keepDistanceToBall.findNextFreeDest(getAiFrame(), curPos, getBotID()));
			} else
			{
				pointChecker.checkPointFreeOfBotsExceptFor(destination);
				if (keepDistanceToBall.isOk(getAiFrame(), destination, getBotID()))
				{
					skill.updateDestination(destination);
				} else
				{
					// Search for next free pos, starting at the current pos to avoid driving through the placement beam
					skill.updateDestination(keepDistanceToBall.findNextFreeDest(getAiFrame(), getPos(), getBotID()));
				}
			}

			skill.updateLookAtTarget(getBall());
		}


		private boolean canOpponentGetBallInPenArea(final IVector2 point)
		{
			// allow opponents to pass through the defense when the ball is inside the penArea
			if (!Geometry.getPenaltyAreaOur().isPointInShapeOrBehind(getBall().getPos()))
			{
				return true;
			}
			double distance = RuleConstraints.getStopRadius();
			return getWFrame().getBots().values().stream()
					.filter(bot -> !bot.getBotId().getTeamColor().equals(getBotID().getTeamColor()))
					.noneMatch(bot -> bot.getPos().distanceTo(point) < distance);
		}
	}

	private class MoveToPenAreaState extends MoveState
	{
		@Override
		protected void onUpdate()
		{
			skill.updateTargetAngle(getTargetAngle());
			skill.updateDestination(validFinalDestination(destination));
			skill.getMoveCon().setPenaltyAreaOurObstacle(!getAiFrame().getGameState().isStoppedGame());
			skill.getMoveCon().setIgnoredBots(nonCloseCompanions());
		}


		private double getTargetAngle()
		{
			return getPos().subtractNew(Geometry.getGoalOur().getCenter()).getAngle();
		}


		private boolean reachedPenArea()
		{
			double margin = Geometry.getBotRadius() + moveToPenAreaMargin;
			return Geometry.getPenaltyAreaOur().withMargin(margin).isPointInShape(getPos());
		}


		private Set<BotID> nonCloseCompanions()
		{
			double marginToCompanions = Geometry.getBotRadius() * 2 + 10;
			return getAiFrame().getPlayStrategy().getActiveRoles(ERole.DEFENDER_PEN_AREA).stream()
					.filter(r -> r.getPos().distanceTo(getPos()) > marginToCompanions)
					.map(ARole::getBotID).collect(Collectors.toUnmodifiableSet());
		}
	}

	private class MoveOnPenAreaState extends RoleState<MoveOnShapeBoundarySkill>
	{
		MoveOnPenAreaState()
		{
			super(MoveOnShapeBoundarySkill::new);
		}


		@Override
		protected void onUpdate()
		{
			final IVector2 finalDestination = validFinalDestination(destination);
			skill.setDestination(finalDestination);
			skill.setPenAreaBoundary(penAreaBoundary);
			skill.setKickParams(calcKickParams());
			skill.setCompanions(companions());
			skill.getMoveCon().setIgnoredBots(ignoredBots());
			skill.getMoveCon().setObstacleAvoidanceMode(EObstacleAvoidanceMode.AGGRESSIVE);

			getShapes(DEFENSE_PENALTY_AREA_ROLE).add(
					new DrawableLine(finalDestination, getPos(), Color.GREEN)
			);
		}


		private Set<BotID> ignoredBots()
		{
			return getWFrame().getOpponentBots().keySet().stream().filter(this::isIgnoredOpponent)
					.collect(Collectors.toUnmodifiableSet());
		}


		private boolean isIgnoredOpponent(BotID opponent)
		{
			if (getTacticalField().getOpponentPassReceiver() != null
					&& getTacticalField().getOpponentPassReceiver().getBotId().equals(opponent))
			{
				return true;
			}
			return getBall().getPos().distanceTo(getWFrame().getBot(opponent).getPos()) < 1000;
		}


		private Set<BotID> companions()
		{
			return getAiFrame().getPlayStrategy().getActiveRoles(ERole.DEFENDER_PEN_AREA).stream()
					.map(ARole::getBotID).collect(Collectors.toUnmodifiableSet());
		}
	}

	private class KickBallState extends RoleState<TouchKickSkill>
	{
		KickBallState()
		{
			super(TouchKickSkill::new);
		}


		@Override
		public void onInit()
		{
			var target = Vector2.fromXY(
					Geometry.getCenter().x(),
					Math.signum(getPos().y()) * Geometry.getFieldWidth() / 2);
			skill.setTarget(target);
			skill.setPassRange(0.6);
			skill.setDesiredKickParams(KickParams.maxChip());
		}


		private boolean timeRunOut()
		{
			return !enoughTimeToKickSafely(0);
		}
	}
}
