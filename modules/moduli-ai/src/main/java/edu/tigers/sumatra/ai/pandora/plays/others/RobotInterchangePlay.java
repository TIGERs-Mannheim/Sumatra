/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.others;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * @author ArneS
 */
public class RobotInterchangePlay extends AMaintenancePlay
{
	
	enum LeftRight
	{
		LEFT(1),
		RIGHT(-1);
		private int factor;
		
		
		LeftRight(final int factor)
		{
			this.factor = factor;
		}
		
		
		public int getFactor()
		{
			return factor;
		}
	}
	
	@Configurable(comment = "Distance between bots in interchange lineup", defValue = "220.0")
	private static double distanceBetweenBotsInInterchangePosition = 220;
	
	@Configurable(comment = "Orientation of bots in interchange lineup", defValue = "0.0")
	private static double targetOrientation = 0;
	
	@Configurable(comment = "Distance from field line for interchangable bots (may be negative)", defValue = "130.0")
	private static double distanceToFieldLine = 130.0;
	
	@Configurable(comment = "Position for interchangable bots relative to goal", defValue = "LEFT")
	private static LeftRight positionRelativeToGoal = LeftRight.LEFT;
	
	@Configurable(comment = "Offset factor to centerLine", defValue = "0.0")
	private static double offsetFactorToCenterline = 0.0;
	
	private static IVector2 lineupDirectionVector = Vector2.fromXY(1, 0)
			.scaleTo(distanceBetweenBotsInInterchangePosition);
	
	
	/**
	 * Create a new interchange play
	 */
	public RobotInterchangePlay()
	{
		super(EPlay.INTERCHANGE);
	}
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame frame)
	{
		calculateBotActions(frame, computeSupportVectorForLineup(getRoles().size()),
				lineupDirectionVector, targetOrientation);
	}
	
	
	private IVector2 computeSupportVectorForLineup(final int nBots)
	{
		double y = Geometry.getField().maxY() + distanceToFieldLine;
		double x = ((offsetFactorToCenterline * Geometry.getFieldLength()) / 2)
				+ ((nBots * distanceBetweenBotsInInterchangePosition) / 2);
		return Vector2.fromXY(-x, positionRelativeToGoal.getFactor() * y);
	}
}
