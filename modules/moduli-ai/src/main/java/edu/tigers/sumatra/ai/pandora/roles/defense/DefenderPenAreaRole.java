/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
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
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.v2.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.skillsystem.skills.MoveOnPenaltyAreaSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.skillsystem.skills.util.PenAreaBoundary;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.ITrackedBot;

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

	private boolean allowedToKickBall = false;
	/*
	 * destination must be initialized, because the defense group will trigger an update through switchRoles before
	 * the destination is initially set
	 */
	private IVector2 destination = Geometry.getPenaltyMarkOur();
	private PenAreaBoundary penAreaBoundary = PenAreaBoundary
			.ownWithMargin(Geometry.getBotRadius() + Geometry.getPenaltyAreaMargin());

	private enum EEvent implements IEvent
	{
		REACHED_PEN_AREA,
		LEFT_PEN_AREA,
		KICK_BALL,
		SITUATION_IS_DANGEROUS,
		STOP_GAME
	}


	public DefenderPenAreaRole()
	{
		super(ERole.DEFENDER_PEN_AREA);

		IState moveToPenArea = new MoveToPenAreaState();
		IState moveOnPenArea = new MoveOnPenAreaState();
		IState kickBall = new KickBallState();
		IState keepDistanceToBall = new KeepDistanceToBallState();

		addTransition(EEvent.REACHED_PEN_AREA, moveOnPenArea);
		addTransition(kickBall, EEvent.SITUATION_IS_DANGEROUS, moveOnPenArea);
		addTransition(EEvent.LEFT_PEN_AREA, moveToPenArea);
		addTransition(moveOnPenArea, EEvent.KICK_BALL, kickBall);
		addTransition(EEvent.STOP_GAME, keepDistanceToBall);

		// start with going straight to the target position
		setInitialState(moveToPenArea);
	}


	/**
	 * can be called from play to set the destination of this role
	 *
	 * @param destination
	 */
	public void setDestination(final IVector2 destination)
	{
		this.destination = destination;
	}


	public void setPenAreaBoundary(final PenAreaBoundary penAreaBoundary)
	{
		this.penAreaBoundary = penAreaBoundary;
	}


	public boolean isAllowedToKickBall()
	{
		return allowedToKickBall;
	}


	public void setAllowedToKickBall(final boolean allowedToKickBall)
	{
		this.allowedToKickBall = allowedToKickBall;
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
		final double botBallRadius = Geometry.getBotRadius() + Geometry.getBallRadius();
		final PenAreaBoundary adaptedPenArea = penAreaBoundary.withMargin(-botBallRadius);
		if (getBot().getVel().getLength2() > 0.5)
		{
			for (ITrackedBot bot : getWFrame().getOpponentBots().values())
			{
				if (bot.getPos().distanceTo(intermediatePos) < Geometry.getBotRadius() * 1.5)
				{
					return penAreaBoundary
							.projectPoint(LineMath.stepAlongLine(intermediatePos, getPos(), Geometry.getBotRadius() * 1.5));
				}
			}
			if (!adaptedPenArea.isPointInShape(getBall().getPos()))
			{
				final IVector2 ballProjectedToPenArea = adaptedPenArea.projectPoint(getBall().getPos());
				final double ballToPenAreaDist = ballProjectedToPenArea.distanceTo(getBall().getPos());

				if (intermediatePos.distanceTo(getBall().getPos()) < botBallRadius)
				{
					// ball is inside destination. Better not drive onto ball
					return adaptedPenArea.withMargin(ballToPenAreaDist + botBallRadius).projectPoint(intermediatePos);
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
		return Geometry.getPenaltyAreaOur().isPointInShape(getWFrame().getBall().getPos(), margin);
	}


	private boolean lostBallResponsibility()
	{
		return getAiFrame().getTacticalField().getBallResponsibility() != EBallResponsibility.DEFENSE;
	}


	private class KeepDistanceToBallState extends AState
	{
		private MoveToSkill skill;
		private final PointChecker pointChecker = new PointChecker()
				.checkBallDistances()
				.checkInsideField()
				.checkCustom(this::canOpponentGetBallInPenArea);
		private final KeepDistanceToBall keepDistanceToBall = new KeepDistanceToBall(pointChecker);


		@Override
		public void doEntryActions()
		{
			skill = MoveToSkill.createMoveToSkill();
			setNewSkill(skill);
		}


		@Override
		public void doUpdate()
		{
			skill.getMoveCon().setPenaltyAreaOurObstacle(false);

			pointChecker.checkPointFreeOfBotsExceptFor(destination);
			if (keepDistanceToBall.isOk(getAiFrame(), destination, getBotID()))
			{
				skill.updateDestination(destination);
			} else
			{
				// Search for next free pos, starting at the current pos to avoid driving through the placement beam
				skill.updateDestination(keepDistanceToBall.findNextFreeDest(getAiFrame(), getPos(), getBotID()));
			}

			skill.updateLookAtTarget(getBall());

			if (!getAiFrame().getGameState().isStoppedGame())
			{
				triggerEvent(EEvent.LEFT_PEN_AREA);
			}
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
					.filter(bot -> bot.getBotId().getTeamColor() != getBotID().getTeamColor())
					.noneMatch(bot -> bot.getPos().distanceTo(point) < distance);
		}
	}

	private class MoveToPenAreaState extends AState
	{
		private MoveToSkill skill;


		@Override
		public void doEntryActions()
		{
			skill = MoveToSkill.createMoveToSkill();
			setNewSkill(skill);
		}


		@Override
		public void doUpdate()
		{
			if (getAiFrame().getGameState().isStoppedGame())
			{
				triggerEvent(EEvent.STOP_GAME);
			} else if (Geometry.getPenaltyAreaOur().isPointInShape(getPos(),
					Geometry.getBotRadius() + moveToPenAreaMargin))
			{
				triggerEvent(EEvent.REACHED_PEN_AREA);
			}

			double targetAngle = getPos().subtractNew(Geometry.getGoalOur().getCenter()).getAngle();
			skill.getMoveCon().setPenaltyAreaOurObstacle(!getAiFrame().getGameState().isStoppedGame());
			skill.updateTargetAngle(targetAngle);
			skill.updateDestination(validFinalDestination(destination));
			skill.getMoveCon().setIgnoredBots(nonCloseCompanions());
		}


		private Set<BotID> nonCloseCompanions()
		{
			double marginToCompanions = Geometry.getBotRadius() * 2 + 10;
			return getAiFrame().getPlayStrategy().getActiveRoles(ERole.DEFENDER_PEN_AREA).stream()
					.filter(r -> r.getPos().distanceTo(getPos()) > marginToCompanions)
					.map(ARole::getBotID).collect(Collectors.toSet());
		}
	}

	private class MoveOnPenAreaState extends AState
	{
		private MoveOnPenaltyAreaSkill moveOnPenAreaSkill;


		@Override
		public void doEntryActions()
		{
			moveOnPenAreaSkill = new MoveOnPenaltyAreaSkill();
			setNewSkill(moveOnPenAreaSkill);
		}


		@Override
		public void doUpdate()
		{
			final IVector2 finalDestination = validFinalDestination(destination);
			moveOnPenAreaSkill.setDestination(finalDestination);
			moveOnPenAreaSkill.setPenAreaBoundary(penAreaBoundary);
			moveOnPenAreaSkill.setKickParams(calcKickParams());

			getShapes(DEFENSE_PENALTY_AREA_ROLE)
					.add(new DrawableLine(Line.fromPoints(finalDestination, getPos()), Color.GREEN));
			if (getAiFrame().getGameState().isStoppedGame())
			{
				triggerEvent(EEvent.STOP_GAME);
			} else if (allowedToKickBall && enoughTimeToKickSafely(slackTimeHyst) && !situationIsDangerous())
			{
				triggerEvent(EEvent.KICK_BALL);
			}
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


		@Override
		public void onUpdate()
		{
			if (getAiFrame().getGameState().isStoppedGame())
			{
				triggerEvent(EEvent.STOP_GAME);
			} else if (situationIsDangerous() || timeRunOut())
			{
				triggerEvent(EEvent.SITUATION_IS_DANGEROUS);
			}
		}


		private boolean timeRunOut()
		{
			return !enoughTimeToKickSafely(0);
		}
	}
}
