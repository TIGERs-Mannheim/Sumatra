/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy;

import java.awt.Color;
import java.util.Optional;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.time.TimestampTimer;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Activate free ball if skirmish was detected
 */
public class SkirmishFreeBallFeature extends AOffensiveStrategyFeature
{
	private TimestampTimer timer = new TimestampTimer(0.25);
	
	private boolean fail = false;
	private String info = "";
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField,
			OffensiveStrategy strategy)
	{
		fail = false;
		info = "";
		
		if (!currentStrategyIsFreeBall(newTacticalField, strategy))
		{
			return;
		}
		
		if (getAiFrame().getPrevFrame().getTacticalField().getSkirmishInformation().isStartCircleMove())
		{
			startCircleMove(newTacticalField, strategy);
			return;
		}
		
		checkAngle(newTacticalField, strategy);
		
		
		// check if secondary supp has arrived
		ITrackedBot bot = getSupportiveAttacker(getAiFrame());
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
		ITrackedBot attacker = getWFrame().getBot(strategy.getAttackerBot().orElse(BotID.noBot()));
		if (!attacker.hasBallContact())
		{
			info += "ballContact error | ";
			fail = true;
		}
		
		// check if enemy bot is actually close to ball
		IVector2 enemyPos = newTacticalField.getEnemyClosestToBall().getBot().getPos();
		IVector2 ballPos = getBall().getPos();
		if (enemyPos.distanceTo(ballPos) > Geometry.getBotRadius() + Geometry.getBallRadius() + 35)
		{
			info += "enemy dist fail | ";
			fail = true;
		}
		
		if (!Geometry.getField().isPointInShape(ballPos, -400))
		{
			info += "field Border |";
			fail = true;
		}
		
		if (Geometry.getPenaltyAreaOur().isPointInShape(ballPos, 1000))
		{
			info += "Geometry Area";
			fail = true;
		}
		
		if (fail)
		{
			DrawableBorderText dt = new DrawableBorderText(Vector2.fromXY(10, 70), info, Color.red);
			dt.setFontSize(12);
			newTacticalField.getDrawableShapes().get(EAiShapesLayer.AI_SKIRMISH_DETECTOR).add(dt);
			return;
		}
		
		info = "Activate !!!";
		DrawableBorderText dt = new DrawableBorderText(Vector2.fromXY(10, 70), info, Color.red);
		dt.setFontSize(12);
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.AI_SKIRMISH_DETECTOR).add(dt);
		
		// init turn move timer here.
		timer.reset();
		timer.update(getWFrame().getTimestamp());
		newTacticalField.getSkirmishInformation().setStartCircleMove(true);
	}
	
	
	private void checkAngle(final TacticalField newTacticalField, final OffensiveStrategy strategy)
	{
		// check if frontal combat
		IVector2 ballPos = getBall().getPos();
		ITrackedBot attackerBot = getWFrame().getBot(strategy.getAttackerBot().orElse(BotID.noBot()));
		IVector2 ballToMe = attackerBot.getPos().subtractNew(ballPos);
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
	}
	
	
	private boolean currentStrategyIsFreeBall(final TacticalField newTacticalField,
			final OffensiveStrategy strategy)
	{
		return strategy.getAttackerBot().isPresent() && newTacticalField.getSkirmishInformation()
				.getStrategy() == SkirmishInformation.ESkirmishStrategy.FREE_BALL;
	}
	
	
	private void startCircleMove(final TacticalField newTacticalField,
			final OffensiveStrategy strategy)
	{
		timer.update(getWFrame().getTimestamp());
		boolean startMove = true;
		if (timer.isTimeUp(getWFrame().getTimestamp()))
		{
			timer.reset();
			startMove = false;
		} else
		{
			// set actual turn command here
			DrawableBorderText dt = new DrawableBorderText(Vector2.fromXY(10, 110), "TURN !", Color.red);
			dt.setFontSize(12);
			newTacticalField.getDrawableShapes().get(EAiShapesLayer.AI_SKIRMISH_DETECTOR).add(dt);
			strategy.putPlayConfiguration(strategy.getAttackerBot().orElseThrow(IllegalStateException::new),
					EOffensiveStrategy.FREE_SKIRMISH);
		}
		newTacticalField.getSkirmishInformation().setStartCircleMove(startMove);
	}
	
	
	private ITrackedBot getSupportiveAttacker(final BaseAiFrame baseAiFrame)
	{
		return baseAiFrame.getPrevFrame().getPlayStrategy().getActiveRoles(ERole.SUPPORTIVE_ATTACKER)
				.stream()
				.map(ARole::getBot)
				.findAny().orElse(null);
	}
	
}
