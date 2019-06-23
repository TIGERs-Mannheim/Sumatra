/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy;

import java.awt.Color;
import java.text.DecimalFormat;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.defense.DefenseMath;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Add a pass receiver if required
 */
public class AddSecondaryFeature extends AOffensiveStrategyFeature
{
	private static final DecimalFormat DF = new DecimalFormat();
	
	static
	{
		DF.setMaximumFractionDigits(2);
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final OffensiveStrategy strategy)
	{
		if (newTacticalField.getGameState().isKickoffOrPrepareKickoffForUs()
				|| !strategy.getActivePassTarget().isPresent()
				|| !strategy.getActivePassTarget().get().getBotId().isBot())
		{
			return;
		}
		
		IPassTarget passTarget = strategy.getActivePassTarget().get();
		
		BotID attacker = strategy.getAttackerBot().orElse(
				getAiFrame().getKeeperId());
		
		ITrackedBot attackerBot = getWFrame().getBot(attacker);
		double kickSpeed = OffensiveMath.passSpeedStraight(
				attackerBot.getBotKickerPos(),
				passTarget.getKickerPos(),
				DefenseMath.getBisectionGoal(passTarget.getKickerPos()));
		double passDistance = getBall().getPos().distanceTo(passTarget.getKickerPos());
		double ballTravelTime = getBall().getStraightConsultant().getTimeForKick(passDistance, kickSpeed);
		
		double shootTime = newTacticalField.getBallInterceptions().get(attacker).getBallContactTime();
		double totalTime = shootTime + ballTravelTime;
		DrawableAnnotation dt = new DrawableAnnotation(
				passTarget.getKickerPos().addNew(Vector2.fromXY(200, 0)),
				"totalTime: " + DF.format(totalTime) +
						"\nshootTime: " + DF.format(shootTime) +
						"\nballTime: " + DF.format(ballTravelTime) +
						"\ninitVel: " + DF.format(kickSpeed) +
						"\ndist: " + DF.format(passDistance),
				Color.black);
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_PASSING).add(dt);
		
		BotID passReceiver = passTarget.getBotId();
		if (getWFrame().tigerBotsAvailable.containsKey(passReceiver)
				&& !newTacticalField.getCrucialDefender().contains(passReceiver)
				&& passReceiver != attacker)
		{
			strategy.addDesiredBot(passReceiver);
			strategy.putPlayConfiguration(passReceiver, EOffensiveStrategy.RECEIVE_PASS);
		}
	}
}
