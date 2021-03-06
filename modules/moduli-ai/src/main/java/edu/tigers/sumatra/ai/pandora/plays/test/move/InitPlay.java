/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.test.move;

import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;

import java.util.ArrayList;
import java.util.List;


/**
 * Simple play, that handles 6 {@link MoveRole}, which don't do anything.
 */
public class InitPlay extends APlay
{
	private static final int DIV_1 = 20;
	private static final int DIV_2 = 25;
	private static final int DIV_3 = 24;
	private static final int DIV_4 = 14;
	private static final int DIV_5 = 9;

	private final List<IVector2> destinations = new ArrayList<>(7);

	private boolean update = true;


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
					destination = Vector2.fromXY((-l / 2.0) + (l / DIV_3), w / DIV_4);
					break;
				case 1:
					destination = Vector2.fromXY(-l / 3, -w / DIV_1);
					break;
				case 2:
					destination = Vector2.fromXY(-l / DIV_5, 0);
					break;
				case 3:
					destination = Vector2.fromXY(-l / DIV_2, w / 4.0);
					break;
				case 4:
					destination = Vector2.fromXY(-l / DIV_2, -w / 4.0);
					break;
				case 5:
					destination = Vector2.fromXY(-l / 4, w / DIV_1);
					break;
				case 6:
					destination = Vector2.fromXY(0, 0);
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
		update = true;
		return getLastRole();
	}


	@Override
	protected ARole onAddRole()
	{
		update = true;
		return new MoveRole();
	}


	@Override
	protected void doUpdateAfterRoles()
	{
		super.doUpdateBeforeRoles();
		if (update)
		{
			List<IVector2> sortedDestinations = reorderDestinationsToRoles(destinations.subList(0, getRoles().size()));
			int i = 0;
			for (ARole role : getRoles())
			{
				MoveRole moveRole = (MoveRole) role;
				moveRole.getMoveCon().setPenaltyAreaOurObstacle(false);
				moveRole.getMoveCon().setPenaltyAreaTheirObstacle(false);
				moveRole.updateDestination(sortedDestinations.get(i));
				moveRole.updateTargetAngle(0);
				i++;
			}
			update = false;
		}
	}
}
