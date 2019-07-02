/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import java.awt.Color;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.KickTarget;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.ai.metis.targetrater.MaxAngleKickRater;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * Kick the ball away fast to clear a dangerous situation
 */
public class ClearingKickActionMove extends AOffensiveActionMove
{
	@Configurable(defValue = "2.5")
	private static double ballSpeedAtTarget = 2.5;
	
	
	private double via = 0;
	
	
	public ClearingKickActionMove()
	{
		super(EOffensiveActionMove.CLEARING_KICK);
	}
	
	
	@Override
	public EActionViability isActionViable(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		if (baseAiFrame.getWorldFrame().getBall().getPos().x() > 0 )
		{
			// No clearing kick far in enemies half, chance of shooting the ball out of field is to high.
			return EActionViability.FALSE;
		}
		boolean clear = isClearingShotNeeded(id, newTacticalField, baseAiFrame);
		if (clear)
		{
			return EActionViability.TRUE;
		}
		// check probability score here.
		via = calcDangerScore(newTacticalField, baseAiFrame);
		if (via > 0.6)
		{
			return EActionViability.TRUE;
		}
		return EActionViability.PARTIALLY;
	}
	
	
	@Override
	public OffensiveAction activateAction(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		// calculate good target here...
		double danger = calcDangerScore(newTacticalField, baseAiFrame) * 0.3;
		IVector2 ballPos = baseAiFrame.getWorldFrame().getBall().getPos();
		IVector2 bestShootTargt = calcAndRateTarget(baseAiFrame, ballPos)
				.map(IRatedTarget::getTarget)
				.map(DynamicPosition::getPos)
				.orElse(Geometry.getGoalTheir().getCenter());
		IVector2 clearingDir = ballPos.subtractNew(Geometry.getGoalOur().getCenter()).scaleToNew(100 * (danger));
		IVector2 targetDir = bestShootTargt.subtractNew(ballPos).scaleToNew(100 * (1 - danger));
		IVector2 midDir = clearingDir.multiplyNew(1).addNew(targetDir).scaleToNew(3500);
		IVector2 midTarget = ballPos.addNew(midDir);
		IVector2 bestClearingTarget = ballPos.addNew(clearingDir.scaleToNew(3500));
		
		DrawableLine dlShoot = new DrawableLine(Line.fromPoints(ballPos, bestShootTargt), Color.orange);
		DrawableLine dlClear = new DrawableLine(Line.fromPoints(ballPos, bestClearingTarget), Color.orange);
		DrawableLine dlMid = new DrawableLine(Line.fromPoints(ballPos, midTarget), Color.RED);
		
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_CLEARING_KICK).add(dlShoot);
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_CLEARING_KICK).add(dlClear);
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_CLEARING_KICK).add(dlMid);
		
		double passRange = 0.6;
		KickTarget kickTarget = KickTarget.pass(new DynamicPosition(midTarget, passRange), ballSpeedAtTarget,
				KickTarget.ChipPolicy.ALLOW_CHIP);
		
		return createOffensiveAction(EOffensiveAction.CLEARING_KICK, kickTarget);
	}
	
	
	private double calcDangerScore(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		// 1 is dangerous
		double enemyDistToBall = Math.max(0.001,
				1 - Math.min(newTacticalField.getEnemyClosestToBall().getDist(), 500) / 500.0);
		
		// 1 is dangerous
		double distToGoal = 1 - Math.min(4000, Geometry.getGoalOur().getCenter()
				.distanceTo(baseAiFrame.getWorldFrame().getBall().getPos())) / 4000.0;
		
		// 1 is good chance for enemy to score goal
		double enemyScoreChance = MaxAngleKickRater.getFoeScoreChanceWithDefender(
				baseAiFrame.getWorldFrame().getTigerBotsVisible().values(),
				baseAiFrame.getWorldFrame().getBall().getPos());
		
		return enemyScoreChance * 0.4 + enemyDistToBall * 0.5 + distToGoal * 0.1;
	}
	
	
	@Override
	public double calcViabilityScore(final BotID id, final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		return via * ActionMoveConstants.getViabilityMultiplierClearingKick();
	}
	
	
	private boolean isClearingShotNeeded(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		IVector2 target = Geometry.getGoalOur().getCenter();
		IVector2 botPos = baseAiFrame.getWorldFrame().getBot(id).getPos();
		IVector2 ballPos = baseAiFrame.getWorldFrame().getBall().getPos();
		IVector2 ballToTarget = ballPos.subtractNew(target);
		IVector2 behindBall = ballPos.addNew(ballToTarget.normalizeNew().multiplyNew(-650));
		IVector2 normal = ballToTarget.getNormalVector().normalizeNew();
		IVector2 behindBallOff1 = behindBall.addNew(normal.multiplyNew(500));
		IVector2 behindBallOff2 = behindBall.addNew(normal.multiplyNew(-500));
		DrawableTriangle tria = new DrawableTriangle(ballPos, behindBallOff1, behindBallOff2,
				new Color(100, 200, 100, 20));
		tria.setFill(true);
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_CLEARING_KICK).add(tria);
		
		IVector2 behindBallOff11 = ballPos.addNew(ballPos.subtractNew(behindBallOff1));
		IVector2 behindBallOff12 = ballPos.addNew(ballPos.subtractNew(behindBallOff2));
		DrawableTriangle tria2 = new DrawableTriangle(ballPos, behindBallOff11, behindBallOff12,
				new Color(200, 100, 100, 20));
		tria2.setFill(true);
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_CLEARING_KICK).add(tria2);
		
		if (!tria.getTriangle().isPointInShape(botPos))
		{
			return false;
		}
		// bot in front of ball
		DrawableCircle dc = new DrawableCircle(Circle.createCircle(botPos, 120), Color.green);
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_CLEARING_KICK).add(dc);
		for (BotID enemy : baseAiFrame.getWorldFrame().foeBots.keySet())
		{
			IVector2 enemeyPos = baseAiFrame.getWorldFrame().getFoeBot(enemy).getPos();
			if (tria2.getTriangle().isPointInShape(enemeyPos))
			{
				DrawableCircle dc2 = new DrawableCircle(Circle.createCircle(enemeyPos, 120), Color.red);
				newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_CLEARING_KICK).add(dc2);
				// one or more enemy bot is behind the ball.. maybe ready to shoot on our goal !
				if (ballPos.distanceTo(Geometry.getGoalOur().getCenter()) < 3000)
				{
					// ball on our half of the field
					return true;
				}
			}
		}
		return false;
	}
}
