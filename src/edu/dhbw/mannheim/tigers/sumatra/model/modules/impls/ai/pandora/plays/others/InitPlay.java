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

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole.EMoveBehavior;


/**
 * Simple play, that handles 6 {@link MoveRole}, which don't do anything.
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
	private static final int		DIV_1				= 20;
	private static final int		DIV_2				= 25;
	private static final int		DIV_3				= 24;
	private static final int		DIV_4				= 14;
	private static final int		DIV_5				= 9;
	
	private final List<IVector2>	destinations	= new ArrayList<IVector2>(7);
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public InitPlay()
	{
		super(EPlay.INIT);
		final float l = AIConfig.getGeometry().getFieldLength();
		final float w = AIConfig.getGeometry().getFieldWidth();
		
		for (int botNum = 0; botNum < 6; botNum++)
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
					break;
				default:
					throw new IllegalStateException();
			}
			destinations.add(destination);
		}
	}
	
	
	@Override
	protected ARole onRemoveRole()
	{
		return getLastRole();
	}
	
	
	@Override
	protected ARole onAddRole()
	{
		MoveRole role = new MoveRole(EMoveBehavior.NORMAL);
		if (getRoles().isEmpty())
		{
			// keeper
			role.getMoveCon().setPenaltyAreaAllowed(true);
		}
		role.getMoveCon().updateDestination(destinations.get(getRoles().size()));
		role.getMoveCon().updateTargetAngle(0);
		return (role);
	}
	
	
	@Override
	protected void onGameStateChanged(EGameState gameState)
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
