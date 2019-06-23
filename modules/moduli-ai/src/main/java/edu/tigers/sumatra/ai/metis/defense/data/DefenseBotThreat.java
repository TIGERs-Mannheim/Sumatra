/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense.data;

import edu.tigers.sumatra.ai.math.DefenseMath;
import edu.tigers.sumatra.ai.metis.defense.DefenseConstants;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * A bot threat to our goal.
 */
public class DefenseBotThreat implements IDefenseThreat
{
	private ITrackedBot bot = null;
	private final double shootingAngle;
	private final double tGoal;
	
	
	/**
	 * @param bot
	 * @param shootingAngle
	 * @param tGoal
	 */
	public DefenseBotThreat(final ITrackedBot bot, final double shootingAngle,
			final double tGoal)
	{
		assert bot != null;
		
		this.bot = bot;
		this.shootingAngle = shootingAngle;
		this.tGoal = tGoal;
	}
	

	public BotID getBotID()
	{
		return bot.getBotId();
	}
	
	
	public double getShootingAngle()
	{
		return shootingAngle;
	}
	
	
	public double getTGoal()
	{
		return tGoal;
	}
	
	
	@Override
	public ILineSegment getThreatLine()
	{
		IVector2 pointInGoal = DefenseMath.getBisectionGoal(bot.getPosByTime(DefenseConstants
				.getLookaheadBotThreats(bot.getVel().getLength())));

		return Lines.segmentFromPoints(bot.getPos(), pointInGoal);
	}
	
	
	@Override
	public double getScore()
	{
		return shootingAngle;
	}
	
	
	@Override
	public IVector2 getVel()
	{
		return bot.getVel();
	}
	
	
	@Override
	public boolean isBot()
	{
		return true;
	}
}
