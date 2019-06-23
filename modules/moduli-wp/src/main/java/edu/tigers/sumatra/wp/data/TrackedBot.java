/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 4, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.data;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.bot.DummyBot;
import edu.tigers.sumatra.bot.IBot;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;


/**
 * Simple data holder describing tracked bots
 * 
 * @see ATrackedObject
 * @author Gero
 */
@Persistent(version = 1)
public class TrackedBot extends ATrackedObject implements ITrackedBot
{
	private BotID		botId;
	
	private IVector2	pos			= AVector2.ZERO_VECTOR;
	/** m/s */
	private IVector2	vel			= AVector2.ZERO_VECTOR;
	/** m/s */
	private IVector2	acc			= AVector2.ZERO_VECTOR;
	
	/** [rad] */
	private double		angle			= 0;
	/** rad/s */
	private double		aVel			= 0;
	
	private double		aAcc			= 0;
	
	
	private boolean	ballContact	= false;
	private boolean	visible		= true;
	private IBot		bot;
	
	
	@SuppressWarnings("unused")
	protected TrackedBot()
	{
		super();
		botId = BotID.get();
		pos = AVector2.ZERO_VECTOR;
		vel = AVector2.ZERO_VECTOR;
		acc = AVector2.ZERO_VECTOR;
		angle = 0;
		aVel = 0;
		bot = new DummyBot();
	}
	
	
	/**
	 * @param timestamp
	 * @param botId
	 */
	public TrackedBot(final long timestamp, final BotID botId)
	{
		super(timestamp);
		this.botId = botId;
		bot = new DummyBot(botId);
	}
	
	
	/**
	 * Create a deep copy
	 * 
	 * @param o
	 */
	public TrackedBot(final ITrackedBot o)
	{
		super(o);
		botId = o.getBotId();
		pos = o.getPos();
		vel = o.getVel();
		acc = o.getAcc();
		angle = o.getAngle();
		aVel = o.getaVel();
		aAcc = o.getaAcc();
		ballContact = o.hasBallContact();
		visible = o.isVisible();
		bot = o.getBot();
	}
	
	
	/**
	 * @return
	 */
	@Override
	public ITrackedBot mirrorNew()
	{
		TrackedBot nBot = new TrackedBot(this);
		nBot.pos = pos.multiplyNew(-1);
		nBot.vel = vel.multiplyNew(-1);
		nBot.acc = acc.multiplyNew(-1);
		nBot.angle = AngleMath.normalizeAngle(angle + AngleMath.PI);
		nBot.aVel = aVel;
		nBot.aAcc = aAcc;
		return nBot;
	}
	
	
	/**
	 * Calculates the position of the dribbler/kicker of the given bot.
	 * Use this position for ball receivers, etc.
	 * 
	 * @return
	 */
	@Override
	public IVector2 getBotKickerPos()
	{
		return GeoMath.getBotKickerPos(getPos(), getAngle(), getCenter2DribblerDist());
	}
	
	
	@Override
	public IVector2 getBotKickerPosByTime(final double t)
	{
		
		return GeoMath.getBotKickerPos(getPosByTime(t), getAngle(), getCenter2DribblerDist());
	}
	
	
	/**
	 * @param t
	 * @return
	 */
	@Override
	public IVector2 getPosByTime(final double t)
	{
		synchronized (bot)
		{
			if (bot.getCurrentTrajectory().isPresent()
					&& (bot.getCurrentTrajectory().get().getRemainingTrajectoryTime(getTimestamp()) > 0.1))
			{
				return bot.getCurrentTrajectory().get().getPositionMM(getTimestamp() + (long) (t * 1e9)).getXYVector();
			}
			return getPos().addNew(getVel().multiplyNew(1000 * t));
		}
	}
	
	
	/**
	 * @param t
	 * @return
	 */
	@Override
	public IVector2 getVelByTime(final double t)
	{
		synchronized (bot)
		{
			if (bot.getCurrentTrajectory().isPresent()
					&& (bot.getCurrentTrajectory().get().getRemainingTrajectoryTime(getTimestamp()) > 0.1))
			{
				return bot.getCurrentTrajectory().get().getVelocity(getTimestamp() + (long) (t * 1e9)).getXYVector();
			}
			return getVel();
		}
	}
	
	
	/**
	 * @param t
	 * @return
	 */
	@Override
	public double getAngleByTime(final double t)
	{
		synchronized (bot)
		{
			if (bot.getCurrentTrajectory().isPresent()
					&& (bot.getCurrentTrajectory().get().getRemainingTrajectoryTime(getTimestamp()) > 0.1))
			{
				return bot.getCurrentTrajectory().get().getPosition(getTimestamp() + (long) (t * 1e9)).z();
			}
			return AngleMath.normalizeAngle(getAngle() + (getaVel() * t));
		}
	}
	
	
	@Override
	public IVector2 getPos()
	{
		return pos;
	}
	
	
	@Override
	public IVector2 getVel()
	{
		return vel;
	}
	
	
	@Override
	public IVector2 getAcc()
	{
		return acc;
	}
	
	
	@Override
	public BotID getBotId()
	{
		return botId;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public ETeamColor getTeamColor()
	{
		return getBotId().getTeamColor();
	}
	
	
	/**
	 * @return the angle
	 */
	@Override
	public double getAngle()
	{
		return angle;
	}
	
	
	/**
	 * @return the aVel
	 */
	@Override
	public double getaVel()
	{
		return aVel;
	}
	
	
	/**
	 * @return the aVel
	 */
	@Override
	public double getaAcc()
	{
		return aAcc;
	}
	
	
	/**
	 * @return the ballContact
	 */
	@Override
	public boolean hasBallContact()
	{
		return ballContact;
	}
	
	
	/**
	 * @return the visible
	 */
	@Override
	public boolean isVisible()
	{
		return visible;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public double getCenter2DribblerDist()
	{
		return bot.getCenter2DribblerDist();
	}
	
	
	/**
	 * @return the excludeFromAi
	 */
	@Override
	public boolean isAvailableToAi()
	{
		return bot.isAvailableToAi();
	}
	
	
	/**
	 * @return the bot
	 */
	@Override
	public final IBot getBot()
	{
		return bot;
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("TrackedBot [id=");
		builder.append(botId);
		builder.append(", pos=");
		builder.append(pos);
		builder.append(", vel=");
		builder.append(vel);
		builder.append(", angle=");
		builder.append(angle);
		builder.append(", aVel=");
		builder.append(aVel);
		builder.append("]");
		return builder.toString();
	}
	
	
	/**
	 * @param pos the pos to set
	 */
	public void setPos(final IVector2 pos)
	{
		this.pos = pos;
	}
	
	
	/**
	 * @param vel the vel to set
	 */
	public void setVel(final IVector2 vel)
	{
		this.vel = vel;
	}
	
	
	/**
	 * @param acc the acc to set
	 */
	public void setAcc(final IVector2 acc)
	{
		this.acc = acc;
	}
	
	
	/**
	 * @param angle the angle to set
	 */
	public void setAngle(final double angle)
	{
		this.angle = angle;
	}
	
	
	/**
	 * @param aVel the aVel to set
	 */
	public void setaVel(final double aVel)
	{
		this.aVel = aVel;
	}
	
	
	/**
	 * @param aAcc the aAcc to set
	 */
	public void setaAcc(final double aAcc)
	{
		this.aAcc = aAcc;
	}
	
	
	/**
	 * @param visible the visible to set
	 */
	public void setVisible(final boolean visible)
	{
		this.visible = visible;
	}
	
	
	/**
	 * contact to ball?
	 * 
	 * @param ballContact the ballContact to set
	 */
	public void setBallContact(final boolean ballContact)
	{
		this.ballContact = ballContact;
	}
	
	
	/**
	 * @param bot the bot to set
	 */
	public final void setBot(final IBot bot)
	{
		this.bot = bot;
	}
	
	
}