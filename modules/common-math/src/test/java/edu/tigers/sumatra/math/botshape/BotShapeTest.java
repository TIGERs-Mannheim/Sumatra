/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.botshape;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


public class BotShapeTest
{
	private IVector2 position = Vector2.fromXY(1, 2);
	private double radius = 0.09;
	private double center2Dribbler = 0.07;
	private double orientation = Math.PI;
	private BotShape botShape = BotShape.fromFullSpecification(position, radius, center2Dribbler, orientation);


	@Test
	public void testConstruction()
	{
		assertThat(botShape.center()).isEqualTo(position);
		assertThat(botShape.radius()).isCloseTo(radius, within(1e-10));
		assertThat(botShape.getCenter2Dribbler()).isCloseTo(center2Dribbler, within(1e-10));
		assertThat(botShape.getOrientation()).isCloseTo(orientation, within(1e-10));
	}


	@Test
	public void testMirror()
	{
		IBotShape mirror = botShape.mirror();
		assertThat(mirror.center()).isEqualTo(Vector2.fromXY(-1, -2));
		assertThat(mirror.radius()).isCloseTo(radius, within(1e-10));
		assertThat(mirror.getCenter2Dribbler()).isCloseTo(center2Dribbler, within(1e-10));
		assertThat(mirror.getOrientation()).isCloseTo(Math.PI * 2, within(1e-10));
	}
}