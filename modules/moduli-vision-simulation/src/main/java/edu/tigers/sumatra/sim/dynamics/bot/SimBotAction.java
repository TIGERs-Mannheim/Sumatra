package edu.tigers.sumatra.sim.dynamics.bot;

import org.apache.commons.lang.Validate;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.bot.params.BotMovementLimits;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.IVectorN;


/**
 * Data that is required by a simulated bot to be actuated
 */
public class SimBotAction
{
	/** --- DRIVE output -- */
	private final IVector3 targetPos;
	private final IVector3 targetVelLocal;
	private final IVectorN targetWheelVel;
	private final IVector2 primaryDirection;
	
	private final EDriveMode modeXY;
	private final EDriveMode modeW;
	
	private final MoveConstraints driveLimits;
	private final boolean strictVelocityLimit;
	
	private final double kickSpeed;
	private final boolean chip;
	private final boolean disarm;
	private final double dribbleRpm;
	
	
	private SimBotAction(final Builder builder)
	{
		targetPos = builder.targetPos;
		targetVelLocal = builder.targetVelLocal;
		targetWheelVel = builder.targetWheelVel;
		primaryDirection = builder.primaryDirection;
		modeXY = builder.modeXY;
		modeW = builder.modeW;
		driveLimits = builder.driveLimits;
		strictVelocityLimit = builder.strictVelocityLimit;
		kickSpeed = builder.kickSpeed;
		chip = builder.chip;
		disarm = builder.disarm;
		dribbleRpm = builder.dribbleRpm;
	}
	
	
	public static SimBotAction idle()
	{
		return Builder.create().empty().build();
	}
	
	
	public IVector3 getTargetPos()
	{
		return targetPos;
	}
	
	
	public IVector3 getTargetVelLocal()
	{
		return targetVelLocal;
	}
	
	
	public IVectorN getTargetWheelVel()
	{
		return targetWheelVel;
	}
	
	
	public EDriveMode getModeXY()
	{
		return modeXY;
	}
	
	
	public EDriveMode getModeW()
	{
		return modeW;
	}
	
	
	public IVector2 getPrimaryDirection()
	{
		return primaryDirection;
	}
	
	
	public MoveConstraints getDriveLimits()
	{
		return driveLimits;
	}
	
	
	public boolean isStrictVelocityLimit()
	{
		return strictVelocityLimit;
	}
	
	
	public double getKickSpeed()
	{
		return kickSpeed;
	}
	
	
	public boolean isChip()
	{
		return chip;
	}
	
	
	public boolean isDisarm()
	{
		return disarm;
	}
	
	
	public double getDribbleRpm()
	{
		return dribbleRpm;
	}
	
	/**
	 * Builder
	 */
	public static final class Builder
	{
		private IVector3 targetPos;
		private IVector3 targetVelLocal;
		private IVectorN targetWheelVel;
		private IVector2 primaryDirection;
		private EDriveMode modeXY = EDriveMode.OFF;
		private EDriveMode modeW = EDriveMode.OFF;
		private MoveConstraints driveLimits;
		private boolean strictVelocityLimit;
		private double kickSpeed = 0;
		private boolean chip = false;
		private boolean disarm = false;
		private double dribbleRpm = 0;
		
		
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
		 * @return
		 */
		public Builder empty()
		{
			modeXY = EDriveMode.OFF;
			modeW = EDriveMode.OFF;
			driveLimits = new MoveConstraints(new BotMovementLimits());
			return this;
		}
		
		
		/**
		 * @param targetPos
		 * @return this builder
		 */
		public Builder targetPos(final IVector3 targetPos)
		{
			this.targetPos = targetPos;
			return this;
		}
		
		
		/**
		 * @param targetVelLocal
		 * @return this builder
		 */
		public Builder targetVelLocal(final IVector3 targetVelLocal)
		{
			this.targetVelLocal = targetVelLocal;
			return this;
		}
		
		
		/**
		 * @param targetWheelVel
		 * @return this builder
		 */
		public Builder targetWheelVel(final IVectorN targetWheelVel)
		{
			this.targetWheelVel = targetWheelVel;
			return this;
		}
		
		
		/**
		 * @param primaryDirection
		 * @return this builder
		 */
		public Builder primaryDirection(final IVector2 primaryDirection)
		{
			this.primaryDirection = primaryDirection;
			return this;
		}
		
		
		/**
		 * @param modeXY
		 * @return this builder
		 */
		public Builder modeXY(final EDriveMode modeXY)
		{
			this.modeXY = modeXY;
			return this;
		}
		
		
		/**
		 * @param modeW
		 * @return this builder
		 */
		public Builder modeW(final EDriveMode modeW)
		{
			this.modeW = modeW;
			return this;
		}
		
		
		/**
		 * @param driveLimits
		 * @return this builder
		 */
		public Builder driveLimits(final MoveConstraints driveLimits)
		{
			this.driveLimits = driveLimits;
			return this;
		}
		
		
		/**
		 * @param strictVelocityLimit
		 * @return this builder
		 */
		public Builder strictVelocityLimit(final boolean strictVelocityLimit)
		{
			this.strictVelocityLimit = strictVelocityLimit;
			return this;
		}
		
		
		/**
		 * @param kickSpeed
		 * @return this builder
		 */
		public Builder kickSpeed(final double kickSpeed)
		{
			this.kickSpeed = kickSpeed;
			return this;
		}
		
		
		/**
		 * @param chip
		 * @return this builder
		 */
		public Builder chip(final boolean chip)
		{
			this.chip = chip;
			return this;
		}
		
		
		/**
		 * @param disarm
		 * @return this builder
		 */
		public Builder disarm(final boolean disarm)
		{
			this.disarm = disarm;
			return this;
		}
		
		
		/**
		 * @param dribbleRpm
		 * @return this builder
		 */
		public Builder dribbleRpm(final double dribbleRpm)
		{
			this.dribbleRpm = dribbleRpm;
			return this;
		}
		
		
		/**
		 * @return new instance
		 */
		public SimBotAction build()
		{
			if (modeXY != EDriveMode.OFF)
			{
				Validate.isTrue((targetPos != null) || (targetVelLocal != null) || (targetWheelVel != null));
			}
			Validate.notNull(modeXY);
			Validate.notNull(modeW);
			Validate.notNull(driveLimits);
			
			return new SimBotAction(this);
		}
	}
}
