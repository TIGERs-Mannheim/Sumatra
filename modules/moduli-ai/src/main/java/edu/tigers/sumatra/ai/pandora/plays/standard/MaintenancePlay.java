/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.standard;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Setter;


/**
 * This play moves all bots to the maintenance position.
 */
public class MaintenancePlay extends AMaintenancePlay
{
	@Configurable(comment = "first maintenance position", defValue = "-2000.0;500.0")
	private static IVector2 defaultStartingPos = Vector2.fromXY(-2000, 500);

	@Configurable(comment = "Direction from startingPos with length", defValue = "0.0;-400.0")
	private static IVector2 defaultDirection = Vector2.fromXY(0, -400);

	@Configurable(comment = "Orientation of bots", defValue = "0.0")
	private static double defaultOrientation = 0;


	@Setter
	private IVector2 startingPos = defaultStartingPos;
	@Setter
	private IVector2 direction = defaultDirection;
	@Setter
	private double orientation = defaultOrientation;


	public MaintenancePlay()
	{
		super(EPlay.MAINTENANCE);
	}


	@Override
	protected void doUpdateAfterRoles()
	{
		super.doUpdateAfterRoles();
		drawDestinations(startingPos, direction, orientation);
		calculateBotActions(startingPos, direction, orientation);
	}
}
