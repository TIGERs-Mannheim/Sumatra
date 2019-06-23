/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.commands.botskills.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.bot.params.BotMovementLimits;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.IVectorN;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class BotSkillOutput
{
	/** --- DRIVE output -- */
	private final IVector3			targetPos;
	private final IVector3			targetVelLocal;
	private final IVectorN			targetWheelVel;
	
	private final EDriveMode		modeXY;
	private final EDriveMode		modeW;
	
	private final MoveConstraints	driveLimits;
	
	/** --- DRIBBLER output -- */
	private final double				dribblerRPM;
	
	/** --- KICKER output -- */
	private final EKickerDevice	kickDevice;
	private final EKickerMode		kickMode;
	private final double				kickSpeed;
	
	/** Optional commands a bot skill can send */
	private final List<ACommand>	commands;
	
	
	private BotSkillOutput(final Builder builder)
	{
		targetPos = builder.targetPos;
		targetVelLocal = builder.targetVelLocal;
		targetWheelVel = builder.targetWheelVel;
		modeXY = builder.modeXY;
		modeW = builder.modeW;
		dribblerRPM = builder.dribblerRPM;
		kickDevice = builder.kickDevice;
		kickMode = builder.kickMode;
		kickSpeed = builder.kickSpeed;
		driveLimits = builder.driveLimits;
		commands = builder.commands;
	}
	
	/**
	 * Builder
	 */
	public static final class Builder
	{
		private IVector3			targetPos;
		private IVector3			targetVelLocal;
		private IVectorN			targetWheelVel;
		private EDriveMode		modeXY;
		private EDriveMode		modeW;
		private MoveConstraints	driveLimits;
		private double				dribblerRPM;
		private EKickerDevice	kickDevice;
		private EKickerMode		kickMode;
		private double				kickSpeed;
		private List<ACommand>	commands	= new ArrayList<>();
		
		
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
			targetPos = Vector3.zero();
			modeXY = EDriveMode.OFF;
			modeW = EDriveMode.OFF;
			driveLimits = new MoveConstraints(new BotMovementLimits());
			kickDevice = EKickerDevice.STRAIGHT;
			kickMode = EKickerMode.DISARM;
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
		 * @param cmd
		 * @return
		 */
		public Builder addCommand(final ACommand cmd)
		{
			commands.add(cmd);
			return this;
		}
		
		
		/**
		 * @param cmds
		 * @return
		 */
		public Builder addCommands(final List<ACommand> cmds)
		{
			commands.addAll(cmds);
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
		 * @param dribblerRPM
		 * @return this builder
		 */
		public Builder dribblerRPM(final double dribblerRPM)
		{
			this.dribblerRPM = dribblerRPM;
			return this;
		}
		
		
		/**
		 * @param kickDevice
		 * @return this builder
		 */
		public Builder kickDevice(final EKickerDevice kickDevice)
		{
			this.kickDevice = kickDevice;
			return this;
		}
		
		
		/**
		 * @param kickMode
		 * @return this builder
		 */
		public Builder kickMode(final EKickerMode kickMode)
		{
			this.kickMode = kickMode;
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
		 * @param driveLimits
		 * @return this builder
		 */
		public Builder driveLimits(final MoveConstraints driveLimits)
		{
			this.driveLimits = driveLimits;
			return this;
		}
		
		
		/**
		 * @return new instance
		 */
		public BotSkillOutput build()
		{
			if (modeXY != EDriveMode.OFF)
			{
				Validate.isTrue((targetPos != null) || (targetVelLocal != null) || (targetWheelVel != null));
			}
			Validate.notNull(modeXY);
			Validate.notNull(modeW);
			Validate.notNull(kickDevice);
			Validate.notNull(kickMode);
			Validate.notNull(driveLimits);
			
			return new BotSkillOutput(this);
		}
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
	
	
	public double getDribblerRPM()
	{
		return dribblerRPM;
	}
	
	
	public EKickerDevice getKickDevice()
	{
		return kickDevice;
	}
	
	
	public EKickerMode getKickMode()
	{
		return kickMode;
	}
	
	
	public double getKickSpeed()
	{
		return kickSpeed;
	}
	
	
	public MoveConstraints getDriveLimits()
	{
		return driveLimits;
	}
	
	
	public List<ACommand> getCommands()
	{
		return commands;
	}
}
