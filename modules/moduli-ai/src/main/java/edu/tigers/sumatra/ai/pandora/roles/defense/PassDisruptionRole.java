/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.defense;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.trajectory.ITrajectory;

import java.awt.Color;
import java.util.Objects;
import java.util.Set;


/**
 * Defender that protects near an opponent (marking the threat).
 */
public class PassDisruptionRole extends ADefenseRole
{
	private IVector2 interceptionPoint;


	public PassDisruptionRole()
	{
		super(ERole.PASS_DISRUPTION_DEFENDER);

		setInitialState(new DefendState());
	}


	@Override
	protected void beforeFirstUpdate()
	{
		interceptionPoint = getTacticalField().getDefensePassDisruptionAssignment().getInterceptionPoint();
		getShapes(EAiShapesLayer.DEFENSE_PASS_DISRUPTION).add(
				new DrawableCircle(Circle.createCircle(interceptionPoint, 30), Color.MAGENTA)
		);
	}


	private class DefendState extends MoveState
	{
		@Override
		protected void onUpdate()
		{
			skill.setKickParams(calcKickParams());

			final IVector2 destination = moveToValidDest(findDest());
			skill.updateDestination(destination);
			skill.updateTargetAngle(getBall().getVel().multiplyNew(-1).getAngle());

			skill.getMoveCon().setBallObstacle(false);
			skill.getMoveCon().setIgnoredBots(Set.of());
		}


		private IVector2 findDest()
		{
			var ballLine = Lines.halfLineFromDirection(getBall().getPos(), getBall().getVel());
			// Ball travel line might change, so project the point back onto the line.
			var defaultInterceptionPoint = ballLine.closestPointOnPath(interceptionPoint);
			final IVector2 interceptionPosAdapted;
			final IVector2 fallBackInterceptPoint;
			final IVector2 interceptionPosOpponent;
			if (getAiFrame().getTacticalField().getOpponentPassReceiver() == null)
			{
				interceptionPosAdapted = defaultInterceptionPoint;
				fallBackInterceptPoint = defaultInterceptionPoint;
				interceptionPosOpponent = null;
			} else
			{
				interceptionPosOpponent = ballLine.closestPointOnPath(
						getAiFrame().getTacticalField().getOpponentPassReceiver().getBotKickerPos());
				interceptionPosAdapted = getBall().getPos()
						.addNew(Vector2.fromAngleLength(getBall().getVel().getAngle(), Math.min(
								interceptionPosOpponent.distanceTo(getBall().getPos()) - (3 * Geometry.getBotRadius()),
								defaultInterceptionPoint.distanceTo(getBall().getPos())
						)));
				fallBackInterceptPoint = ballLine.closestPointOnPath(
						getAiFrame().getTacticalField().getOpponentPassReceiver().getPos());
			}

			var ballTravelTime = getBall().getTrajectory().getTimeByPos(interceptionPosAdapted);
			var trajectory = TrajectoryGenerator.generatePositionTrajectoryToReachPointInTime(
					getBot(),
					getBot().getMoveConstraints(),
					interceptionPosAdapted,
					ballTravelTime
			);
			if (isPossible(trajectory, interceptionPosAdapted, interceptionPosOpponent, ballTravelTime))
			{
				return trajectory.getPositionMM(Double.POSITIVE_INFINITY);
			} else
			{
				// We can't disrupt the pass by catching it before the opponent so just go to the same position and annoy
				// the opponent
				if (getTacticalField().getOpponentPassReceiver() != null)
				{
					// Ignore the opponents pass receiver for path planning as we directly drive on his position we will be
					// pretty slow and won't do weird path planning stuff trying to avoid him.
					skill.getMoveCon().setIgnoredBots(Set.of(getTacticalField().getOpponentPassReceiver().getBotId()));
				}
				return fallBackInterceptPoint;
			}
		}


		private boolean isPossible(ITrajectory<IVector2> interceptTrajectory, IVector2 interceptPos,
				IVector2 interceptionPosOpponent, double ballTime)
		{
			return isFastEnough(interceptTrajectory, interceptPos, ballTime) && !willDisturbOwnTeam(interceptPos,
					interceptionPosOpponent);
		}


		private boolean isFastEnough(ITrajectory<IVector2> interceptTrajectory, IVector2 interceptPos, double ballTime)
		{
			var distanceAtIntercept = interceptTrajectory.getPositionMM(ballTime).distanceTo(interceptPos);
			return distanceAtIntercept <= (2 * Geometry.getBotRadius());
		}


		private boolean willDisturbOwnTeam(IVector2 interceptPos, IVector2 interceptionPosOpponent)
		{
			return getAiFrame().getTacticalField().getDesiredBotMap().get(EPlay.OFFENSIVE).stream()
					.map(botID -> getAiFrame().getTacticalField().getOffensiveActions().get(botID))
					.filter(Objects::nonNull)
					.map(RatedOffensiveAction::getAction)
					.filter(Objects::nonNull)
					.anyMatch(oa -> isDisturbingPosition(interceptPos, oa.getBallContactPos(),
							interceptionPosOpponent));
		}


		private boolean isDisturbingPosition(IVector2 interceptPosUs, IVector2 interceptPosOffense,
				IVector2 interceptionPosOpponent)
		{
			if (interceptPosOffense == null)
			{
				return false;
			}
			var distanceUs = interceptPosUs.distanceTo(getBall().getPos());
			var distanceOffense = interceptPosOffense.distanceTo(getBall().getPos());
			if (interceptionPosOpponent == null)
			{
				return distanceUs + 2 * Geometry.getBotRadius() < distanceOffense;
			}
			var distanceOpponent = interceptionPosOpponent.distanceTo(getBall().getPos());
			return distanceOpponent + 2 * Geometry.getBotRadius() < distanceOffense;
		}
	}
}
