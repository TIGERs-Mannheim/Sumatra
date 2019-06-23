/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 18.04.2011
 * Author(s):
 * FlorianS
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.other;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EGameSituation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.AimingCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ABaseRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillFacade;


/**
 * This role is supposed to aim the ball facing a designated target.
 * 
 * @author FlorianS
 * 
 */
public class AimingRole extends ABaseRole
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= 7399538266459602405L;
	
	private final float			BOT_RADIUS			= AIConfig.getGeometry().getBotRadius();
	private final float			BALL_RADIUS			= AIConfig.getGeometry().getBallRadius();
	
	private final AimingCon		aimingCon;
	private final float			TOLERANCE			= BALL_RADIUS + BOT_RADIUS + 100;
	
	private Vector2				target				= new Vector2(AIConfig.INIT_VECTOR);
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param type
	 */
	public AimingRole()
	{
		super(ERole.AIMING);
		destCon.setTolerance(TOLERANCE);
		
		this.aimingCon = new AimingCon();
		addCondition(aimingCon);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Updates target which shall be faced
	 * 
	 * @param target a point that shall be looked at
	 */
	public void updateTarget(Vector2 target)
	{
		this.target = target;
	}
	

	@Override
	public void update(AIInfoFrame currentFrame)
	{
		aimingCon.updateAimingTarget(target);
		destCon.updateDestination(currentFrame.worldFrame.ball.pos);
	}
	

	@Override
	public void calculateSkills(WorldFrame wFrame, SkillFacade skills)
	{
		if (!aimingCon.checkCondition(wFrame))
		{
			skills.aiming(aimingCon, EGameSituation.SET_PIECE);
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
