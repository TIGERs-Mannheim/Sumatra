/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense.data;

import java.util.Optional;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class DefenseBallThreat implements IDefenseThreat
{
	private final IVector2 pos;
	private final IVector2 endPos;
	private final IVector2 vel;
	private final ITrackedBot passReceiver;
	
	
	/**
	 * @param pos
	 * @param endPos
	 * @param vel
	 * @param passReceiver
	 */
	public DefenseBallThreat(final IVector2 pos, final IVector2 endPos, final IVector2 vel,
			final ITrackedBot passReceiver)
	{
		this.pos = pos;
		this.endPos = Geometry.getField().nearestPointInside(endPos, pos);
		this.vel = vel;
		this.passReceiver = passReceiver;
	}
	
	
	@Override
	public ILineSegment getThreatLine()
	{
		return Lines.segmentFromPoints(pos, endPos);
	}
	
	
	@Override
	public double getScore()
	{
		return 0;
	}
	
	
	@Override
	public IVector2 getVel()
	{
		return vel;
	}
	
	
	@Override
	public boolean isBot()
	{
		return false;
	}
	
	
	/**
	 * @return the passReceiver
	 */
	public Optional<ITrackedBot> getPassReceiver()
	{
		return Optional.ofNullable(passReceiver);
	}
}
