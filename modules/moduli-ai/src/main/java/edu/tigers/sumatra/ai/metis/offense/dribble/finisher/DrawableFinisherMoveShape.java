/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.dribble.finisher;

import edu.tigers.sumatra.drawable.ADrawable;
import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.IDrawableTool;
import lombok.AllArgsConstructor;

import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.util.ArrayList;
import java.util.List;


@AllArgsConstructor
public class DrawableFinisherMoveShape extends ADrawable
{
	private FinisherMoveShape shape;

	@Override
	public void paintShape(Graphics2D g, IDrawableTool tool, boolean invert)
	{
		super.paintShape(g, tool, invert);
		getShapes().forEach(e -> e.paintShape(g, tool, invert));
	}

	public List<IDrawableShape> getShapes()
	{
		List<IDrawableShape> shapes = new ArrayList<>();
		shapes.add(new DrawableLine(shape.getLineLeft(), getColor()));
		shapes.add(new DrawableLine(shape.getLineBottom(), getColor()));
		shapes.add(new DrawableLine(shape.getLineRight(), getColor()));
		shapes.add(new DrawableArc(shape.getLeftArc(), getColor()).setArcType(Arc2D.OPEN));
		shapes.add(new DrawableArc(shape.getRightArc(), getColor()).setArcType(Arc2D.OPEN));
		return shapes;
	}

}
