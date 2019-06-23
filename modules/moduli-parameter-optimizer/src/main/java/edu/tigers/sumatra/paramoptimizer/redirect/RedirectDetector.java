/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.paramoptimizer.redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.paramoptimizer.redirect.DirectionChangeDetector.DirectionChange;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RedirectDetector
{
	@SuppressWarnings("unused")
	private static final Logger				log				= Logger.getLogger(RedirectDetector.class.getName());
	
	private static final long					MAX_FRAME_AGE	= (long) 10e9;
	private final DirectionChangeDetector	dcd				= new DirectionChangeDetector();
	
	private final List<RedirectSample>		buffer			= new ArrayList<>();
	
	
	/**
	 * @param swf
	 * @return
	 */
	public Optional<RedirectSample> process(final SimpleWorldFrame swf)
	{
		while (!buffer.isEmpty() && ((swf.getTimestamp() - buffer.get(0).getTimestamp()) > MAX_FRAME_AGE))
		{
			buffer.remove(0);
		}
		Optional<DirectionChange> dc = dcd.process(swf);
		if (dc.isPresent())
		{
			RedirectSample rs = findRedirectSample(dc.get().getTimestamp());
			if ((rs != null) && (rs.bot != null))
			{
				rs.dc = dc.get();
				if (VectorMath.distancePP(rs.bot.getBotKickerPos(), dc.get().getIntersection()) < 500)
				{
					return Optional.of(rs);
				}
			} else
			{
				log.warn("Could not find matching redirect sample. Probably buffer to small.");
			}
		}
		return Optional.empty();
	}
	
	
	private RedirectSample findRedirectSample(final long timestamp)
	{
		for (RedirectSample aBuffer : buffer)
		{
			if (aBuffer.getTimestamp() >= timestamp)
			{
				return aBuffer;
			}
		}
		return null;
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public static class RedirectSample
	{
		private DirectionChange		dc				= null;
		private long					timestamp	= 0;
		private ITrackedBot			bot			= null;
		private double					kickSpeed	= 0;
		private Optional<IVector2>	target		= Optional.empty();
		
		
		/**
		 * @return
		 */
		public double getDirectionChange()
		{
			return dc.getDirIn().multiplyNew(-1).angleToAbs(dc.getDirOut()).orElse(0.0);
		}
		
		
		/**
		 * @return the dc
		 */
		public DirectionChange getDc()
		{
			return dc;
		}
		
		
		/**
		 * @return the timestamp
		 */
		public long getTimestamp()
		{
			return timestamp;
		}
		
		
		/**
		 * @return the bot
		 */
		public ITrackedBot getBot()
		{
			return bot;
		}
		
		
		/**
		 * @return the kickSpeed
		 */
		public double getKickSpeed()
		{
			return kickSpeed;
		}
		
		
		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			builder.append("RedirectSample [timestamp=");
			builder.append(timestamp);
			builder.append(", kickSpeed=");
			builder.append(kickSpeed);
			builder.append(", bot=");
			if (bot != null)
			{
				builder.append(bot.getBotId());
			}
			builder.append(", dc=");
			builder.append(dc);
			builder.append(", dirChange=");
			builder.append(getDirectionChange());
			builder.append("]");
			return builder.toString();
		}
		
		
		/**
		 * @return the target
		 */
		public Optional<IVector2> getTarget()
		{
			return target;
		}
	}
}
