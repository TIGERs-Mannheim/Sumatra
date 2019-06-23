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
 * The altigraph manages the flys
 * 
 * @author Birgit
 * 
 */
public class Altigraph
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	public ArrayList<Fly> 	m_flys			  = new ArrayList<Fly>();
	private boolean 			m_isBallFlying   = false;
	private Coord 				m_currentBall;
	private Fly 				m_fly;


	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public Altigraph()
	{
		
	}

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------

	/*
	 * if ball was in kickerzone, create a new fly
	 */
	public void addKickerZoneIdentified(
			final double roboPosX,
			final double roboPosY, 
			final double angle)
	{
		Coord roboPos = new Coord(roboPosX, roboPosY);
		
		//create a new fly and add to fly-list
		//System.out.println("+++++++++++++++++++++++++++++++++++++++++\nNeuer Flug");
		Fly fly = new Fly(roboPos, angle);
		m_flys.add(fly);
		
		//are there to much flys?
		if(m_flys.size() > Def.MAX_NUMBER_FLYS)
		{
			m_flys.remove(1);
		}
	}
	
	/*
	 * for every camera frame, add the ball and timestamp
	 */
	public void addCamFrame(
			final double ballPosX, 
			final double ballPosY,
			final int camID)
	{
		//if we current have no fly: skip
		if(0 == m_flys.size())
		{
			return;
		}
		
		//else append the ball
		m_currentBall = new Coord(ballPosX, ballPosY);
		
		//delete flys, to which the ball does not fit
		addCameFrameAndDeleteUnusedFlys(m_currentBall, camID);
		
		//if we current have no fly: skip
		if(0 == m_flys.size())
		{
			m_isBallFlying = false;
			return;
		}
		//calculate the regressionParabel
		dedectAndCalulateFlyingBall();	
	}
	
	/*
	 * for the first fly, calculate the parabel
	 */
	private void dedectAndCalulateFlyingBall()
	{
		//if in the first fly are more than 4 balls
		//calculate the parabel
		m_fly = m_flys.get(0);
		
		if(m_fly.size() >= 4)
		{
			//System.out.println("Ich habe mind 4 bälle:"+m_fly.getNumberOfBalls());
			m_fly.calculateFly();
			
			m_isBallFlying = true;
			
			if(!m_fly.isAtLeastMinHighReached())
			{
				m_flys.remove(0);
				m_isBallFlying = false;
			}
			
		}
		//if not enough balls there, reset the fly-state
		else
		{
			m_isBallFlying = false;
		}
	}

	/*
	 * add the balls and delete impossible flies
	 */
	private void addCameFrameAndDeleteUnusedFlys(final Coord ballPos, final int camID)
	{
		int number = m_flys.size();
		
		//for all flies try to add the ball
		//if it not fit, delete the fly
		for(int i = 0; i < number; i++)
		{
			if(Def.debug)
			System.out.println("Flug +"+i);
			if(Def.debug)
			System.out.print("\t Ball +"+(m_flys.get(i).size()+1));
			//add the ball
			boolean ok = m_flys.get(i).addNewBall(ballPos, camID);
			//if the ball wasn't correct in this fly, remember the fly for delete
			if(!ok)
			{
				//System.out.println("++++++++++++++++++++++++++++++++++++++\nFly +"+i+": Ball does not fit-->Lösche Flug "+i);
				m_flys.remove(i);
				i--;
				number--;
			}
		}
	}

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public boolean isBallFlying()
	{
		return m_isBallFlying;
	}
	
	
	public PredBall getCorrectedFrame()
	{
		PredBall ball = new PredBall();
		
		if(!m_isBallFlying)
		{
			ball.setX(m_currentBall.x());
			ball.setY(m_currentBall.y());
			ball.setZ(0);
		}
		else
		{
			ball.setX(m_fly.getCurrentBallPosition().x());
			ball.setY(m_fly.getCurrentBallPosition().y());
			ball.setZ(m_fly.getCurrentBallHeight());
		}
		return ball;
	}
	
	
	public String toString()
	{
		String str = "";
		str += "#################################################\n";
		
		for(int i = 0; i < m_flys.size(); i++)
		{
			str += "###|> Fly "+i+": ##########################\n";
			//TODO erste zeile wieder einkommentieren
			//str += m_flys.get(i).toString();
			str += "Bälle: "+m_flys.get(i).size();
			str += "###Fly "+i+" <| ##########################\n";
		}
		
		str += "#################################################\n";
		return str;
	}
}
