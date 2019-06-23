/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.polygon;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Test;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * @author KaiE
 */
public class PolygonTest
{
	
	
	/**
	 * DEBUG METHOD to plot the Polygon.... saved as png to the given path
	 */
	@SuppressWarnings("unused")
	private void plotSetup(final String path, final Polygon p,
			final double margin, final double min, final double max, final int reso) throws Exception
	{
		BufferedImage bi = new BufferedImage(reso, reso, BufferedImage.TYPE_INT_ARGB);
		Color blue = Color.BLUE;
		Color green = Color.GREEN;
		Color red = Color.RED;
		for (int i = 0; i < reso; ++i)
		{
			for (int j = 0; j < reso; ++j)
			{
				
				final IVector2 pnt = Vector2.fromXY(min + ((max - min) * ((double) i / reso)),
						min + ((max - min) * ((double) j / reso)));
				int t = 0;
				t += p.isPointInShape(pnt, 0) ? 1 : 0;
				t += p.isPointInShape(pnt, margin) ? 1 : 0;
				bi.setRGB(i, j, t == 0 ? blue.getRGB() : (t == 1 ? green.getRGB() : red.getRGB()));
			}
		}
		// retrieve image
		File outputfile = new File(path);
		ImageIO.write(bi, "png", outputfile);
	}
	
	
	@Test
	public void testPointInShape()
	{
		PolygonBuilder b = new PolygonBuilder();
		b.addPoint(Vector2.fromXY(0, 0));
		b.addPoint(Vector2.fromXY(1, 0));
		b.addPoint(Vector2.fromXY(1, 1));
		b.addPoint(Vector2.fromXY(0, 1));
		Polygon p = b.build();
		
		Assert.assertTrue(p.isPointInShape(Vector2.fromXY(0, 0)));
		Assert.assertTrue(p.isPointInShape(Vector2.fromXY(1, 0)));
		Assert.assertTrue(p.isPointInShape(Vector2.fromXY(1, 1)));
		Assert.assertTrue(p.isPointInShape(Vector2.fromXY(0, 1)));
		Assert.assertTrue(p.isPointInShape(Vector2.fromXY(0.5, 0.5)));
		Assert.assertFalse(p.isPointInShape(Vector2.fromXY(2, 2)));
		Assert.assertTrue(p.isPointInShape(Vector2.fromXY(2, 2), 1));
	}
	
	
	@Test
	public void testNearestPointOutside()
	{
		
		PolygonBuilder b = new PolygonBuilder();
		b.addPoint(Vector2.fromXY(-1, -1));
		b.addPoint(Vector2.fromXY(1, -1));
		b.addPoint(Vector2.fromXY(1, 1));
		b.addPoint(Vector2.fromXY(-1, 1));
		Polygon p = b.build();
		
		// nearest point is the most left point as the distance is always the same... -> first edge is taken
		IVector2 res1 = p.nearestPointOutside(Vector2f.ZERO_VECTOR);
		Assert.assertTrue(res1.x() == 0);
		Assert.assertTrue(res1.y() == -1);
		
		IVector2 res2 = p.nearestPointOutside(Vector2f.X_AXIS, 1);
		Assert.assertTrue(res2.x() == 2);
		Assert.assertTrue(res2.y() == 0);
		
		IVector2 res3 = p.nearestPointOutside(Vector2.fromXY(0.1, 0.1), Vector2.fromXY(0.1, 0.1).add(Vector2f.Y_AXIS), 1);
		Assert.assertTrue(res3.y() == 2);
		Assert.assertEquals(0.1, res3.x(), 1e-4);
		
	}
	
}

