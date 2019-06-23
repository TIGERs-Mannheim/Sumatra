/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.07.2011
 * Author(s): Malte
 * *********************************************************
 */
package edu.tigers.sumatra.wp.data;

import static edu.tigers.sumatra.math.AVector2.X_AXIS;
import static edu.tigers.sumatra.math.AVector2.Y_AXIS;
import static edu.tigers.sumatra.math.AVector2.ZERO_VECTOR;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector2f;
import edu.tigers.sumatra.shapes.rectangle.Rectangle;


/**
 * Test class for {@link PenaltyArea}
 * 
 * @author Frieder Berthold
 */
public class PenaltyAreaTest
{
	private final PenaltyArea		tigers		= new PenaltyArea(ETeam.TIGERS);
	private final PenaltyArea		opponents	= new PenaltyArea(ETeam.OPPONENTS);
															
	private final static double	EPSILON		= 1e-4;
	private final static double	EPSILON_SQR	= EPSILON * EPSILON;
															
															
	/**
	 * 
	 */
	@Test
	public void testPenaltyMark()
	{
		double xpos = (Geometry.getFieldLength() / 2) - Geometry.getDistanceToPenaltyMark();
		assertTrue(tigers.getPenaltyMark().equals(new Vector2(-xpos, 0)));
		assertTrue(opponents.getPenaltyMark().equals(new Vector2(xpos, 0)));
	}
	
	
	/**
	 * simply check if the area of the setup is calculated correctly
	 */
	@Test
	public void testArea()
	{
		final double precalculatedArea = (AngleMath.PI_HALF * Geometry.getDistanceToPenaltyMark()
				* Geometry.getDistanceToPenaltyMark())
				+ (Geometry.getLengthOfPenaltyAreaFrontLine() * Geometry.getDistanceToPenaltyMark());
				
		assertTrue(Double.compare(precalculatedArea, tigers.getArea()) == 0);
		assertTrue(Double.compare(precalculatedArea, opponents.getArea()) == 0);
	}
	
	
	/**
	 * checks if all points of step along are within the outer rectangle + old unit test
	 */
	@Test
	public void testStepAlong()
	{
		
		for (int i = 0; i < tigers.getPerimeterFrontCurve(); ++i)
		{
			assertTrue(tigers.getOuterArea().isPointInShape(tigers.stepAlongPenArea(i)));
			assertTrue(opponents.getOuterArea().isPointInShape(opponents.stepAlongPenArea(i)));
			
		}
		doTestStepAlongPenArea(tigers);
		doTestStepAlongPenArea(opponents);
	}
	
	
	/** taken from old penalty area test */
	private void doTestStepAlongPenArea(final PenaltyArea penaltyArea)
	{
		double perimeterQuart = (tigers.getRadiusOfPenaltyArea() * AngleMath.PI) / 2.0;
		double frontLineLen = tigers.getLengthOfPenaltyAreaFrontLineHalf() * 2;
		IVector2 pFrontLinePos = new Vector2(tigers.getPenaltyAreaFrontLine().supportVector().x(), penaltyArea
				.getPenaltyRectangle().yExtend() / 2.0);
		IVector2 pFrontLineNeg = new Vector2(tigers.getPenaltyAreaFrontLine().supportVector().x(), -penaltyArea
				.getPenaltyRectangle().yExtend() / 2.0);
		IVector2 pLastPoint = new Vector2(tigers.getGoalCenter().x(),
				-((tigers.getPenaltyRectangle().yExtend() / 2.0) + tigers.getPenaltyCircleNeg().radius()));
				
		IVector2 point;
		point = tigers.stepAlongPenArea(perimeterQuart);
		assertTrue("Exp:" + pFrontLinePos + " but:" + point, pFrontLinePos.equals(point, EPSILON));
		point = tigers.stepAlongPenArea(perimeterQuart + frontLineLen);
		assertTrue("Exp:" + pFrontLineNeg + " but:" + point, pFrontLineNeg.equals(point, EPSILON));
		point = tigers.stepAlongPenArea((perimeterQuart * 2) + frontLineLen);
		assertTrue("Exp:" + pLastPoint + " but:" + point, pLastPoint.equals(point, EPSILON));
	}
	
	
	/**
	 * nearest Point outside of area without line from old test
	 * Using the implementation to generate nearest points outside to compare is
	 * not the best testing behaviour therefore a additional test exists for this method
	 */
	@Test
	public void testNearestPointOutside()
	{
		// inside our Area
		final IVector2 testPoint1 = tigers.getPenaltyCirclePosCentre().addNew(
				new Vector2(tigers.getPenaltyCirclePos().radius() / 2,
						tigers.getPenaltyCirclePos().radius() / 3.0));
		final IVector2 testPointReturn1 = tigers.getPenaltyCirclePos().nearestPointOutside(testPoint1);
		final IVector2 testPoint2 = tigers.getPenaltyCircleNegCentre().addNew(
				new Vector2(tigers.getPenaltyCirclePos().radius() / 3,
						-tigers.getPenaltyCirclePos().radius() / 4.0));
		final IVector2 testPointReturn2 = tigers.getPenaltyCircleNeg().nearestPointOutside(testPoint2);
		final IVector2 testPoint3 = tigers.getGoalCenter().addNew(
				new Vector2(tigers.getRadiusOfPenaltyArea() / 3, tigers.getRadiusOfPenaltyArea() / 4.0));
		final IVector2 testPointReturn3 = new Vector2(
				tigers.getPenaltyCirclePosCentre().x() + tigers.getRadiusOfPenaltyArea(), testPoint3.y());
				
		// inside their Area
		final IVector2 testPoint4 = opponents.getPenaltyCirclePosCentre().addNew(
				new Vector2(-opponents.getPenaltyCirclePos().radius() / 2,
						opponents.getPenaltyCirclePos().radius() / 3.0));
		final IVector2 testPointReturn4 = opponents.getPenaltyCirclePos().nearestPointOutside(testPoint4);
		final IVector2 testPoint5 = opponents.getPenaltyCircleNegCentre().addNew(
				new Vector2(-opponents.getPenaltyCirclePos().radius() / 3, -opponents.getPenaltyCirclePos()
						.radius() / 4.0));
		final IVector2 testPointReturn5 = opponents.getPenaltyCircleNeg().nearestPointOutside(testPoint5);
		final IVector2 testPoint6 = opponents.getGoalCenter().addNew(
				new Vector2(-tigers.getRadiusOfPenaltyArea() / 3, tigers.getRadiusOfPenaltyArea() / 4.0));
		final IVector2 testPointReturn6 = new Vector2(
				opponents.getPenaltyCirclePosCentre().x() - tigers.getRadiusOfPenaltyArea(), testPoint6.y());
				
		// outside of both Areas
		final IVector2 testPoint7 = new Vector2();
		final IVector2 testPoint8 = new Vector2(
				opponents.getPenaltyCirclePosCentre().x() - tigers.getRadiusOfPenaltyArea(), testPoint6.y());
				
		assertTrue(tigers.nearestPointOutside(testPoint1).equals(testPointReturn1, EPSILON));
		assertTrue(tigers.nearestPointOutside(testPoint2).equals(testPointReturn2, EPSILON));
		assertTrue(tigers.nearestPointOutside(testPoint3).equals(testPointReturn3, EPSILON));
		
		assertTrue(opponents.nearestPointOutside(testPoint4).equals(testPointReturn4, EPSILON));
		assertTrue(opponents.nearestPointOutside(testPoint5).equals(testPointReturn5, EPSILON));
		assertTrue(opponents.nearestPointOutside(testPoint6).equals(testPointReturn6, EPSILON));
		
		assertTrue(opponents.nearestPointOutside(testPoint7).equals(testPoint7, EPSILON));
		assertTrue(opponents.nearestPointOutside(testPoint8).equals(testPoint8, EPSILON));
		
		
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testNearestPointOutsideWithMargin()
	{
		Rectangle field = Geometry.getField();
		double margin = 500.0d;
		double penCircleRadius = opponents.getRadiusOfPenaltyArea();
		
		double posX = ((field.getxExtend() / 2) - penCircleRadius);
		double posY = (opponents.getLengthOfPenaltyAreaFrontLineHalf()) + 250;
		IVector2 point = new Vector2(posX, posY);
		
		IVector2 circleCenter = opponents.getPenaltyCirclePosCentre();
		IVector2 expected = circleCenter.addNew(
				point.subtractNew(circleCenter).multiply(
						(penCircleRadius + margin) / GeoMath.distancePP(point, circleCenter)));
		IVector2 actual = opponents.nearestPointOutside(point, margin);
		
		assertTrue(expected.equals(actual, EPSILON));
	}
	
	
	/**
	 * the mentioned additional test for nearest point outside
	 */
	@Test
	public void testNearestPointOutsideAdditionalTest()
	{
		
		final PenaltyArea areas[] = { tigers, opponents };
		for (PenaltyArea pa : areas)
		{
			IVector2 points[] = {
					pa.getGoalCenter(),
					pa.getPenaltyCirclePosCentre(),
					pa.getPenaltyCircleNegCentre(),
					pa.getPenaltyMark()
			};
			for (int i = 0; i < points.length; ++i)
			{
				final IVector2 v = points[i];
				final IVector2 r = pa.nearestPointOutside(v);
				assertTrue(i + "-th point of penalty-" + pa.getOwner() + " failed", !pa.isPointInShape(r, -EPSILON));
			}
		}
	}
	
	
	/**
	 * check if point in shape is determined correctly on the border
	 */
	@Test
	public void testBorder()
	{
		for (int i = 0; i < tigers.getPerimeterFrontCurve(); ++i)
		{
			/** with epsilon in the area */
			assertTrue(tigers.isPointInShape(tigers.stepAlongPenArea(i), EPSILON));
			assertTrue(opponents.isPointInShape(opponents.stepAlongPenArea(i), EPSILON));
			
			/** directly on the border */
			assertTrue(tigers.isPointInShape(tigers.stepAlongPenArea(i)));
			assertTrue(opponents.isPointInShape(opponents.stepAlongPenArea(i)));
			
			/** with epsilon outside of area */
			assertFalse(tigers.isPointInShape(tigers.stepAlongPenArea(i), -EPSILON));
			assertFalse(opponents.isPointInShape(opponents.stepAlongPenArea(i), -EPSILON));
			
			/** negative test */
			assertTrue(!tigers.isPointInShape(opponents.stepAlongPenArea(i)));
			assertTrue(!opponents.isPointInShape(tigers.stepAlongPenArea(i)));
			
		}
	}
	
	
	/**
	 * check some intersections with sample lines with many possible states
	 */
	@Test
	public void testIntersection()
	{
		final double radius = tigers.getRadiusOfPenaltyArea();
		
		/** check line right through the area **/
		ILine tline = new Line(new Vector2f(tigers.getGoalCenter().x() + 500, -500), Y_AXIS);
		List<IVector2> r = tigers.lineIntersections(tline);
		assertTrue(r.size() == 2);
		assertTrue(
				Math.abs(GeoMath.distancePP(r.get(0), tigers.getPenaltyCircleNegCentre()) - radius) < EPSILON_SQR);
		assertTrue(
				Math.abs(GeoMath.distancePP(r.get(1), tigers.getPenaltyCirclePosCentre()) - radius) < EPSILON_SQR);
				
		tline = new Line(new Vector2f(opponents.getGoalCenter().x() - 500, -500), Y_AXIS);
		r = opponents.lineIntersections(tline);
		assertTrue(r.size() == 2);
		assertTrue(Math
				.abs(GeoMath.distancePP(r.get(0), opponents.getPenaltyCircleNegCentre()) - radius) < EPSILON_SQR);
		assertTrue(Math
				.abs(GeoMath.distancePP(r.get(1), opponents.getPenaltyCirclePosCentre()) - radius) < EPSILON_SQR);
				
		/** check a negative test **/
		tline = new Line(ZERO_VECTOR, Y_AXIS);
		assertFalse(tigers.isLineIntersectingShape(tline));
		assertFalse(opponents.isLineIntersectingShape(tline));
		
		/** check the front-line cut through the penalty mark **/
		tline = new Line(ZERO_VECTOR, X_AXIS);
		r = opponents.lineIntersections(tline);
		assertTrue(r.size() == 1);
		assertTrue(r.get(0).equals(opponents.getPenaltyMark(), EPSILON_SQR));
		
		r = tigers.lineIntersections(tline);
		assertTrue(r.size() == 1);
		assertTrue(r.get(0).equals(tigers.getPenaltyMark(), EPSILON_SQR));
		
		/** check if the front-line intersection is detected with the front-line itself **/
		assertTrue(tigers.isLineIntersectingShape(tigers.getPenaltyAreaFrontLine(), EPSILON));
		assertTrue(opponents.isLineIntersectingShape(opponents.getPenaltyAreaFrontLine(), EPSILON));
		
		/**
		 * check if the line a bit right from the circle centre intersects the area only once (and not on the circle part
		 * outside the area
		 **/
		tline = new Line(new Vector2(0, tigers.getPenaltyCirclePosCentre().y() + 10), X_AXIS);
		r = tigers.lineIntersections(tline);
		assertTrue(r.size() == 1);
		r = opponents.lineIntersections(tline);
		assertTrue(r.size() == 1);
		
		/** same for the other circle **/
		tline = new Line(new Vector2(0, tigers.getPenaltyCircleNegCentre().y() - 10), X_AXIS);
		r = tigers.lineIntersections(tline);
		assertTrue(r.size() == 1);
		r = opponents.lineIntersections(tline);
		assertTrue(r.size() == 1);
		
		/** check if the intersection with line through the circle centre is only detected once (edge case) **/
		tline = new Line(tigers.getPenaltyCirclePosCentre(), X_AXIS);
		r = tigers.lineIntersections(tline);
		assertTrue(r.size() == 1);
		r = opponents.lineIntersections(tline);
		assertTrue(r.size() == 1);
		
		tline = new Line(tigers.getPenaltyCircleNegCentre(), X_AXIS);
		r = tigers.lineIntersections(tline);
		assertTrue(r.size() == 1);
		r = opponents.lineIntersections(tline);
		assertTrue(r.size() == 1);
		
		/** check if the line on the corners of the circle is detected with security margin **/
		tline = new Line(
				tigers.getPenaltyCirclePosCentre().addNew(new Vector2(0, tigers.getRadiusOfPenaltyArea())),
				X_AXIS);
		assertTrue(tigers.isLineIntersectingShape(tline, 0));
		assertTrue(tigers.isLineIntersectingShape(tline, EPSILON));
		assertTrue(!tigers.isLineIntersectingShape(tline, -EPSILON_SQR));
		
		assertTrue(opponents.isLineIntersectingShape(tline, 0));
		assertTrue(opponents.isLineIntersectingShape(tline, EPSILON));
		assertTrue(!opponents.isLineIntersectingShape(tline, -EPSILON_SQR));
		
		tline = new Line(
				tigers.getPenaltyCircleNegCentre().subtractNew(new Vector2(0, tigers.getRadiusOfPenaltyArea())),
				X_AXIS);
				
		assertTrue(tigers.isLineIntersectingShape(tline, 0));
		assertTrue(tigers.isLineIntersectingShape(tline, EPSILON));
		assertFalse(tigers.isLineIntersectingShape(tline, -EPSILON_SQR));
		
		assertTrue(opponents.isLineIntersectingShape(tline, 0));
		assertTrue(opponents.isLineIntersectingShape(tline, EPSILON));
		assertFalse(opponents.isLineIntersectingShape(tline, -EPSILON_SQR));
		
		
	}
	
	
	/**
	 * Test taken from old penalty area to check compliance
	 */
	@Test
	public void testNearestPointOutsideWithLineOld()
	{
		
		// inside our Area
		final IVector2 testPoint1 = tigers.getPenaltyCirclePosCentre().addNew(
				new Vector2(tigers.getPenaltyCirclePos().radius() / 2,
						tigers.getPenaltyCirclePos().radius() / 3.0));
		final IVector2 testPointReturn1 = tigers.getPenaltyCirclePos().nearestPointOutside(testPoint1);
		final IVector2 testPoint2 = tigers.getPenaltyCircleNegCentre().addNew(
				new Vector2(tigers.getPenaltyCirclePos().radius() / 3,
						-tigers.getPenaltyCirclePos().radius() / 4.0));
		final IVector2 testPointReturn2 = tigers.getPenaltyCircleNeg().nearestPointOutside(testPoint2);
		final IVector2 testPoint3 = tigers.getGoalCenter().addNew(
				new Vector2(tigers.getRadiusOfPenaltyArea() / 3, tigers.getRadiusOfPenaltyArea() / 4.0));
		final IVector2 testPointReturn3 = new Vector2(
				tigers.getPenaltyCirclePosCentre().x() + tigers.getRadiusOfPenaltyArea(), testPoint3.y()
						- (tigers.getRadiusOfPenaltyArea() / 4.0));
						
		// inside their Area
		final IVector2 testPoint4 = opponents.getPenaltyCirclePosCentre().addNew(
				new Vector2(-opponents.getPenaltyCirclePos().radius() / 2,
						opponents.getPenaltyCirclePos().radius() / 3.0));
		final IVector2 testPointReturn4 = opponents.getPenaltyCirclePos().nearestPointOutside(testPoint4);
		final IVector2 testPoint5 = opponents.getPenaltyCircleNegCentre().addNew(
				new Vector2(-opponents.getPenaltyCirclePos().radius() / 3, -opponents.getPenaltyCirclePos()
						.radius() / 4.0));
		final IVector2 testPointReturn5 = opponents.getPenaltyCircleNeg().nearestPointOutside(testPoint5);
		final IVector2 testPoint6 = opponents.getGoalCenter().addNew(
				new Vector2(-tigers.getRadiusOfPenaltyArea() / 3, tigers.getRadiusOfPenaltyArea() / 4.0));
		final IVector2 testPointReturn6 = new Vector2(
				opponents.getPenaltyCirclePosCentre().x() - tigers.getRadiusOfPenaltyArea(), testPoint6.y()
						- (tigers.getRadiusOfPenaltyArea() / 4.0));
						
		// outside of both Areas
		final IVector2 testPoint7 = new Vector2();
		final IVector2 testPoint8 = new Vector2(
				opponents.getPenaltyCirclePosCentre().x() - tigers.getRadiusOfPenaltyArea(), testPoint6.y());
				
		assertTrue(tigers.nearestPointOutside(testPoint1, testPointReturn1, 0).equals(testPointReturn1, EPSILON));
		assertTrue(tigers.nearestPointOutside(testPoint2, testPointReturn2, 0).equals(testPointReturn2, EPSILON));
		assertTrue(tigers.nearestPointOutside(testPoint3, testPointReturn3, 0).equals(testPointReturn3, EPSILON));
		
		assertTrue(
				opponents.nearestPointOutside(testPoint4, testPointReturn4, 0).equals(testPointReturn4, EPSILON));
		assertTrue(
				opponents.nearestPointOutside(testPoint5, testPointReturn5, 0).equals(testPointReturn5, EPSILON));
		assertTrue(
				opponents.nearestPointOutside(testPoint6, testPointReturn6, 0).equals(testPointReturn6, EPSILON));
				
		assertTrue(opponents.nearestPointOutside(testPoint7, testPoint8, 0).equals(testPoint7, EPSILON));
		assertTrue(opponents.nearestPointOutside(testPoint8, testPoint7, 0).equals(testPoint8, EPSILON));
	}
	
	
	/**
	 * additional test for boundary condition
	 * ->102002 border cases... for each side
	 */
	@Test
	public void testLineToCircleBoundary()
	{
		for (PenaltyArea area : new PenaltyArea[] { tigers, opponents })
		{
			
			// positive
			final IVector2 p2blPos = area.getCirclePointUpperPos();
			for (double margin = 0; margin < 1000; margin += margin < 500 ? 0.5 : 0.01)
			{
				try
				{
					final IVector2 pointLeft = area.getPenaltyCirclePosCentre()
							.subtractNew(new Vector2(0, 1 / (margin + 1)));
					area.nearestPointOutside(pointLeft, p2blPos, margin);
					area.nearestPointOutside(pointLeft, p2blPos, 1 / (margin + 1));
					final IVector2 pointRight = area.getPenaltyCirclePosCentre().addNew(new Vector2(0, 1 / (margin + 1)));
					area.nearestPointOutside(pointRight, p2blPos, margin);
					area.nearestPointOutside(pointRight, p2blPos, 1 / (margin + 1));
				} catch (RuntimeException ex)
				{
					assertTrue("the border condition falied", false);
				}
			}
			
			final IVector2 p2bl = area.getCirclePointUpperNeg();
			for (double margin = 0; margin < 1000; margin += margin < 500 ? 0.5 : 0.01)
			{
				try
				{
					final IVector2 pointLeft = area.getPenaltyCircleNegCentre()
							.subtractNew(new Vector2(0, 1 / (margin + 1)));
					area.nearestPointOutside(pointLeft, p2bl, margin);
					area.nearestPointOutside(pointLeft, p2bl, 1 / (margin + 1));
					
					final IVector2 pointRight = area.getPenaltyCircleNegCentre().addNew(new Vector2(0, 1 / (margin + 1)));
					area.nearestPointOutside(pointRight, p2bl, margin);
					area.nearestPointOutside(pointRight, p2bl, 1 / (margin + 1));
				} catch (RuntimeException ex)
				{
					assertTrue("the border condition falied", false);
				}
			}
		}
	}
	
	
}
