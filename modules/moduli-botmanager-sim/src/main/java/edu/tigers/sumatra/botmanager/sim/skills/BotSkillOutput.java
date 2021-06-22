/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.sim.skills;

import edu.tigers.sumatra.bot.IMoveConstraints;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerMode;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.IVectorN;
import edu.tigers.sumatra.sim.dynamics.bot.EDriveMode;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotAction;


/**
 * Data that is produced by bot skills in simulation
 */
public class BotSkillOutput
{
	/** --- DRIVE output -- */
	private final SimBotAction action;


	private BotSkillOutput(final Builder builder)
	{
		action = builder.action.build();
	}

	/**
	 * Builder
	 */
	public static final class Builder
	{
		private SimBotAction.Builder action = SimBotAction.Builder.create();


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
			action.driveLimits(new MoveConstraints());
			return this;
		}


		/**
		 * @param targetPos
		 * @return this builder
		 */
		public Builder targetPos(final IVector3 targetPos)
		{
			action.targetPos(targetPos);
			return this;
		}


		/**
		 * @param targetVelLocal
		 * @return this builder
		 */
		public Builder targetVelLocal(final IVector3 targetVelLocal)
		{
			action.targetVelLocal(targetVelLocal);
			return this;
		}


		/**
		 * @param targetWheelVel
		 * @return this builder
		 */
		public Builder targetWheelVel(final IVectorN targetWheelVel)
		{
			action.targetWheelVel(targetWheelVel);
			return this;
		}


		/**
		 * @param primaryDirection
		 * @return this builder
		 */
		public Builder primaryDirection(final IVector2 primaryDirection)
		{
			action.primaryDirection(primaryDirection);
			return this;
		}


		/**
		 * @param modeXY
		 * @return this builder
		 */
		public Builder modeXY(final EDriveMode modeXY)
		{
			action.modeXY(modeXY);
			return this;
		}


		/**
		 * @param modeW
		 * @return this builder
		 */
		public Builder modeW(final EDriveMode modeW)
		{
			action.modeW(modeW);
			return this;
		}


		/**
		 * @param dribblerRPM
		 * @return this builder
		 */
		public Builder dribblerRPM(final double dribblerRPM)
		{
			action.dribbleRpm(dribblerRPM);
			return this;
		}


		/**
		 * @param kickDevice
		 * @return this builder
		 */
		public Builder kickDevice(final EKickerDevice kickDevice)
		{
			action.chip(kickDevice == EKickerDevice.CHIP);
			return this;
		}


		/**
		 * @param kickMode
		 * @return this builder
		 */
		public Builder kickMode(final EKickerMode kickMode)
		{
			action.disarm(kickMode == EKickerMode.DISARM);
			return this;
		}


		/**
		 * @param kickSpeed
		 * @return this builder
		 */
		public Builder kickSpeed(final double kickSpeed)
		{
			action.kickSpeed(kickSpeed * 1000);
			return this;
		}


		/**
		 * @param driveLimits
		 * @return this builder
		 */
		public Builder driveLimits(final IMoveConstraints driveLimits)
		{
			action.driveLimits(driveLimits);
			return this;
		}


		/**
		 * @param strictVelocityLimit
		 * @return this builder
		 */
		public Builder strictVelocityLimit(final boolean strictVelocityLimit)
		{
			action.strictVelocityLimit(strictVelocityLimit);
			return this;
		}


		/**
		 * @return new instance
		 */
		public BotSkillOutput build()
		{
			return new BotSkillOutput(this);
		}
	}


	public SimBotAction getAction()
	{
		return action;
	}
}
