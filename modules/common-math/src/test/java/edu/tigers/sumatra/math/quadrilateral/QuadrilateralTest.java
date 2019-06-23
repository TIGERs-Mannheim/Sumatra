/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.quadrilateral;

import com.google.common.collect.Collections2;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class QuadrilateralTest
{
	@Test
	public void fromCorners() throws Exception
	{
		List<IVector2> corners = new ArrayList<>(4);
		
		corners.add(Vector2.fromXY(-1, -1));
		corners.add(Vector2.fromXY(-1, 1));
		corners.add(Vector2.fromXY(1, -1));
		corners.add(Vector2.fromXY(1, 1));
		
		IQuadrilateral quadrilateral = Quadrilateral.fromCorners(corners);
		for (List<IVector2> permCorners : Collections2.permutations(corners))
		{
			IQuadrilateral permQuadrangle = Quadrilateral.fromCorners(permCorners);
			assertThat(permQuadrangle.getCorners()).containsAll(permCorners);
			assertThat(permQuadrangle).isEqualTo(quadrilateral);
		}
	}
	
	
	@Test
	public void triangles() throws Exception
	{
		
	}
	
	
	@Test
	public void isPointInShape() throws Exception
	{
		List<IVector2> acorners = new ArrayList<>(4);
		
		acorners.add(Vector2.fromXY(1, -1));
		acorners.add(Vector2.fromXY(-1, 1));
		acorners.add(Vector2.fromXY(1, 1));
		acorners.add(Vector2.fromXY(-1, -1));
		
		for (List<IVector2> corners : Collections2.permutations(acorners)) {
			IQuadrilateral quadrilateral = Quadrilateral.fromCorners(corners.get(0), corners.get(1), corners.get(2),
					corners.get(3));
			assertThat(quadrilateral.isPointInShape(Vector2.fromXY(0, 0)));
			assertThat(quadrilateral.isPointInShape(Vector2.fromXY(1, 0.9)));
			assertThat(quadrilateral.isPointInShape(Vector2.fromXY(1, 0.5)));
			assertThat(quadrilateral.isPointInShape(Vector2.fromXY(-1, -0.9)));
			assertThat(quadrilateral.isPointInShape(Vector2.fromXY(-1, -0.5)));
			assertThat(quadrilateral.isPointInShape(Vector2.fromXY(-1, -0)));
			assertThat(quadrilateral.isPointInShape(Vector2.fromXY(0, -0.9)));
			assertThat(quadrilateral.isPointInShape(Vector2.fromXY(0, 0.9)));
			assertThat(quadrilateral.isPointInShape(Vector2.fromXY(0, -0.2)));
		}
	}
	
	
	@Test
	public void isPointInShape1() throws Exception
	{
		
	}
	
	
	@Test
	public void testEquals()
	{
		EqualsVerifier.forClass(Quadrilateral.class)
				.verify();
	}
	
}