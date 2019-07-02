/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.passtarget;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotIDMapConst;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;

/**
 * Input parameters for the PassTargetRatingFactory
 */
public class PassTargetRatingFactoryInput
{
	private final ITrackedBot attacker;
	private final ITrackedBall ball;
	private final IVector2 passOrigin;
	private final BotIDMapConst<ITrackedBot> foeBots;
	private final double maxChipVel;
	
	
	private PassTargetRatingFactoryInput(final ITrackedBot attacker, final ITrackedBall ball, final IVector2 passOrigin,
                                         final BotIDMapConst<ITrackedBot> foeBots, final double maxChipVel)
	{
		this.attacker = attacker;
		this.ball = ball;
		this.passOrigin = passOrigin;
		this.foeBots = foeBots;
		this.maxChipVel = maxChipVel;
	}
	
	
	public static PassTargetRatingFactoryInput fromAiFrame(final BaseAiFrame baseAiFrame)
	{
		final ITrackedBot attacker = attackerFromAiFrame(baseAiFrame);
		final ITrackedBall ball = ballFromAiFrame(baseAiFrame);
		final IVector2 passOrigin = passOriginFromAttackerAndBall(attacker, ball);
		final BotIDMapConst<ITrackedBot> foeBots = foeBotsFromAiFrame(baseAiFrame);
		final double maxChipVel = maxChipVelFromAttacker(attacker);
		
		return new PassTargetRatingFactoryInput(attacker, ball, passOrigin, foeBots, maxChipVel);
	}
	
	
	private static ITrackedBot attackerFromAiFrame(BaseAiFrame baseAiFrame)
	{
		return baseAiFrame.getPrevFrame().getTacticalField().getOffensiveStrategy().getAttackerBot()
				.map(id -> baseAiFrame.getWorldFrame().getBot(id))
				.orElse(null);
	}
	
	
	private static ITrackedBall ballFromAiFrame(BaseAiFrame baseAiFrame)
	{
		return baseAiFrame.getWorldFrame().getBall();
	}
	
	
	private static IVector2 passOriginFromAttackerAndBall(ITrackedBot attacker, ITrackedBall ball)
	{
		return attacker != null
				? ball.getTrajectory().getTravelLineRolling().closestPointOnLine(attacker.getBotKickerPos())
				: ball.getPos();
	}
	
	
	private static BotIDMapConst<ITrackedBot> foeBotsFromAiFrame(BaseAiFrame baseAiFrame)
	{
		return baseAiFrame.getWorldFrame().getFoeBots();
	}
	
	
	private static double maxChipVelFromAttacker(ITrackedBot attacker)
	{
		return attacker == null ? RuleConstraints.getMaxBallSpeed()
				: attacker.getRobotInfo().getBotParams().getKickerSpecs().getMaxAbsoluteChipVelocity();
	}
	
	
	public ITrackedBot getAttacker()
	{
		return attacker;
	}
	
	
	public ITrackedBall getBall()
	{
		return ball;
	}
	
	
	public IVector2 getPassOrigin()
	{
		return passOrigin;
	}
	
	
	public BotIDMapConst<ITrackedBot> getFoeBots()
	{
		return foeBots;
	}
	
	
	public double getMaxChipVel()
	{
		return maxChipVel;
	}
}
