/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.paramoptimizer.redirect;

import java.util.Optional;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class DirectionChangeDetector
{
	@SuppressWarnings("unused")
	private static final Logger	log				= Logger.getLogger(DirectionChangeDetector.class.getName());
	
	private static final double	MIN_VEL			= 0.1;
	private static final double	MIN_ANGLE		= 0.2;
	
	private SimpleWorldFrame		lastSwf			= null;
	
	private ITrackedBall				firstBall		= null;
	private ITrackedBall				midBallPre		= null;
	private ITrackedBall				midBallPost		= null;
	private ITrackedBall				lastBall			= null;
	private long						midTimestamp	= 0;
	
	
	/**
	 * @param swf
	 * @return
	 */
	public Optional<DirectionChange> process(final SimpleWorldFrame swf)
	{
		if (lastSwf == null)
		{
			lastSwf = swf;
			return Optional.empty();
		}
		
		DirectionChange dc = null;
		ITrackedBall oldBall = lastSwf.getBall();
		ITrackedBall newBall = swf.getBall();
		
		final ITrackedBall preBall;
		if (midBallPost != null)
		{
			preBall = midBallPost;
		} else
		{
			preBall = firstBall;
		}
		
		if ((newBall.getVel().getLength() < MIN_VEL)
				|| (oldBall.getVel().getLength() < MIN_VEL))
		{
			// ball stopped
			if (midBallPost != null)
			{
				lastBall = newBall;
				dc = collectSample();
			}
			// reset
			firstBall = null;
			midBallPost = null;
			midBallPre = null;
			lastBall = null;
			midTimestamp = 0;
		} else if ((firstBall == null))
		{
			if (newBall.getVel().getLength() > oldBall.getVel().getLength())
			{
				// ball is getting faster and is fast enough
				firstBall = newBall;
			}
		} else if (preBall.getVel().angleToAbs(
				newBall.getVel()).orElse(0.0) > MIN_ANGLE)
		{
			if (midBallPre != null)
			{
				if ((swf.getTimestamp() - midTimestamp) > 2e8)
				{
					lastBall = oldBall;
					dc = collectSample();
					firstBall = midBallPost;
					lastBall = null;
					midBallPre = oldBall;
					midTimestamp = swf.getTimestamp();
				}
			} else
			{
				midBallPre = oldBall;
				midTimestamp = swf.getTimestamp();
			}
			midBallPost = newBall;
		}
		
		lastSwf = swf;
		
		return Optional.ofNullable(dc);
	}
	
	
	private DirectionChange collectSample()
	{
		DirectionChange dc = new DirectionChange();
		dc.velIn = midBallPre.getVel();
		dc.dirIn = midBallPre.getPos().subtractNew(firstBall.getPos()).normalize();
		dc.velOut = midBallPost.getVel();
		dc.dirOut = lastBall.getPos().subtractNew(midBallPost.getPos()).normalize();
		
		ILine line1 = Line.fromPoints(firstBall.getPos(), midBallPre.getPos());
		ILine line2 = Line.fromPoints(midBallPost.getPos(), lastBall.getPos());
		dc.intersection = LineMath.intersectionPoint(line1, line2).orElse(midBallPre.getPos());
		dc.timestamp = midTimestamp;
		return dc;
	}
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public static class DirectionChange
	{
		private IVector2	velIn, velOut, dirIn, dirOut;
		private IVector2	intersection;
		private long		timestamp;
		
		
		/**
		 * @return the velIn
		 */
		public IVector2 getVelIn()
		{
			return velIn;
		}
		
		
		/**
		 * @return the velOut
		 */
		public IVector2 getVelOut()
		{
			return velOut;
		}
		
		
		/**
		 * @return the dirIn
		 */
		public IVector2 getDirIn()
		{
			return dirIn;
		}
		
		
		/**
		 * @return the dirOut
		 */
		public IVector2 getDirOut()
		{
			return dirOut;
		}
		
		
		/**
		 * @return the intersection
		 */
		public IVector2 getIntersection()
		{
			return intersection;
		}
		
		
		/**
		 * @return the timestamp
		 */
		public long getTimestamp()
		{
			return timestamp;
		}
		
		
		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			builder.append("DirectionChange [timestamp=");
			builder.append(timestamp);
			builder.append(", intersection=");
			builder.append(intersection);
			builder.append(", dirIn=");
			builder.append(dirIn);
			builder.append(", velIn=");
			builder.append(velIn);
			builder.append(", dirOut=");
			builder.append(dirOut);
			builder.append(", velOut=");
			builder.append(velOut);
			builder.append("]");
			return builder.toString();
		}
		
		
	}
}
