/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 25.02.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.KeeperSoloRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.PassiveDefenderRole;


/**
 * Simple play, that handles 6 {@link PassiveDefenderRole}, which don't do anything.
 * 
 * @author Malte
 * 
 */
public class InitPlay extends APlay
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/** sry, no use for a name... */
	private static final int	DIV_1	= 20;
	private static final int	DIV_2	= 25;
	private static final int	DIV_3	= 24;
	private static final int	DIV_4	= 14;
	private static final int	DIV_5	= 9;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public InitPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		final float l = AIConfig.getGeometry().getFieldLength();
		final float w = AIConfig.getGeometry().getFieldWidth();
		
		// Intitial positions of the bots
		final Vector2 target = new Vector2(AIConfig.getGeometry().getCenter());
		
		for (int botNum = 0; botNum < numAssignedRoles; botNum++)
		{
			final Vector2 destination;
			switch (botNum)
			{
				case 0:
					// keeper pos
					destination = new Vector2((-l / 2) + (l / DIV_3), w / DIV_4);
					break;
				case 1:
					destination = new Vector2(-l / 3, -w / DIV_1);
					break;
				case 2:
					destination = new Vector2(-l / DIV_5, 0);
					break;
				case 3:
					destination = new Vector2(-l / DIV_2, w / 4);
					break;
				case 4:
					destination = new Vector2(-l / DIV_2, -w / 4);
					break;
				case 5:
					destination = new Vector2(-l / 4, w / DIV_1);
					break;
				case 6:
					destination = new Vector2(0, 0);
				default:
					throw new IllegalStateException();
			}
			
			if (botNum == 0)
			{
				// First role is a keeper
				ARole role = new KeeperSoloRole();
				addDefensiveRole(role, destination);
			} else
			{
				ARole role = new PassiveDefenderRole(destination, target);
				addDefensiveRole(role, destination);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		// nothing todo
	}
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
		// nothing todo
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
