/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.geometry;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import edu.tigers.sumatra.math.vector.Vector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class OpponentPenaltyAreaTest
{
	
	
	@Test
	public void isPointInShape() throws Exception
	{
		IPenaltyArea their = Geometry.getPenaltyAreaTheir();
		IPenaltyArea our = Geometry.getPenaltyAreaOur();
		assertThat(our.isPointInShape(Vector2.fromXY(-4000, 0))).isTrue();
		assertThat(our.isPointInShape(Vector2.fromXY(4000, 0))).isFalse();
		assertThat(their.isPointInShape(Vector2.fromXY(4000, 0))).isTrue();
		assertThat(their.isPointInShape(Vector2.fromXY(-4000, 0))).isFalse();
	}
	
}