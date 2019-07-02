/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.calc;

import edu.tigers.autoreferee.AutoRefFrame;
import edu.tigers.autoreferee.EAutoRefShapesLayer;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.wp.util.BallLeftFieldCalculator;


/**
 * Save the moment, the ball left the field
 */
public class BallLeftFieldAutoRefCalc implements IAutoRefereeCalc
{
	private final BallLeftFieldCalculator ballLeftFieldCalculator = new BallLeftFieldCalculator();
	
	
	@Override
	public void process(final AutoRefFrame frame)
	{
		frame.setBallLeftFieldPos(ballLeftFieldCalculator.process(frame.getWorldFrame()));
		frame.setBallInsideField(Geometry.getField().withMargin(Geometry.getLineWidth() + Geometry.getBallRadius())
				.isPointInShape(frame.getWorldFrame().getBall().getPos()));
		
		drawBallLeftFieldPos(frame);
	}
	
	
	private void drawBallLeftFieldPos(final AutoRefFrame frame)
	{
		if (frame.getBallLeftFieldPos().isPresent())
		{
			frame.getShapes().get(EAutoRefShapesLayer.BALL_LEFT_FIELD)
					.add(new DrawableCircle(
							Circle.createCircle(frame.getBallLeftFieldPos().get().getPosition().getPos(), 100)));
			frame.getShapes().get(EAutoRefShapesLayer.BALL_LEFT_FIELD)
					.add(new DrawablePoint(frame.getBallLeftFieldPos().get().getPosition().getPos()));
		}
	}
}
