/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.goal.EPossibleGoal;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.vision.data.IKickEvent;


/**
 * Statistics for direct Shots to goal
 *
 * @author Stefan Schneyer
 */
public class DirectShotsStatsCalc extends AStatsCalc
{
	static
	{
		ConfigRegistration.registerClass("metis", DirectShotsStatsCalc.class);
	}
	
	// Number of Direct Shots
	private final Map<Integer, Integer> directBotShots = new HashMap<>();
	// Success Percentage for direct Shots
	private final Percentage successGeneral = new Percentage();
	private final Map<Integer, Percentage> successPerBot = new HashMap<>();
	// Buffer Variables for detection of direct shots
	private boolean lastIsDirectShot = false;
	private BotID shooter = null;
	@Configurable(comment = "maximalDetectionDistanceToGoal", defValue = "9000")
	private double maximalDetectionDistanceToGoal = 9000;
	
	
	@Override
	public void saveStatsToMatchStatistics(final MatchStats matchStatistics)
	{
		// Direct Shots
		int directShotsSum = successPerBot.values().stream().mapToInt(Percentage::getAll).sum();
		StatisticData directShotTigersStats = new StatisticData(directBotShots, directShotsSum);
		matchStatistics.putStatisticData(EMatchStatistics.DIRECT_SHOTS, directShotTigersStats);
		
		// Direct Shots Success rate
		StatisticData ballPossessionTigers = new StatisticData(successPerBot, successGeneral);
		matchStatistics.putStatisticData(EMatchStatistics.DIRECT_SHOTS_SUCCESS_RATE, ballPossessionTigers);
	}
	
	
	@Override
	public void onStatisticUpdate(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		Optional<IKickEvent> directShot = newTacticalField.getDetectedGoalKick(ETeam.TIGERS);
		boolean isDirectShot = directShot.isPresent();
		
		final IVector2 ballPosition = baseAiFrame.getWorldFrame().getBall().getPos();
		
		if (Geometry.getGoalTheir().getCenter().distanceToSqr(ballPosition) <= Math.pow(maximalDetectionDistanceToGoal,
				2))
		{
			if (!lastIsDirectShot && isDirectShot)
				directShotDetected(directShot.get().getKickingBot());
			else
				watchingForGoal(newTacticalField, baseAiFrame);
		}
		
		lastIsDirectShot = isDirectShot;
	}
	
	
	private void directShotDetected(BotID shooter)
	{
		successPerBot.computeIfAbsent(shooter.getNumber(), sb -> new Percentage(0, 0)).incAll();
		successGeneral.incAll();
		
		directBotShots.putIfAbsent(shooter.getNumber(), 0);
		directBotShots.compute(shooter.getNumber(), (id, shot) -> shot + 1);
		
		this.shooter = shooter;
	}
	
	
	private void watchingForGoal(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		if (newTacticalField.getPossibleGoal() == EPossibleGoal.WE && shooter != null)
		{
			successPerBot.computeIfAbsent(shooter.getNumber(), sb -> new Percentage(0, 0)).inc();
			shooter = null;
			
			successGeneral.inc();
		}
		if (baseAiFrame.getWorldFrame().getKickEvent().isPresent())
		{
			shooter = null;
		}
	}
}
