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
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.ai.metis.offense.dribble.finisher.FinisherMoveShape;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.ai.metis.offense.dribble.finisher.FinisherMoveShapeObstacle;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;
import edu.tigers.sumatra.skillsystem.skills.dribbling.DribbleKickSkill;
import edu.tigers.sumatra.time.TimestampTimer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


public class DribblingKickState extends AAttackerRoleState<DribbleKickSkill>
{
	@Configurable(defValue = "-20.0")
	private static double isInsideObstacleShapeOffset = -20;
	@Configurable(defValue = "2.0", comment = "[m/s] if a dribbling violation is imminent and we do not roughly look towards opponent goal")
	private static double violationImminentKickSpeed = 2.0;
	private final TimestampTimer canShootClearanceTimer = new TimestampTimer(0.10);
	private final TimestampTimer driveToFakePointTimer = new TimestampTimer(1.30);
	private DribbleToPos lastValidDribbleToPos = null;
	private boolean isMovingOutside = false;
	private boolean alreadyReachedFakePoint = false;


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
	public void doEntryActions()
	{
		super.doEntryActions();
		driveToFakePointTimer.reset();

		alreadyReachedFakePoint = Math.random() > 0.5;
		var action = getRole().getAction();
		if (action.getDribbleToPos().getDribbleToDestination()
				.distanceTo(getRole().getPos()) < Geometry.getBotRadius() * 4)
		{
			alreadyReachedFakePoint = true;
		}

		isMovingOutside = false;
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

				setMoveToDestination(action);

				List<IObstacle> obstacles = new ArrayList<>();
				obstacles.add(obstacleShape);

				skill.getMoveCon().setCustomObstacles(obstacles);

				bestGoalKick.ifPresent(e -> doDribbleSkill(botStateKickerPos, e, canScoreGoalHere));
				handleDribblingViolations(canScoreGoalHere, action);
			}
		} else if (lastValidDribbleToPos != null)
		{
			skill.setKickIfTargetOrientationReached(canScoreGoalHere);
		}
	}


	private void setMoveToDestination(OffensiveAction action)
	{
		driveToFakePointTimer.update(getRole().getWFrame().getTimestamp());
		if (!driveToFakePointTimer.isTimeUp(getRole().getWFrame().getTimestamp())
				&& action.getDribbleToPos().getDribbleKickData().getFakePoint() != null
				&& !alreadyReachedFakePoint)
		{
			// we will drive towards fake target to get some dribbling distance!
			IVector2 fakePoint = action.getDribbleToPos().getDribbleKickData().getFakePoint();
			skill.setDestination(fakePoint);
			getRole().getShapes(EAiShapesLayer.OFFENSE_FINISHER)
					.add(new DrawableAnnotation(getRole().getPos(), "move to fake",
							Vector2.fromY(390)).setColor(Color.ORANGE));
			if (getRole().getPos().distanceTo(fakePoint) < Geometry.getBotRadius() * 2)
			{
				alreadyReachedFakePoint = true;
			}
		} else
		{
			// do the real thing
			skill.setDestination(action.getDribbleToPos().getDribbleToDestination());
			getRole().getShapes(EAiShapesLayer.OFFENSE_FINISHER)
					.add(new DrawableAnnotation(getRole().getPos(), "move to real",
							Vector2.fromY(390)).setColor(Color.ORANGE));
		}
	}


	private void handleDribblingViolations(boolean canScoreGoalHere, OffensiveAction action)
	{
		boolean isUnavoidable = action.getDribbleToPos().getDribbleKickData().isViolationUnavoidable();
		boolean isImminent = action.getDribbleToPos().getDribbleKickData().isViolationImminent();
		getRole().getShapes(EAiShapesLayer.OFFENSE_FINISHER)
				.add(new DrawableAnnotation(getRole().getPos(), "Is unavoidable: " + isUnavoidable,
						Vector2.fromY(250)).setColor(Color.ORANGE));
		getRole().getShapes(EAiShapesLayer.OFFENSE_FINISHER)
				.add(new DrawableAnnotation(getRole().getPos(), "Is imminent: " + isImminent,
						Vector2.fromY(300)).setColor(Color.ORANGE));
		lastValidDribbleToPos = action.getDribbleToPos();
		skill.setKickIfTargetOrientationReached(canScoreGoalHere || isUnavoidable);
		getRole().getShapes(EAiShapesLayer.OFFENSE_FINISHER)
				.add(new DrawableAnnotation(getRole().getPos(), "can score now: " + canScoreGoalHere,
						Vector2.fromY(340)).setColor(Color.ORANGE));
		if (isImminent)
		{
			var lookLine = Lines.halfLineFromDirection(getRole().getPos(),
					Vector2.fromAngle(getRole().getBot().getOrientation()));
			if (Geometry.getGoalTheir().getGoalLine().intersect(lookLine).isPresent())
			{

				skill.setForceKickSpeed(Double.POSITIVE_INFINITY);
			} else
			{
				skill.setForceKickSpeed(violationImminentKickSpeed);
				skill.setForceDribblerOff(true);
			}
		} else
		{
			skill.setForceDribblerOff(false);
		}
	}


	private Optional<IRatedTarget> getRatedTarget(IVector2 botStateKickerPos)
	{
		var rater = AngleRangeRater.forGoal(Geometry.getGoalTheir());
		rater.setTimeToKick(0.1); // We should already look towards the goal, but we cannot kick immediately.
		rater.setObstacles(
				Stream.concat(
						getRole().getWFrame().getOpponentBots().values().stream(),
						getRole().getWFrame().getTigerBotsAvailable().values().stream()
								.filter(e -> e.getBotId() != getRole().getBotID())
								.filter(e -> e.getBotId() != getRole().getAiFrame().getKeeperId())
				).toList()
		);
		return rater.rate(botStateKickerPos);
	}


	private void doDribbleSkill(IVector2 botStateKickerPos, IRatedTarget bestGoalKick, boolean canScoreGoalHere)
	{
		IVector2 targetArrowDir = bestGoalKick.getTarget().subtractNew(botStateKickerPos);
		getRole().getShapes(EAiShapesLayer.OFFENSE_DRIBBLE)
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
		var hasGoodScoreChance = bestGoalKick.getScore() > 0.4;
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
