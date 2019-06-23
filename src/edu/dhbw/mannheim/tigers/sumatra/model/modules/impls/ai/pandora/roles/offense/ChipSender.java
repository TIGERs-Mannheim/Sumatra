/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.05.2011
 * Author(s): FlorianS
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EGameSituation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.AimingCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ABaseRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillFacade;


/**
 * This Role will chip kick the ball to a corresponding PassReceiver
 * 
 * @author FlorianS
 * 
 */
public class ChipSender extends ABaseRole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID			= -3319104965694731158L;
	
	private boolean				forceChip;
	
	private final AimingCon		aiming;
	
	private boolean				chipIsDone;
	
	private final float			AIMING_DISTANCE			= AIConfig.getTolerances().getPositioning()
																				+ AIConfig.getGeometry().getBallRadius()
																				+ AIConfig.getGeometry().getBotRadius();
	private final float			DESTINATION_TOLERANCE	= AIMING_DISTANCE + 50;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * creates a new Role, that will pass the ball to a target bot
	 * @see ChipSender
	 */
	public ChipSender()
	{
		super(ERole.CHIP_SENDER);
		destCon.setTolerance(DESTINATION_TOLERANCE);
		
		aiming = new AimingCon();
		addCondition(aiming);
		
		forceChip = false;
		chipIsDone = false;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void update(AIInfoFrame currentFrame)
	{
		destCon.updateDestination(currentFrame.worldFrame.ball.pos);
	}
	

	@Override
	public void calculateSkills(WorldFrame wFrame, SkillFacade skills)
	{
		// if chip is forced by play, the skills are added and returned immediately
		if (forceChip)
		{
			TrackedTigerBot bot = wFrame.tigerBots.get(getBotID());
			float distance = AIMath.distancePP(bot.pos, aiming.getAimingTarget());
			
			skills.chipArm(distance); // Kick when the IR-sensors detects the ball
			skills.moveTo(destCon.getDestination(), aiming.getAimingTarget());
		} else
		{
			skills.aiming(aiming, EGameSituation.SET_PIECE);
		}
		
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * forces the Role to Chip ASAP
	 */
	public void forceChip()
	{
		forceChip = true;
	}
	

	public void forceStopPass()
	{
		forceChip = false;
	}
	

	/**
	 * the position the sender shall move to,
	 * to be only set by the play
	 * 
	 * @param desPos
	 */
	public void updateDesignatedPos(IVector2 desPos)
	{
		destCon.updateDestination(desPos);
	}
	

	/**
	 * only needed to be set, when a chip is forced
	 * 
	 * @param newReceiverPos
	 */
	public void updateRecieverPos(IVector2 newReceiverPos)
	{
		aiming.updateAimingTarget(newReceiverPos);
	}
	

	/**
	 * Returns if role is ready to chip, which means that
	 * it has aimed at its target and is also in position
	 * given by StandNearCondition
	 * 
	 * @return
	 */
	public boolean checkReadyToShoot(WorldFrame worldFrame)
	{
		return aiming.checkCondition(worldFrame);
	}
	

	public boolean isPassDone()
	{
		return chipIsDone;
	}
	

	public Vector2 getTarget()
	{
		return aiming.getAimingTarget();
	}
}
