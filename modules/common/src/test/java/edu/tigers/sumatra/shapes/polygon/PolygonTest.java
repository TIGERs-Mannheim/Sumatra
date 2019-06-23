/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 18.05.2016
 * Author(s): KaiE
 * *********************************************************
 */
package edu.tigers.sumatra.shapes.polygon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;

import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;


/**
 * @author KaiE
 */
public class PolygonTest
{
	
	
	/**
	 * DEBUG METHOD to plot the Polygon.... saved as png to the given path
	 */
	@SuppressWarnings("unused")
	private final void plotSetup(final String path, final Polygon p,
			final double margin, final double min, final double max, final int reso)
	{
		BufferedImage bi = new BufferedImage(reso, reso, BufferedImage.TYPE_INT_ARGB);
		Color blue = Color.BLUE;
		Color green = Color.GREEN;
		Color red = Color.RED;
		for (int i = 0; i < reso; ++i)
		{
			for (int j = 0; j < reso; ++j)
			{
				
				final IVector2 pnt = new Vector2(min + ((max - min) * ((double) i / reso)),
						min + ((max - min) * ((double) j / reso)));
				int t = 0;
				t += p.isPointInShape(pnt, 0) ? 1 : 0;
				t += p.isPointInShape(pnt, margin) ? 1 : 0;
				bi.setRGB(i, j, t == 0 ? blue.getRGB() : (t == 1 ? green.getRGB() : red.getRGB()));
			}
		}
		try
		{
			// retrieve image
			File outputfile = new File(path);
			ImageIO.write(bi, "png", outputfile);
			
		} catch (IOException e)
		{
		}
	}
	
	
	/**
	 * test area
	 */
	@Test
	public void testArea()
	{
		PolygonBuilder builder = new PolygonBuilder();
		final int size = 1000;
		for (int i = 0; i < size; ++i)
		{
			final IVector2 p = new Vector2(Math.cos((i * 2 * Math.PI) / size),
					Math.sin((i * 2 * Math.PI) / size));
			builder.addPoint(p);
			
		}
		Polygon p = builder.build();
		
		assertEquals(p.getArea(), Math.PI, 1e-4);
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testPointInShape()
	{
		PolygonBuilder b = new PolygonBuilder();
		b.addPoint(new Vector2(0, 0));
		b.addPoint(new Vector2(1, 0));
		b.addPoint(new Vector2(1, 1));
		b.addPoint(new Vector2(0, 1));
		Polygon p = b.build();
		
		assertTrue(p.isPointInShape(new Vector2(0, 0)));
		assertTrue(p.isPointInShape(new Vector2(1, 0)));
		assertTrue(p.isPointInShape(new Vector2(1, 1)));
		assertTrue(p.isPointInShape(new Vector2(0, 1)));
		assertTrue(p.isPointInShape(new Vector2(0.5, 0.5)));
		assertFalse(p.isPointInShape(new Vector2(2, 2)));
		assertTrue(p.isPointInShape(new Vector2(2, 2), 1));
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testNearestPointOutside()
	{
		
		PolygonBuilder b = new PolygonBuilder();
		b.addPoint(new Vector2(-1, -1));
		b.addPoint(new Vector2(1, -1));
		b.addPoint(new Vector2(1, 1));
		b.addPoint(new Vector2(-1, 1));
		Polygon p = b.build();
		
		// nearest point is the most left point as the distance is always the same... -> first edge is taken
		IVector2 res1 = p.nearestPointOutside(AVector2.ZERO_VECTOR);
		assertTrue(res1.x() == 0);
		assertTrue(res1.y() == -1);
		
		IVector2 res2 = p.nearestPointOutside(AVector2.X_AXIS, 1);
		assertTrue(res2.x() == 2);
		assertTrue(res2.y() == 0);
		
		IVector2 res3 = p.nearestPointOutside(new Vector2(0.1, 0.1), new Vector2(0.1, 0.1).add(AVector2.Y_AXIS), 1);
		assertTrue(res3.y() == 2);
		assertEquals(0.1, res3.x(), 1e-4);
		
	}
	
}

