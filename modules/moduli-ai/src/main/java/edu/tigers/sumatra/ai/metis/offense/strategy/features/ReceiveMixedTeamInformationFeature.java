/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy.features;

import java.util.List;

import edu.tigers.sumatra.ai.data.MultiTeamRobotPlan;
import edu.tigers.sumatra.ai.data.OffensiveStrategy;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.offense.data.TemporaryOffensiveInformation;
import edu.tigers.sumatra.ai.metis.offense.strategy.AOffensiveStrategyFeature;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Handle team plans from other team in mixed team mode
 * 
 * @author MarkG
 */
public class ReceiveMixedTeamInformationFeature extends AOffensiveStrategyFeature
{
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame,
			TemporaryOffensiveInformation tempInfo, OffensiveStrategy strategy)
	{
		mixedChallengeReceiveOffensiveInformation(baseAiFrame, strategy, tempInfo.getPrimaryBot());
	}
	
	
	private void mixedChallengeReceiveOffensiveInformation(
			final BaseAiFrame baseAiFrame, OffensiveStrategy offensiveStrategy, ITrackedBot primaryBot)
	{
		List<MultiTeamRobotPlan> offensiveRobotPlans = baseAiFrame.getIncomingMultiTeamMessage().getTeamPlan()
				.getRobotPlans(MultiTeamRobotPlan.EMultiTeamRobotRole.OFFENSE);
		for (MultiTeamRobotPlan plan : offensiveRobotPlans)
		{
			processRobotPlan(baseAiFrame, offensiveStrategy, primaryBot, plan);
		}
	}
	
	
	private void processRobotPlan(final BaseAiFrame baseAiFrame, final OffensiveStrategy offensiveStrategy,
			final ITrackedBot primaryBot, final MultiTeamRobotPlan plan)
	{
		BotID planBotID = plan.getBotID();
		ITrackedBall ball = baseAiFrame.getWorldFrame().getBall();
		IVector2 ballPos = ball.getPos();
		if (!baseAiFrame.getWorldFrame().getTigerBotsAvailable().containsKey(planBotID) &&
				baseAiFrame.getWorldFrame().getTigerBotsVisible().containsKey(planBotID) &&
				plan.getTargetPose().isPresent())
		{
			IVector2 moveTarget = plan.getTargetPose().get().getPos();
			if (moveTarget.distanceTo(ballPos) < 1000)
			{
				IVector2 friendlyPos = baseAiFrame.getWorldFrame().getBot(planBotID).getPos();
				if (ballPos.distanceTo(primaryBot.getPos()) > friendlyPos.distanceTo(ballPos))
				{
					// other teams's bot will drive to the ball, so drop our offensive bot
					offensiveStrategy.getDesiredBots().clear();
					offensiveStrategy.setMinNumberOfBots(0);
					offensiveStrategy.setMaxNumberOfBots(1);
				}
			}
		}
	}
}
