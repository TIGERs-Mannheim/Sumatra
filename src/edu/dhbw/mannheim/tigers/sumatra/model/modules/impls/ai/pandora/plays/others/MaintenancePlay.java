/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.01.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.other.GenericMoveRole;


/**
 * This play moves all bots to the maintenance position.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class MaintenancePlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= -2729251776691337425L;
	private final GenericMoveRole	moveBot1;
	private final GenericMoveRole	moveBot2;
	private final GenericMoveRole	moveBot3;
	private final GenericMoveRole	moveBot4;
	private final GenericMoveRole	moveBot5;
	
	private final Vector2f			posStart;
	private final Vector2f			distance;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param type
	 * @param wf
	 */
	public MaintenancePlay(AIInfoFrame aiFrame)
	{
		super(EPlay.MAINTENANCE, aiFrame);
		
		distance = new Vector2f(AIConfig.getGeometry().getBotRadius() + 120, 0);
		posStart = new Vector2f(AIConfig.getGeometry().getMaintenancePosition().subtractNew(distance.multiplyNew(2)));
		
		moveBot1 = new GenericMoveRole(0);
		moveBot2 = new GenericMoveRole(0);
		moveBot3 = new GenericMoveRole(0);
		moveBot4 = new GenericMoveRole(0);
		moveBot5 = new GenericMoveRole(0);
		
		Vector2 initPos = new Vector2(posStart);
		
		initPos.add(distance);
		addAggressiveRole(moveBot1, initPos);
		initPos.add(distance);
		addAggressiveRole(moveBot2, initPos);
		initPos.add(distance);
		addAggressiveRole(moveBot3, initPos);
		initPos.add(distance);
		addAggressiveRole(moveBot4, initPos);
		initPos.add(distance);
		addAggressiveRole(moveBot5, initPos);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		for (int i = 1; i <= getRoles().size(); i++)
		{
			GenericMoveRole role = (GenericMoveRole) getRoles().get(i - 1);
			Vector2 newPos = distance.multiplyNew(i);
			newPos.add(posStart);
			role.updatePosition(newPos);
		}
	}
	

	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		for (int i = 0; i < getRoles().size(); i++)
		{
			ARole role = getRoles().get(i);
			
			if (!role.checkAllConditions(currentFrame))
			{
				return;
			}
		}
		
		// all conditions of all roles are true
		changeToSucceeded();
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	

	@Override
	public int calcPlayableScore(AIInfoFrame currentFrame)
	{
		return 0;
	}
	
}
