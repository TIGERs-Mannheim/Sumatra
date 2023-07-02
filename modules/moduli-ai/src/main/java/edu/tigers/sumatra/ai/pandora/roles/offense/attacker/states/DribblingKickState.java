/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker.states;


import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.dribble.DribbleToPos;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRangeRater;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.penarea.FinisherMoveShape;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.obstacles.FinisherMoveShapeObstacle;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;
import edu.tigers.sumatra.skillsystem.skills.dribbling.DribbleKickSkill;
import edu.tigers.sumatra.time.TimestampTimer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class DribblingKickState extends AAttackerRoleState<DribbleKickSkill>
{
	private DribbleToPos lastValidDribbleToPos = null;

	private final TimestampTimer canShootClearanceTimer = new TimestampTimer(0.10);

	private boolean isMovingOutside = false;

	@Configurable(defValue = "-20.0")
	private static double isInsideObstacleShapeOffset = -20;


	public DribblingKickState(AttackerRole role)
	{
		super(DribbleKickSkill::new, role, EAttackerState.DRIBBLING_KICK);
	}


	@Override
	protected boolean isNecessaryDataAvailable()
	{
		return getRole().getAction().getKick() != null;
	}


	@Override
	protected void doStandardUpdate()
	{
		IVector2 botStatePos = getRole().getBot().getBotState().getPos();
		double botStateOrient = getRole().getBot().getBotState().getOrientation();
		IVector2 botStateKickerPos = botStatePos.addNew(Vector2.fromAngle(botStateOrient)
				.scaleTo(getRole().getBot().getCenter2DribblerDist()));
		Optional<IRatedTarget> bestGoalKick = getRatedTarget(botStateKickerPos);

		boolean canScoreGoalHere =
				bestGoalKick.isPresent() && canGoalBeScored(bestGoalKick.get(), canShootClearanceTimer);
		var action = getRole().getAction();
		if (action.getDribbleToPos() != null && action.getKick() != null)
		{
			var shape = action.getDribbleToPos().getDribbleKickData().getShape();
			FinisherMoveShapeObstacle obstacleShape = getFinisherMoveShapeObstacle(shape);

			if (obstacleShape.getShape().withMargin(Geometry.getBotRadius() + isInsideObstacleShapeOffset)
					.isPointInShape(getRole().getPos()))
			{
				moveOutwardsOfObstacle(action, shape);
			} else
			{
				isMovingOutside = false;
				skill.setTarget(action.getKick().getTarget());
				skill.setDestination(action.getDribbleToPos().getDribbleToDestination());

				List<IObstacle> obstacles = new ArrayList<>();
				obstacles.add(obstacleShape);
				skill.getMoveCon().setCustomObstacles(obstacles);

				bestGoalKick.ifPresent(e -> doDribbleSkill(botStateKickerPos, e, canScoreGoalHere));

				boolean isUnavoidable = action.getDribbleToPos().getDribbleKickData().isViolationUnavoidable();
				boolean isImminent = action.getDribbleToPos().getDribbleKickData().isViolationImminent();
				getRole().getShapes(EAiShapesLayer.OFFENSIVE_FINISHER)
						.add(new DrawableAnnotation(getRole().getPos(), "Is unavoidable: " + isUnavoidable,
								Vector2.fromY(250)).setColor(Color.ORANGE));
				getRole().getShapes(EAiShapesLayer.OFFENSIVE_FINISHER)
						.add(new DrawableAnnotation(getRole().getPos(), "Is imminent: " + isImminent,
								Vector2.fromY(300)).setColor(Color.ORANGE));
				lastValidDribbleToPos = action.getDribbleToPos();
				skill.setKickIfTargetOrientationReached(canScoreGoalHere || isUnavoidable);
				getRole().getShapes(EAiShapesLayer.OFFENSIVE_FINISHER)
						.add(new DrawableAnnotation(getRole().getPos(), "can score now: " + canScoreGoalHere,
								Vector2.fromY(340)).setColor(Color.ORANGE));
				skill.setForceKick(isImminent);


			}
		} else if (lastValidDribbleToPos != null)
		{
			skill.setKickIfTargetOrientationReached(canScoreGoalHere);
		}
	}


	private Optional<IRatedTarget> getRatedTarget(IVector2 botStateKickerPos)
	{
		var rater = AngleRangeRater.forGoal(Geometry.getGoalTheir());
		rater.setObstacles(getRole().getWFrame().getOpponentBots().values());
		return rater.rate(botStateKickerPos);
	}


	private void doDribbleSkill(IVector2 botStateKickerPos, IRatedTarget bestGoalKick, boolean canScoreGoalHere)
	{
		IVector2 targetArrowDir = bestGoalKick.getTarget().subtractNew(botStateKickerPos);
		getRole().getShapes(EAiShapesLayer.OFFENSIVE_DRIBBLE)
				.add(new DrawableArrow(botStateKickerPos, targetArrowDir,
						Color.PINK));
		if (canScoreGoalHere)
		{
			skill.setTarget(bestGoalKick.getTarget());
		}
	}


	private void moveOutwardsOfObstacle(OffensiveAction action, FinisherMoveShape shape)
	{
		// move outwards first
		List<IObstacle> obstacles = new ArrayList<>();
		skill.getMoveCon().setCustomObstacles(obstacles);

		isMovingOutside = true;
		IVector2 nearestPointOnShapeBorder = shape.stepOnShape(shape.getStepPositionOnShape(getRole().getPos()));
		skill.setDestination(nearestPointOnShapeBorder);
		skill.setTarget(action.getKick().getTarget());
	}


	private FinisherMoveShapeObstacle getFinisherMoveShapeObstacle(FinisherMoveShape shape)
	{
		if (isMovingOutside)
		{
			return new FinisherMoveShapeObstacle(shape.withMargin(-100));
		}
		return new FinisherMoveShapeObstacle(shape.withMargin(-145));
	}


	private boolean canGoalBeScored(IRatedTarget bestGoalKick, TimestampTimer timer)
	{
		var hasGoodScoreChance = bestGoalKick.getScore() > 0.6;
		timer.update(getRole().getWFrame().getTimestamp());
		if (!timer.isRunning() && hasGoodScoreChance)
		{
			timer.start(getRole().getWFrame().getTimestamp());
		} else if (!hasGoodScoreChance)
		{
			timer.reset();
		}
		return timer.isTimeUp(getRole().getWFrame().getTimestamp());
	}


	@Override
	protected void doFallbackUpdate()
	{
		// no fallback
	}
}
