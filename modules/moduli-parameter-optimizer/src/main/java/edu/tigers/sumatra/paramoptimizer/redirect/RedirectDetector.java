/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 16, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.paramoptimizer.redirect;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.bot.IBot;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RedirectDetector
{
	@SuppressWarnings("unused")
	private static final Logger	log			= Logger.getLogger(RedirectDetector.class.getName());
	
	private static final double	TIME_WINDOW	= 0.2;
	private static final double	MIN_ANGLE	= 0.2;
	private final List<DataSet>	data			= new ArrayList<>();
	
	
	private boolean collectData(final SimpleWorldFrame swf)
	{
		// reset if ball not moving
		if (swf.getBall().getVel().getLength() < 0.1)
		{
			data.clear();
			return false;
		}
		
		DataSet ds = new DataSet();
		ds.timestamp = swf.getTimestamp();
		ds.vel = swf.getBall().getVel();
		ds.pos = swf.getBall().getPos();
		ITrackedBot bot = findBot(swf);
		if (bot != null)
		{
			ds.bot = bot;
		}
		data.add(ds);
		
		if (data.size() < 5)
		{
			return false;
		}
		
		while (((swf.getTimestamp() - data.get(0).timestamp) / 1e9) > TIME_WINDOW)
		{
			data.remove(0);
		}
		
		return true;
	}
	
	
	/**
	 * @param swf
	 * @return
	 */
	public Optional<RedirectDataSet> process(final SimpleWorldFrame swf)
	{
		if (!collectData(swf))
		{
			return Optional.empty();
		}
		
		List<Double> dirs = getDirections();
		double dirPre = dirs.get((dirs.size() / 2) - 1);
		double dirPost = dirs.get(dirs.size() / 2);
		if (Math.abs(AngleMath.getShortestRotation(dirPre, dirPost)) > MIN_ANGLE)
		{
			DataSet dsPre = data.get(0);
			DataSet dsPost = data.get(data.size() - 1);
			RedirectDataSet dataSet = new RedirectDataSet(dsPost.timestamp);
			dataSet.velIn = dsPre.vel;
			dataSet.velOut = dsPost.vel;
			
			ITrackedBot bot = dsPre.bot;
			if (bot != null)
			{
				double dist = GeoMath.distancePP(bot.getBotKickerPos(), data.get(data.size() / 2).pos);
				if (dist < 500)
				{
					dataSet.kickVel = new Vector2(bot.getAngle()).scaleTo(bot.getBot().getKickSpeed());
					dataSet.bot = bot.getBot();
					
					// DecimalFormat df = new DecimalFormat("0.000");
					// log.info(
					// "angle: " + df.format(GeoMath.angleBetweenVectorAndVector(dsPre.vel.multiplyNew(-1), dsPost.vel)));
					data.clear();
					return Optional.of(dataSet);
				}
			}
		}
		return Optional.empty();
	}
	
	
	private ITrackedBot findBot(final SimpleWorldFrame swf)
	{
		List<ITrackedBot> bots = new ArrayList<>(swf.getBots().values());
		
		bots.sort(new DistanceComparator(swf.getBall().getPos()));
		
		if (bots.isEmpty())
		{
			return null;
		}
		return bots.get(0);
	}
	
	
	private List<Double> getDirections()
	{
		List<Double> dirs = new ArrayList<>(data.size());
		for (DataSet ds : data)
		{
			dirs.add(ds.vel.getAngle());
		}
		return dirs;
	}
	
	
	private static class DataSet
	{
		long			timestamp;
		IVector2		pos;
		IVector2		vel;
		ITrackedBot	bot;
	}
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public static class RedirectDataSet
	{
		private final long	timestamp;
		private IVector2		velIn, velOut;
		private IVector2		kickVel	= null;
		private IBot			bot		= null;
		
		
		/**
		 * @param timestamp
		 */
		public RedirectDataSet(final long timestamp)
		{
			super();
			this.timestamp = timestamp;
		}
		
		
		/**
		 * @return the timestamp
		 */
		public long getTimestamp()
		{
			return timestamp;
		}
		
		
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
		 * @return the kickVel
		 */
		public IVector2 getKickVel()
		{
			return kickVel;
		}
		
		
		/**
		 * @return the bot
		 */
		public IBot getBot()
		{
			return bot;
		}
	}
	
	private static class DistanceComparator implements Comparator<ITrackedBot>
	{
		IVector2 pos;
		
		
		/**
		 * @param pos
		 */
		public DistanceComparator(final IVector2 pos)
		{
			this.pos = pos;
		}
		
		
		@Override
		public int compare(final ITrackedBot bot1, final ITrackedBot bot2)
		{
			double distBot1 = GeoMath.distancePP(bot1.getPos(), pos);
			double distBot2 = GeoMath.distancePP(bot2.getPos(), pos);
			return (int) Math.signum(distBot1 - distBot2);
		}
	}
}
