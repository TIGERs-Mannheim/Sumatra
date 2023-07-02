/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.standard;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Setter;


/**
 * This play moves all bots to the maintenance position.
 */
public class MaintenancePlay extends AMaintenancePlay
{
	@Configurable(comment = "Maintenance x position value relative to own field half", defValue = "0.5")
	private static double defaultStartingXPos = 0.5;

	@Configurable(comment = "Direction from startingPos with length", defValue = "0.0;-400.0")
	private static IVector2 defaultDirection = Vector2.fromXY(0, -400);

	@Configurable(comment = "Orientation of bots", defValue = "0.0")
	private static double defaultOrientation = 0;


	@Setter
	private double startingXPos = defaultStartingXPos;
	@Setter
	private IVector2 direction = defaultDirection;
	@Setter
	private double orientation = defaultOrientation;


	public MaintenancePlay()
	{
		super(EPlay.MAINTENANCE);
	}


	private IVector2 calculateStartingPos()
	{
		int nBots = getRoles().size();
		double totalLength = (nBots - 1) * direction.y();
		return Vector2.fromXY(startingXPos * Geometry.getField().minX(), -totalLength / 2.0);
	}


	@Override
	protected void doUpdateAfterRoles()
	{
		super.doUpdateAfterRoles();
		calculateBotActions(calculateStartingPos(), direction, orientation);
	}
}
