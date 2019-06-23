/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects;

import java.io.Serializable;
import java.util.Comparator;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.AObjectID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.DummyBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data.RobotMotionResult_V2;


/**
 * Simple data holder describing TIGER-bots recognized and tracked by the
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor}
 * <p>
 * <i>(Being aware of EJ-SE Items 13, 14 and 55: members are public to reduce noise)</i>
 * </p>
 * 
 * @see TrackedBot
 * @see ATrackedObject
 * @author Gero
 */
@Persistent(version = 2)
public class TrackedTigerBot extends TrackedBot
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private boolean	ballContact;
	
	private boolean	visible	= true;
	private ABot		bot		= new DummyBot();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@SuppressWarnings("unused")
	private TrackedTigerBot()
	{
		super();
	}
	
	
	/**
	 * Create TrackedTigerBot from TrackedBot
	 * 
	 * @param o
	 */
	protected TrackedTigerBot(final TrackedBot o, final ABot bot)
	{
		this(o.getId(), o.getPos(), o.getVel(), o.getAcc(), o.getHeight(), o.getAngle(), o.getaVel(), o.getaAcc(),
				o.confidence, bot, o.getTeamColor());
	}
	
	
	/**
	 * @param id
	 * @param pos
	 * @param vel
	 * @param acc
	 * @param height
	 * @param angle
	 * @param aVel
	 * @param aAcc
	 * @param confidence
	 * @param bot
	 * @param color
	 */
	public TrackedTigerBot(final AObjectID id, final IVector2 pos, final IVector2 vel, final IVector2 acc,
			final int height, final float angle, final float aVel,
			final float aAcc, final float confidence, final ABot bot, final ETeamColor color)
	{
		super(id, pos, vel, acc, height, angle, aVel, aAcc, confidence, ETeam.TIGERS, color);
		if (bot != null)
		{
			this.bot = bot;
		} else
		{
			this.bot = new DummyBot();
		}
	}
	
	
	/**
	 * Create a deep copy
	 * 
	 * @param o
	 */
	public TrackedTigerBot(final TrackedTigerBot o)
	{
		this(o.id, o.getPos(), o.getVel(), o.getAcc(), o.getHeight(), o.getAngle(), o.getaVel(), o.getaAcc(), o
				.getConfidence(), o.getBot(), o.getTeamColor());
		ballContact = o.ballContact;
		visible = o.visible;
	}
	
	
	/**
	 * static factory for creating a TrackedTigerBot
	 * 
	 * @param id
	 * @param motion
	 * @param height
	 * @param bot
	 * @param color
	 * @return
	 */
	public static TrackedTigerBot motionToTrackedBot(final AObjectID id, final RobotMotionResult_V2 motion,
			final int height, final ABot bot,
			final ETeamColor color)
	{
		TrackedBot trackedBot = TrackedBot.motionToTrackedBot(id, motion, height, color);
		return new TrackedTigerBot(trackedBot, bot);
	}
	
	
	/**
	 * Create a bot with zero-state
	 * 
	 * @param botId
	 * @param bot
	 * @return
	 */
	public static TrackedTigerBot defaultBot(final BotID botId, final ABot bot)
	{
		return new TrackedTigerBot(botId, AVector2.ZERO_VECTOR, AVector2.ZERO_VECTOR, AVector2.ZERO_VECTOR, 135, 0, 0, 0,
				0, bot, botId.getTeamColor());
	}
	
	
	/**
	 * @param t
	 * @return
	 */
	public IVector2 getPosByTime(final float t)
	{
		if (bot.getPathFinder().getCurPath() != null)
		{
			return bot.getPathFinder().getCurPath().getPosition(bot.getPathFinder().getCurPath().getVeryCurrentTime() + t);
		}
		return getPos().addNew(getVel().multiplyNew(t));
	}
	
	
	/**
	 * @param t
	 * @return
	 */
	public IVector2 getVelByTime(final float t)
	{
		if (bot.getPathFinder().getCurPath() != null)
		{
			return bot.getPathFinder().getCurPath().getVelocity(bot.getPathFinder().getCurPath().getVeryCurrentTime() + t);
		}
		return getVel();
	}
	
	
	/**
	 * @param t
	 * @return
	 */
	public float getAngleByTime(final float t)
	{
		if (bot.getPathFinder().getCurPath() != null)
		{
			return bot.getPathFinder().getCurPath()
					.getOrientation(bot.getPathFinder().getCurPath().getVeryCurrentTime() + t);
		}
		return AngleMath.normalizeAngle(getAngle() + (getaVel() * t));
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the botType
	 */
	public final EBotType getBotType()
	{
		return bot.getType();
	}
	
	
	/**
	 * @return
	 */
	public boolean isBlocked()
	{
		return (getBot() != null) && !getBot().getControlledBy().isEmpty();
	}
	
	
	/**
	 * @return the ballContact
	 */
	public boolean hasBallContact()
	{
		return ballContact;
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("TrackedTigerBot [bot=");
		builder.append(bot);
		builder.append(", pos=");
		builder.append(getPos());
		builder.append("]");
		return builder.toString();
	}
	
	
	/**
	 * @return the bot
	 */
	public final ABot getBot()
	{
		return bot;
	}
	
	
	/**
	 * Used by WP, do not modify!
	 * 
	 * @param ballContact the ballContact to set
	 */
	public final void setBallContact(final boolean ballContact)
	{
		this.ballContact = ballContact;
	}
	
	
	/**
	 * @return the visible
	 */
	public final boolean isVisible()
	{
		return visible;
	}
	
	
	/**
	 * @param visible the visible to set
	 */
	public final void setVisible(final boolean visible)
	{
		this.visible = visible;
	}
	
	/**  */
	public static final Comparator<? super TrackedTigerBot>	DISTANCE_TO_GOAL_COMPARATOR	= new DistanceToGoalComparator();
	
	
	private static class DistanceToGoalComparator implements Comparator<TrackedTigerBot>, Serializable
	{
		
		/**  */
		private static final long	serialVersionUID	= 1794858044291002364L;
		
		
		@Override
		public int compare(final TrackedTigerBot v1, final TrackedTigerBot v2)
		{
			if (GeoMath.distancePP(v1.getPos(), AIConfig.getGeometry().getGoalOur().getGoalCenter()) > GeoMath.distancePP(
					v2.getPos(), AIConfig.getGeometry().getGoalOur().getGoalCenter()))
			{
				return 1;
			} else if (GeoMath.distancePP(v1.getPos(), AIConfig.getGeometry().getGoalOur().getGoalCenter()) < GeoMath
					.distancePP(v2.getPos(), AIConfig.getGeometry().getGoalOur().getGoalCenter()))
			{
				return -1;
			} else
			{
				return 0;
			}
		}
		
	}
}
