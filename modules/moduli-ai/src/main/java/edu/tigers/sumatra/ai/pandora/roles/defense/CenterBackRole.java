/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.defense;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.defense.DefenseMath;
import edu.tigers.sumatra.ai.metis.defense.data.IDefenseThreat;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.Goal;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Protects a threat on the protection line and try to cover the whole goal.
 */
public class CenterBackRole extends AOuterDefenseRole
{
	@Configurable(comment = "The space between the bots (actual distance = configured distance + bot diameter)", defValue = "20.0")
	private static double distanceBetweenBots = 20.0;
	@Configurable(comment = "Max distance that a robot may be away from protection line, before switching to intercept", defValue = "500.0")
	private static double switchToInterceptStateDist = 500.0;
	@Configurable(comment = "Min distance that a robot must be away from protection line, before switching to protection", defValue = "20.0")
	private static double switchToProtectStateDist = 20.0;

	@Configurable(comment = "Distance [mm] from bot pos to final dest at which the rush back state switches to normal defend.", defValue = "2000.0")
	private static double rushBackToDefendSwitchThreshold = 2000.0;

	@Configurable(comment = "After rushing back, use a single defend state instead of combination of intercept and protect.", defValue = "true")
	private static boolean useSingleDefendState = true;

	@Configurable(comment = "Start with rushing back until close to dest", defValue = "true")
	private static boolean useRushBackState = true;

	private CoverMode coverMode = CoverMode.CENTER;
	private Set<BotID> companions = new HashSet<>();


	/**
	 * Creates a new CenterBackRole to protect the goal from the given threat
	 *
	 * @param threat The threat
	 */
	public CenterBackRole(final IDefenseThreat threat)
	{
		super(ERole.CENTER_BACK, threat);

		addTransition(EEvent.PROTECTION_LINE_LEFT, new CenterInterceptState());
		addTransition(EEvent.PROTECTION_LINE_REACHED, new ProtectionState());

		if (useSingleDefendState)
		{
			addTransition(EEvent.RUSHED_BACK, new DefendState());
		} else
		{
			addTransition(EEvent.RUSHED_BACK, new CenterInterceptState());
		}

		if (useRushBackState)
		{
			setInitialState(new RushBackState());
		} else if (useSingleDefendState)
		{
			setInitialState(new DefendState());
		} else
		{
			setInitialState(new CenterInterceptState());
		}
	}


	public void setCoverMode(final CoverMode coverMode)
	{
		this.coverMode = coverMode;
	}


	public void setCompanions(final Set<BotID> companions)
	{
		this.companions = companions;
	}


	private IVector2 idealProtectionPoint()
	{
		Goal goal = Geometry.getGoalOur();

		IVector2 idealProtectionPoint = DefenseMath.calculateLineDefPoint(
				threat.getPos(),
				goal.getLeftPost(),
				goal.getRightPost(),
				Geometry.getBotRadius() * companions.size());

		ILineSegment protectionLine = protectionLine();
		return protectionLine.closestPointOnLine(idealProtectionPoint);
	}


	private boolean isBlockedByOwnBot(final IVector2 idealProtectionDest, final double backOffDistance)
	{
		return getWFrame().getTigerBotsVisible().values().stream()
				.filter(b -> !companions.contains(b.getBotId()))
				.anyMatch(b -> b.getPos().distanceTo(idealProtectionDest) < backOffDistance);
	}


	private IVector2 makeRoomForAttacker(final double backOffDistance)
	{
		final ILineSegment protectionLine = protectionLine();
		final IVector2 projectedBallPosOnProtectionLine = protectionLine.closestPointOnLine(getBall().getPos());
		final IVector2 backOffPoint = projectedBallPosOnProtectionLine.addNew(
				protectionLine.directionVector().scaleToNew(backOffDistance));
		return idealProtectionDest(protectionLine.closestPointOnLine(backOffPoint));
	}


	private IVector2 idealProtectionDest(IVector2 protectionPoint)
	{
		IVector2 positioningDirection = threat.getProtectionLine()
				.orElseThrow(IllegalStateException::new)
				.directionVector()
				.getNormalVector()
				.normalizeNew();

		double distance = (Geometry.getBotRadius() * 2) + distanceBetweenBots;
		double positioningDistance = getDistanceToProtectionLine(distance);

		return protectionPoint.addNew(
				positioningDirection.multiplyNew(positioningDistance));
	}


	private double getDistanceToProtectionLine(final double distance)
	{
		if (singleProtector())
		{
			return 0.0;
		}
		switch (coverMode)
		{
			case LEFT:
				return distance;
			case RIGHT:
				return -distance;
			case CENTER_LEFT:
				return distance / 2;
			case CENTER_RIGHT:
				return -(distance / 2);
			case CENTER:
				return 0.0;
			default:
				throw new IllegalStateException("Unknown CoverMode!");
		}
	}


	private boolean singleProtector()
	{
		double timeToDest = getBot().getRobotInfo().getTrajectory().map(ITrajectory::getTotalTime).orElse(0.0);
		double timeDiff = 1;
		return companions.stream()
				.filter(id -> id != getBotID())
				.map(id -> getWFrame().getBot(id).getRobotInfo().getTrajectory())
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(ITrajectory::getTotalTime)
				.allMatch(t -> (t - timeToDest) > timeDiff); // true, if all need more than `timeDiff` longer
	}


	@Override
	protected IVector2 findDest()
	{
		final IVector2 protectionPoint = idealProtectionPoint();
		final IVector2 idealProtectionDest = idealProtectionDest(protectionPoint);
		final double backOffDistance = Geometry.getBotRadius() * 3;
		if (isBlockedByOwnBot(idealProtectionDest, backOffDistance))
		{
			return makeRoomForAttacker(backOffDistance);
		}
		return adaptToThreat(idealProtectionDest);
	}


	private IVector2 adaptToThreat(IVector2 currentDest)
	{
		final Optional<ITrackedBot> opponentPassReceiver = getAiFrame().getTacticalField().getOpponentPassReceiver();
		final IVector2 predictedPos = mimicThreatVelocity(currentDest);
		if (!threat.getObjectId().isBall() || !opponentPassReceiver.isPresent())
		{
			return predictedPos;
		}
		// the protection line is already based on the opponent pass receiver, so no additional predictions needed
		return currentDest;
	}


	@Override
	protected Set<BotID> ignoredBots(IVector2 dest)
	{
		final Set<BotID> bots = closeOpponentBots(dest);
		bots.addAll(companions.stream()
				.filter(id -> id != getBotID())
				// if companions are moving, we ignore them
				// if they are still, we respect them to avoid getting stuck at each other
				.filter(this::botIsMoving)
				.collect(Collectors.toSet()));
		return bots;
	}


	private boolean botIsMoving(final BotID id)
	{
		return getWFrame().getBot(id).getVel().getLength2() > 0.5;
	}


	/**
	 * Rushing back from the front to the back of the field, using fastMove mode.
	 */
	private class RushBackState extends AState
	{
		private AMoveToSkill skill;
		private double leftOrRight;


		@Override
		public void doEntryActions()
		{
			skill = AMoveToSkill.createMoveToSkill();
			skill.getMoveCon().getMoveConstraints().setFastMove(true);
			setNewSkill(skill);

			ILineSegment protectionLine = protectionLine();

			IVector2 normalDir = protectionLine.directionVector().getNormalVector();
			leftOrRight = getPos().subtractNew(protectionLine.closestPointOnLine(getPos()))
					.angleToAbs(normalDir).orElse(0.0) > AngleMath.PI_HALF ? -1 : 1;
		}


		@Override
		public void doUpdate()
		{
			skill.getMoveCon().setBallObstacle(isBehindBall());

			IVector2 finalDest = findRushBackDest();

			skill.getMoveCon().updateDestination(moveToValidDest(finalDest));
			skill.getMoveCon().updateTargetAngle(protectionTargetAngle());

			if (finalDest.distanceTo(getPos()) < rushBackToDefendSwitchThreshold)
			{
				triggerEvent(EEvent.RUSHED_BACK);
			}
		}


		/**
		 * Get a destination next to the protection line so that the robot does not have to drive around its
		 * assigned threat while rushing back.
		 *
		 * @return
		 */
		private IVector2 findRushBackDest()
		{
			ILineSegment protectionLine = protectionLine();
			IVector2 normalDir = protectionLine.directionVector().getNormalVector();
			return protectionLine.closestPointOnLine(getPos())
					.addNew(normalDir.scaleToNew(leftOrRight * 300));
		}
	}

	/**
	 * Protect on the protection line
	 */
	private class ProtectionState extends AState
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
			final IVector2 destination = moveToValidDest(adaptToThreat(findDest()));
			skill.getMoveCon().updateDestination(destination);
			skill.getMoveCon().updateTargetAngle(protectionTargetAngle());

			skill.getMoveCon().setIgnoredBots(ignoredBots(destination));
			skill.getMoveCon().setCustomObstacles(closeOpponentBotObstacles(destination));
			skill.getMoveCon().setBallObstacle(isBehindBall());
			skill.getMoveCon().getMoveConstraints().setPrimaryDirection(threat.getThreatLine().directionVector());
			armDefenders(skill);

			if (protectionLine().distanceTo(getPos()) > switchToInterceptStateDist)
			{
				triggerEvent(EEvent.PROTECTION_LINE_LEFT);
			}
		}
	}

	/**
	 * Intercept protection line when not close to it.
	 */
	private class CenterInterceptState extends AState
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
			final IVector2 destination = moveToValidDest(adaptToThreat(calcDest()));
			skill.getMoveCon().updateDestination(destination);
			skill.getMoveCon().updateTargetAngle(protectionTargetAngle());

			skill.getMoveCon().setIgnoredBots(ignoredBots(destination));
			skill.getMoveCon().setCustomObstacles(closeOpponentBotObstacles(destination));
			skill.getMoveCon().setBallObstacle(isBehindBall());
			skill.getMoveCon().getMoveConstraints().setPrimaryDirection(protectionLine().directionVector());

			if (protectionLine().distanceTo(getPos()) < switchToProtectStateDist)
			{
				triggerEvent(EEvent.PROTECTION_LINE_REACHED);
			}
		}


		private IVector2 calcDest()
		{
			final IVector2 protectionPoint = idealProtectionPoint();
			final IVector2 closestPointToProtectionLine = protectionLine().closestPointOnLine(getPos());

			if (companions.size() == 1)
			{
				return closestPointToProtectionLine;
			} else
			{
				final Optional<BotID> closestCompanion = companions.stream()
						.min(Comparator.comparingDouble(b -> getWFrame().getBot(b).getPos().distanceToSqr(protectionPoint)));
				if (closestCompanion.isPresent() && closestCompanion.get() == getBotID())
				{
					return protectionPoint;
				} else if (closestPointToProtectionLine.distanceTo(protectionPoint) > Geometry.getBotRadius() * 2)
				{
					return closestPointToProtectionLine;
				} else
				{
					return protectionPoint
							.addNew(protectionLine().directionVector().scaleToNew(Geometry.getBotRadius() * 3));
				}
			}
		}
	}


	/**
	 * Specifies the bots position
	 */
	public enum CoverMode
	{
		/** On the line */
		CENTER,
		/** Left from CENTER */
		LEFT,
		/** Right from CENTER */
		RIGHT,
		/** Left position if two bots are assigned to center */
		CENTER_LEFT,
		/** Right position if two bots are assigned to center */
		CENTER_RIGHT
	}


	private enum EEvent implements IEvent
	{
		PROTECTION_LINE_LEFT,
		PROTECTION_LINE_REACHED,
		RUSHED_BACK,
	}
}
