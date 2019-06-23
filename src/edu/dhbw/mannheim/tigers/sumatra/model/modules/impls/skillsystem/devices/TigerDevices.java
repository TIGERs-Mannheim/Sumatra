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

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.functions.Function1dPoly;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.functions.IFunction1D;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerDribble;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2.EKickerMode;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;
import edu.dhbw.mannheim.tigers.sumatra.util.units.DistanceUnit;


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
	
	private static final Logger	log									= Logger.getLogger(TigerDevices.class.getName());
	
	/** constant for using maximum kicker power */
	public static final float		KICKER_MAX							= -1f;
	private static final float		GRAVITY								= 9.81f;
	
	private boolean					dribblerOn							= false;
	
	@Configurable(comment = "Vel [rpm] - default speed of dribble device for on/off toggle", speziType = EBotType.class, spezis = { "GRSIM" })
	private static int				dribbleDefaultRpm					= 10000;
	
	@Configurable(comment = "Vel [duration] - kicker duration for kickMax (not automatically the highest possible duration!", speziType = EBotType.class, spezis = { "GRSIM" })
	private static int				straightKickDefaultDuration	= 10000;
	
	@Configurable(comment = "Vel [duration] - max kicker duration that should be applied on straight kicker", speziType = EBotType.class, spezis = { "GRSIM" })
	private static int				straightKickMaxDuration			= 10000;
	
	@Configurable(comment = "Function f s.t. duration = f(dist,endVel)", speziType = EBotType.class, spezis = { "GRSIM" })
	private static IFunction1D		straightDurationFn				= Function1dPoly.constant(6000);
	
	@Configurable(comment = "Function f s.t. duration = f(chipDist,rollDist)", speziType = EBotType.class, spezis = { "GRSIM" })
	private static IFunction1D		chipDurationFn						= Function1dPoly.constant(6000);
	
	@Configurable(comment = "Function f s.t. dribbleSpeed = f(chipDist,rollDist)", speziType = EBotType.class, spezis = { "GRSIM" })
	private static IFunction1D		chipDribbleDurationFn			= Function1dPoly.constant(6000);
	
	@Configurable(comment = "Function f s.t. duration = f(chipDist)", speziType = EBotType.class, spezis = { "GRSIM" })
	private static IFunction1D		chipFastDurationFn				= Function1dPoly.constant(6000);
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public TigerDevices()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- public-method(s) -----------------------------------------------------
	// --------------------------------------------------------------------------
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
		if (!dribblerOn || (status != dribblerOn))
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
		doKick(cmds, straightKickDefaultDuration, EKickerMode.ARM);
	}
	
	
	/**
	 * Kick with given duration
	 * 
	 * @param cmds
	 * @param duration
	 */
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
	public void kickForce(final List<ACommand> cmds, final int duration)
	{
		doKick(cmds, duration, EKickerMode.FORCE);
	}
	
	
	private void doKick(final List<ACommand> cmds, final float duration, final EKickerMode mode)
	{
		final int device = TigerKickerKickV2.Device.STRAIGHT;
		ACommand cmd = new TigerKickerKickV2(device, mode, duration);
		log.trace("Kick with: duration=" + duration + " mode=" + mode);
		cmds.add(cmd);
	}
	
	
	/**
	 * Chip with given duration and dribbler speed
	 * 
	 * @param cmds
	 * @param params
	 * @param mode
	 */
	public void chip(final List<ACommand> cmds, final ChipParams params, final EKickerMode mode)
	{
		if (params.getDribbleSpeed() > 0)
		{
			dribble(cmds, params.getDribbleSpeed());
		}
		doChip(cmds, params.getDuration(), mode);
	}
	
	
	private void doChip(final List<ACommand> cmds, final float duration, final EKickerMode mode)
	{
		final int device = EKickDevice.CHIP.getValue();
		
		log.debug("Chip kick duration: " + duration);
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
	public void kickGeneral(final List<ACommand> cmds, final EKickerMode mode, final EKickDevice device,
			final float duration, final int dribble)
	{
		if (dribble > 0)
		{
			dribble(cmds, dribble);
		}
		log.debug("Kick general duration=" + duration + " mode=" + mode + " device=" + device);
		cmds.add(new TigerKickerKickV2(device.getValue(), mode, duration));
	}
	
	
	/**
	 * Puts the disarm commands to cmd
	 * 
	 * @param cmds already contain commands and will include disarm cmds afterwards
	 */
	public void disarm(final List<ACommand> cmds)
	{
		cmds.add(new TigerKickerKickV2(TigerKickerKickV2.Device.CHIP, EKickerMode.DISARM, 0));
		cmds.add(new TigerKickerKickV2(TigerKickerKickV2.Device.STRAIGHT, EKickerMode.DISARM, 0));
		log.trace("Disarm");
	}
	
	
	/**
	 * Switch all devices off
	 * 
	 * @param cmds
	 */
	public void allOff(final List<ACommand> cmds)
	{
		dribblerOn = true;
		dribble(cmds, false);
		disarm(cmds);
	}
	
	
	/**
	 * This function calculates a firing duration for the kicking device of the tiger robot to fit a specific pass
	 * length and an end velocity
	 * 
	 * @param kickLengthMM the length of the path. [mm]
	 * @param endVelocity of the ball after he covers the kick length.
	 * @param botType
	 * @return the firing duration for the kicking device.
	 */
	@Deprecated
	public static int calcStraightFiringDuration(final float kickLengthMM, final float endVelocity,
			final EBotType botType)
	{
		float ballFrictionSlide = 0.5f;
		float ballFrictionRoll = 0.05f;
		float edgeFactor = 0.5f;
		float refVelocity = 3.9f;
		
		/*
		 * E(ges) = E(kin) + E(reibung)
		 * E(reibung) = m * g * rkoeffizient * s
		 * Fall1 (nur gleiten):
		 * E(kin_v0) = E(gleitreib) + E(kin_end)
		 * Fall2 (gleiten und rollen):
		 * E(kin_v0) = E(gleitreib) + E(rollreib) + E(kin_end)
		 * weg_gleitreib = (1-edgefactor^2)*v0^2 / 2 * �_gleit * 9.81 //ergibt sich aus der betrachtung nach fall1
		 * weg_rollreib = gesamtweg - weg_gleitreib
		 * dann einsetzten und nach v0 umstelen
		 */
		
		int duration = 0;
		float kickLength = DistanceUnit.MILLIMETERS.toMeters(kickLengthMM);
		
		if (kickLengthMM == KICKER_MAX)
		{
			duration = straightKickDefaultDuration;
		} else
		{
			// start velocity
			float v0 = 0;
			
			// slide only movement
			v0 = SumatraMath.sqrt((2 * ballFrictionSlide * GRAVITY * kickLength) + SumatraMath.square(endVelocity));
			
			if ((endVelocity / v0) < edgeFactor)
			{
				// slide only not valid, consider rolling as well
				v0 = SumatraMath.sqrt(((2 * ballFrictionRoll * GRAVITY * kickLength) + SumatraMath.square(endVelocity))
						/ (SumatraMath.square(edgeFactor) + ((ballFrictionRoll / ballFrictionSlide) * (1 - SumatraMath
								.square(edgeFactor)))));
			}
			
			if (v0 > refVelocity)
			{
				log.warn("you try to kick faster than possible: " + v0 + " (only " + refVelocity + ")");
			}
			
			duration = Math.round((straightKickMaxDuration / refVelocity) * v0);
		}
		
		if (duration > straightKickMaxDuration)
		{
			duration = straightKickMaxDuration;
		}
		return duration;
	}
	
	
	/**
	 * @param dist [mm]
	 * @param endVel [m/s]
	 * @return
	 */
	public static float calcStraightDuration(final float dist, final float endVel)
	{
		return straightDurationFn.eval(dist / 1000, endVel);
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
		int duration = (int) chipFastDurationFn.eval(chipDist / 1000);
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
	
	
	/**
	 * @return the straightKickMaxDuration
	 */
	public static final int getStraightKickMaxDuration()
	{
		return straightKickMaxDuration;
	}
}
