/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.geometry;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static edu.tigers.sumatra.math.vector.AVector2.X_AXIS;
import static edu.tigers.sumatra.math.vector.AVector2.Y_AXIS;
import static edu.tigers.sumatra.math.vector.AVector2.ZERO_VECTOR;
import static java.lang.Math.signum;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Test class for {@link PenaltyArea}
 *
 * @author Frieder Berthold
 */
@RunWith(Parameterized.class)
public class PenaltyAreaTest {
    private final static double EPSILON = 1e-6;
    private final static double EPSILON_SQR = EPSILON * EPSILON;
    private final IPenaltyArea penaltyArea;


    public PenaltyAreaTest(IPenaltyArea penaltyArea) {
        this.penaltyArea = penaltyArea;
    }


    @Parameterized.Parameters()
    public static Collection primeNumbers() {
        // noinspection RedundantArrayCreation
        return Arrays.asList(new Object[][]{
                {Geometry.getPenaltyAreaOur()}
                // ,
                // { Geometry.getPenaltyAreaTheir() }
        });
    }


    /**
     * checks if all points of step along are within the outer rectangle
     */
    @Test
    public void testStepAlongPenArea() {
        double penAreaRadius = penaltyArea.getRadius();
        double perimeterQuart = (penAreaRadius * AngleMath.PI) / 2.0;
        double frontLineLen = penaltyArea.getFrontLineHalfLength() * 2;
        double frontLineX = penaltyArea.getFrontLine().getStart().x();
        IVector2 pFrontLinePos = Vector2.fromXY(frontLineX, penaltyArea.getFrontLineHalfLength());
        IVector2 pFrontLineNeg = Vector2.fromXY(frontLineX, -penaltyArea.getFrontLineHalfLength());
        IVector2 pLastPoint = Vector2.fromXY(penaltyArea.getGoalCenter().x(),
                -(penaltyArea.getFrontLineHalfLength() + penAreaRadius));

        IVector2 point;
        point = this.penaltyArea.stepAlongPenArea(perimeterQuart);
        assertTrue("Exp:" + pFrontLinePos + " but:" + point, pFrontLinePos.isCloseTo(point, EPSILON));
        point = this.penaltyArea.stepAlongPenArea(perimeterQuart + frontLineLen);
        assertTrue("Exp:" + pFrontLineNeg + " but:" + point, pFrontLineNeg.isCloseTo(point, EPSILON));
        point = this.penaltyArea.stepAlongPenArea((perimeterQuart * 2) + frontLineLen);
        assertTrue("Exp:" + pLastPoint + " but:" + point, pLastPoint.isCloseTo(point, EPSILON));
    }

    @Test
    public void testLineOfLengthToPointOnPenArea() {
        Random random = new Random();
        double randomValue, result;
        randomValue = (penaltyArea.getLength()) * random.nextDouble();
        result = penaltyArea.lengthToPointOnPenArea(penaltyArea.stepAlongPenArea(randomValue));
        assertTrue(randomValue - result < EPSILON);
        randomValue = (penaltyArea.getLength()) * random.nextDouble();
        result = penaltyArea.lengthToPointOnPenArea(penaltyArea.stepAlongPenArea(randomValue));
        assertTrue(randomValue - result < EPSILON);
        randomValue = (penaltyArea.getLength()) * random.nextDouble();
        result = penaltyArea.lengthToPointOnPenArea(penaltyArea.stepAlongPenArea(randomValue));
        assertTrue(randomValue - result < EPSILON);
    }

    /*
     * tests iteratively if a point with random x and y value is close to penalty area line after being projected
     */
    @Test
    public void testProjectPointOnPenArea() {
        final int NUM_ITERATIONS = 10;
        final double rangeMin = -10000.0;
        final double rangeMax = 10000.0;
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            Random random = new Random();
            final double randomValueX = rangeMin + (rangeMax - rangeMin) * random.nextDouble();
            final double randomValueY = rangeMin + (rangeMax - rangeMin) * random.nextDouble();
            final IVector2 projectedPoint = penaltyArea.
                    projectPointOnPenaltyAreaLine(Vector2.fromXY(randomValueX, randomValueY));
            final boolean condition0 = penaltyArea.getFrontLine().distanceTo(projectedPoint) < EPSILON;
            final boolean condition1 = penaltyArea.getArcNeg().center().distanceTo(projectedPoint)
                    - penaltyArea.getRadius() < EPSILON;
            final boolean condition2 = penaltyArea.getArcPos().center().distanceTo(projectedPoint)
                    - penaltyArea.getRadius() < EPSILON;
            assertTrue(condition0 || condition1 || condition2);
        }

    }


    /**
     * nearest Point outside of area without line from old test
     * Using the implementation to generate nearest points outside to compare is
     * not the best testing behaviour therefore a additional test exists for this method
     */
    @Test
    public void testNearestPointOutside() {
        double step = 100;

        double penAreaRadius = penaltyArea.getRadius();

        IVector2 outerPositive = Vector2.fromXY(penaltyArea.getGoalCenter().x() + EPSILON,
                penAreaRadius + penaltyArea.getFrontLineHalfLength() - step);
        IVector2 outerPositiveReturn = Vector2.fromXY(penaltyArea.getGoalCenter().x() + EPSILON,
                penAreaRadius + penaltyArea.getFrontLineHalfLength());
        assertThat(penaltyArea.nearestPointOutside(outerPositive)).isEqualTo(outerPositiveReturn);

        IVector2 outerNegative = Vector2.fromXY(penaltyArea.getGoalCenter().x() + EPSILON,
                -(penAreaRadius + penaltyArea.getFrontLineHalfLength() - step));
        IVector2 outerNegativeReturn = Vector2.fromXY(penaltyArea.getGoalCenter().x() + EPSILON,
                -(penAreaRadius + penaltyArea.getFrontLineHalfLength()));
        assertThat(penaltyArea.nearestPointOutside(outerNegative)).isEqualTo(outerNegativeReturn);

        IVector2 circlePositive = penaltyArea.getArcPos().center().addNew(
                Vector2.fromXY(penAreaRadius / 2, penAreaRadius / 3));
        IVector2 circlePositiveReturn = penaltyArea.getArcPos()
                .nearestPointOutside(circlePositive);
        assertThat(penaltyArea.nearestPointOutside(circlePositive)).isEqualTo(circlePositiveReturn);

        IVector2 circleNegative = penaltyArea.getArcNeg().center().addNew(
                Vector2.fromXY(penAreaRadius / 3, -penAreaRadius / 4));
        IVector2 circleNegativeReturn = penaltyArea.getArcNeg()
                .nearestPointOutside(circleNegative);
        assertThat(penaltyArea.nearestPointOutside(circleNegative)).isEqualTo(circleNegativeReturn);

        IVector2 nearGoalCenter = penaltyArea.getGoalCenter().addNew(
                Vector2.fromXY(penAreaRadius / 2, 0));
        IVector2 centerReturn = Vector2.fromXY(penaltyArea.getGoalCenter().x() + penAreaRadius, nearGoalCenter.y());
        assertThat(penaltyArea.nearestPointOutside(nearGoalCenter)).isEqualTo(centerReturn);

        IVector2 fieldCenter = Vector2.zero();
        assertThat(penaltyArea.nearestPointOutside(fieldCenter)).isEqualTo(fieldCenter);

        IVector2 outsideCirclePositive = penaltyArea.getArcPos().center().addNew(
                Vector2.fromXY(1, 1).scaleTo(penAreaRadius + step));
        assertThat(penaltyArea.nearestPointOutside(outsideCirclePositive)).isEqualTo(outsideCirclePositive);

        IVector2 outsideCircleNegative = penaltyArea.getArcNeg().center().addNew(
                Vector2.fromXY(1, -1).scaleTo(penAreaRadius + step));
        assertThat(penaltyArea.nearestPointOutside(outsideCircleNegative)).isEqualTo(outsideCircleNegative);
    }


    /**
     * check if point in shape is determined correctly on the border
     */
    @Test
    public void testIsPointInShapeAroundBorder() {
        for (double curveLocation = 1; curveLocation < penaltyArea
                .getPerimeterFrontCurve() - 1; curveLocation += 1) {
            assertThat(penaltyArea.withMargin(EPSILON)
                    .isPointInShape(penaltyArea.stepAlongPenArea(curveLocation)))
                    .withFailMessage("Failed for %.0f", curveLocation)
                    .isTrue();
            assertThat(penaltyArea.withMargin(-EPSILON)
                    .isPointInShape(penaltyArea.stepAlongPenArea(curveLocation)))
                    .withFailMessage("Failed for %.0f", curveLocation)
                    .isFalse();
        }
        double penAreaWidthHalf = penaltyArea.getRadius()
                + penaltyArea.getFrontLineHalfLength();
        for (double backLocation = -penAreaWidthHalf + EPSILON; backLocation < penAreaWidthHalf
                - EPSILON; backLocation += 1) {
            double x = penaltyArea.getGoalCenter().x();
            IVector2 pointInside = Vector2.fromXY(x - EPSILON * signum(x), backLocation);
            IVector2 pointOutside = Vector2.fromXY(x + EPSILON * signum(x), backLocation);
            assertThat(penaltyArea.isPointInShape(pointInside))
                    .withFailMessage("Failed for %.0f", backLocation)
                    .isTrue();
            assertThat(penaltyArea.isPointInShape(pointOutside))
                    .withFailMessage("Failed for %.0f", backLocation)
                    .isFalse();
        }
    }


    /**
     * check some intersections with sample lines with many possible states
     */
    @Test
    public void testIntersection() {
        final double radius = penaltyArea.getRadius();

		/* check line right through the area **/
        ILine tline = Line.fromDirection(Vector2f.fromXY(penaltyArea.getGoalCenter().x() + 500, -500), Y_AXIS);
        List<IVector2> r = penaltyArea.lineIntersections(tline);
        assertTrue(r.size() == 2);
        assertThat(penaltyArea.getArcNeg().center().nearestTo(r).distanceTo(penaltyArea.getArcNeg().center()))
                .isLessThan(radius);
        assertThat(penaltyArea.getArcPos().center().nearestTo(r).distanceTo(penaltyArea.getArcPos().center()))
                .isLessThan(radius);
        assertThat(r.get(0).x()).isCloseTo(penaltyArea.getGoalCenter().x() + 500, within(EPSILON));
        assertThat(r.get(1).x()).isCloseTo(penaltyArea.getGoalCenter().x() + 500, within(EPSILON));

		/* check a negative test */
        tline = Line.fromDirection(ZERO_VECTOR, Y_AXIS);
        assertFalse(penaltyArea.isIntersectingWithLine(tline));

		/* check the front-line cut through the penalty mark */
        tline = Line.fromDirection(ZERO_VECTOR, X_AXIS);

        r = penaltyArea.lineIntersections(tline);
        assertTrue(r.size() == 1);
        assertTrue(r.get(0).isCloseTo(Vector2.fromXY(penaltyArea.getFrontLine().getStart().x(), 0.0), EPSILON_SQR));

		/* check if the front-line intersection is not detected with the front-line itself **/
        assertFalse(penaltyArea.isIntersectingWithLine(
                Line.fromPoints(penaltyArea.getFrontLine().getStart(), penaltyArea.getFrontLine().getEnd())));

		/*
         * check if the line a bit right from the circle centre intersects the area only once (and not on the circle part
		 * outside the area
		 **/
        tline = Line.fromDirection(Vector2.fromXY(0, penaltyArea.getArcPos().center().y() + 10), X_AXIS);
        r = penaltyArea.lineIntersections(tline);
        assertThat(r).hasSize(1);

		/* same for the other circle **/
        tline = Line.fromDirection(Vector2.fromXY(0, penaltyArea.getArcNeg().center().y() - 10), X_AXIS);
        r = penaltyArea.lineIntersections(tline);
        assertThat(r).hasSize(1);

		/* check if the intersection with line through the circle centre is only detected once (edge case) **/
        tline = Line.fromDirection(penaltyArea.getArcPos().center(), X_AXIS);
        r = penaltyArea.lineIntersections(tline);
        assertThat(r).hasSize(1);

        tline = Line.fromDirection(penaltyArea.getArcNeg().center(), X_AXIS);
        r = penaltyArea.lineIntersections(tline);
        assertThat(r).hasSize(1);

		/* check if the line on the corners of the circle is detected with security margin **/
        tline = Line.fromDirection(
                penaltyArea.getArcPos().center()
                        .addNew(Vector2.fromXY(0, penaltyArea.getRadius())),
                X_AXIS);
        assertFalse(penaltyArea.isIntersectingWithLine(tline));

        tline = Line.fromDirection(
                penaltyArea.getArcNeg().center()
                        .subtractNew(Vector2.fromXY(0, penaltyArea.getRadius())),
                X_AXIS);

        assertFalse(penaltyArea.isIntersectingWithLine(tline));
    }


    @Test
    public void testLineToCircleBoundary() {
        IVector2 p2blPos = penaltyArea.getFrontLine().getStart();
        IVector2 pointLeft = penaltyArea.getArcPos().center();
        penaltyArea.nearestPointOutside(pointLeft, p2blPos);
        penaltyArea.nearestPointOutside(pointLeft, p2blPos);
        IVector2 pointRight = penaltyArea.getArcPos().center();
        penaltyArea.nearestPointOutside(pointRight, p2blPos);
        penaltyArea.nearestPointOutside(pointRight, p2blPos);
        IVector2 p2bl = penaltyArea.getFrontLine().getEnd();

        IVector2 pointLeft2 = penaltyArea.getArcNeg().center();
        penaltyArea.nearestPointOutside(pointLeft2, p2bl);
        penaltyArea.nearestPointOutside(pointLeft2, p2bl);

        IVector2 pointRight2 = penaltyArea.getArcNeg().center();
        penaltyArea.nearestPointOutside(pointRight2, p2bl);
        penaltyArea.nearestPointOutside(pointRight2, p2bl);
    }


    @Test
    public void testNearestPointOutsideAlwaysInField() {
        for (double x = (-Geometry.getFieldLength() / 2); x < 1000; x += 100) {
            for (double y = -Geometry.getFieldWidth() / 2; y < (Geometry.getFieldWidth() / 2); y += 100) {
                IVector2 point = Vector2.fromXY(x, y);
                IVector2 pointOutside = penaltyArea.nearestPointOutside(point);

                IVector2 pointOutsideWithPointToBuildLineOutside = penaltyArea.nearestPointOutside(
                        point,
                        Vector2.fromXY(-Geometry.getFieldLength() / 2 - 100, signum(y) * 2000));

                assertThat(Geometry.getField().isPointInShape(pointOutside)).isTrue();
                assertThat(Geometry.getField().isPointInShape(pointOutsideWithPointToBuildLineOutside)).isTrue();
            }
        }
    }


    @Test
    public void nearestPointOutsideBehindPA() {
        IVector2 behindPos = penaltyArea.getGoalCenter()
                .addNew(Vector2.fromXY(-EPSILON, penaltyArea.getFrontLineHalfLength() + 1));
        assertThat(penaltyArea.nearestPointOutside(behindPos))
                .isEqualTo(penaltyArea.getArcPos().center().addNew(Vector2.fromY(penaltyArea.getRadius())));
        IVector2 behindNeg = penaltyArea.getGoalCenter()
                .addNew(Vector2.fromXY(-EPSILON, -penaltyArea.getFrontLineHalfLength() - 1));
        assertThat(penaltyArea.nearestPointOutside(behindNeg))
                .isEqualTo(penaltyArea.getArcNeg().center().addNew(Vector2.fromY(-penaltyArea.getRadius())));
        IVector2 behindCenter = penaltyArea.getGoalCenter()
                .addNew(Vector2.fromXY(-EPSILON, 0));
        assertThat(penaltyArea.nearestPointOutside(behindCenter))
                .isEqualTo(penaltyArea.getGoalCenter().addNew(Vector2.fromX(penaltyArea.getRadius())));
    }


    @Test
    public void nearestPointInside() {
        assertThat(penaltyArea.nearestPointInside(Vector2.fromX(-1000)))
                .isEqualTo(Vector2.fromX(penaltyArea.getFrontLine().getStart().x()));

        IVector2 innerPos = penaltyArea.getArcPos().center().addNew(Vector2.fromXY(100, 100));
        assertThat(penaltyArea.nearestPointInside(innerPos)).isEqualTo(innerPos);

        IVector2 innerNeg = penaltyArea.getArcNeg().center().addNew(Vector2.fromXY(100, -100));
        assertThat(penaltyArea.nearestPointInside(innerNeg)).isEqualTo(innerNeg);
    }

}
