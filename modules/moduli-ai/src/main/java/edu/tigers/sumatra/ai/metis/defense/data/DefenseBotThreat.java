/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense.data;

import java.util.Optional;

import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * A bot threat to our goal.
 */
public class DefenseBotThreat implements IDefenseThreat
{
	private final ITrackedBot bot;

	private final ILineSegment threatLine;
	private final ILineSegment protectionLine;

	private final double shootingAngle;


	public DefenseBotThreat(
			final ITrackedBot bot,
			final ILineSegment threatLine,
			final ILineSegment protectionLine,
			final double shootingAngle)
	{
		this.bot = bot;
		this.threatLine = threatLine;
		this.protectionLine = protectionLine;
		this.shootingAngle = shootingAngle;
	}


	@Override
	public IVector2 getPos()
	{
		return threatLine.getStart();
	}


	public BotID getBotID()
	{
		return bot.getBotId();
	}


	public double getShootingAngle()
	{
		return shootingAngle;
	}


	@Override
	public ILineSegment getThreatLine()
	{
		return threatLine;
	}


	@Override
	public Optional<ILineSegment> getProtectionLine()
	{
		return Optional.ofNullable(protectionLine);
	}


	@Override
	public IVector2 getVel()
	{
		return bot.getVel();
	}


	@Override
	public AObjectID getObjectId()
	{
		return getBotID();
	}


	@Override
	public EDefenseThreatType getType()
	{
		return EDefenseThreatType.BOT_TO_GOAL;
	}


	@Override
	public boolean sameAs(final IDefenseThreat o)
	{
		if (this == o)
		{
			return true;
		}

		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		return getObjectId() == o.getObjectId();
	}
}
