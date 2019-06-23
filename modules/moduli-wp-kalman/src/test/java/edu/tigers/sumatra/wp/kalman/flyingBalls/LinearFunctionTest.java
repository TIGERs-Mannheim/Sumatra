package edu.tigers.sumatra.wp.kalman.flyingBalls;


/**
 * TODO reenable tests for Linear functions: replace LinFunc with Function1dPol.linear(), ...
 */
public class LinearFunctionTest
{
	// private static final double HUND = 0.01;
	// private static final double eps = 12e-8f;
	
	
	/**
	 */
	// @Test
	// public void testF()
	// {
	// final IVector2 point = new Vector2(1, 3);
	// final IVector2 view = new Vector2(4, -2);
	// // f(x) = -0.5x+3.5;
	//
	// final LinFunc func = new LinFunc(point, view, true);
	//
	// assertEquals(4, func.f(-1), eps);
	// assertEquals(3, func.f(1), eps);
	// assertEquals(2, func.f(3), eps);
	// }
	//
	//
	// /**
	// */
	// @Test
	// public void testFWithAngle()
	// {
	// final IVector2 point = new Vector2(1, 3);
	// final double viewAngle = -0.463;
	// // f(x) = -0.5x+3.5;
	//
	// final LinFunc func = new LinFunc(point, viewAngle);
	//
	// assertEquals(4, func.f(-1), HUND * 0.5);
	// assertEquals(3, func.f(1), HUND * 0.5);
	// assertEquals(2, func.f(3), HUND * 0.5);
	// }
	//
	//
	// /**
	// */
	// @Test
	// public void testGetXfromCut()
	// {
	// final IVector2 f1_Point = new Vector2(1, 3);
	// final IVector2 f1_View = new Vector2(4, -2);
	// // f1(x) = -0.5x+3.5;
	//
	// final IVector2 f2_Point = new Vector2(4, 5);
	// final IVector2 f2_View = new Vector2(-1, -3);
	// // f1(x) = 3x-7;
	//
	// final LinFunc func1 = new LinFunc(f1_Point, f1_View, true);
	// final LinFunc func2 = new LinFunc(f2_Point, f2_View, true);
	//
	// final IVector2 g_CutPoint = LinFunc.getCutCoords(func1, func2);
	// final IVector2 e_CutPoint = new Vector2(3, 2);
	//
	// assertEquals(e_CutPoint.x(), g_CutPoint.x(), eps);
	// assertEquals(e_CutPoint.y(), g_CutPoint.y(), eps);
	//
	// }
	//
	//
	// /**
	// */
	// @Test(expected = IllegalArgumentException.class)
	// public void testCutWithoutCut()
	// {
	// final IVector2 f1_Point = new Vector2(0, 0);
	// // in no point
	// final IVector2 f2_Point = new Vector2(1, 0);
	// final IVector2 f_View = new Vector2(4, -2);
	// // f1(x) = -0.5x+3.5;
	//
	// final LinFunc func1 = new LinFunc(f1_Point, f_View, true);
	// final LinFunc func2 = new LinFunc(f2_Point, f_View, true);
	//
	// LinFunc.getCutCoords(func1, func2);
	// }
	//
	//
	// /**
	// */
	// @Test(expected = IllegalArgumentException.class)
	// public void testCutWithoutInfinityCut()
	// {
	// final IVector2 f1_Point = new Vector2(0, 0);
	// // in all points
	// final IVector2 f3_Point = new Vector2(1, 1);
	// final IVector2 f_View = new Vector2(4, -2);
	// // f1(x) = -0.5x+3.5;
	//
	// final LinFunc func1 = new LinFunc(f1_Point, f_View, true);
	// final LinFunc func3 = new LinFunc(f3_Point, f_View, true);
	//
	// LinFunc.getCutCoords(func1, func3);
	// }
	//
	//
	// /**
	// */
	// @Test
	// public void goDistanceFromPointTestWithViewNormal()
	// {
	//
	// final LinFunc func = new LinFunc(new Vector2(1, 2), new Vector2(3, 4), true);
	// final IVector2 newPos = func.goDistanceFromPoint(new Vector2(1, 2), 5);
	//
	// assertEquals(4, newPos.x(), eps);
	// assertEquals(6, newPos.y(), eps);
	// }
	//
	//
	// /**
	// */
	// @Test
	// public void goDistanceFromPointTestWithIVector2Regular()
	// {
	//
	// final LinFunc func = new LinFunc(new Vector2(1, 2), new Vector2(4, 6));
	// final IVector2 newPos = func.goDistanceFromPoint(new Vector2(1, 2), 5);
	//
	// assertEquals(4, newPos.x(), eps);
	// assertEquals(6, newPos.y(), eps);
	// }
	//
	//
	// /**
	// */
	// @Test
	// public void testCreateWithAngleRegular()
	// {
	// final double alpha = Math.atan(4.0 / 3.0);
	// final LinFunc func = new LinFunc(new Vector2(1, 2), alpha);
	// final IVector2 newPos = func.goDistanceFromPoint(new Vector2(1, 2), 5);
	//
	// assertEquals(4, newPos.x(), eps);
	// assertEquals(6, newPos.y(), eps);
	// }
	//
	//
	// /**
	// */
	// @Test
	// public void testCreateWithAngleUnRegular()
	// {
	// final double alpha = Math.PI - Math.atan(4.0 / 3.0);
	// final LinFunc func = new LinFunc(new Vector2(1, 2), alpha);
	// final IVector2 newPos = func.goDistanceFromPoint(new Vector2(1, 2), 5);
	//
	// assertEquals(-2, newPos.x(), eps);
	// assertEquals(6, newPos.y(), eps);
	// }
	//
	//
	// /**
	// */
	// @Test
	// @Ignore
	// public void goDistanceFromPointTestWithIVector2UnRegular()
	// {
	//
	// final LinFunc func = new LinFunc(new Vector2(4, 6), new Vector2(1, 2));
	// final IVector2 newPos = func.goDistanceFromPoint(new Vector2(1, 2), 5);
	//
	// assertEquals(-2, newPos.x(), eps);
	// // TODO use double epsilon
	// assertEquals(-2, newPos.y(), eps);
	// }
	//
	//
	// /**
	// */
	// @Test
	// public void getAngleFromVector()
	// {
	// final double n = -2;
	// final IVector2 view = new Vector2(1, -1);
	//
	// final LinFunc func = new LinFunc(new Vector2(0, n), view, true);
	//
	// final double quart = Math.PI / 4.0;
	//
	// final IVector2[] vec = new Vector2[8];
	// final double[] e_angle = new double[8];
	//
	// vec[0] = new Vector2(0, 1);
	// e_angle[0] = quart * 3;
	//
	// vec[1] = new Vector2(1, 1);
	// e_angle[1] = quart * 2;
	//
	// vec[2] = new Vector2(1, 0);
	// e_angle[2] = quart;
	//
	// vec[3] = new Vector2(1, -1);
	// e_angle[3] = 0;
	//
	// vec[4] = new Vector2(0, -1);
	// e_angle[4] = quart;
	//
	// vec[5] = new Vector2(-1, -1);
	// e_angle[5] = quart * 2;
	//
	// vec[6] = new Vector2(-1, 0);
	// e_angle[6] = quart * 3;
	//
	// vec[7] = new Vector2(-1, 1);
	// e_angle[7] = quart * 4;
	//
	// for (int i = 0; i < 8; i++)
	// {
	// assertEquals(e_angle[i], func.getAngleToVector(vec[i]), HUND * 0.5);
	// }
	//
	// }
}
