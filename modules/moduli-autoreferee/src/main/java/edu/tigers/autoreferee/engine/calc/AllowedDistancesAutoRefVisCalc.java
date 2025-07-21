/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.calc;

import edu.tigers.autoreferee.AutoRefFrame;
import edu.tigers.autoreferee.EAutoRefShapesLayer;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableShapeBoundary;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.NGeometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.circle.Circle;

import java.awt.Color;
import java.awt.geom.Arc2D;
import java.util.List;


public class AllowedDistancesAutoRefVisCalc implements IAutoRefereeCalc
{
	@Override
	public void process(final AutoRefFrame frame)
	{
		List<IDrawableShape> shapes = frame.getShapes().get(EAutoRefShapesLayer.ALLOWED_DISTANCES);

		if (frame.getGameState().isStandardSituation() || frame.getGameState().isStoppedGame())
		{
			NGeometry.getPenaltyAreas().stream()
					.map(p -> p.withMargin(RuleConstraints.getBotToPenaltyAreaMarginStandard())
							.withRoundedCorners(RuleConstraints.getBotToPenaltyAreaMarginStandard()))
					.map(DrawableShapeBoundary::new)
					.map(s -> s.setArcType(Arc2D.OPEN))
					.forEach(shapes::add);
		}

		if (frame.getGameState().isStoppedGame())
		{
			shapes.add(new DrawableCircle(Circle.createCircle(frame.getWorldFrame().getBall().getPos(),
					RuleConstraints.getStopRadius() + Geometry.getBallRadius())));
		}
		shapes.forEach(s -> s.setColor(Color.red));
	}
}
