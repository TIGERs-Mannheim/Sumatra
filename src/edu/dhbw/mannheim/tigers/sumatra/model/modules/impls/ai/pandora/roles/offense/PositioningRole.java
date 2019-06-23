/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.10.2010
 * Author(s):
 * FlorianS
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.LookAtCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ABaseRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillFacade;


/**
 * The only purpose of PositionRole is to change the bot's position and angle.
 * In contrast to MoveRole there is no need of standing around the center of a
 * circle. Furthermore the dribbling device may be enabled.
 * 
 * @author FlorianS
 * 
 */
public class PositioningRole extends ABaseRole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= -6292418311451558593L;
	
	private Vector2f				destination;
	private Vector2f				target;
	
	private boolean				ballCarrier;
	
	private LookAtCon				lookAtCon			= new LookAtCon();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public PositioningRole(boolean ballCarrier)
	{
		super(ERole.POSITIONINGROLE);
		this.ballCarrier = ballCarrier;
		
		destination = new Vector2f(0, -AIConfig.getGeometry().getFieldLength() / 4);
		target = new Vector2f(AIConfig.getGeometry().getCenter());
		
		lookAtCon = new LookAtCon();
		addCondition(lookAtCon);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void update(AIInfoFrame currentFrame)
	{
		destCon.updateDestination(destination);
		lookAtCon.updateTarget(target);
	}
	

	@Override
	public void calculateSkills(WorldFrame worldFrame, SkillFacade skills)
	{
		boolean correctPosition = destCon.checkCondition(worldFrame);
		boolean correctAngle = lookAtCon.checkCondition(worldFrame);
		
		// check conditions for move skill
		if (!correctPosition || !correctAngle)
		{
			if (ballCarrier)
			{
				skills.moveBallTo(destCon.getDestination(), lookAtCon.getLookAtTarget());
				skills.dribble(true);
			} else
			{
				skills.moveTo(destCon.getDestination(), lookAtCon.getLookAtTarget());
			}
		}
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public void setDestination(IVector2 destination)
	{
		this.destination = new Vector2f(destination);
	}
	

	public void setTarget(IVector2 target)
	{
		this.target = new Vector2f(target);
	}
}