/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line;


import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * @author Lukas Magel
 */
abstract class AbstractLineTest
{
	protected static final double ACCURACY = 1e-6;


	void doTestGetSlope(final LineConstructor lineConstructor)
	{
		ILineBase zeroLine = lineConstructor.apply(Vector2f.ZERO_VECTOR);
		assertThat(zeroLine.getSlope()).isNotPresent();

		ILineBase verticalLine = lineConstructor.apply(Vector2f.Y_AXIS);
		assertThat(verticalLine.getSlope()).isNotPresent();

		IVector2 dV = Vector2.fromXY(21, 42);
		ILineBase properLine = lineConstructor.apply(dV);
		assertThat(properLine.getSlope()).isPresent();
		assertThat(properLine.getSlope().get()).isCloseTo(dV.y() / dV.x(), within(ACCURACY));
	}


	void doTestGetAngle(final LineConstructor lineConstructor)
	{
		ILineBase zeroLine = lineConstructor.apply(Vector2f.ZERO_VECTOR);
		assertThat(zeroLine.getAngle()).isNotPresent();

		double angle = Math.PI / 2;
		IVector2 dV = Vector2.fromAngle(angle);
		ILineBase properLine = lineConstructor.apply(dV);
		assertThat(properLine.getAngle()).isPresent();
		assertThat(properLine.getAngle().get()).isCloseTo(angle, within(ACCURACY));
	}


	void doTestOrientation(final LineConstructor lineConstructor)
	{
		ILineBase zeroLine = lineConstructor.apply(Vector2f.ZERO_VECTOR);
		assertThat(zeroLine.isHorizontal()).isFalse();
		assertThat(zeroLine.isVertical()).isFalse();

		ILineBase verticalLine = lineConstructor.apply(Vector2f.Y_AXIS);
		assertThat(verticalLine.isHorizontal()).isFalse();
		assertThat(verticalLine.isVertical()).isTrue();

		ILineBase horizontalLine = lineConstructor.apply(Vector2f.X_AXIS);
		assertThat(horizontalLine.isHorizontal()).isTrue();
		assertThat(horizontalLine.isVertical()).isFalse();

		ILineBase nonOrthogonalLine = lineConstructor.apply(Vector2.fromAngle(Math.PI / 4));
		assertThat(nonOrthogonalLine.isHorizontal()).isFalse();
		assertThat(nonOrthogonalLine.isVertical()).isFalse();
	}


	void doTestIsParallelTo(final LineConstructor lineConstructor)
	{
		IVector2 dV = Vector2.fromAngle(1.5d);
		ILineBase nonZeroLine = lineConstructor.apply(dV);
		ILineBase zeroLine = lineConstructor.apply(Vector2f.ZERO_VECTOR);

		assertThat(nonZeroLine.isParallelTo(zeroLine)).isFalse();
		assertThat(zeroLine.isParallelTo(nonZeroLine)).isFalse();

		assertThat(nonZeroLine.isParallelTo(nonZeroLine)).isTrue();
		assertThat(zeroLine.isParallelTo(zeroLine)).isFalse();
	}

	interface LineConstructor extends Function<IVector2, ILineBase>
	{

	}
}
