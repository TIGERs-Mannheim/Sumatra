/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.05.2012
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.support;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole.EMoveBehavior;


/**
 * Uses the pattern detector to block opponents attacks
 * 
 * @author osteinbrecher
 * 
 */
public class PatternBlockPlay extends ASupportPlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log						= Logger.getLogger(PatternBlockPlay.class.getName());
	
	private static final float		DISTANCE_TO_PASSER	= 200;
	
	private final MoveRole			blockerRole;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public PatternBlockPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		blockerRole = new MoveRole(EMoveBehavior.LOOK_AT_BALL);
		
		addDefensiveRole(blockerRole, getBlockPos(aiFrame));
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeUpdate(AIInfoFrame aiFrame)
	{
		blockerRole.updateDestination(getBlockPos(aiFrame));
	}
	
	
	private IVector2 getBlockPos(final AIInfoFrame aiFrame)
	{
		if (!aiFrame.tacticalInfo.getPlayPattern().isEmpty())
		{
			final IVector2 passerToBlockPos = aiFrame.tacticalInfo.getPlayPattern().get(0).getPasser(aiFrame).getPos();
			return GeoMath.stepAlongLine(passerToBlockPos, AIConfig.getGeometry().getGoalOur().getGoalCenter(),
					DISTANCE_TO_PASSER);
		}
		log.warn("PatternBlockPlay has no patterns to use and will be finished.");
		changeToFinished();
		return AIConfig.getGeometry().getGoalOur().getGoalCenter();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		// nothing todo
	}
}
