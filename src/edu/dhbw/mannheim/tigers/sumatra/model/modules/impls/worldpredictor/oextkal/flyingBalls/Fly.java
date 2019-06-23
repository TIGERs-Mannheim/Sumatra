/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 17, 2011
 * Author(s): Birgit
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.Coord;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.Def.Cam;


/**
 * One possible Fly
 * @author Birgit
 * 
 */
public class Fly
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log					= Logger.getLogger(Fly.class.getName());
	
	/** */
	private List<FlyingBall>		balls					= new ArrayList<FlyingBall>();
	private Coord						kickPos				= new Coord(Def.DUMMY, Def.DUMMY);
	private LinFunc					roboViewFunction	= null;
	private RegParab					flyParabel			= null;
	private double						lastDistance		= 0;
	private int							backCount			= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * create a new fly
	 * @param aRoboPos
	 * @param aViewAngle
	 */
	public Fly(final Coord aRoboPos, final double aViewAngle)
	{
		// function through roboter and angle
		roboViewFunction = new LinFunc(aRoboPos, aViewAngle);
		// set the kickingPosition
		kickPos = roboViewFunction.goDistanceFromPoint(aRoboPos, Def.BOT_RADIUS);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * add a new ball and control, weather it has an orientation between given borders
	 * @param aBallPos
	 * @param camID
	 * @return
	 */
	public boolean addNewBall(final Coord aBallPos, final int camID)
	{
		// Create new ballInstance
		final FlyingBall ball = new FlyingBall(aBallPos);
		
		// set the line through the ball and the cam
		LinFunc ballOrientation;
		
		Cam cam = Def.cams.get(camID);
		if (Def.DEBUG_CAM)
		{
			log.info("Kamera: " + cam.id + "..." + cam.x + ":" + cam.y);
		}
		ballOrientation = new LinFunc(new Coord(cam.x, cam.y), aBallPos);
		
		// look for cut with robot-line, which is flying position
		final Coord flyingPosition = LinFunc.getCutCoords(roboViewFunction, ballOrientation);
		ball.setFlyPositionAndCalculateFlyingHeight(flyingPosition, camID);
		ball.calculateDistanceToStart(kickPos);
		
		// add ball to the list
		balls.add(ball);
		
		return doesBallFit();
	}
	
	
	/*
	 * check, weather the vector between current and before ball is between borders,
	 * given in definitions
	 */
	private boolean doesBallFit()
	{
		boolean fit = true;
		
		final Coord ball = balls.get(balls.size() - 1).getBottomPosition();
		final double distStartBallX = ball.x() - kickPos.x();
		final double distStartBallY = ball.y() - kickPos.y();
		final Coord distanceStartBall = new Coord(distStartBallX, distStartBallY);
		final double newDis = distanceStartBall.getLength();
		final double angle = roboViewFunction.getAngleToVector(distanceStartBall);
		
		
		// check for longest distance
		if (flyParabel != null)
		{
			final double maxDis = -flyParabel.getD() * 2;
			if (Def.DEBUG_FLY_HEIGHT)
			{
				log.info("H�chste Distanz: " + maxDis + " und wir haben " + newDis);
			}
			if (balls.size() == 1)
			{
				lastDistance = newDis;
				if (Def.DEBUG_FLY_HEIGHT)
				{
					log.info("Erster Ball");
				}
			}
			
			if (newDis < lastDistance)
			{
				backCount++;
				if (Def.DEBUG_FLY_HEIGHT)
				{
					log.info("Fehler erh�ht");
				}
			}
			
			if ((newDis > maxDis) || (backCount >= Def.MAX_NUMBER_BALLS_GO_BACK))
			{
				if (Def.DEBUG_FLY_HEIGHT)
				{
					log.info("STOP");
				}
				fit = false;
			}
			
			lastDistance = newDis;
		}
		
		
		if (Def.DEBUG)
		{
			log.debug(" Winkel : " + angle);
		}
		
		// angle fits
		if (angle < Def.RUN_BOT2BALL_MAX_ANGLE)
		{
			// -- check for long enough distance was passed
			
			// calculate lastPos
			Coord lastBall;
			// if we have the first ball, get the position from start
			if (1 == balls.size())
			{
				lastBall = kickPos;
			}
			// if we have many balls, get the previous ball-position
			else
			{
				lastBall = balls.get(balls.size() - 2).getBottomPosition();
			}
			final double distBallBallX = ball.x() - lastBall.x();
			final double distBallBallY = ball.y() - lastBall.y();
			
			final Coord ball2ballDistance = new Coord(distBallBallX, distBallBallY);
			
			if (ball2ballDistance.getLength() > Def.BALL2BALL_MIN_DISTANCE)
			{
				if (Def.DEBUG)
				{
					log.info(" go ");
				}
			} else
			{
				if (Def.DEBUG)
				{
					log.info(" STOP BY DISTANCE ");
				}
				fit = false;
			}
			
			
		}
		// angle fits not
		else
		{
			if (Def.DEBUG)
			{
				log.info(" stop!!!");
			}
			fit = false;
		}
		return fit;
	}
	
	
	/**
	 * calculate the regressionParabel
	 */
	public void calculateFly()
	{
		
		// if no parabel exists
		if (flyParabel == null)
		{
			final int number = balls.size();
			final Coord[] flyingBalls = new Coord[number];
			
			// getAllData
			for (int i = 0; i < number; i++)
			{
				final FlyingBall ball = balls.get(i);
				flyingBalls[i] = new Coord(ball.getDistance(), ball.getFlyingHeight());
			}
			flyParabel = new RegParab(flyingBalls);
		} else
		{
			final int last = balls.size() - 1;
			flyParabel.appendData(new Coord(balls.get(last).getDistance(), balls.get(last).getFlyingHeight()));
		}
	}
	
	
	/**
	 * return, weather minimum high at least is reached
	 * @return
	 */
	public boolean isAtLeastMinHighReached()
	{
		
		if (Def.DEBUG)
		{
			log.info("Erwartete H�he: " + flyParabel.getE() + " bei " + (-flyParabel.getD()));
		}
		if ((flyParabel.getE() > Def.MIN_FLY_HEIGHT) && ((-flyParabel.getD()) > Def.MIN_FLY_LENGTH))
		{
			if (Def.DEBUG)
			{
				log.info("weiter");
			}
			return true;
		}
		if (Def.DEBUG)
		{
			log.info("beendet wegen Parameter");
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
	public Coord getStartPos()
	{
		return kickPos;
	}
	
	
	/**
	 * @return
	 */
	public RegParab getParabel()
	{
		return flyParabel;
	}
	
	
	/**
	 * @return
	 */
	public Coord getCurrentBallPosition()
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
