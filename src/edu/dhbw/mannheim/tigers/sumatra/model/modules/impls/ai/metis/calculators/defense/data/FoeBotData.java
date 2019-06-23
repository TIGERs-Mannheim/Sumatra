/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 22, 2014
 * Author(s): FelixB <bayer.fel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data;

import java.util.Comparator;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;


/**
 * FoeBotData
 * 
 * @author FelixB <bayer.fel@gmail.com>
 */
@Persistent(version = 3)
public class FoeBotData
{
	/**  */
	public static final Comparator<? super FoeBotData>	DANGER_COMPARATOR	= new DangerComparator();
	
	private final TrackedTigerBot								trackedFoeBot;
	
	private IVector2												bot2goal;
	private IVector2												bot2goalNearestToGoal;
	private IVector2												bot2goalNearestToBot;
	
	private IVector2												ball2bot;
	private IVector2												ball2botNearestToBall;
	private IVector2												ball2botNearestToBot;
	
	private List<IntersectionPoint>							bot2goalIntersecsBot2bot;
	private List<IntersectionPoint>							bot2goalIntersecsBall2bot;
	private List<IntersectionPoint>							ball2botIntersecsBot2bot;
	private List<IntersectionPoint>							ball2botIntersecsBot2goal;
	
	private boolean												posessesBall		= false;
	
	
	@SuppressWarnings("unused")
	private FoeBotData()
	{
		// berkeley support
		trackedFoeBot = null;
		
		bot2goal = null;
		bot2goalNearestToGoal = null;
		bot2goalNearestToBot = null;
		
		ball2bot = null;
		ball2botNearestToBall = null;
		ball2botNearestToBot = null;
		
		bot2goalIntersecsBot2bot = null;
		bot2goalIntersecsBall2bot = null;
		ball2botIntersecsBot2bot = null;
		ball2botIntersecsBot2goal = null;
	}
	
	
	/**
	 * @param foeBot
	 */
	public FoeBotData(final TrackedTigerBot foeBot)
	{
		trackedFoeBot = foeBot;
		
		bot2goal = null;
		bot2goalNearestToGoal = null;
		bot2goalNearestToBot = null;
		
		ball2bot = null;
		ball2botNearestToBall = null;
		ball2botNearestToBot = null;
		
		bot2goalIntersecsBot2bot = null;
		bot2goalIntersecsBall2bot = null;
		ball2botIntersecsBot2bot = null;
		ball2botIntersecsBot2goal = null;
	}
	
	
	/**
	 * TODO FelixB <bayer.fel@gmail.com>, add comment!
	 * 
	 * @param bot2goal
	 * @param bot2goalNearestToBot
	 * @param bot2goalNearestToGoal
	 * @return
	 */
	public FoeBotData setBot2goal(final IVector2 bot2goal, final IVector2 bot2goalNearestToBot,
			final IVector2 bot2goalNearestToGoal)
	{
		this.bot2goal = bot2goal;
		this.bot2goalNearestToBot = bot2goalNearestToBot;
		this.bot2goalNearestToGoal = bot2goalNearestToGoal;
		
		return this;
	}
	
	
	/**
	 * TODO FelixB <bayer.fel@gmail.com>, add comment!
	 * 
	 * @param ball2Bot
	 * @param ball2botNearestToBall
	 * @param ball2botNearestToBot
	 * @return
	 */
	public FoeBotData setBall2bot(final IVector2 ball2Bot, final IVector2 ball2botNearestToBall,
			final IVector2 ball2botNearestToBot)
	{
		ball2bot = ball2Bot;
		this.ball2botNearestToBall = ball2botNearestToBall;
		this.ball2botNearestToBot = ball2botNearestToBot;
		
		return this;
	}
	
	
	/**
	 * TODO FelixB <bayer.fel@gmail.com>, add comment!
	 * 
	 * @param bot2goalIntersecsBot2bot
	 * @return
	 */
	public FoeBotData setBot2goalIntersecsBot2bot(final List<IntersectionPoint> bot2goalIntersecsBot2bot)
	{
		this.bot2goalIntersecsBot2bot = bot2goalIntersecsBot2bot;
		
		return this;
	}
	
	
	/**
	 * TODO FelixB <bayer.fel@gmail.com>, add comment!
	 * 
	 * @param bot2goalIntersecsBall2bot
	 * @return
	 */
	public FoeBotData setBot2goalIntersecsBall2bot(final List<IntersectionPoint> bot2goalIntersecsBall2bot)
	{
		this.bot2goalIntersecsBall2bot = bot2goalIntersecsBall2bot;
		
		return this;
	}
	
	
	/**
	 * TODO FelixB <bayer.fel@gmail.com>, add comment!
	 * 
	 * @param ball2botIntersecsBot2bot
	 * @return
	 */
	public FoeBotData setBall2botIntersecsBot2bot(final List<IntersectionPoint> ball2botIntersecsBot2bot)
	{
		this.ball2botIntersecsBot2bot = ball2botIntersecsBot2bot;
		
		return this;
	}
	
	
	/**
	 * TODO FelixB <bayer.fel@gmail.com>, add comment!
	 * 
	 * @param ball2botIntersecsBot2goal
	 * @return
	 */
	public FoeBotData setBall2botIntersecsBot2goal(final List<IntersectionPoint> ball2botIntersecsBot2goal)
	{
		this.ball2botIntersecsBot2goal = ball2botIntersecsBot2goal;
		
		return this;
	}
	
	
	/**
	 * TODO FelixB <bayer.fel@gmail.com>, add comment!
	 * 
	 * @param possessesBall
	 * @return
	 */
	public FoeBotData setPossessesBall(final boolean possessesBall)
	{
		posessesBall = possessesBall;
		
		return this;
	}
	
	
	/**
	 * @return the covered foe bot
	 */
	public TrackedTigerBot getFoeBot()
	{
		return trackedFoeBot;
	}
	
	
	/**
	 * TODO FelixB <bayer.fel@gmail.com>, add comment!
	 * 
	 * @return
	 */
	public IVector2 getBot2goal()
	{
		return bot2goal;
	}
	
	
	/**
	 * TODO FelixB <bayer.fel@gmail.com>, add comment!
	 * 
	 * @return
	 */
	public IVector2 getBot2goalNearestToGoal()
	{
		return bot2goalNearestToGoal;
	}
	
	
	/**
	 * TODO FelixB <bayer.fel@gmail.com>, add comment!
	 * 
	 * @return
	 */
	public IVector2 getBot2goalNearestToBot()
	{
		return bot2goalNearestToBot;
	}
	
	
	/**
	 * TODO FelixB <bayer.fel@gmail.com>, add comment!
	 * 
	 * @return
	 */
	public IVector2 getBall2bot()
	{
		return ball2bot;
	}
	
	
	/**
	 * TODO FelixB <bayer.fel@gmail.com>, add comment!
	 * 
	 * @return
	 */
	public IVector2 getBall2botNearestToBall()
	{
		return ball2botNearestToBall;
	}
	
	
	/**
	 * TODO FelixB <bayer.fel@gmail.com>, add comment!
	 * 
	 * @return
	 */
	public IVector2 getBall2botNearestToBot()
	{
		return ball2botNearestToBot;
	}
	
	
	/**
	 * TODO FelixB <bayer.fel@gmail.com>, add comment!
	 * 
	 * @return
	 */
	public List<IntersectionPoint> getBot2goalIntersecsBot2bot()
	{
		return bot2goalIntersecsBot2bot;
	}
	
	
	/**
	 * TODO FelixB <bayer.fel@gmail.com>, add comment!
	 * 
	 * @return
	 */
	public List<IntersectionPoint> getBot2goalIntersecsBall2bot()
	{
		return bot2goalIntersecsBall2bot;
	}
	
	
	/**
	 * TODO FelixB <bayer.fel@gmail.com>, add comment!
	 * 
	 * @return
	 */
	public List<IntersectionPoint> getBall2botIntersecsBot2bot()
	{
		return ball2botIntersecsBot2bot;
	}
	
	
	/**
	 * TODO FelixB <bayer.fel@gmail.com>, add comment!
	 * 
	 * @return
	 */
	public List<IntersectionPoint> getBall2botIntersecsBot2goal()
	{
		return ball2botIntersecsBot2goal;
	}
	
	
	/**
	 * @return ball possession
	 */
	public boolean posessesBall()
	{
		return posessesBall;
	}
	
	@SuppressWarnings("unused")
	private static class DistanceComparator implements Comparator<FoeBotData>
	{
		@Override
		public int compare(final FoeBotData bot1, final FoeBotData bot2)
		{
			if (bot1.posessesBall())
			{
				return 1;
			} else if (bot2.posessesBall())
			{
				return -1;
			} else
			{
				float distBot1 = GeoMath.distancePP(bot1.getFoeBot(), AIConfig.getGeometry().getGoalOur().getGoalCenter());
				float distBot2 = GeoMath.distancePP(bot2.getFoeBot(), AIConfig.getGeometry().getGoalOur().getGoalCenter());
				return (int) Math.signum(distBot1 - distBot2);
			}
		}
	}
	
	@SuppressWarnings("unused")
	private static class DangerComparator implements Comparator<FoeBotData>
	{
		private static float	halfFieldWidth		= AIConfig.getGeometry().getFieldWidth() / 2f;
		
		/**
		 * The factor a bot becomes more dangerous if he is in the center of the field y wise.
		 */
		private static float	fieldWidthFactor	= 1.2f;
		
		
		@Override
		public int compare(final FoeBotData bot1, final FoeBotData bot2)
		{
			if (bot1.posessesBall())
			{
				return 1;
			} else if (bot2.posessesBall())
			{
				return -1;
			} else
			{
				float distBot1 = GeoMath.distancePP(bot1.getFoeBot(), AIConfig.getGeometry().getGoalOur().getGoalCenter());
				float distBot2 = GeoMath.distancePP(bot2.getFoeBot(), AIConfig.getGeometry().getGoalOur().getGoalCenter());
				
				distBot1 *= (((1f - fieldWidthFactor) / Math.abs(halfFieldWidth)) * bot1.getFoeBot().getPos().y())
						+ fieldWidthFactor;
				distBot2 *= (((1f - fieldWidthFactor) / Math.abs(halfFieldWidth)) * bot2.getFoeBot().getPos().y())
						+ fieldWidthFactor;
				
				return (int) Math.signum(distBot1 - distBot2);
			}
		}
	}
	
}
