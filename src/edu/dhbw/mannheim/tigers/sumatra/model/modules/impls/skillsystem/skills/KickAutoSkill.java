/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 23, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature.EFeatureState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices.TigerDevices;


/**
 * Do the movement for a reliable straight kick
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class KickAutoSkill extends AKickSkill
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger	log						= Logger.getLogger(KickAutoSkill.class.getName());
	private long						timeLostBallContact	= 0;
	private final float				length;
	private final float				endVelocity;
	private float						duration					= 0;
	private int							dribbleSpeed			= 0;
	private int							retries					= 0;
	private boolean					useDribbler				= false;
	private static final int		MAX_RETRY				= 3;
	private static final int		TIMEOUT_BALLCONTACT	= 1000;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * This constructor is only for testing/calibrating the kicker!
	 * 
	 * @param duration
	 * @param dribbleSpeed
	 */
	public KickAutoSkill(float duration, int dribbleSpeed)
	{
		super(ESkillName.KICK_AUTO);
		length = 0;
		endVelocity = 0;
		this.duration = duration;
		this.dribbleSpeed = dribbleSpeed;
	}
	
	
	/**
	 * @param length
	 */
	public KickAutoSkill(float length)
	{
		this(null, length, 0);
	}
	
	
	/**
	 * @param lookAtTarget
	 * @param length
	 */
	public KickAutoSkill(IVector2 lookAtTarget, float length)
	{
		this(lookAtTarget, length, 0);
	}
	
	
	/**
	 * @param lookAtTarget
	 * @param length
	 * @param endVelocity
	 */
	public KickAutoSkill(IVector2 lookAtTarget, float length, float endVelocity)
	{
		super(ESkillName.KICK_AUTO);
		setLookAtTarget(lookAtTarget);
		this.length = length;
		this.endVelocity = endVelocity;
		duration = 0;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public List<ACommand> doCalcEntryActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		super.doCalcEntryActions(bot, cmds);
		getDevices().dribble(cmds, true);
		
		if (duration > 0)
		{
			getDevices().kickTest(cmds, duration, dribbleSpeed);
		} else if (bot.getBot().getBotFeatures().get(EFeature.STRAIGHT_KICKER) == EFeatureState.WORKING)
		{
			if (useDribbler)
			{
				getDevices().kickDribble(cmds, length, endVelocity);
			} else
			{
				getDevices().kick(cmds, length, endVelocity);
			}
		} else if (bot.getBot().getBotFeatures().get(EFeature.CHIP_KICKER) == EFeatureState.WORKING)
		{
			// kicker is broken, lets chip with 1/2 length :)
			if (length != TigerDevices.KICKER_MAX)
			{
				getDevices().chipRoll(cmds, (length / 2));
			} else
			{
				float len = (GeoMath.distancePP(getLookAtTarget(), getWorldFrame().ball.getPos()) / 2);
				getDevices().chipRoll(cmds, len);
			}
		} else
		{
			log.warn("Kicker of bot " + bot.getId().getNumber() + " is configured broken!");
		}
		calcSpline(bot);
		
		return cmds;
	}
	
	
	@Override
	protected void periodicProcess(TrackedTigerBot bot, List<ACommand> cmds)
	{
	}
	
	
	@Override
	protected boolean isComplete(TrackedTigerBot bot)
	{
		boolean completed = super.isComplete(bot);
		
		if (completed)
		{
			return completed;
		}
		
		if (!bot.hasBallContact())
		{
			if (timeLostBallContact == 0)
			{
				timeLostBallContact = System.currentTimeMillis();
			} else if ((System.currentTimeMillis() - timeLostBallContact) > TIMEOUT_BALLCONTACT)
			{
				log.debug("KickAutoSkill timed out due to lost ball Contact without kicker discharge");
				if (retry(bot))
				{
					timeLostBallContact = 0;
					completed = false;
					return completed;
				}
				completed = true;
				return completed;
			}
		} else
		{
			timeLostBallContact = 0;
		}
		
		// completed = super.isComplete(bot);
		// if (!completed)
		// {
		// return !retry(bot);
		// }
		return completed;
	}
	
	
	private boolean retry(TrackedTigerBot bot)
	{
		if (retries < MAX_RETRY)
		{
			log.debug("retry " + retries);
			retries++;
			resetTimeOut(bot);
			notifyEvent(EEvent.RETRY);
			calcSpline(bot);
			return true;
		}
		log.debug("Timed out after retries");
		notifyEvent(EEvent.TIMED_OUT);
		return false;
	}
	
	
	/**
	 * @return the useDribbler
	 */
	public final boolean isUseDribbler()
	{
		return useDribbler;
	}
	
	
	/**
	 * @param useDribbler the useDribbler to set
	 */
	public final void setUseDribbler(boolean useDribbler)
	{
		this.useDribbler = useDribbler;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
