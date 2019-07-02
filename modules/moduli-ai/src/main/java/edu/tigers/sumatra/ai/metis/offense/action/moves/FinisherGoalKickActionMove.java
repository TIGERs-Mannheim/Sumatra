/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import java.awt.Color;
import java.util.List;
import java.util.Optional;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.KickTarget;
import edu.tigers.sumatra.ai.metis.offense.finisher.FinisherMoves;
import edu.tigers.sumatra.ai.metis.offense.finisher.IFinisherMove;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRangeRater;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Kick directly on the opponent goal
 */
public class FinisherGoalKickActionMove extends AOffensiveActionMove
{
	
	@Configurable(defValue = "0.9")
	private static double scorePenaltyForFinisherMove = 0.9;
	
	@Configurable(defValue = "30.0", comment = "angle in degree")
	private static double maxAllowedOrientationError = 30.0;
	
	@Configurable(defValue = "false")
	private static boolean enableFinisherMoves = false;
	
	private IFinisherMove finisherMove;
	private IVector2 kickTarget;
	private double viability = 0;
	
	
	static
	{
		ConfigRegistration.registerClass("metis", FinisherGoalKickActionMove.class);
	}
	
	
	public FinisherGoalKickActionMove()
	{
		super(EOffensiveActionMove.GOAL_KICK);
	}
	
	
	@Override
	public EActionViability isActionViable(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		if (!enableFinisherMoves || newTacticalField.getGameState().isStandardSituation())
		{
			return EActionViability.FALSE;
		}
		
		AngleRangeRater rater = AngleRangeRater.forGoal(Geometry.getGoalTheir());
		IBotIDMap<ITrackedBot> obstacles = new BotIDMap<>(baseAiFrame.getWorldFrame().getBots());
		obstacles.remove(id);
		rater.setObstacles(obstacles.values());
		rater.setStraightBallConsultant(baseAiFrame.getWorldFrame().getBall().getStraightConsultant());
		
		IVector2 bestShotTarget = null;
		double bestScore = -1;
		Optional<IFinisherMove> bestFinisherMove = Optional.empty();
		
		List<IFinisherMove> finisherMoves = FinisherMoves.all().getFinisherMoves(baseAiFrame, id);
		for (IFinisherMove finisher : finisherMoves)
		{
			Pose kickLocation = finisher.getKickLocation(baseAiFrame);
			Optional<IRatedTarget> target = rater.rate(kickLocation.getPos());
			double finisherScore = 0;
			double orientationError = AngleMath.deg2rad(180);
			if (target.isPresent())
			{
				IVector2 finalOrientation = Vector2.fromAngle(kickLocation.getOrientation());
				IVector2 finalLocToTarget = target.get().getTarget().getPos().subtractNew(kickLocation.getPos());
				orientationError = finalOrientation.angleTo(finalLocToTarget).orElse(AngleMath.deg2rad(180.0));
				
				finisherScore = target.get().getScore();
				IVector2 shotTarget = target.get().getTarget().getPos();
				if ((finisherScore > bestScore) && finisher.isApplicable(baseAiFrame, id) &&
						(orientationError < AngleMath.deg2rad(maxAllowedOrientationError)))
				{
					bestScore = finisherScore;
					bestShotTarget = shotTarget;
					bestFinisherMove = Optional.of(finisher);
					drawApplicableHighlighting(newTacticalField, kickLocation);
				}
				drawTargetLine(newTacticalField, kickLocation, finisherScore, shotTarget);
			}
			drawShapes(newTacticalField, kickLocation, finisherScore, orientationError);
		}
		
		if (bestFinisherMove.isPresent())
		{
			kickTarget = bestShotTarget;
			finisherMove = bestFinisherMove.get();
			viability = bestScore;
			return EActionViability.PARTIALLY;
		}
		
		kickTarget = null;
		finisherMove = null;
		viability = 0;
		
		return EActionViability.FALSE;
	}
	
	
	@Override
	public OffensiveAction activateAction(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		KickTarget target = KickTarget.goalShot(new DynamicPosition(kickTarget));
		return createOffensiveAction(EOffensiveAction.FINISHER_KICK, finisherMove, target);
	}
	
	
	@Override
	public double calcViabilityScore(final BotID id, final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		return viability * ActionMoveConstants.getViabilityMultiplierDirectKick() * scorePenaltyForFinisherMove;
	}
	
	
	private void drawApplicableHighlighting(final TacticalField newTacticalField, final Pose kickLocation)
	{
		DrawableCircle dc = new DrawableCircle(Circle.createCircle(kickLocation.getPos(), 110), Color.CYAN);
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_FINISHER).add(dc);
	}
	
	
	private void drawAnnotation(final TacticalField newTacticalField, final Pose kickLocation, final double err)
	{
		DrawableAnnotation da = new DrawableAnnotation(kickLocation.getPos(),
				"o-err: " + String.format("%.2f", AngleMath.rad2deg(err)), Vector2.fromXY(-100, -50));
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_FINISHER).add(da);
	}
	
	
	private void drawShapes(final TacticalField newTacticalField, final Pose kickLocation,
							final double finisherScore, final double err)
	{
		DrawableCircle dc = new DrawableCircle(Circle.createCircle(kickLocation.getPos(), 100));
		dc.setFill(true);
		dc.setColor(new Color((int) ((1 - finisherScore) * 255), 0, (int) (finisherScore * 255), 200));
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_FINISHER).add(dc);
		DrawableLine oLine = new DrawableLine(Line.fromDirection(kickLocation.getPos(),
				Vector2.fromAngle(kickLocation.getOrientation()).scaleTo(100)));
		oLine.setColor(new Color(255, 255, 255, 250));
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_FINISHER).add(oLine);
		drawAnnotation(newTacticalField, kickLocation, err);
	}
	
	
	private void drawTargetLine(final TacticalField newTacticalField, final Pose kickLocation,
			final double finisherScore, final IVector2 shotTarget)
	{
		DrawableLine line = new DrawableLine(Line.fromPoints(kickLocation.getPos(), shotTarget));
		line.setColor(new Color((int) ((1 - finisherScore) * 255), 0, (int) (finisherScore * 255), 200));
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_FINISHER).add(line);
	}
}
