/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.defense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.EObstacleAvoidanceMode;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.Setter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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
	private List<CenterBackRole> companions = new ArrayList<>();
	@Setter
	private Set<BotID> closeCompanions = new HashSet<>();
	@Setter
	private double distanceToProtectionLine = 0.0;
	@Setter
	private double distanceToProtectionLineIntercept = 0.0;
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


	private double calcDistanceToDestination()
	{
		return getDefendDestination().getPos().distanceTo(getPos());
	}


	private IVector2 offsetPositionWithinGroup(IVector2 centerPosition, double distance)
	{
		return centerPosition.addNew(protectionLine().directionVector().getNormalVector().scaleTo(distance));
	}


	private IVector2 idealProtectionDest(IVector2 protectionPoint)
	{
		var idealDest = offsetPositionWithinGroup(protectionPoint, distanceToProtectionLine);

		var shapes = getShapes(EAiShapesLayer.DEFENSE_CENTER_BACK);
		shapes.add(new DrawableLine(threat.getProtectionLine().orElseThrow(), Color.WHITE));
		shapes.add(new DrawableCircle(threat.getProtectionLine().orElseThrow().getPathStart(), 10, Color.GREEN));
		shapes.add(new DrawableCircle(threat.getProtectionLine().orElseThrow().getPathEnd(), 10, Color.RED));
		shapes.add(new DrawableLine(protectionPoint, idealDest, distanceToProtectionLine > 0 ? Color.GREEN : Color.RED));

		return idealDest;
	}


	@Override
	protected Destination findDest()
	{
		var idealProtectionDest = idealProtectionDest(idealProtectionPoint);
		if (threat.getObjectId().isBall())
		{
			var supportingBallReceptionPos = getAiFrame().getTacticalField().getDefenseSupportingBallReceptionPos();
			if (supportingBallReceptionPos != null)
			{
				return new Destination(
						offsetPositionWithinGroup(supportingBallReceptionPos, distanceToProtectionLine), null);
			}
			if (isGoalShotInterceptNecessary(idealProtectionDest))
			{
				return interceptGoalShot(offsetPositionWithinGroup(Vector2.zero(), distanceToProtectionLineIntercept));
			}
		}
		return new Destination(adaptToThreat(idealProtectionDest), null);
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
	protected Set<BotID> ignoredBots()
	{
		if (closeCompanions.contains(getBotID()))
		{
			return Stream.concat(
							companions.stream()
									.map(ARole::getBotID)
									.filter(botID -> !botID.equals(getBotID()))
									.filter(botID -> !closeCompanions.contains(botID)),
							companions.stream()
									.filter(bot -> !bot.getBotID().equals(getBotID()))
									.filter(bot -> closeCompanions.contains(bot.getBotID()))
									.filter(this::companionIsMoving)
									.map(ARole::getBotID)
					)
					.collect(Collectors.toUnmodifiableSet());
		} else
		{
			return Set.of();
		}
	}


	@Override
	protected Optional<EObstacleAvoidanceMode> forceObstacleAvoidanceMode()
	{
		if (threat.getObjectId().isBall()
				&& getAiFrame().getTacticalField().getDefenseSupportingBallReceptionPos() != null)
		{
			return Optional.of(EObstacleAvoidanceMode.NORMAL);
		}
		return Optional.empty();
	}


	private boolean companionIsMoving(CenterBackRole companion)
	{
		var thisDistance = this.calcDistanceToDestination();
		var thisVel = this.getBot().getVel().getLength2();

		var companionDistance = companion.calcDistanceToDestination();
		var companionVel = companion.getBot().getVel().getLength2();

		return (thisDistance < Geometry.getBotRadius() && thisVel < 0.5) // We are very close -> ignore all companions
				|| companionDistance > Geometry.getBotRadius()
				|| companionVel > 0.5;
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
			if (isNotBetweenBallAndGoal())
			{
				skill.getMoveCon().setCustomObstacles(offensePassObstacles());
			} else
			{
				skill.getMoveCon().setCustomObstacles(List.of());
			}

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
