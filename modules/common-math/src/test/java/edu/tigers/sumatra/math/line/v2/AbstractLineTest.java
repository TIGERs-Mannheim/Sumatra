/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line.v2;

import static edu.tigers.sumatra.Present.isNotPresent;
import static edu.tigers.sumatra.Present.isPresentAnd;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

import java.util.function.Function;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * @author Lukas Magel
 */
abstract class AbstractLineTest
{
	protected static final double ACCURACY = 1e-6;
	
	
	void doTestGetSlope(final LineConstructor lineConstructor)
	{
		ILineBase zeroLine = lineConstructor.apply(Vector2f.ZERO_VECTOR);
		assertThat(zeroLine.getSlope(), isNotPresent());
		
		ILineBase verticalLine = lineConstructor.apply(Vector2f.Y_AXIS);
		assertThat(verticalLine.getSlope(), isNotPresent());
		
		IVector2 dV = Vector2.fromXY(21, 42);
		ILineBase properLine = lineConstructor.apply(dV);
		assertThat(properLine.getSlope(), isPresentAnd(is(dV.y() / dV.x())));
	}
	
	
	void doTestGetAngle(final LineConstructor lineConstructor)
	{
		ILineBase zeroLine = lineConstructor.apply(Vector2f.ZERO_VECTOR);
		assertThat(zeroLine.getAngle(), isNotPresent());
		
		double angle = Math.PI / 2;
		IVector2 dV = Vector2.fromAngle(angle);
		ILineBase properLine = lineConstructor.apply(dV);
		assertThat(properLine.getAngle(), isPresentAnd(closeTo(angle, ACCURACY)));
	}
	
	
	void doTestOrientation(final LineConstructor lineConstructor)
	{
		ILineBase zeroLine = lineConstructor.apply(Vector2f.ZERO_VECTOR);
		assertThat(zeroLine.isHorizontal(), is(false));
		assertThat(zeroLine.isVertical(), is(false));
		
		ILineBase verticalLine = lineConstructor.apply(Vector2f.Y_AXIS);
		assertThat(verticalLine.isHorizontal(), is(false));
		assertThat(verticalLine.isVertical(), is(true));
		
		ILineBase horizontalLine = lineConstructor.apply(Vector2f.X_AXIS);
		assertThat(horizontalLine.isHorizontal(), is(true));
		assertThat(horizontalLine.isVertical(), is(false));
		
		ILineBase nonOrthogonalLine = lineConstructor.apply(Vector2.fromAngle(Math.PI / 4));
		assertThat(nonOrthogonalLine.isHorizontal(), is(false));
		assertThat(nonOrthogonalLine.isVertical(), is(false));
	}
	
	
	void doTestIsParallelTo(final LineConstructor lineConstructor)
	{
		IVector2 dV = Vector2.fromAngle(1.5d);
		ILineBase nonZeroLine = lineConstructor.apply(dV);
		ILineBase zeroLine = lineConstructor.apply(Vector2f.ZERO_VECTOR);
		
		assertThat(nonZeroLine.isParallelTo(zeroLine), is(false));
		assertThat(zeroLine.isParallelTo(nonZeroLine), is(false));
		
		assertThat(nonZeroLine.isParallelTo(nonZeroLine), is(true));
		assertThat(zeroLine.isParallelTo(zeroLine), is(false));
	}
	
	interface LineConstructor extends Function<IVector2, ILineBase>
	{
		
	}
}
