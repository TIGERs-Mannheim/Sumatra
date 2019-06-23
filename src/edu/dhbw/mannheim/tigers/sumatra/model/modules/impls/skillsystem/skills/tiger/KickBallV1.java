/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillGroup;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * This is a kicker skill for using the kicking device of the TigerRobot.
 * 
 * Kicker is ready when capacity (capLevel) has reached 200 V.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class KickBallV1 extends ASkill
{
	/**
	 * This enum will be used to differ between the
	 * different kicker modes.
	 * 
	 * <strong>force</strong> = forces the bot to shoot ;
	 * <strong>arm</strong> = the bot shoots when a ball is armed / detected ;
	 * <strong>disarm</strong> = disarms ball
	 * 
	 * @author Oliver Steinbrecher <OST1988@aol.com>
	 */
	public enum EKickMode
	{
		FORCE(0),
		ARM(1),
		DISARM(2);
		
		public final int	value;
		
		
		private EKickMode(int value)
		{
			this.value = value;
		}
	}
	
	/**
	 * choose kicking device
	 * @author DanielW
	 */
	public enum EKickDevice
	{
		STRAIGHT(0),
		CHIP(1);
		
		public final int	value;
		
		
		private EKickDevice(int value)
		{
			this.value = value;
		}
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final float	MAX						= -1f;
	

	/**  */
	private final float			BALL_FRICTION_SLIDE	= AIConfig.getSkills().getBallFrictionSlide();
	private final float			BALL_FRICTION_ROLL	= AIConfig.getSkills().getBallFrictionRoll();
	private final float			EDGE_FACTOR				= AIConfig.getSkills().getEdgeFactor();
	
	private final int				REF_FIRING_DURATION	= AIConfig.getSkills().getRefFiringDuration();
	private final float			REF_VELOCITY			= AIConfig.getSkills().getRefVelocity();
	
	private final float			desiredKickLength;
	/** the velocity of the ball after he covers the kick length */
	private final float			ballEndVelocity;
	
	/**
	 * Stores the operating mode for ball detection of the kicking device.
	 */
	private final EKickMode		kickMode;
	
	private final EKickDevice	kickDevice;
	
	private final boolean		directMode;
	private final float			directDuration;
	

	private final Logger			LOG						= Logger.getLogger(getClass());
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * kick with a maximum ball speed
	 * @param mode the kicker mode ( see {@link EKickMode} for more information).
	 * @param device what device to use {@link EKickDevice}
	 */
	public KickBallV1(EKickMode mode, EKickDevice device)
	{
		super(ESkillName.KICK_BALL, ESkillGroup.KICK);
		
		this.desiredKickLength = MAX;
		this.ballEndVelocity = 10;
		this.kickMode = mode;
		this.kickDevice = device;
		this.directMode = false;
		this.directDuration = 0;
	}
	

	/**
	 * direct mode kick, uses the raw duration.
	 * @param mode {@link EKickMode}
	 * @param device {@link EKickDevice}
	 * @param duration duration that is directly applied to the kicker [us]
	 */
	public KickBallV1(EKickMode mode, EKickDevice device, float duration)
	{
		super(ESkillName.KICK_BALL, ESkillGroup.KICK);
		this.desiredKickLength = 0;
		this.ballEndVelocity = 0;
		this.kickMode = mode;
		this.kickDevice = device;
		this.directMode = true;
		this.directDuration = duration;
	}
	

	/**
	 * kick a certain distance. the ball is supposed to stop there
	 * @param kickLength defines the length of a kick.
	 * @param mode {@link EKickMode}
	 * @param device {@link EKickDevice}
	 */
	public KickBallV1(float kickLength, EKickMode mode, EKickDevice device)
	{
		super(ESkillName.KICK_BALL, ESkillGroup.KICK);
		
		this.desiredKickLength = kickLength;
		this.ballEndVelocity = 0;
		this.kickMode = mode;
		this.kickDevice = device;
		this.directMode = false;
		this.directDuration = 0;
	}
	

	/**
	 * kick a certain distance, when the ball is there, it has a defined end-velocity
	 * @param kickLength defines the length of a kick.
	 * @param ballEndVelocity the velocity the ball should have after kickLength
	 * @param mode {@link EKickMode}
	 * @param device {@link EKickDevice}
	 */
	public KickBallV1(float kickLength, float ballEndVelocity, EKickMode mode, EKickDevice device)
	{
		super(ESkillName.KICK_BALL, ESkillGroup.KICK);
		
		this.desiredKickLength = kickLength;
		this.ballEndVelocity = ballEndVelocity;
		this.kickMode = mode;
		this.kickDevice = device;
		this.directMode = false;
		this.directDuration = 0;
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- public-method(s) -----------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public ArrayList<ACommand> calcActions(ArrayList<ACommand> cmds)
	{
		// check if bot is available
		if (getBot() == null)
		{
			return cmds;
		}
		
		if (directMode)
		{
			cmds.add(new TigerKickerKickV2(kickDevice.value, kickMode.value, directDuration));
			complete();
			return cmds;
		}
		

		int duration = 0;
		int mode = kickMode.value;
		int device = kickDevice.value;
		
		switch (kickDevice)
		{
			case STRAIGHT:
				duration = calcStraightFiringDuration(desiredKickLength, ballEndVelocity);
				break;
			
			case CHIP:
				duration = calcChipFiringDuration(desiredKickLength);
				break;
		}
//		System.out.println(duration);
		cmds.add(new TigerKickerKickV2(device, mode, duration));
		complete();
		
		return cmds;
	}
	

	/**
	 * calculates the firing duration for chip kicks based on the distance the ball should "fly"
	 * 
	 * @param kickLength the length of the path.
	 * @return the firing duration for the kicking device.
	 */
	private int calcChipFiringDuration(float kickLength)
	{
		// TODO OliverS, how to calculate duration for chips?
		return REF_FIRING_DURATION;
	}
	

	/**
	 * 
	 * This function calculates a firing duration for the kicking device of the tiger robot to fit a specific pass
	 * length and an end velocity
	 * 
	 * @param kickLength the length of the path.
	 * @param endVeloctiy of the ball after he covers the kick length.
	 * @return the firing duration for the kicking device.
	 */
	private int calcStraightFiringDuration(float kickLength, float endVelocity)
	{
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
		 * weg_gleitreib = (1-edgefactor^2)*v0^2 / 2 * µ_gleit * 9.81 //ergibt sich aus der betrachtung nach fall1
		 * weg_rollreib = gesamtweg - weg_gleitreib
		 * dann einsetzten und nach v0 umstelen
		 */

		int duration = 0;
		
		if (kickLength == MAX)
		{
			duration = REF_FIRING_DURATION;
		} else
		{
			float v0 = 0; // start velocity
			
			// slide only movement
			v0 = AIMath.sqrt(2 * BALL_FRICTION_SLIDE * 9.81f * kickLength + AIMath.square(endVelocity));
			
			if (endVelocity / v0 < EDGE_FACTOR)
			{
				// slide only not valid, consider rolling as well
				v0 = AIMath.sqrt((2 * BALL_FRICTION_ROLL * 9.81f * kickLength + AIMath.square(endVelocity))
						/ (AIMath.square(EDGE_FACTOR) + BALL_FRICTION_ROLL / BALL_FRICTION_SLIDE
								* (1 - AIMath.square(EDGE_FACTOR))));
			}
			
			if (v0 > REF_VELOCITY)
			{
				LOG.warn("you try to kick faster than possible!");
			}
			
			// scale startvelocity to reference firing duration
			duration = Math.round(REF_FIRING_DURATION / REF_VELOCITY * v0);
		}
		
		return duration;
	}
	

	@Override
	protected boolean compareContent(ASkill newSkill)
	{
		KickBallV1 kick = (KickBallV1) newSkill;
		return kickMode == kick.kickMode && kickDevice == kick.kickDevice && ballEndVelocity == kick.ballEndVelocity
				&& desiredKickLength == kick.desiredKickLength;
	}
	
}
