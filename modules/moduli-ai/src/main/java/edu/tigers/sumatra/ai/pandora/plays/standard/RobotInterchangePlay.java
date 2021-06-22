/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.standard;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.AllArgsConstructor;


/**
 * @author ArneS
 */
public class RobotInterchangePlay extends AMaintenancePlay
{
	@Configurable(comment = "Distance between bots in interchange lineup", defValue = "220.0")
	private static double distanceBetweenBotsInInterchangePosition = 220;

	@Configurable(comment = "Orientation of bots in interchange lineup", defValue = "0.0")
	private static double targetOrientation = 0;

	@Configurable(comment = "Distance from field line for interchangable bots (may be negative)", defValue = "-130.0")
	private static double distanceToFieldLine = -130.0;

	@Configurable(comment = "Position for interchangable bots relative to goal", defValue = "LEFT")
	private static LeftRight positionRelativeToGoal = LeftRight.LEFT;

	@Configurable(comment = "Offset factor to centerLine", defValue = "0.0")
	private static double offsetFactorToCenterline = 0.0;

	private IVector2 lineupDirectionVector = Vector2.fromXY(1, 0)
			.scaleTo(distanceBetweenBotsInInterchangePosition);


	/**
	 * Create a new interchange play
	 */
	public RobotInterchangePlay()
	{
		super(EPlay.INTERCHANGE);
	}


	@Override
	protected void doUpdateAfterRoles()
	{
		super.doUpdateAfterRoles();
		calculateBotActions(computeSupportVectorForLineup(getRoles().size()), lineupDirectionVector, targetOrientation);
	}


	private IVector2 computeSupportVectorForLineup(final int nBots)
	{
		double y = Geometry.getField().maxY() + distanceToFieldLine;
		double x = ((offsetFactorToCenterline * Geometry.getFieldLength()) / 2)
				+ ((nBots * distanceBetweenBotsInInterchangePosition) / 2);
		return Vector2.fromXY(-x, positionRelativeToGoal.factor * y);
	}


	@AllArgsConstructor
	enum LeftRight
	{
		LEFT(1),
		RIGHT(-1),

		;
		private final int factor;
	}
}
