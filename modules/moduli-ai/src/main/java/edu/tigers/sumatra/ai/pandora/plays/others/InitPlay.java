/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 25.02.2011
 * Author(s): Malte
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays.others;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole.EMoveBehavior;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * Simple play, that handles 6 {@link MoveRole}, which don't do anything.
 * 
 * @author Malte
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
	
	private boolean					update			= true;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public InitPlay()
	{
		super(EPlay.INIT);
		final double l = Geometry.getFieldLength();
		final double w = Geometry.getFieldWidth();
		
		for (int botNum = 0; botNum < 6; botNum++)
		{
			final Vector2 destination;
			switch (botNum)
			{
				case 0:
					// keeper pos
					destination = new Vector2((-l / 2.0) + (l / DIV_3), w / DIV_4);
					break;
				case 1:
					destination = new Vector2(-l / 3, -w / DIV_1);
					break;
				case 2:
					destination = new Vector2(-l / DIV_5, 0);
					break;
				case 3:
					destination = new Vector2(-l / DIV_2, w / 4.0);
					break;
				case 4:
					destination = new Vector2(-l / DIV_2, -w / 4.0);
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
	protected ARole onRemoveRole(final MetisAiFrame frame)
	{
		update = true;
		return getLastRole();
	}
	
	
	@Override
	protected ARole onAddRole(final MetisAiFrame frame)
	{
		update = true;
		MoveRole role = new MoveRole(EMoveBehavior.NORMAL);
		return (role);
	}
	
	
	@Override
	protected void onGameStateChanged(final EGameStateTeam gameState)
	{
	}
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame frame)
	{
		super.doUpdate(frame);
		if (update)
		{
			reorderRolesToDestinations(destinations.subList(0, getRoles().size()));
			int i = 0;
			for (ARole role : getRoles())
			{
				MoveRole moveRole = (MoveRole) role;
				moveRole.getMoveCon().updateDestination(destinations.get(i));
				moveRole.getMoveCon().updateTargetAngle(0);
				moveRole.getMoveCon().setPenaltyAreaAllowedOur(true);
				moveRole.getMoveCon().setPenaltyAreaAllowedTheir(true);
				i++;
			}
			update = false;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
