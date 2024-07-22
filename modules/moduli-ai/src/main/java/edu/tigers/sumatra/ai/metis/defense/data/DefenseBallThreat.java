/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense.data;

import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.BallID;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Value;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.Optional;


/**
 * The ball threat for the defenders.
 */
@Value
public class DefenseBallThreat implements IDefenseThreat
{
	IVector2 vel;
	ILineSegment threatLine;
	ILineSegment protectionLine;
	ITrackedBot passReceiver;
	EDefenseBallThreatSourceType sourceType;


	@Override
	public IVector2 getPos()
	{
		return threatLine.getPathStart();
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
		return BallID.instance();
	}


	/**
	 * @return the passReceiver
	 */
	public Optional<ITrackedBot> getPassReceiver()
	{
		return Optional.ofNullable(passReceiver);
	}


	@Override
	public EDefenseThreatType getType()
	{
		return EDefenseThreatType.BALL;
	}


	@Override
	public double getThreatRating()
	{
		return Double.POSITIVE_INFINITY;
	}


	@Override
	public String toString()
	{
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("vel", vel)
				.append("threatLine", threatLine)
				.append("protectionLine", protectionLine)
				.append("passReceiver", passReceiver)
				.toString();
	}


	@Override
	public boolean sameAs(final IDefenseThreat o)
	{
		if (this == o)
		{
			return true;
		}

		return o != null && getClass() == o.getClass();
	}
}
