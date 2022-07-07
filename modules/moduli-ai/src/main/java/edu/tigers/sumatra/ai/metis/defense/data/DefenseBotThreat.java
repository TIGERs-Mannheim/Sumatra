/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
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
public class DefenseBotThreat implements IDefenseThreat
{
	DefenseBotThreatDefStrategyData defStrategyData;
	ITrackedBot bot;
	double threatRating;


	public ITrackedBot getBot()
	{
		return bot;
	}


	public double getThreatRating()
	{
		return threatRating;
	}


	public EDefenseBotThreatDefStrategy getDefendStrategy()
	{
		return defStrategyData.type();
	}


	@Override
	public IVector2 getPos()
	{
		return defStrategyData.threatPos();
	}


	public BotID getBotID()
	{
		return bot.getBotId();
	}


	@Override
	public ILineSegment getThreatLine()
	{
		return defStrategyData.threatLine();
	}


	@Override
	public Optional<ILineSegment> getProtectionLine()
	{
		return Optional.ofNullable(defStrategyData.protectionLine());
	}


	public Optional<IVector2> getProtectionPosition()
	{
		return Optional.ofNullable(defStrategyData.protectionPos());
	}


	@Override
	public IVector2 getVel()
	{
		return defStrategyData.threatVel();
	}


	@Override
	public AObjectID getObjectId()
	{
		return getBotID();
	}


	@Override
	public EDefenseThreatType getType()
	{
		return switch (defStrategyData.type())
				{
					case CENTER_BACK -> EDefenseThreatType.BOT_CB;
					case MAN_2_MAN_MARKER -> EDefenseThreatType.BOT_M2M;
				};
	}


	@Override
	public boolean sameAs(final IDefenseThreat o)
	{
		if (this == o)
		{
			return true;
		}

		if (o == null || getClass() != o.getClass() || getType() != o.getType())
		{
			return false;
		}

		return getObjectId() == o.getObjectId();
	}
}
