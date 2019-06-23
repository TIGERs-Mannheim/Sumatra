/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.others;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * This play moves all bots to the maintenance position.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 */
public class MaintenancePlay extends AMaintenancePlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	@Configurable(comment = "first maintenance position", defValue = "-2000.0;600.0")
	private static IVector2 startingPos = Vector2.fromXY(-2000, 600);
	
	@Configurable(comment = "Direction from startingPos with length", defValue = "0.0;-220.0")
	private static IVector2 direction = Vector2.fromXY(0, -220);
	
	@Configurable(comment = "Orientation of bots", defValue = "0.0")
	private static double orientation = 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Creates a new MaintenancePlay
	 */
	public MaintenancePlay()
	{
		super(EPlay.MAINTENANCE);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame frame)
	{
		calculateBotActions(frame, startingPos, direction, orientation);
	}
}
