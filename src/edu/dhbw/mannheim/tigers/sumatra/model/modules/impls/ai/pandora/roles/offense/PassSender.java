/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.10.2010
 * Author(s): GuntherB
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
 * This Role will pass the ball to a corresponding PassReceiver
 * 
 * @author GuntherB
 * 
 */
public class PassSender extends ABaseRole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long		serialVersionUID		= 4523052064201118142L;
	
	private boolean					forcePass;
	
	private final AimingCon			aiming;
	
	private boolean					passIsDone;
	
	private final EGameSituation	gameSituation;
	
	private final float				TOLERANCE_PRE_AIMING	= AIConfig.getGeometry().getBallRadius()
																				+ AIConfig.getGeometry().getBotRadius() + 250;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * creates a new Role, that will pass the ball to a target bot
	 * @see PassSender
	 */
	public PassSender(EGameSituation gameSituation)
	{
		super(ERole.PASS_SENDER);
		destCon.setTolerance(TOLERANCE_PRE_AIMING);
		
		aiming = new AimingCon(AIConfig.getTolerances().getAiming() * 1.5f);
		addCondition(aiming);
		
		forcePass = false;
		passIsDone = false;
		
		this.gameSituation = gameSituation;
		
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
		// if pass is forced by play, the skills are added and returned immediately
		if (forcePass)
		{
			TrackedTigerBot bot = wFrame.tigerBots.get(getBotID());
			float distance = AIMath.distancePP(bot.pos, aiming.getAimingTarget());
			
			skills.kickArm(distance * 0.6f); // Kick when the IR-sensors detects the ball
			skills.kickAuto(distance * 0.6f);
			skills.dribble(false);
			
			forcePass = false;
			return;
		}
		
		if (!destCon.doCheckCondition(wFrame, getBotID()))
		{
			skills.moveTo(destCon.getDestination(), aiming.getAimingTarget());
		} else
		{
			if (!aiming.doCheckCondition(wFrame, getBotID()))
			{
				skills.aiming(aiming, gameSituation);
				skills.disarm();
				skills.dribble(true);
			} else
			{
				// idle
			}
		}
		
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * forces the Role to Pass ASAP
	 */
	public void forcePass()
	{
		forcePass = true;
	}
	

	public void forceStopPass()
	{
		forcePass = false;
	}
	

	/**
	 * only needs to be set, when a pass is forced
	 * 
	 * @param newReceiverPos
	 */
	public void updateRecieverPos(IVector2 newReceiverPos)
	{
		aiming.updateAimingTarget(newReceiverPos);
	}
	

	/**
	 * Returns if role is ready to shoot, which means that
	 * it has aimed at its target and is also in position
	 * given by DestCondition
	 * 
	 * @return
	 */
	public boolean checkReadyToShoot(WorldFrame worldFrame)
	{
		return aiming.checkCondition(worldFrame) && destCon.checkCondition(worldFrame);
	}
	

	@Deprecated
	public boolean isPassDone()
	{
		return passIsDone;
	}
	

	public Vector2 getTarget()
	{
		return aiming.getAimingTarget();
	}
}
