/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.10.2010
 * Author(s):
 * FlorianS
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.other;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.LookAtCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ABaseRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillFacade;


/**
 * This Role is quite similar to {@link MoveRole.java MoveRole}. The only
 * difference is the direction the bot will look at. The bot fulfilling this
 * Role will always face the ball in order to catch a pass made by the opponent
 * ball carrier.
 * 
 * @author FlorianS
 * 
 */
public class PassBlockerRole extends ABaseRole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= 2927236652711535414L;
	
	private IVector2				center;
	private float					radius;
	private Vector2				direction;
	
	private LookAtCon				lookAtCon;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public PassBlockerRole()
	{
		super(ERole.PASS_BLOCKER);
		
		lookAtCon = new LookAtCon();
		addCondition(lookAtCon);
		
		direction = new Vector2(AIConfig.INIT_VECTOR);
		center = AIConfig.INIT_VECTOR;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public void updateCirclePos(Vector2f center, float radius, Vector2 direction)
	{
		this.center = center;
		this.radius = radius;
		this.direction = new Vector2(direction);
	}
	

	@Override
	public void update(AIInfoFrame currentFrame)
	{
		if (direction.equals(AIConfig.INIT_VECTOR))
		{
			System.out.println("PassBlockerRoles direction is InitVector");
		}
		
		if (center.equals(AIConfig.INIT_VECTOR))
		{
			System.out.println("PassBlockerRoles center is InitVector");
		}
		Vector2 ballPos = new Vector2(currentFrame.worldFrame.ball.pos);
		// sets the length of the vector to 'radius'
		direction.scaleTo(radius);
		// adds the vector to the center
		destCon.updateDestination(center.addNew(direction));
		
		// update lookAt
		lookAtCon.updateTarget(ballPos);
	}
	

	@Override
	public void calculateSkills(WorldFrame wFrame, SkillFacade skills)
	{
		boolean correctPosition = destCon.checkCondition(wFrame);
		boolean correctAngle = lookAtCon.checkCondition(wFrame);
		
		// try a combined skill!
		if (!correctPosition || !correctAngle)
		{
			skills.moveTo(destCon.getDestination(), lookAtCon.getTargetViewAngle());
		}
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
