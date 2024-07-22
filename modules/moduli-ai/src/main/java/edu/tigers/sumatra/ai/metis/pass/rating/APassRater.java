/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass.rating;

import edu.tigers.sumatra.drawable.IDrawableShape;
import lombok.Setter;

import java.util.List;
import java.util.function.Supplier;


/**
 * Base class for pass raters.
 */
public abstract class APassRater implements IPassRater
{
	@Setter
	private List<IDrawableShape> shapes;


	/**
	 * Draw a shape, if drawing is enabled.
	 *
	 * @param shapeSupplier
	 */
	protected final void draw(Supplier<IDrawableShape> shapeSupplier)
	{
		if (shapes != null)
		{
			shapes.add(shapeSupplier.get());
		}
	}
}
