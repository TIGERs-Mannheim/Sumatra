/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 31, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices;

import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.functions.IFunction1D;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerDribble;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.util.units.DistanceUnit;


/**
 * Devices like kicker, chipper and dribbler of our tiger bot
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class TigerDevices
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log			= Logger.getLogger(TigerDevices.class.getName());
	
	/** constant for using maximum kicker power */
	public static final float		KICKER_MAX	= -1f;
	private static final float		GRAVITY		= 9.81f;
	
	private final EBotType			botType;
	private boolean					dribblerOn	= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param botType
	 */
	public TigerDevices(EBotType botType)
	{
		this.botType = botType;
	}
	
	
	// --------------------------------------------------------------------------
	// --- public-method(s) -----------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * dribble with given speed
	 * @param cmds
	 * @param rpm
	 */
	public void dribble(List<ACommand> cmds, int rpm)
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
	 * @param cmds
	 * @param status
	 */
	public void dribble(List<ACommand> cmds, boolean status)
	{
		if (!dribblerOn || (status != dribblerOn))
		{
			dribblerOn = status;
			cmds.add(new TigerDribble(status ? AIConfig.getSkills(botType).getRefRPM() : 0));
		}
	}
	
	
	/**
	 * Straight kick
	 * 
	 * @param cmds can already contain commands and will include disarm cmds afterwards
	 * @param kickLength
	 * @param ballEndVelocity
	 */
	public void kickForce(List<ACommand> cmds, float kickLength, float ballEndVelocity)
	{
		final int mode = TigerKickerKickV2.Mode.FORCE;
		doKick(cmds, kickLength, ballEndVelocity, mode);
	}
	
	
	/**
	 * Straight kick
	 * 
	 * @param cmds can already contain commands and will include disarm cmds afterwards
	 * @param kickLength
	 * @param ballEndVelocity
	 */
	public void kick(List<ACommand> cmds, float kickLength, float ballEndVelocity)
	{
		final int mode = TigerKickerKickV2.Mode.ARM;
		doKick(cmds, kickLength, ballEndVelocity, mode);
	}
	
	
	/**
	 * Maximum kick strength
	 * 
	 * @param cmds already contain commands and will include disarm cmds afterwards
	 */
	public void kickMax(List<ACommand> cmds)
	{
		doKick(cmds, KICKER_MAX, 0, TigerKickerKickV2.Mode.ARM);
	}
	
	
	/**
	 * Kick by duration. This is only intended for testing
	 * 
	 * @param cmds
	 * @param duration
	 * @param dribbleSpeed [rpm]
	 */
	public void kickTest(List<ACommand> cmds, float duration, int dribbleSpeed)
	{
		dribble(cmds, dribbleSpeed);
		
		final int mode = TigerKickerKickV2.Mode.ARM;
		doKick(cmds, duration, mode);
	}
	
	
	/**
	 * Kick and use dribbler
	 * 
	 * @param cmds
	 * @param kickLength
	 * @param ballEndVelocity may also be negative
	 */
	public void kickDribble(List<ACommand> cmds, float kickLength, float ballEndVelocity)
	{
		final int dribbleSpeed = calcKickDribbleSpeed(kickLength, botType);
		dribble(cmds, dribbleSpeed);
		
		final int mode = TigerKickerKickV2.Mode.DRIBBLER;
		doKick(cmds, kickLength, ballEndVelocity, mode);
	}
	
	
	private void doKick(List<ACommand> cmds, float kickLength, float ballEndVelocity, int mode)
	{
		final int duration = calcStraightFiringDuration(kickLength, ballEndVelocity, botType);
		doKick(cmds, duration, mode);
	}
	
	
	private void doKick(List<ACommand> cmds, float duration, int mode)
	{
		final int device = TigerKickerKickV2.Device.STRAIGHT;
		cmds.add(new TigerKickerKickV2(device, mode, duration));
	}
	
	
	/**
	 * Chip kick without dribbler, so ball will roll
	 * 
	 * @param cmds
	 * @param kickLength
	 */
	public void chipRoll(List<ACommand> cmds, float kickLength)
	{
		final int duration = calcChipFiringDuration(kickLength, botType);
		doChip(cmds, duration, TigerKickerKickV2.Mode.ARM);
	}
	
	
	/**
	 * Chip kick with dribbler, so that ball will almost stop on touch down
	 * 
	 * @param cmds
	 * @param kickLength
	 * @param rollDistance atm not used. Will be assumed to be 0
	 */
	public void chipStop(List<ACommand> cmds, float kickLength, float rollDistance)
	{
		final int dribbleSpeed = calcChipDribbleSpeed(kickLength, botType);
		dribble(cmds, dribbleSpeed);
		
		final int duration = calcChipFiringDuration(kickLength, botType);
		doChip(cmds, duration, TigerKickerKickV2.Mode.DRIBBLER);
	}
	
	
	/**
	 * Test chipper by specifying the duration.
	 * 
	 * @param cmds
	 * @param duration
	 * @param dribbleSpeed
	 */
	public void chipTest(List<ACommand> cmds, float duration, float dribbleSpeed)
	{
		final int mode;
		if (dribbleSpeed > 0)
		{
			mode = TigerKickerKickV2.Mode.DRIBBLER;
		} else
		{
			mode = TigerKickerKickV2.Mode.ARM;
		}
		doChip(cmds, duration, mode);
	}
	
	
	private void doChip(List<ACommand> cmds, float duration, int mode)
	{
		final int device = EKickDevice.CHIP.getValue();
		
		log.debug("Chip kick duration: " + duration);
		cmds.add(new TigerKickerKickV2(device, mode, duration));
	}
	
	
	/**
	 * Puts the disarm commands to cmd
	 * @param cmds already contain commands and will include disarm cmds afterwards
	 */
	public void disarm(List<ACommand> cmds)
	{
		cmds.add(new TigerKickerKickV2(TigerKickerKickV2.Device.CHIP, TigerKickerKickV2.Mode.DISARM, 0));
		cmds.add(new TigerKickerKickV2(TigerKickerKickV2.Device.STRAIGHT, TigerKickerKickV2.Mode.DISARM, 0));
	}
	
	
	/**
	 * Switch all devices off
	 * 
	 * @param cmds
	 */
	public void allOff(List<ACommand> cmds)
	{
		dribblerOn = true;
		dribble(cmds, false);
		disarm(cmds);
	}
	
	
	/**
	 * calculates the firing duration for chip kicks based on the distance the ball should "fly"
	 * 
	 * @param kickLength the length of the path.
	 * @return the firing duration for the kicking device.
	 */
	private int calcChipFiringDuration(float kickLength, EBotType botType)
	{
		IFunction1D chipDistanceFunc = AIConfig.getSkills(botType).getChipDistanceFunc();
		int duration = (int) chipDistanceFunc.eval(kickLength);
		log.debug("fire duration: " + chipDistanceFunc + "(" + kickLength + ") = " + duration);
		return duration;
	}
	
	
	/**
	 * calculates the dribbling speed for chip kicks based on the distance the ball should "fly"
	 * 
	 * @param kickLength the length of the path.
	 * @param botType
	 * @return the dribbling speed
	 */
	public int calcChipDribbleSpeed(float kickLength, EBotType botType)
	{
		IFunction1D func = AIConfig.getSkills(botType).getChipDribbleFunc();
		int speed = (int) func.eval(kickLength);
		log.debug("dribble Speed: " + func + "(" + kickLength + ") = " + speed);
		return speed;
	}
	
	
	/**
	 * calculates the dribbling speed for straight kicks
	 * 
	 * @param kickLength the length of the path.
	 * @param botType
	 * @return the dribbling speed
	 */
	public int calcKickDribbleSpeed(float kickLength, EBotType botType)
	{
		IFunction1D func = AIConfig.getSkills(botType).getKickDribbleFunc();
		int speed = (int) func.eval(kickLength);
		log.debug("dribble Speed: " + func + "(" + kickLength + ") = " + speed);
		return speed;
	}
	
	
	/**
	 * 
	 * This function calculates a firing duration for the kicking device of the tiger robot to fit a specific pass
	 * length and an end velocity
	 * 
	 * @param kickLengthMM the length of the path. [mm]
	 * @param endVelocity of the ball after he covers the kick length.
	 * @return the firing duration for the kicking device.
	 */
	private int calcStraightFiringDuration(float kickLengthMM, float endVelocity, EBotType botType)
	{
		float ballFrictionSlide = AIConfig.getSkills(botType).getBallFrictionSlide();
		float ballFrictionRoll = AIConfig.getSkills(botType).getBallFrictionRoll();
		float edgeFactor = AIConfig.getSkills(botType).getEdgeFactor();
		
		int refFiringDuration = AIConfig.getSkills(botType).getRefFiringDuration();
		float refVelocity = AIConfig.getSkills(botType).getRefKickVelocity();
		
		/*
		 * E(ges) = E(kin) + E(reibung)
		 * 
		 * E(reibung) = m * g * rkoeffizient * s
		 * 
		 * Fall1 (nur gleiten):
		 * E(kin_v0) = E(gleitreib) + E(kin_end)
		 * 
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
			duration = refFiringDuration;
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
			
			duration = Math.round((refFiringDuration / refVelocity) * v0);
		}
		
		if (duration > refFiringDuration)
		{
			duration = refFiringDuration;
		}
		return duration;
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
