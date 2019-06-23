/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 3, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature.EFeatureState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2.EKickerMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices.ChipParams;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices.TigerDevices;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Chip the ball
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ChipSkill extends PositionSkill
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log							= Logger.getLogger(ChipSkill.class.getName());
	
	private IVector2					target;
	private float						kickLength;
	private final float				rollDist;
	private IVector2					initBallPos					= null;
	private long						dribbleWaitStartTime		= System.nanoTime();
	
	@Configurable(comment = "Dist [mm] - Max distance between current ball pos and initial ball pos before completing this skill")
	private static float				maxDist2InitBallPos		= 30;
	
	@Configurable(comment = "Tol [rpm] - If dribble speed is below this value, dribbler speed will not be considered for chip kick.")
	private static int				enableDribblerTolerance	= 100;
	
	@Configurable(comment = "Time [ms] - Waiting time for dribbler to be ready. Only used, if dribbleArm on TigerBot can not be used, e.g. if barrier broken or for grSim Bots")
	private static long				dribbleWaitTime			= 3000;
	
	@Configurable(comment = "Dist [mm] - Correction for distance from bot to ball")
	private static float				distCorrection				= -10;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param target
	 * @param rollDist
	 */
	public ChipSkill(final IVector2 target, final float rollDist)
	{
		super(ESkillName.CHIP_KICK_TARGET, target, 0);
		this.target = target;
		kickLength = 0;
		this.rollDist = rollDist;
	}
	
	
	/**
	 * @param length
	 * @param rollDist
	 */
	public ChipSkill(final float length, final float rollDist)
	{
		super(ESkillName.CHIP_KICK_LENGTH, AVector2.ZERO_VECTOR, 0);
		target = null;
		kickLength = length;
		this.rollDist = rollDist;
	}
	
	
	protected ChipSkill(final ESkillName skillName, final IVector2 target, final float rollDist)
	{
		super(skillName, target, 0);
		this.target = target;
		this.rollDist = rollDist;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public List<ACommand> calcEntryActions(final List<ACommand> cmds)
	{
		initBallPos = getWorldFrame().getBall().getPos();
		if (target == null)
		{
			target = getPos().addNew(new Vector2(kickLength, 0).turnTo(getAngle()));
		} else
		{
			kickLength = GeoMath.distancePP(getPos(), target) - AIConfig.getGeometry().getBotRadius();
		}
		calcDestOrient();
		ChipParams chipValues = getChipParams();
		if (chipValues.getDribbleSpeed() < enableDribblerTolerance)
		{
			getDevices().chip(cmds, chipValues, EKickerMode.ARM);
		} else if (getBot().getBotFeatures().get(EFeature.BARRIER) == EFeatureState.WORKING)
		{
			getDevices().chip(cmds, chipValues, EKickerMode.DRIBBLER);
		} else
		{
			getDevices().dribble(cmds, chipValues.getDribbleSpeed());
		}
		return cmds;
	}
	
	
	@Override
	protected void doCalcActions(final List<ACommand> cmds)
	{
		long timeDiff = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - dribbleWaitStartTime);
		if ((getBot().getBotFeatures().get(EFeature.BARRIER) != EFeatureState.WORKING))
		{
			boolean ready = false;
			if (getBotType() == EBotType.TIGER_V2)
			{
				TigerBotV2 botV2 = (TigerBotV2) getBot();
				if (botV2.getSystemStatusV2().isDribblerOverloaded())
				{
					log.warn("Dribbler overloaded! Dribble rpm: " + getChipParams().getDribbleSpeed());
					ready = true;
				}
				if (botV2.getSystemStatusV2().isDribblerSpeedReached())
				{
					log.trace("DribblerSpeed Reached.");
					ready = true;
				}
			}
			if ((timeDiff > dribbleWaitTime))
			{
				log.trace("Dribble wait time reached.");
				ready = true;
			}
			if (ready)
			{
				getDevices().chip(cmds, getChipParams(), EKickerMode.FORCE);
				startTimeout(100);
				dribbleWaitStartTime = Long.MAX_VALUE;
			}
		}
		
		calcDestOrient();
		
		if (!initBallPos.equals(getWorldFrame().getBall().getPos(), maxDist2InitBallPos))
		{
			log.trace("Ball pos changed. Complete skill.");
			complete();
		}
	}
	
	
	protected ChipParams getChipParams()
	{
		return TigerDevices.calcChipParams(kickLength, rollDist);
	}
	
	
	private void calcDestOrient()
	{
		float stepSize = AIConfig.getGeometry().getBotCenterToDribblerDist() + AIConfig.getGeometry().getBallRadius()
				+ distCorrection;
		IVector2 dest = GeoMath.stepAlongLine(getWorldFrame().getBall().getPos(), target, -stepSize);
		float orient = target.subtractNew(getWorldFrame().getBall().getPos()).getAngle();
		setDestination(dest);
		setOrientation(orient);
	}
	
	
	@Override
	public List<ACommand> calcExitActions(final List<ACommand> cmds)
	{
		getDevices().allOff(cmds);
		return cmds;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param target the target to set
	 */
	protected final void setTarget(final IVector2 target)
	{
		this.target = target;
	}
	
	
}
