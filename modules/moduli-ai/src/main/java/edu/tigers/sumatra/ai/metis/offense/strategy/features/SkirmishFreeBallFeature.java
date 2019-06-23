/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy.features;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.OffensiveStrategy;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.math.OffensiveMath;
import edu.tigers.sumatra.ai.metis.offense.data.SkirmishInformation;
import edu.tigers.sumatra.ai.metis.offense.data.TemporaryOffensiveInformation;
import edu.tigers.sumatra.ai.metis.offense.strategy.AOffensiveStrategyFeature;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.time.TimestampTimer;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.awt.*;
import java.util.Optional;


/**
 * @author MarkG
 */
public class SkirmishFreeBallFeature extends AOffensiveStrategyFeature
{
	
	private TimestampTimer	timer = new TimestampTimer(0.25);

	
	/**
	 * Default
	 */
	public SkirmishFreeBallFeature()
	{
		super();
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame,
			TemporaryOffensiveInformation tempInfo, OffensiveStrategy strategy)
	{
		if (tempInfo.getPrimaryBot() == null)
		{
			return;
		}
		
		boolean fail = false;
		if (newTacticalField.getSkirmishInformation().getStrategy() != SkirmishInformation.ESkirmishStrategy.FREE_BALL)
		{
			return;
		}
		
		if (baseAiFrame.getPrevFrame().getTacticalField().getSkirmishInformation().isStartCircleMove())
		{
			timer.update(baseAiFrame.getWorldFrame().getTimestamp());
			boolean startMove = true;
			if (timer.isTimeUp(baseAiFrame.getWorldFrame().getTimestamp()))
			{
				timer.reset();
				startMove = false;
			} else
			{
				// set actual turn command here
				DrawableBorderText dt = new DrawableBorderText(Vector2.fromXY(10, 110), "TURN !", Color.red);
				dt.setFontSize(12);
				newTacticalField.getDrawableShapes().get(EAiShapesLayer.SKIRMISH_DETECTOR).add(dt);
				strategy.getCurrentOffensivePlayConfiguration().put(tempInfo.getPrimaryBot().getBotId(), OffensiveStrategy.EOffensiveStrategy.FREE_SKIRMISH);
			}
			newTacticalField.getSkirmishInformation().setStartCircleMove(startMove);
			return;
		}
		
		String info = "";
		// check if frontal comat
		IVector2 ballPos = baseAiFrame.getWorldFrame().getBall().getPos();
		IVector2 ballToMe = tempInfo.getPrimaryBot().getPos().subtractNew(ballPos);
		// enemy bot cannot be null, else there would be no skirmish
		IVector2 enemyPos = newTacticalField.getEnemyClosestToBall().getBot().getPos();
		IVector2 ballToEnemy = enemyPos.subtractNew(ballPos);
		Optional<Double> angle = ballToEnemy.angleTo(ballToMe);
		if (angle.isPresent())
		{
			double degress = Math.abs(AngleMath.rad2deg(angle.get()));
			info += degress + " | ";
			if (180 - degress > 30)
			{
				fail = true;
				info += "degree fail | ";
			}
		} else
		{
			fail = true;
		}

		// check if secondary supp has arrived
		ITrackedBot bot = OffensiveMath.getSupportiveAttacker(newTacticalField, baseAiFrame);
		if (bot == null)
		{
			fail = true;
			info += "No Secondary | ";
		} else
		{
			double dist = bot.getPos().distanceTo(newTacticalField.getSkirmishInformation().getSupportiveCircleCatchPos());
			if (dist > 100)
			{
				fail = true;
				info += "dist error | ";
			}
		}

		// check if hasContact()
		if (!tempInfo.getPrimaryBot().hasBallContact())
		{
			info += "ballContact error | ";
			fail = true;
		}

		// check if enemy bot is actually close to ball
		if (enemyPos.distanceTo(ballPos) > Geometry.getBotRadius() + Geometry.getBallRadius() + 35)
		{
			info += "enemy dist fail | ";
			fail = true;
		}

		if (!Geometry.getField().isPointInShape(ballPos,-400))
		{
			info += "field Border |";
			fail = true;
		}

		if (Geometry.getPenaltyAreaOur().isPointInShape(ballPos,1000))
		{
			info += "Geometry Area";
			fail = true;
		}

		if (fail)
		{
			DrawableBorderText dt = new DrawableBorderText(Vector2.fromXY(10, 70), info, Color.red);
			dt.setFontSize(12);
			newTacticalField.getDrawableShapes().get(EAiShapesLayer.SKIRMISH_DETECTOR).add(dt);
			return;
		}

		info = "Activate !!!";
		DrawableBorderText dt = new DrawableBorderText(Vector2.fromXY(10, 70), info, Color.red);
		dt.setFontSize(12);
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.SKIRMISH_DETECTOR).add(dt);
		
		// init turn move timer here.
		timer.reset();
		timer.update(baseAiFrame.getWorldFrame().getTimestamp());
		newTacticalField.getSkirmishInformation().setStartCircleMove(true);
	}

}
