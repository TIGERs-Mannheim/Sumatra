/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.defense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.DefenseMath;
import edu.tigers.sumatra.ai.pandora.plays.match.defense.CenterBackGroup;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.awt.Color;
import java.util.HashSet;
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
	private CoverMode coverMode = CoverMode.CENTER;
	@Setter
	private Set<BotID> companions = new HashSet<>();
	@Setter
	private double distanceToProtectionLine = 0.0;


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
			rushBackState.addTransition(rushBackState::isRushedBack, defendState);
			setInitialState(rushBackState);
		} else
		{
			setInitialState(defendState);
		}
	}


	private IVector2 idealProtectionPoint()
	{
		var goal = Geometry.getGoalOur();

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


		var idealDest = protectionPoint.addNew(positioningDirection.multiplyNew(distanceToProtectionLine));

		var shapes = getShapes(EAiShapesLayer.DEFENSE_CENTER_BACK);

		shapes.add(new DrawableLine(threat.getProtectionLine().orElseThrow(), Color.WHITE));
		shapes.add(new DrawableCircle(threat.getProtectionLine().orElseThrow().getStart(), 10, Color.GREEN));
		shapes.add(new DrawableCircle(threat.getProtectionLine().orElseThrow().getEnd(), 10, Color.RED));
		shapes.add(new DrawableLine(Lines.segmentFromPoints(protectionPoint, idealDest),
				distanceToProtectionLine > 0 ? Color.GREEN : Color.RED));

		return idealDest;
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
	 * Specifies the bots position
	 * Take care the order of this Enum is used in {@link CenterBackGroup#sortCoverModes(CenterBackRole, CenterBackRole)}
	 */
	@RequiredArgsConstructor
	public enum CoverMode implements Comparable<CoverMode>
	{
		RIGHT,
		CENTER_RIGHT, // Halfway between RIGHT and CENTER
		CENTER,
		CENTER_LEFT, // Halfway between LEFT and CENTER
		LEFT,

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
			leftOrRight = getPos().subtractNew(protectionLine.closestPointOnLine(getPos()))
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
			return protectionLine.closestPointOnLine(getPos())
					.addNew(normalDir.scaleToNew(leftOrRight * 300));
		}
	}
}
