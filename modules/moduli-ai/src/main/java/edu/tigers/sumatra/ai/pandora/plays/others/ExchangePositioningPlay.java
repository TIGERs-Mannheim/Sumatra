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
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class ExchangePositioningPlay extends AMaintenancePlay
{
	
	@Configurable(comment = "first maintenance position", defValue = "-3100.0;-2500.0")
	private static IVector2 startingPos = Vector2.fromXY(-3100, -2500);
	
	@Configurable(comment = "Direction from startingPos with length", defValue = "200.0;0.0")
	private static IVector2 direction = Vector2.fromXY(200, 0);
	
	@Configurable(comment = "Orientation of bots", defValue = "90.0")
	private static double orientation = 90;
	
	
	/**
	 * Creates a new ExchangePositioningPlay
	 */
	public ExchangePositioningPlay()
	{
		super(EPlay.EXCHANGE_POSITIONING);
	}
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame frame)
	{
		calculateBotActions(frame, startingPos, direction, orientation);
	}
}
