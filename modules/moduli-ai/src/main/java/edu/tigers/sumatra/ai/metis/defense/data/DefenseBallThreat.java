/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense.data;

import java.util.Optional;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.BallID;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * The ball threat for the defenders.
 */
public class DefenseBallThreat implements IDefenseThreat
{
	private final IVector2 vel;
	private final ILineSegment threatLine;
	private final ILineSegment protectionLine;
	private final ITrackedBot passReceiver;


	public DefenseBallThreat(
			final IVector2 vel,
			final ILineSegment threatLine,
			final ILineSegment protectionLine,
			final ITrackedBot passReceiver)
	{
		this.threatLine = threatLine;
		this.protectionLine = protectionLine;
		this.vel = vel;
		this.passReceiver = passReceiver;
	}


	@Override
	public IVector2 getPos()
	{
		return threatLine.getStart();
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
		return EDefenseThreatType.BALL_TO_GOAL;
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
