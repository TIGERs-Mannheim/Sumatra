/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.calc;

import java.awt.Color;
import java.util.List;

import edu.tigers.autoreferee.AutoRefFrame;
import edu.tigers.autoreferee.EAutoRefShapesLayer;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.NGeometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.circle.Circle;


public class AllowedDistancesAutoRefVisCalc implements IAutoRefereeCalc
{
	@Override
	public void process(final AutoRefFrame frame)
	{
		List<IDrawableShape> shapes = frame.getShapes().get(EAutoRefShapesLayer.ALLOWED_DISTANCES);
		
		if (frame.getGameState().isStandardSituation() || frame.getGameState().isStoppedGame())
		{
			NGeometry.getPenaltyAreas().stream()
					.map(p -> p.withMargin(RuleConstraints.getBotToPenaltyAreaMarginStandard()).getDrawableShapes())
					.forEach(shapes::addAll);
		}
		
		if (frame.getGameState().isStoppedGame())
		{
			shapes.add(new DrawableCircle(Circle.createCircle(frame.getWorldFrame().getBall().getPos(),
					RuleConstraints.getStopRadius() + Geometry.getBallRadius())));
		}
		shapes.forEach(s -> s.setColor(Color.red));
	}
}
