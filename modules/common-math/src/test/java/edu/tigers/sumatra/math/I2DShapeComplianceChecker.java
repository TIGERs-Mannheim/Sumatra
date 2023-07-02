/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


public class I2DShapeComplianceChecker
{
	private final I2DShape shape;
	private final boolean closedShape;


	private I2DShapeComplianceChecker(I2DShape shape, boolean closedShape)
	{
		this.shape = shape;
		this.closedShape = closedShape;
	}


	public static void checkCompliance(I2DShape shape, boolean closedShape)
	{
		var checker = new I2DShapeComplianceChecker(shape, closedShape);
		checker.checkGetPerimeterPath();
		checker.checkGetPerimeterLength();
	}


	private void checkGetPerimeterPath()
	{
		var paths = shape.getPerimeterPath();
		IBoundedPath lastPath = null;
		for (var path : paths)
		{
			if (lastPath != null)
			{
				assertThat(lastPath.getPathEnd()).isEqualTo(path.getPathStart());
			}
			lastPath = path;
		}
		if (closedShape)
		{
			assertThat(paths.get(0).getPathStart()).isEqualTo(paths.get(paths.size() - 1).getPathEnd());
		}
	}


	private void checkGetPerimeterLength()
	{
		var path = shape.getPerimeterPath();
		assertThat(shape.getPerimeterLength()).isCloseTo(path.stream().mapToDouble(IBoundedPath::getLength).sum(),
				within(1e-10));
	}
}
