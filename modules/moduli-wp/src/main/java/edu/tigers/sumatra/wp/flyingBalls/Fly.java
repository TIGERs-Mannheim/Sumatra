/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 17, 2011
 * Author(s): Birgit
 * *********************************************************
 */
package edu.tigers.sumatra.wp.flyingBalls;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.functions.Function1dPoly;
import edu.tigers.sumatra.functions.IFunction1D;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.MathException;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * One possible Fly
 * 
 * @author Birgit
 */
public class Fly
{
	@SuppressWarnings("unused")
	private static final Logger		log								= Logger.getLogger(Fly.class.getName());
	
	private static final int			MAX_NUMBER_BALLS_GO_BACK	= 4;
	/** mm */
	private static final double		MIN_FLY_HEIGHT					= 50;
	/** mm */
	private static final double		MIN_FLY_LENGTH					= 400;
	/** */
	private static final double		RUN_BOT2BALL_MAX_ANGLE		= Math.PI / 6.0;
	/** mm */
	private static final double		BALL2BALL_MIN_DISTANCE		= 10;
	
	private final List<FlyingBall>	balls								= new ArrayList<FlyingBall>();
	private IVector2						kickPos;
	private final IFunction1D			roboViewFunction;
	private RegressionParabel			flyParabel						= null;
	private double							lastDistance					= 0;
	private int								backCount						= 0;
	
	
	/**
	 * create a new fly
	 * 
	 * @param aRoboPos
	 * @param aViewAngle
	 */
	public Fly(final IVector2 aRoboPos, final double aViewAngle)
	{
		// function through roboter and angle (bx+a = y)
		double a = Math.tan(aViewAngle);
		double b = aRoboPos.y() - (a * aRoboPos.x());
		roboViewFunction = Function1dPoly.linear(a, b);
		// set the kickingPosition
		try
		{
			kickPos = stepAlongFunction1D(roboViewFunction, aRoboPos, Geometry.getCenter2DribblerDistDefault());
		} catch (MathException err)
		{
			kickPos = aRoboPos;
			log.error("RoboViewFunction not linear", err);
		}
	}
	
	
	/**
	 * Steps along a linear Function
	 * 
	 * @param func
	 * @param aPoint
	 * @param distance
	 * @return
	 * @throws MathException
	 */
	public static IVector2 stepAlongFunction1D(final IFunction1D func, final IVector2 aPoint, final double distance)
			throws MathException
	{
		if (func.getParameters().size() != 2)
		{
			throw new MathException("Not a linear Function");
		}
		final double deltaX = Math.cos(Math.atan(func.getParameters().get(1))) * distance;
		double newX = aPoint.x() + deltaX;
		final Vector2 res = new Vector2(newX, func.eval(newX));
		
		return res;
	}
	
	
	/**
	 * add a new ball and control, weather it has an orientation between given borders
	 * 
	 * @param aBallPos
	 * @param camID
	 * @return
	 */
	public boolean addNewBall(final IVector2 aBallPos, final int camID)
	{
		// Create new ballInstance
		final FlyingBall ball = new FlyingBall(aBallPos);
		
		// set the line through the ball and the cam
		IFunction1D ballOrientation;
		
		double prinX = Geometry.getCameraPrincipalPointX()[camID];
		double prinY = Geometry.getCameraPrincipalPointY()[camID];
		double a = ((aBallPos.y() - prinY) / (aBallPos.x() - prinX));
		double b = prinY - (a * prinX);
		ballOrientation = Function1dPoly.linear(a, b);
		
		// look for intersection with robot-line, which is flying position
		IVector2 flyingPosition;
		try
		{
			flyingPosition = intersectionPointLinearFunctions(roboViewFunction, ballOrientation);
		} catch (MathException err)
		{
			flyingPosition = AVector2.ZERO_VECTOR;
			log.error("No flyingPosition found. temporairly using Zero vector", err);
		}
		ball.setFlyPositionAndCalculateFlyingHeight(flyingPosition, camID);
		ball.calculateDistanceToStart(kickPos);
		
		return doesBallFit(ball);
	}
	
	
	/**
	 * Calculates Intersection point between two linear functions
	 * 
	 * @param l1
	 * @param l2
	 * @return
	 * @throws MathException
	 */
	private IVector2 intersectionPointLinearFunctions(final IFunction1D l1, final IFunction1D l2)
			throws MathException
	{
		if ((l1.getParameters().size() != 2) || (l2.getParameters().size() != 2))
		{
			throw new MathException("No Linear Function");
		}
		
		/** m_a*x + n_a = m_b*x + n_b */
		final double x = ((l2.getParameters().get(0) - l1.getParameters().get(0))
				/ (l1.getParameters().get(1) - l2.getParameters().get(1)));
		final double y = l1.eval(x);
		
		// if the cut not or cut in every point, because they are the same, return default
		if (new Double(x).isInfinite() || new Double(y).isInfinite())
		{
			throw new MathException("Linera intersections: The Linear Functions have no, or infinite much cutpoints!");
		}
		final Vector2 res = new Vector2(x, y);
		
		return res;
		
		
	}
	
	
	/*
	 * check, weather the vector between current and before ball is between borders,
	 * given in definitions
	 */
	private boolean doesBallFit(final FlyingBall flyingBall)
	{
		final IVector2 ball = flyingBall.getBottomPosition();
		final IVector2 distanceStartBall = ball.subtractNew(kickPos);
		final double newDis = distanceStartBall.getLength2();
		final double angle = GeoMath.angleBetweenVectorAndVector(
				new Vector2(1.0f, roboViewFunction.getParameters().get(1)), distanceStartBall);
		
		
		// check for longest distance
		if (flyParabel != null)
		{
			final double maxDis = -flyParabel.getD() * 2;
			if (balls.size() == 1)
			{
				lastDistance = newDis;
			}
			
			if (newDis < lastDistance)
			{
				backCount++;
			}
			
			if ((newDis > maxDis) || (backCount >= MAX_NUMBER_BALLS_GO_BACK))
			{
				return false;
			}
			
			lastDistance = newDis;
		}
		
		
		// angle fits
		if (angle < RUN_BOT2BALL_MAX_ANGLE)
		{
			// -- check for long enough distance was passed
			
			// calculate lastPos
			IVector2 lastBall;
			// if we have the first ball, get the position from start
			if (balls.isEmpty())
			{
				lastBall = kickPos;
			}
			// if we have many balls, get the previous ball-position
			else
			{
				lastBall = balls.get(balls.size() - 1).getBottomPosition();
			}
			final double distBallBallX = ball.x() - lastBall.x();
			final double distBallBallY = ball.y() - lastBall.y();
			
			final IVector2 ball2ballDistance = new Vector2(distBallBallX, distBallBallY);
			
			if (ball2ballDistance.getLength2() <= BALL2BALL_MIN_DISTANCE)
			{
				// it probably fits, but this one ball is not useful for us...
				return true;
			}
		}
		// angle fits not
		else
		{
			return false;
		}
		balls.add(flyingBall);
		return true;
	}
	
	
	/**
	 * calculate the regressionParabel
	 */
	public void calculateFly()
	{
		// if no parabel exists
		if (flyParabel == null)
		{
			final List<IVector2> flyingBalls = new ArrayList<IVector2>(balls.size());
			
			// getAllData
			for (FlyingBall ball : balls)
			{
				flyingBalls.add(new Vector2(ball.getDistance(), ball.getFlyingHeight()));
			}
			flyParabel = new RegressionParabel(flyingBalls);
		} else
		{
			final int last = balls.size() - 1;
			flyParabel.appendData(new Vector2(balls.get(last).getDistance(), balls.get(last).getFlyingHeight()));
		}
	}
	
	
	/**
	 * return, weather minimum high at least is reached
	 * 
	 * @return
	 */
	public boolean isAtLeastMinHighReached()
	{
		if ((flyParabel.getE() > MIN_FLY_HEIGHT) && ((-flyParabel.getD()) > MIN_FLY_LENGTH))
		{
			return true;
		}
		return false;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public int size()
	{
		return balls.size();
	}
	
	
	/**
	 * @return
	 */
	public IVector2 getStartPos()
	{
		return kickPos;
	}
	
	
	/**
	 * @return
	 */
	public RegressionParabel getParabel()
	{
		return flyParabel;
	}
	
	
	/**
	 * @return
	 */
	public IVector2 getCurrentBallPosition()
	{
		return balls.get(balls.size() - 1).getFlyingPosition();
	}
	
	
	/**
	 * @return
	 */
	public double getCurrentBallHeight()
	{
		return balls.get(balls.size() - 1).getFlyingHeight();
	}
	
	
	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder();
		
		str.append("Fly: ");
		for (int i = 0; i < balls.size(); i++)
		{
			str.append("Ball " + i + ":\n");
			str.append(balls.get(i).toString());
		}
		
		return str.toString();
	}
	
	
	/**
	 * @return the balls
	 */
	public List<FlyingBall> getBalls()
	{
		return balls;
	}
	
	
}
