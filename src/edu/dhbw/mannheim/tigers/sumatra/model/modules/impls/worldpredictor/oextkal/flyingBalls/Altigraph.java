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
	private static final Logger	log				= Logger.getLogger(Altigraph.class.getName());
	/** */
	private List<Fly>					mFlys				= new ArrayList<Fly>();
	private boolean					mIsBallFlying	= false;
	private Coord						mCurrentBall;
	private Fly							mFly;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public Altigraph()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * if ball was in kickerzone, create a new fly
	 * 
	 * @param roboPosX
	 * @param roboPosY
	 * @param angle
	 */
	public void addKickerZoneIdentified(final double roboPosX, final double roboPosY, final double angle)
	{
		final Coord roboPos = new Coord(roboPosX, roboPosY);
		
		// create a new fly and add to fly-list
		final Fly fly = new Fly(roboPos, angle);
		mFlys.add(fly);
		
		// are there to much flys?
		if (mFlys.size() > Def.MAX_NUMBER_FLYS)
		{
			mFlys.remove(1);
		}
	}
	
	
	/**
	 * for every camera frame, add the ball and timestamp
	 * 
	 * @param ballPosX
	 * @param ballPosY
	 * @param camID
	 */
	public void addCamFrame(final double ballPosX, final double ballPosY, final int camID)
	{
		// if we current have no fly: skip
		if (0 == mFlys.size())
		{
			return;
		}
		
		// else append the ball
		mCurrentBall = new Coord(ballPosX, ballPosY);
		
		// delete flys, to which the ball does not fit
		addCameFrameAndDeleteUnusedFlys(mCurrentBall, camID);
		
		// if we current have no fly: skip
		if (0 == mFlys.size())
		{
			mIsBallFlying = false;
			return;
		}
		// calculate the regressionParabel
		dedectAndCalulateFlyingBall();
	}
	
	
	/*
	 * for the first fly, calculate the parabel
	 */
	private void dedectAndCalulateFlyingBall()
	{
		// if in the first fly are more than 4 balls
		// calculate the parabel
		mFly = mFlys.get(0);
		
		if (mFly.size() >= 4)
		{
			mFly.calculateFly();
			
			mIsBallFlying = true;
			
			if (!mFly.isAtLeastMinHighReached())
			{
				mFlys.remove(0);
				mIsBallFlying = false;
			}
			
		}
		// if not enough balls there, reset the fly-state
		else
		{
			mIsBallFlying = false;
		}
	}
	
	
	/*
	 * add the balls and delete impossible flies
	 */
	private void addCameFrameAndDeleteUnusedFlys(final Coord ballPos, final int camID)
	{
		int number = mFlys.size();
		
		// for all flies try to add the ball
		// if it not fit, delete the fly
		for (int i = 0; i < number; i++)
		{
			if (Def.DEBUG)
			{
				log.debug("Flug +" + i);
			}
			if (Def.DEBUG)
			{
				log.debug("\t Ball +" + (mFlys.get(i).size() + 1));
			}
			// add the ball
			final boolean ok = mFlys.get(i).addNewBall(ballPos, camID);
			// if the ball wasn't correct in this fly, remember the fly for delete
			if (!ok)
			{
				mFlys.remove(i);
				i--;
				number--;
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @return
	 */
	public boolean isBallFlying()
	{
		return mIsBallFlying;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public PredBall getCorrectedFrame()
	{
		final PredBall ball = new PredBall();
		
		if (!mIsBallFlying)
		{
			ball.setX(mCurrentBall.x());
			ball.setY(mCurrentBall.y());
			ball.setZ(0);
		} else
		{
			ball.setX(mFly.getCurrentBallPosition().x());
			ball.setY(mFly.getCurrentBallPosition().y());
			ball.setZ(mFly.getCurrentBallHeight());
		}
		return ball;
	}
	
	
	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder();
		str.append("#################################################\n");
		
		for (int i = 0; i < mFlys.size(); i++)
		{
			str.append("###|> Fly ");
			str.append(i);
			str.append(": ##########################\n");
			str.append(mFlys.get(i).toString());
			str.append("Balls: ");
			str.append(mFlys.get(i).size());
			str.append("###Fly ");
			str.append(i);
			str.append(" <| ##########################\n");
		}
		
		str.append("#################################################\n");
		return str.toString();
	}
	
	
	/**
	 * @return the mFlys
	 */
	public List<Fly> getFlys()
	{
		return mFlys;
	}
}
