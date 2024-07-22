/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.standard;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


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

	@Configurable(comment = "Should bots show a T shape during maintenance?", defValue = "true")
	private static boolean useTShape = true;


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


	private IVector2 calculateStartingPos(int nBots, double y)
	{
		double totalLength = (nBots - 1) * y;
		return Vector2.fromXY(startingXPos * Geometry.getField().minX(), -totalLength / 2.0);
	}


	@Override
	protected void doUpdateAfterRoles()
	{
		super.doUpdateAfterRoles();
		calculateActions();
	}


	private void calculateActions()
	{
		List<MoveRole> roles = findRoles(MoveRole.class);
		if (roles.isEmpty())
		{
			completionTimer.reset();
			return;
		} else if (completionTimer.isTimeUp(getWorldFrame().getTimestamp()))
		{
			roles.forEach(MoveRole::disableMotors);
			return;
		}
		if (roles.size() > 4 && useTShape)
		{
			calculateBotActionsForT(direction);
		} else
		{
			calculateBotActions(calculateStartingPos(roles.size(), direction.y()), direction, orientation);
		}
	}


	/**
	 * Compute positions for T shaped maintenance
	 * Be aware that it does not adjust to very small field sizes!!
	 *
	 * @param direction
	 */
	private void calculateBotActionsForT(IVector2 direction)
	{
		List<MoveRole> roles = findRoles(MoveRole.class);
		direction = Vector2.fromXY(direction.x(), direction.y() * (getWorldFrame().isInverted() ? -1.0 : 1.0));
		direction = setMinDistance(direction);

		roles.sort(Comparator.comparing(ARole::getBotID));
		List<IVector2> drawingDestinations = new ArrayList<>();

		int nBots = roles.size();
		int botsPerStroke = nBots / 2;
		int count = 0;
		int posOffset = 1;
		IVector2 dest = calculateStartingPos(botsPerStroke + 1, direction.y()).subtractNew(direction);
		IVector2 normalDirection = direction.getNormalVector();
		for (MoveRole role : roles)
		{
			do
			{
				if (count > botsPerStroke)
				{
					dest = dest.addNew(normalDirection.multiplyNew(Math.pow(-1, count) * posOffset));
					posOffset++;
				} else
				{
					dest = dest.addNew(direction);
					if (count == botsPerStroke && (nBots - botsPerStroke) % 2 == 0)
					{
						dest = dest.addNew(normalDirection.multiplyNew(0.5 * Math.pow(-1, count)));
					}
				}
				count++;
				drawingDestinations.add(dest);
			} while (!pointChecker.allMatch(getAiFrame().getBaseAiFrame(), dest, role.getBotID()));
			role.updateDestination(dest);
			role.updateTargetAngle(AngleMath.deg2rad(orientation));
		}
		drawDestinations(drawingDestinations, orientation);
		updateTimer(roles);
	}


	private IVector2 setMinDistance(IVector2 direction)
	{
		return direction.getLength() < Geometry.getBotRadius() * 2.5 ?
				direction.scaleToNew(Geometry.getBotRadius() * 2.5) :
				direction;
	}

}
