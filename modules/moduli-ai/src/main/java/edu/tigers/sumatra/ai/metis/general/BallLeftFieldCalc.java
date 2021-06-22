/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.general;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.wp.data.BallLeftFieldPosition;
import edu.tigers.sumatra.wp.util.BallLeftFieldCalculator;
import lombok.Getter;


/**
 * Save the moment, the ball left the field
 */
public class BallLeftFieldCalc extends ACalculator
{
	private final BallLeftFieldCalculator ballLeftFieldCalculator = new BallLeftFieldCalculator();

	@Getter
	private BallLeftFieldPosition ballLeftFieldPosition;


	@Override
	public void doCalc()
	{
		ballLeftFieldPosition = ballLeftFieldCalculator.process(getWFrame());

		if (ballLeftFieldPosition != null)
		{
			getShapes(EAiShapesLayer.AI_BALL_LEFT_FIELD).add(
					new DrawableCircle(
							Circle.createCircle(ballLeftFieldPosition.getPosition().getPos(), 100)));
		}
	}
}
