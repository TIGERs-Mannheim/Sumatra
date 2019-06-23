/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 26.05.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.skilltester;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EGameSituation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.AimingCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ABaseRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillFacade;


/**
 * TODO osteinbrecher, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author osteinbrecher
 * 
 */
public class BallMoveTester extends ABaseRole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= 6641617374156781703L;
	
	private AimingCon				aimCond;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	

	public BallMoveTester()
	{
		super(ERole.BALL_MOVE_TESTER);
		
		destCon.updateDestination(new Vector2(-1000, 0));
		
		aimCond = new AimingCon();
		
		aimCond.updateAimingTarget(new Vector2(0, 0));
		
		addCondition(aimCond);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void update(AIInfoFrame currentFrame)
	{
		// destCon.updateDestination(currentFrame.worldFrame.foeBots.get(101).pos.subtractNew(new Vector2(200f, 0f)));
	}
	

	@Override
	public void calculateSkills(WorldFrame wFrame, SkillFacade skills)
	{
		skills.dribble(true);
		if (!destCon.checkCondition(wFrame))
		{
			skills.moveTo(destCon.getDestination());
		} else
		{
			if (!aimCond.checkCondition(wFrame))
			{
				skills.aiming(aimCond.getAimingTarget(), AIMath.deg2rad(10), EGameSituation.SET_PIECE);
			} else
			{
				skills.stop();
			}
		}
	}
	

	@Override
	public void initDestination(IVector2 destination)
	{
		destCon.getDestination();
	}
	

	@Override
	public IVector2 getDestination()
	{
		return destCon.getDestination();
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
