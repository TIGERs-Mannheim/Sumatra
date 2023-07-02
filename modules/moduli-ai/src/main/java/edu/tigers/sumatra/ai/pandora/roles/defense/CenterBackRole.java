/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.defense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseThreatType;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Protects a threat on the protection line and try to cover the whole goal.
 */
public class CenterBackRole extends AOuterDefenseRole
{
	@Configurable(comment = "Distance [mm] from bot pos to final dest at which the rush back state switches to normal defend.", defValue = "2000.0")
	private static double rushBackToDefendSwitchThreshold = 2000.0;

	@Configurable(comment = "Start with rushing back until close to dest", defValue = "true")
	private static boolean useRushBackState = true;

	@Setter
	@Getter
	private CoverMode coverMode = null;
	@Setter
	private Set<BotID> companions = new HashSet<>();
	@Setter
	private Set<BotID> closeCompanions = new HashSet<>();
	@Setter
	private double distanceToProtectionLine = 0.0;
	@Setter
	private IVector2 idealProtectionPoint;


	/**
	 * Creates a new CenterBackRole to protect the goal from the given threat
	 */
	public CenterBackRole()
	{
		super(ERole.CENTER_BACK);

		var defendState = new DefendState();

		if (useRushBackState)
		{
			var rushBackState = new RushBackState();
			rushBackState.addTransition("rush back", rushBackState::isRushedBack, defendState);
			setInitialState(rushBackState);
		} else
		{
			setInitialState(defendState);
		}
	}


	private boolean isBlockedByOwnBot(final IVector2 idealProtectionDest, final double backOffDistance)
	{
		List<ITrackedBot> consideredBots;

		if (threat.getType() == EDefenseThreatType.BALL)
		{
			consideredBots = getWFrame().getTigerBotsVisible().values().stream()
					.filter(b -> !companions.contains(b.getBotId()))
					.filter(b -> !getTacticalField().getDesiredBotMap().get(EPlay.SUPPORT).contains(b.getBotId()))
					.filter(b -> !getTacticalField().getDesiredBotMap().get(EPlay.DEFENSIVE).contains(b.getBotId()))
					.toList();
		} else
		{
			consideredBots = getWFrame().getTigerBotsVisible().values().stream()
					.filter(b -> !companions.contains(b.getBotId()))
					.filter(b -> !getTacticalField().getDesiredBotMap().get(EPlay.SUPPORT).contains(b.getBotId()))
					.toList();
		}
		return consideredBots.stream().anyMatch(b -> b.getPos().distanceTo(idealProtectionDest) < backOffDistance);
	}


	private IVector2 makeRoomForAttacker(final double backOffDistance)
	{
		final ILineSegment protectionLine = protectionLine();
		final IVector2 projectedBallPosOnProtectionLine = protectionLine.closestPointOnPath(getBall().getPos());
		final IVector2 backOffPoint = projectedBallPosOnProtectionLine.addNew(
				protectionLine.directionVector().scaleToNew(backOffDistance));
		return idealProtectionDest(protectionLine.closestPointOnPath(backOffPoint));
	}


	private IVector2 idealProtectionDest(IVector2 protectionPoint)
	{
		IVector2 positioningDirection = threat.getProtectionLine()
				.orElseThrow(IllegalStateException::new)
				.directionVector()
				.getNormalVector();


		var idealDest = protectionPoint.addNew(positioningDirection.scaleToNew(distanceToProtectionLine));

		var shapes = getShapes(EAiShapesLayer.DEFENSE_CENTER_BACK);

		shapes.add(new DrawableLine(threat.getProtectionLine().orElseThrow(), Color.WHITE));
		shapes.add(new DrawableCircle(threat.getProtectionLine().orElseThrow().getPathStart(), 10, Color.GREEN));
		shapes.add(new DrawableCircle(threat.getProtectionLine().orElseThrow().getPathEnd(), 10, Color.RED));
		shapes.add(new DrawableLine(protectionPoint, idealDest, distanceToProtectionLine > 0 ? Color.GREEN : Color.RED));

		return idealDest;
	}


	@Override
	protected IVector2 findDest()
	{
		final IVector2 idealProtectionDest = idealProtectionDest(idealProtectionPoint);
		final double backOffDistance = Geometry.getBotRadius() * 3;
		if (isBlockedByOwnBot(idealProtectionDest, backOffDistance))
		{
			return makeRoomForAttacker(backOffDistance);
		}
		return adaptToThreat(idealProtectionDest);
	}


	private IVector2 adaptToThreat(IVector2 currentDest)
	{
		final ITrackedBot opponentPassReceiver = getAiFrame().getTacticalField().getOpponentPassReceiver();
		final IVector2 predictedPos = mimicThreatVelocity(currentDest);
		if (!threat.getObjectId().isBall() || opponentPassReceiver == null)
		{
			return predictedPos;
		}
		// the protection line is already based on the opponent pass receiver, so no additional predictions needed
		return currentDest;
	}


	@Override
	protected Set<BotID> ignoredBots(IVector2 dest)
	{
		if (closeCompanions.contains(getBotID()))
		{
			return companions.stream()
					.filter(id -> !id.equals(getBotID()))
					.filter(closeCompanions::contains)
					.filter(this::botIsMoving)
					.collect(Collectors.toUnmodifiableSet());
		} else
		{
			return Set.of();
		}
	}


	private boolean botIsMoving(final BotID id)
	{
		return getWFrame().getBot(id).getVel().getLength2() > 0.5;
	}


	/**
	 * Specifies the bots position
	 * Take care the order of this Enum, it may be used for sorting.
	 */
	@RequiredArgsConstructor
	public enum CoverMode implements Comparable<CoverMode>
	{
		RIGHT_2_5,
		RIGHT_2,
		RIGHT_1_5,
		RIGHT_1,
		RIGHT_0_5, // Halfway between RIGHT_1 and CENTER
		CENTER,
		LEFT_0_5, // Halfway between LEFT_1 and CENTER
		LEFT_1,
		LEFT_1_5,
		LEFT_2,
		LEFT_2_5,
	}

	/**
	 * Rushing back from the front to the back of the field, using fastMove mode.
	 */
	private class RushBackState extends MoveState
	{
		private double leftOrRight;
		@Getter
		private boolean rushedBack;


		@Override
		protected void onInit()
		{
			skill.getMoveConstraints().setFastMove(true);

			ILineSegment protectionLine = protectionLine();

			IVector2 normalDir = protectionLine.directionVector().getNormalVector();
			leftOrRight = getPos().subtractNew(protectionLine.closestPointOnPath(getPos()))
					.angleToAbs(normalDir).orElse(0.0) > AngleMath.PI_HALF ? -1 : 1;
		}


		@Override
		protected void onUpdate()
		{
			skill.getMoveCon().setBallObstacle(isNotBetweenBallAndGoal());

			IVector2 finalDest = findRushBackDest();

			skill.updateDestination(moveToValidDest(finalDest));
			skill.updateTargetAngle(protectionTargetAngle());

			rushedBack = rushedBack(finalDest);
		}


		private boolean rushedBack(IVector2 finalDest)
		{
			return finalDest.distanceTo(getPos()) < rushBackToDefendSwitchThreshold;
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
			return protectionLine.closestPointOnPath(getPos())
					.addNew(normalDir.scaleToNew(leftOrRight * 300));
		}
	}
}
