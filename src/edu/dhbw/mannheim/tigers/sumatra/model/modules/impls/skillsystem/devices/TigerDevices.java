/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 31, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices;

import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.KickerModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.functions.Function1dPoly;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.functions.IFunction1D;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.EKickerDevice;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.EKickerMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.TigerKickerKickV3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerDribble;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Devices like kicker, chipper and dribbler of our tiger bot
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TigerDevices
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger			log							= Logger.getLogger(TigerDevices.class.getName());
	
	/** constant for using maximum kicker power */
	public static final float				KICKER_MAX					= -1f;
	
	private boolean							dribblerOn					= false;
	
	@Configurable(comment = "Vel [rpm] - default speed of dribble device for on/off toggle", spezis = {
			"", "GRSIM" })
	private static int						dribbleDefaultRpm			= 10000;
	
	@Configurable(comment = "Vel [duration] - kicker duration for kickMax (not automatically the highest possible duration!", spezis = {
			"", "GRSIM" })
	private static int						straightKickMaxDuration	= 6000;
	
	@Configurable(comment = "Function f s.t. duration = f(chipDist,rollDist)", spezis = {
			"", "GRSIM" })
	private static IFunction1D				chipDurationFn				= Function1dPoly.constant(6000);
	
	@Configurable(comment = "Function f s.t. dribbleSpeed = f(chipDist,rollDist)", spezis = {
			"", "GRSIM" })
	private static IFunction1D				chipDribbleDurationFn	= Function1dPoly.constant(6000);
	
	@Configurable(comment = "Function f s.t. duration = f(chipDist)", spezis = { "", "GRSIM" })
	private static IFunction1D				chipFastDurationFn		= Function1dPoly.constant(6000);
	
	
	private KickParams						lastParams					= null;
	
	private final transient KickerModel	kickModel;
	
	
	/**
	 * @param botType
	 */
	public TigerDevices(final EBotType botType)
	{
		kickModel = KickerModel.forBot(botType);
	}
	
	private static class KickParams
	{
		final EKickerMode		mode;
		final EKickerDevice	device;
		final float				duration;
		final float				kickSpeed;
		
		
		/**
		 * @param mode
		 * @param device
		 * @param duration
		 * @param kickSpeed
		 */
		public KickParams(final EKickerMode mode, final EKickerDevice device, final float duration, final float kickSpeed)
		{
			super();
			this.mode = mode;
			this.device = device;
			this.duration = duration;
			this.kickSpeed = kickSpeed;
		}
		
		
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = (prime * result) + ((device == null) ? 0 : device.hashCode());
			result = (prime * result) + (int) duration;
			result = (prime * result) + ((mode == null) ? 0 : mode.hashCode());
			return result;
		}
		
		
		@Override
		public boolean equals(final Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj == null)
			{
				return false;
			}
			if (getClass() != obj.getClass())
			{
				return false;
			}
			KickParams other = (KickParams) obj;
			if (device != other.device)
			{
				return false;
			}
			if (duration != other.duration)
			{
				return false;
			}
			if (kickSpeed != other.kickSpeed)
			{
				return false;
			}
			if (mode != other.mode)
			{
				return false;
			}
			return true;
		}
		
		
		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			builder.append("KickParams [mode=");
			builder.append(mode);
			builder.append(", device=");
			builder.append(device);
			builder.append(", duration=");
			builder.append(duration);
			builder.append(", kickSpeed=");
			builder.append(kickSpeed);
			builder.append("]");
			return builder.toString();
		}
	}
	
	
	private void logKickParams(final EKickerMode mode, final EKickerDevice device, final float duration,
			final float kickSpeed, final int dribble)
	{
		KickParams params = new KickParams(mode, device, duration, kickSpeed);
		if (!params.equals(lastParams))
		{
			log.trace(params.toString());
			lastParams = params;
		}
	}
	
	
	/**
	 * dribble with given speed
	 * 
	 * @param cmds
	 * @param rpm
	 */
	public void dribble(final List<ACommand> cmds, final int rpm)
	{
		if (rpm > 0)
		{
			dribblerOn = true;
		} else
		{
			dribblerOn = false;
		}
		cmds.add(new TigerDribble(rpm));
	}
	
	
	/**
	 * Switch dribble on (ref rpm) or off
	 * 
	 * @param cmds
	 * @param status
	 */
	public void dribble(final List<ACommand> cmds, final boolean status)
	{
		// if (!dribblerOn || (status != dribblerOn))
		{
			dribblerOn = status;
			cmds.add(new TigerDribble(status ? dribbleDefaultRpm : 0));
		}
	}
	
	
	/**
	 * Maximum kick strength
	 * 
	 * @param cmds already contain commands and will include disarm cmds afterwards
	 */
	public void kickMax(final List<ACommand> cmds)
	{
		kickGeneralSpeed(cmds, EKickerMode.ARM, EKickerDevice.STRAIGHT, 8, 0);
	}
	
	
	/**
	 * Kick with given duration
	 * 
	 * @param cmds
	 * @param duration
	 */
	@Deprecated
	public void kick(final List<ACommand> cmds, final int duration)
	{
		doKick(cmds, duration, EKickerMode.ARM);
	}
	
	
	/**
	 * Force Kick with given duration
	 * 
	 * @param cmds
	 * @param duration
	 */
	@Deprecated
	public void kickForce(final List<ACommand> cmds, final int duration)
	{
		doKick(cmds, duration, EKickerMode.FORCE);
	}
	
	
	private void doKick(final List<ACommand> cmds, final float duration, final EKickerMode mode)
	{
		final int device = TigerKickerKickV2.Device.STRAIGHT;
		ACommand cmd = new TigerKickerKickV2(device, mode, duration);
		logKickParams(mode, EKickerDevice.STRAIGHT, duration, -1, 0);
		cmds.add(cmd);
	}
	
	
	/**
	 * Chip with given duration and dribbler speed
	 * 
	 * @param cmds
	 * @param params
	 * @param mode
	 */
	@Deprecated
	public void chip(final List<ACommand> cmds, final ChipParams params, final EKickerMode mode)
	{
		if (params.getDribbleSpeed() > 0)
		{
			dribble(cmds, params.getDribbleSpeed());
		}
		doChip(cmds, params.getDuration(), mode);
	}
	
	
	@Deprecated
	private void doChip(final List<ACommand> cmds, final float duration, final EKickerMode mode)
	{
		final int device = EKickerDevice.CHIP.getValue();
		
		logKickParams(mode, EKickerDevice.CHIP, duration, -1, 0);
		cmds.add(new TigerKickerKickV2(device, mode, duration));
	}
	
	
	/**
	 * Chip or Straight kick with arm or force. General api for more flexible calling
	 * 
	 * @param cmds
	 * @param mode
	 * @param device
	 * @param duration
	 * @param dribble
	 */
	public void kickGeneralDuration(final List<ACommand> cmds, final EKickerMode mode, final EKickerDevice device,
			final int duration, final int dribble)
	{
		if (dribble > 0)
		{
			dribble(cmds, dribble);
		}
		float kickSpeed = -1;
		if (device == EKickerDevice.STRAIGHT)
		{
			kickSpeed = kickModel.getKickSpeed(duration);
		} else if (device == EKickerDevice.CHIP)
		{
			// TODO
			kickSpeed = 8;
		}
		logKickParams(mode, device, duration, kickSpeed, dribble);
		cmds.add(new TigerKickerKickV3(mode, device, kickSpeed, duration));
	}
	
	
	/**
	 * Chip or Straight kick with arm or force. General api for more flexible calling
	 * 
	 * @param cmds
	 * @param mode
	 * @param device
	 * @param kickSpeed
	 * @param dribble
	 */
	public void kickGeneralSpeed(final List<ACommand> cmds, final EKickerMode mode, final EKickerDevice device,
			final float kickSpeed, final int dribble)
	{
		if (dribble > 0)
		{
			dribble(cmds, dribble);
		}
		int duration = calcStraightDuration(kickSpeed);
		if (kickSpeed > 7.9f)
		{
			duration = straightKickMaxDuration;
		}
		logKickParams(mode, device, duration, kickSpeed, dribble);
		cmds.add(new TigerKickerKickV3(mode, device, kickSpeed, duration));
	}
	
	
	/**
	 * Puts the disarm commands to cmd
	 * 
	 * @param cmds already contain commands and will include disarm cmds afterwards
	 */
	public void disarm(final List<ACommand> cmds)
	{
		if (lastParams != null)
		{
			lastParams = null;
			log.trace("Disarm");
		}
		cmds.add(new TigerKickerKickV2(TigerKickerKickV2.Device.CHIP, EKickerMode.DISARM, 0));
		cmds.add(new TigerKickerKickV2(TigerKickerKickV2.Device.STRAIGHT, EKickerMode.DISARM, 0));
	}
	
	
	/**
	 * Switch all devices off
	 * 
	 * @param cmds
	 */
	public void allOff(final List<ACommand> cmds)
	{
		dribblerOn = false;
		dribble(cmds, false);
		disarm(cmds);
	}
	
	
	/**
	 * @param kickSpeed [m/s]
	 * @return
	 */
	public int calcStraightDuration(final float kickSpeed)
	{
		int dur = (int) kickModel.getDuration(kickSpeed);
		dur = Math.max(dur, 0);
		dur = Math.min(dur, straightKickMaxDuration);
		return dur;
	}
	
	
	/**
	 * @param duration
	 * @return the kick speed in [m/s]
	 */
	public float calcKickSpeed(final int duration)
	{
		return kickModel.getKickSpeed(duration);
	}
	
	
	/**
	 * @param chipDist [mm]
	 * @param rollDist [mm]
	 * @return
	 */
	public static ChipParams calcChipParams(final float chipDist, final float rollDist)
	{
		int duration = (int) chipDurationFn.eval(chipDist / 1000, rollDist / 1000);
		int dribbleSpeed = (int) chipDribbleDurationFn.eval(chipDist / 1000, rollDist / 1000);
		ChipParams params = new ChipParams(duration, dribbleSpeed);
		return params;
	}
	
	
	/**
	 * @param chipDist
	 * @return
	 */
	public static ChipParams calcChipFastParams(final float chipDist)
	{
		int duration = (int) chipFastDurationFn.eval(chipDist);
		duration = Math.min(duration, 10000);
		ChipParams params = new ChipParams(duration, 0);
		return params;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the on
	 */
	public final boolean isDribblerOn()
	{
		return dribblerOn;
	}
}
