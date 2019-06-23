/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 8, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.mixedteam;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.AOffensivePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.RedirectRole;


/**
 * Mixed team play for indirect shot receiver
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class IndirectReceiverPlay extends AOffensivePlay
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final RedirectRole	role;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public IndirectReceiverPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		BotID receiverId = AiMath.getReceiver(aiFrame, aiFrame.worldFrame.tigerBotsAvailable);
		if (receiverId == null)
		{
			role = new RedirectRole(Vector2.ZERO_VECTOR, false);
			addAggressiveRole(role, Vector2.ZERO_VECTOR);
			changeToFinished();
			return;
		}
		IVector2 initPosition = aiFrame.worldFrame.tigerBotsVisible.get(receiverId).getPos();
		role = new RedirectRole(initPosition, false);
		addAggressiveRole(role, initPosition);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
		System.out.println(frame.worldFrame.ball.getVel().getLength2());
		if (!role.isReady() && (frame.worldFrame.ball.getVel().getLength2() > 0.3))
		{
			role.setReady();
		}
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		if (role.isCompleted())
		{
			changeToFinished();
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
