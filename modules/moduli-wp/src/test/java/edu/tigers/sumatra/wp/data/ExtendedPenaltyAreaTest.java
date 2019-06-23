/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.05.2016
 * Author(s): KaiE
 * *********************************************************
 */
package edu.tigers.sumatra.wp.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;

import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.I2DShape;


/**
 * @author KaiE
 */
public class ExtendedPenaltyAreaTest
{
	private final PenaltyArea				tigers		= Geometry.getPenaltyAreaOur();
	private final PenaltyArea				opponents	= Geometry.getPenaltyAreaTheir();
																	
	private final ExtendedPenaltyArea	etiger		= new ExtendedPenaltyArea(tigers);
	private final ExtendedPenaltyArea	eoppon		= new ExtendedPenaltyArea(opponents);
																	
																	
	@SuppressWarnings("unused")
	/**
	 * DEBUG METHOD to plot the penalty area and the extensions.... saved as png to the given path
	 */
	private final void plotSetup(final String path, final PenaltyArea pa,
			final double marginE, final double marginN)
	{
		BufferedImage bi = new BufferedImage(1400, 2520, BufferedImage.TYPE_INT_ARGB);
		Color blue = Color.BLUE;
		Color green = Color.GREEN;
		Color red = Color.RED;
		final double sign = pa.getOwner() == ETeam.TIGERS ? 1 : -1;
		ExtendedPenaltyArea epa = new ExtendedPenaltyArea(pa);
		for (int i = 0; i < 1400; ++i)
		{
			for (int j = 0; j < 2520; ++j)
			{
				
				final IVector2 p = new Vector2((sign * -3000) - (sign * i), (sign * -1260) + (sign * j));
				int t = 0;
				t += epa.isPointInShape(p, marginE) ? 1 : 0;
				t += pa.isPointInShape(p, marginN) ? 1 : 0;
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
	 * {@link I2DShape} Test for {@link I2DShape#isPointInShape(edu.tigers.sumatra.math.IVector2)}
	 */
	@Test
	public void testIsPointInShape()
	{
		Vector2 etigerPoint = new Vector2(tigers.getGoalCenter().addNew(new Vector2(-10, 0)));
		
		assertTrue(etiger.isPointInShape(etigerPoint));
		assertTrue(!tigers.isPointInShape(etigerPoint));
		assertTrue(!eoppon.isPointInShape(etigerPoint));
		assertTrue(!opponents.isPointInShape(etigerPoint));
		
		
		Vector2 eoppPoint = new Vector2(-etigerPoint.x(), etigerPoint.y());
		assertTrue(eoppon.isPointInShape(eoppPoint));
		assertTrue(!opponents.isPointInShape(eoppPoint));
		assertTrue(!etiger.isPointInShape(eoppPoint));
		assertTrue(!tigers.isPointInShape(eoppPoint));
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testIsLineIntersectingShape()
	{
		Line throughExtension = Line.newLine(new Vector2(tigers.getGoalCenter().x() - 50, -1600),
				new Vector2(tigers.getGoalCenter().x() - 50, 1600));
		assertTrue(etiger.isLineIntersectingShape(throughExtension));
		assertFalse(tigers.isLineIntersectingShape(throughExtension));
		
		Line throughBoundary = Line.newLine(new Vector2(tigers.getGoalCenter().x(), -1600),
				new Vector2(tigers.getGoalCenter().x(), 1600));
		assertTrue(etiger.isLineIntersectingShape(throughBoundary));
		assertTrue(tigers.isLineIntersectingShape(throughBoundary));
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testNearestPointOutside()
	{
		final IVector2 gp = tigers.getGoalCenter();
		final IVector2 gpEx = tigers.getPenaltyMark();
		assertTrue(etiger.nearestPointOutside(gp).equals(gpEx, 0.01));
		
		final IVector2 pp = new Vector2(tigers.getGoalCenter().x() - 50, 0);
		
		assertTrue(etiger.nearestPointOutside(pp).equals(
				pp.addNew(new Vector2(0, tigers.getRadiusOfPenaltyArea() + tigers.getLengthOfPenaltyAreaFrontLineHalf())),
				0.01));
				
	}
	
	
}
