/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense.data;

import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Value;

import java.util.Optional;


/**
 * A bot threat to our goal.
 */
@Value
public class DefenseBallToBotThreat implements IDefenseThreat
{
	ITrackedBot bot;
	IVector2 vel;

	ILineSegment threatLine;
	ILineSegment protectionLine;


	@Override
	public IVector2 getPos()
	{
		return threatLine.getEnd();
	}


	public BotID getBotID()
	{
		return bot.getBotId();
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
		return vel;
	}


	@Override
	public AObjectID getObjectId()
	{
		return getBotID();
	}


	@Override
	public EDefenseThreatType getType()
	{
		return EDefenseThreatType.BALL_TO_BOT;
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
