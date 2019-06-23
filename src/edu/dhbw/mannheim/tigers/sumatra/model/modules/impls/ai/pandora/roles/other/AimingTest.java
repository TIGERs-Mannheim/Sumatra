/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.10.2010
 * Author(s): DanielW
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.other;

import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EGameSituation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.AimingCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ABaseRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillFacade;


/**
 * simple test-role for the aiming skill
 * 
 * @author DanielW
 * 
 */
public class AimingTest extends ABaseRole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= 2539924688430805333L;
	
	private final float			BOT_RADIUS			= AIConfig.getGeometry().getBotRadius();
	private final float			BALL_RADIUS			= AIConfig.getGeometry().getBallRadius();
	
	private final AimingCon		aiming;
	private final float			TOLERANCE			= BALL_RADIUS + BOT_RADIUS + 100;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public AimingTest()
	{
		super(ERole.AIM_TEST);
		destCon.setTolerance(TOLERANCE + 250);
		
		this.aiming = new AimingCon();
		addCondition(aiming);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void update(AIInfoFrame currentFrame)
	{
		// take first foe bot as target
		aiming.updateAimingTarget(currentFrame.worldFrame.tigerBots.get(7).pos);
		// aiming.updateAimingTarget(AIConfig.getGeometry().getGoalTheir().getGoalCenter());
		destCon.updateDestination(currentFrame.worldFrame.ball.pos);
	}
	

	@Override
	public void calculateSkills(WorldFrame wFrame, SkillFacade skills)
	{
		if (!destCon.checkCondition(wFrame))
		{
			skills.moveTo(wFrame.ball.pos);
			skills.dribble(false);
		} else
		{
			if (!aiming.checkCondition(wFrame))
			{
				skills.aiming(aiming, EGameSituation.SET_PIECE);
				skills.dribble(false);
			} else
			{
				skills.dribble(false);
			}
		}
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
