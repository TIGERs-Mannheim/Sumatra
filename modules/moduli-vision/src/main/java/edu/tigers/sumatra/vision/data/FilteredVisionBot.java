/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision.data;

import org.apache.commons.lang.Validate;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Data structure for a filtered robot.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class FilteredVisionBot
{
	private final BotID		botID;
	private final IVector2	pos;
	private final IVector2	vel;
	private final double		orientation;
	private final double		angularVel;
	private final double		quality;
	
	
	private FilteredVisionBot(final BotID botID, final IVector2 pos, final IVector2 vel,
			final double orientation, final double angularVel, final double quality)
	{
		this.botID = botID;
		this.pos = pos;
		this.vel = vel;
		this.orientation = orientation;
		this.angularVel = angularVel;
		this.quality = quality;
	}
	
	
	public BotID getBotID()
	{
		return botID;
	}
	
	
	public IVector2 getPos()
	{
		return pos;
	}
	
	
	public IVector2 getVel()
	{
		return vel;
	}
	
	
	public double getOrientation()
	{
		return orientation;
	}
	
	
	public double getAngularVel()
	{
		return angularVel;
	}
	
	
	public double getQuality()
	{
		return quality;
	}
	
	
	@Override
	public String toString()
	{
		return "FilteredVisionBot{" +
				"botID=" + botID +
				", pos=" + pos +
				", vel=" + vel +
				", orientation=" + orientation +
				", angularVel=" + angularVel +
				'}';
	}
	
	/**
	 * Builder for sub class
	 */
	public static final class Builder
	{
		private BotID		botID;
		private IVector2	pos;
		private IVector2	vel;
		private Double		orientation;
		private Double		angularVel;
		private double		quality	= 0;
		
		
		private Builder()
		{
		}
		
		
		/**
		 * @return new builder
		 */
		public static Builder create()
		{
			return new Builder();
		}
		
		
		/**
		 * Empty bot.
		 * 
		 * @return
		 */
		public static FilteredVisionBot emptyBot()
		{
			return new FilteredVisionBot(BotID.noBot(), AVector2.ZERO_VECTOR, AVector2.ZERO_VECTOR, 0, 0, 0);
		}
		
		
		/**
		 * @param botID id of the bot
		 * @return this builder
		 */
		public Builder withId(final BotID botID)
		{
			this.botID = botID;
			return this;
		}
		
		
		/**
		 * @param pos of bot
		 * @return this builder
		 */
		public Builder withPos(final IVector2 pos)
		{
			this.pos = pos;
			return this;
		}
		
		
		/**
		 * @param vel of bot
		 * @return this builder
		 */
		public Builder withVel(final IVector2 vel)
		{
			this.vel = vel;
			return this;
		}
		
		
		/**
		 * @param orientation of bot
		 * @return this builder
		 */
		public Builder withOrientation(final double orientation)
		{
			this.orientation = orientation;
			return this;
		}
		
		
		/**
		 * @param aVel of bot
		 * @return this builder
		 */
		public Builder withAVel(final double aVel)
		{
			angularVel = aVel;
			return this;
		}
		
		
		/**
		 * @param quality of bot
		 * @return this builder
		 */
		public Builder withQuality(final double quality)
		{
			this.quality = quality;
			return this;
		}
		
		
		/**
		 * @return new instance
		 */
		public FilteredVisionBot build()
		{
			Validate.notNull(botID);
			Validate.notNull(pos);
			Validate.notNull(vel);
			Validate.notNull(orientation);
			Validate.notNull(angularVel);
			return new FilteredVisionBot(botID, pos, vel, orientation, angularVel, quality);
		}
	}
}
