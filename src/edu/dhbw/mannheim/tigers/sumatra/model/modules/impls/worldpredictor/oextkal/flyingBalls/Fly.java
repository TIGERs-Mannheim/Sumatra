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

import edu.dhbw.mannheim.tigers.sumatra.model.data.Coord;


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
	public  ArrayList<FlyingBall> m_balls	 				= new ArrayList<FlyingBall>();
	private Coord 						m_kickPos 				= new Coord(Def.DUMMY, Def.DUMMY);
	private LinFunc 					m_roboViewFunction	= null;
	private RegParab 					m_flyParabel			= null;
	private double						m_lastDistance			= 0;
	private int 						m_backCount 			= 0;

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/*
	 * create a new fly
	 */
	public Fly (final Coord a_roboPos, final double a_viewAngle)
	{
		//function through roboter and angle
		m_roboViewFunction = new LinFunc(a_roboPos, a_viewAngle);	
		//set the kickingPosition
		m_kickPos = m_roboViewFunction.goDistanceFromPoint(a_roboPos, Def.BOT_RADIUS);	
	}

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/*
	 * add a new ball and control, weather it has an orientation between given borders
	 */
	public boolean addNewBall(final Coord a_ballPos, final int camID)
	{
		//Create new ballInstance
		FlyingBall ball = new FlyingBall(a_ballPos);
		
		//set the line through the ball and the cam
		LinFunc ballOrientation;
		
		//System.out.println("camId: "+camID);
		
		if(camID == Def.CamIDOne)
		{
			if(Def.debugCam)
			System.out.println("Kamera: "+Def.CamIDOne+"..."+Def.CamOneX+":"+Def.CamOneY);
			
			ballOrientation = new LinFunc(
				new Coord(Def.CamOneX, Def.CamOneY), 
				a_ballPos);
		}
		else if(camID == Def.CamIDNull)
		{
			if(Def.debugCam)
			System.out.println("Kamera: "+Def.CamIDNull+"..."+Def.CamNullX+":"+Def.CamNullY);
			
			ballOrientation = new LinFunc(
					new Coord(Def.CamNullX, Def.CamNullY), 
					a_ballPos);
		}
		else
		{
			throw new IllegalArgumentException("Fly: Altigraph is not able, to detect the cam for the ball");
		}
				
		
		//look for cut with robot-line, which is flying position
		Coord flyingPosition = LinFunc.getCutCoords(m_roboViewFunction, ballOrientation);
		ball.setFlyPositionAndCalculateFlyingHeight(flyingPosition, camID);
		ball.calculateDistanceToStart(m_kickPos);
		
		//add ball to the list
		m_balls.add(ball);		
		
		return doesBallFit();
	}
	
	/*
	 * check, weather the vector between current and before ball is between borders,
	 * given in definitions
	 */
	private boolean doesBallFit()
	{
		boolean fit = true;
		
		Coord ball = m_balls.get(m_balls.size()-1).getBottomPosition();
		double dist_start_ball_x = ball.x() - m_kickPos.x();
		double dist_start_ball_y = ball.y() - m_kickPos.y();
		Coord distanceStartBall  = new Coord(dist_start_ball_x,dist_start_ball_y);
		double newDis = distanceStartBall.getLength();
		double angle = m_roboViewFunction.getAngleToVector(distanceStartBall);
		
		
		//check for longest distance
		if(m_flyParabel != null)
		{
			double maxDis = -m_flyParabel.getD()*2;
			if(Def.debugFlyHeight)
			System.out.println("Höchste Distanz: "+maxDis+" und wir haben "+newDis);
		
			if(m_balls.size() == 1)
			{
				m_lastDistance = newDis;
				if(Def.debugFlyHeight)
				System.out.println("Erster Ball");
			}
			
			if(newDis < m_lastDistance)
			{
				m_backCount++;
				if(Def.debugFlyHeight)
				System.out.println("Fehler erhöht");
			}
			
			if(newDis > maxDis || m_backCount >= Def.MAX_NUMBER_BALLS_GO_BACK)
			{
				if(Def.debugFlyHeight)
				System.out.println("STOP");
				fit = false;
			}
			
			m_lastDistance = newDis;
		}


		
		
		if(Def.debug)
		System.out.print(" Winkel : "+angle);
		
		//angle fits
		//if (	Def.MIN_BALL2BALL_ANGLE_BORDER < angle && 
		//		Def.MAX_BALL2BALL_ANGLE_BORDER > angle)
		if(angle < Def.RUN_BOT2BALL_MAX_ANGLE)
		{
			//-- check for long enough distance was passed
			
			//calculate lastPos
			Coord lastBall;
			//if we have the first ball, get the position from start
			if(1 == m_balls.size())
			{
				lastBall = m_kickPos;
			}
			//if we have many balls, get the previous ball-position
			else
			{
				lastBall = m_balls.get(m_balls.size()-2).getBottomPosition();
			}
			double dist_ball_ball_x = ball.x() - lastBall.x();
			double dist_ball_ball_y = ball.y() - lastBall.y();
			
			Coord ball2ballDistance = new Coord(dist_ball_ball_x,dist_ball_ball_y);
			
			if(ball2ballDistance.getLength() > Def.BALL2BALL_MIN_DISTANCE)
			{
				if(Def.debug)
					System.out.println(" go ");
			}
			else
			{
				if(Def.debug)
					System.out.println(" STOP BY DISTANCE ");
				fit = false;
			}
				
				
		}
		//angle fits not
	/*	else
		{
			
			//if error is small enough, allow the way, because it is maybe noisy			
			if(((double) m_balls.size())/((double) m_faultCounter) > 2)
			{
				if(Def.debug)
				System.out.print("Length: "+vector.getLength());
				//control inverted angle
				double angleInvert = Math.PI - angle;
				if (	Def.MIN_BALL2BALL_ANGLE_BORDER < angleInvert && 
						Def.MAX_BALL2BALL_ANGLE_BORDER > angleInvert &&
						vector.getLength() < Def.BALL2BALL_MAX_DISTANCE)
				{
					if(Def.debug)
					System.out.println(" go Fehler++");
					m_faultCounter += 1;
				}
				else
				{
					if(Def.debug)
					System.out.println(" stop!!!");
					fit = false;
				}
				
			}*/
			else
			{
				if(Def.debug)
				System.out.println(" stop!!!");
				fit = false;
			}
		return fit;
	}
	
	
	/*
	 * calculate the regressionParabel
	 */
	public void calculateFly()
	{
	
		//if no parabel exists
		if(null == m_flyParabel)
		{
			//System.out.println("new Parabel");
			int number = m_balls.size();
			Coord[] FlyingBalls = new Coord[number];
			//double[] width      = new double[number];
			//double[] height     = new double[number];
			//double[] time       = new double[number];
			
			//getAllData
			for(int i = 0; i < number; i++)
			{
				FlyingBall ball = m_balls.get(i);
				FlyingBalls[i] = new Coord(ball.getDistance(), ball.getFlyingHeight());
				
				//width[i]  = ball.getDistance();
				//height[i] = ball.getFlyingHeight();
				//time[i]   = ball.getTimestamp();
			}
			m_flyParabel = new RegParab(FlyingBalls);
			//m_trajec = new Trajectory(width, height, time, m_flyParabel.getAlpha());
		}
		else
		{
			//System.out.println("Data append");
			int last = m_balls.size()-1;
			m_flyParabel.appendData(new Coord(m_balls.get(last).getDistance(), 
					m_balls.get(last).getFlyingHeight()));
		}
	}
	
	/*
	 * return, weather minimum high at least is reached
	 */
	public boolean isAtLeastMinHighReached()
	{

		if(Def.debug)
		System.out.println("Erwartete Höhe: "+m_flyParabel.getE()+" bei "+(-m_flyParabel.getD()));
		if(m_flyParabel.getE() > Def.MIN_FLY_HEIGHT && (-m_flyParabel.getD()) > Def.MIN_FLY_LENGTH)
		{
			if(Def.debug)
			System.out.println("weiter");
			return true;
		}
		else
		{
			if(Def.debug)
			System.out.println("beendet wegen Parameter");
			return false;
		}
	}
	


	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public int size()
	{
		return m_balls.size();
	}
	
	public Coord getStartPos()
	{
		return m_kickPos;
	}
	
	public RegParab getParabel()
	{
		return m_flyParabel;
	}
	
	public Coord getCurrentBallPosition()
	{
		return m_balls.get(m_balls.size()-1).getFlyingPosition();
	}
	
	public double getCurrentBallHeight()
	{
		return m_balls.get(m_balls.size()-1).getFlyingHeight();
	}
	
	public String toString()
	{
		String str = "";
		
		for(int i = 0; i< m_balls.size(); i++)
		{
			str += "Ball "+i+":\n";
			str += m_balls.get(i).toString();
		}
		
		return str;
	}


}
