/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.06.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ABaseRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillFacade;


/**
 * TODO Malte, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author Malte
 * 
 */
public class BallDribbler extends ABaseRole
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	

	/**  */
	private static final long	serialVersionUID	= -1215694125646428827L;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	

	/**
	 * @param type
	 */
	public BallDribbler()
	{
		super(ERole.BALL_DRIBBLER);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void update(AIInfoFrame currentFrame)
	{
		
	}
	

	@Override
	public void calculateSkills(WorldFrame wFrame, SkillFacade skills)
	{
		if (destCon.checkCondition(wFrame) != true)
		{
			skills.dribble(true);
			skills.moveBallTo(destCon.getDestination());
		}
		// idle
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public void updateDestination(IVector2 newDest)
	{
		this.destCon.updateDestination(newDest);
	}
	

	public void setDestConTolerance(float newTolerance)
	{
		destCon.setTolerance(newTolerance);
	}
	

	public boolean isDone(WorldFrame worldFrame)
	{
		if (destCon.checkCondition(worldFrame))
		{
			System.out.println("BallDribbler: yep i'm done");
		}
		return destCon.checkCondition(worldFrame);
	}
}
