/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy.features;

import edu.tigers.sumatra.ai.data.EAIControlState;
import edu.tigers.sumatra.ai.data.MultiTeamRobotPlan;
import edu.tigers.sumatra.ai.data.OffensiveStrategy;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.offense.data.TemporaryOffensiveInformation;
import edu.tigers.sumatra.ai.metis.offense.strategy.AOffensiveStrategyFeature;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * MarkG
 */
public class MixedTeamDropBotFeature extends AOffensiveStrategyFeature
{
	/**
	 * Default
	 */
	public MixedTeamDropBotFeature()
	{
		super();
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame,
			TemporaryOffensiveInformation tempInfo, OffensiveStrategy strategy)
	{
		if (baseAiFrame.getPrevFrame().getPlayStrategy().getAIControlState() == EAIControlState.MIXED_TEAM_MODE)
		{
			if (multiTeamChallengeChecks(baseAiFrame, tempInfo.getPrimaryBot()))
			{
				// Drop offensive role if team mates can obtain the ball faster.
				strategy.getDesiredBots().clear();
				strategy.setMinNumberOfBots(0);
				strategy.setMaxNumberOfBots(0);
			}
			
			IPassTarget passTarget = baseAiFrame.getPrevFrame().getAICom().getPassTarget();
			if (passTarget != null)
			{
				MultiTeamRobotPlan robotPlan = new MultiTeamRobotPlan(passTarget.getBotId());
				Pose pose = Pose.from(passTarget.getBotPos(),
						Vector2.fromPoints(passTarget.getBotPos(), baseAiFrame.getWorldFrame().getBall().getPos())
								.getAngle());
				robotPlan.setTargetPose(pose);
				robotPlan.setShootTarget(pose.getPos());
				newTacticalField.getMultiTeamPlan().getRobotPlans().put(passTarget.getBotId(), robotPlan);
			}
		}
	}
	
	
	private boolean multiTeamChallengeChecks(final BaseAiFrame baseAiFrame, ITrackedBot primaryBot)
	{
		if (primaryBot == null)
		{
			return false;
		}
		
		BotIDMap<ITrackedBot> friendlyBots = new BotIDMap<>(baseAiFrame.getWorldFrame().tigerBotsVisible);
		for (BotID key : baseAiFrame.getWorldFrame().getTigerBotsAvailable().keySet())
		{
			friendlyBots.remove(key);
		}
		IVector2 myPos = primaryBot.getPos();
		IVector2 ballPos = baseAiFrame.getWorldFrame().getBall().getPos();
		for (BotID key : friendlyBots.keySet())
		{
			IVector2 friendlyPos = friendlyBots.getWithNull(key).getPos();
			double distMeToBall = VectorMath.distancePP(myPos, ballPos);
			double distFriendToBall = VectorMath.distancePP(friendlyPos, ballPos);
			if (distFriendToBall < 300 && (distMeToBall - 50) > distFriendToBall)
			{
				return true;
			}
		}
		return false;
	}
}
