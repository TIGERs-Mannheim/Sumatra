/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.defense;

import static edu.tigers.sumatra.ai.metis.EAiShapesLayer.DEFENSE_PENALTY_AREA_ROLE;

import java.awt.Color;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.ballresponsibility.EBallResponsibility;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveOnPenaltyAreaSkill;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.skillsystem.skills.util.PenAreaBoundary;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.DynamicPosition;


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
	private IVector2 destination = Vector2.zero();
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
		double minFoeArrivalTime = getWFrame().getFoeBots().values().stream()
				.mapToDouble(bot -> TrajectoryGenerator.generatePositionTrajectory(bot, target).getTotalTime())
				.min()
				.orElse(1000000);
		double myTime = TrajectoryGenerator.generatePositionTrajectory(getBot(), target).getTotalTime();
		double slackTime = minFoeArrivalTime - myTime - requiredMinimumSlackTime - additionalRequiredTime;
		return slackTime > 0;
	}

private IVector2 validFinalDestination(final IVector2 intermediatePos)
	{
		final double botBallRadius = Geometry.getBotRadius() + Geometry.getBallRadius();
		final PenAreaBoundary adaptedPenArea = penAreaBoundary.withMargin(-botBallRadius);
		if (!adaptedPenArea.isPointInShape(getBall().getPos()) && getBot().getVel().getLength2() > 0.5)
		{
			final IVector2 ballProjectedToPenArea = adaptedPenArea.projectPoint(getBall().getPos());
			final double ballToPenAreaDist = ballProjectedToPenArea.distanceTo(getBall().getPos());

			if (intermediatePos.distanceTo(getBall().getPos()) < botBallRadius)
			{
				// ball is inside destination. Better not drive onto ball
				return adaptedPenArea.withMargin(ballToPenAreaDist + botBallRadius).projectPoint(intermediatePos);
			}
		}

		return intermediatePos;
	}

	private class KeepDistanceToBallState extends AState
	{
		private AMoveToSkill skill;
		private final KeepDistanceToBall keepDistanceToBall = new KeepDistanceToBall();


		@Override
		public void doEntryActions()
		{
			skill = AMoveToSkill.createMoveToSkill();
			setNewSkill(skill);
		}


		@Override
		public void doUpdate()
		{
			keepDistanceToBall.update(getAiFrame(), getBotID(), destination);
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			skill.getMoveCon().updateDestination(keepDistanceToBall.freeDestination());
			skill.getMoveCon().updateLookAtTarget(getBall());

			if (!getAiFrame().getGamestate().isStoppedGame())
			{
				triggerEvent(EEvent.LEFT_PEN_AREA);
			}
		}
	}

	private class MoveToPenAreaState extends AState
	{
		private AMoveToSkill skill;


		@Override
		public void doEntryActions()
		{
			skill = AMoveToSkill.createMoveToSkill();
			setNewSkill(skill);
		}


		@Override
		public void doUpdate()
		{
			if (getAiFrame().getGamestate().isStoppedGame())
			{
				triggerEvent(EEvent.STOP_GAME);
			} else if (Geometry.getPenaltyAreaOur().isPointInShape(getPos(),
					Geometry.getBotRadius() + moveToPenAreaMargin))
			{
				triggerEvent(EEvent.REACHED_PEN_AREA);
			}

			double targetAngle = getPos().subtractNew(Geometry.getGoalOur().getCenter()).getAngle();
			skill.getMoveCon().setPenaltyAreaAllowedOur(getAiFrame().getGamestate().isStoppedGame());
			skill.getMoveCon().updateTargetAngle(targetAngle);
			skill.getMoveCon().updateDestination(validFinalDestination(destination));
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
			moveOnPenAreaSkill.updateDestination(finalDestination);
			moveOnPenAreaSkill.setPenAreaBoundary(penAreaBoundary);
			armDefenders(moveOnPenAreaSkill);

			getShapes(DEFENSE_PENALTY_AREA_ROLE)
					.add(new DrawableLine(Line.fromPoints(finalDestination, getPos()), Color.GREEN));
			if (getAiFrame().getGamestate().isStoppedGame())
			{
				triggerEvent(EEvent.STOP_GAME);
			} else if (allowedToKickBall && enoughTimeToKickSafely(slackTimeHyst))
			{
				triggerEvent(EEvent.KICK_BALL);
			}
		}
	}

	private class KickBallState extends AState
	{
		@Override
		public void doEntryActions()
		{
			IVector2 targetDest = Vector2.fromXY(Geometry.getCenter().x(),
					Math.signum(getPos().y()) * Geometry.getFieldWidth() / 2);
			setNewSkill(
					new TouchKickSkill(new DynamicPosition(targetDest, 0.6), KickParams.maxChip()));
		}


		@Override
		public void doUpdate()
		{
			if (getAiFrame().getGamestate().isStoppedGame())
			{
				triggerEvent(EEvent.STOP_GAME);
			} else if (situationIsDangerous())
			{
				triggerEvent(EEvent.SITUATION_IS_DANGEROUS);
			}
		}


		private boolean situationIsDangerous()
		{
			return ballInPenArea() || timeRunOut() || lostBallResponsibility();
		}


		private boolean ballInPenArea()
		{
			final double margin = Geometry.getBotRadius() * 2 + Geometry.getBallRadius();
			return Geometry.getPenaltyAreaOur().isPointInShape(getWFrame().getBall().getPos(), margin);
		}


		private boolean timeRunOut()
		{
			return !enoughTimeToKickSafely(0);
		}


		private boolean lostBallResponsibility()
		{
			return getAiFrame().getTacticalField().getBallResponsibility() != EBallResponsibility.DEFENSE;
		}
	}
}
