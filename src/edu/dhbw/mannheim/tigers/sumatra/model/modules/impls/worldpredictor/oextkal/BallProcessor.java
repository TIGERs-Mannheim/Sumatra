/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.05.2010
 * Authors:
 * Maren K�nemund <Orphen@fantasymail.de>,
 * Peter Birkenkampf <birkenkampf@web.de>,
 * Marcel Sauer <sauermarcel@yahoo.de>,
 * Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.benchmarking.Precision;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.BallMotionResult;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.PredictionContext;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.UnregisteredBall;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.WPCamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.filter.IFilter;


/**
 * Prepares all new data from the incoming
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamDetectionFrame} concerning the ball(s), and add it
 * to the {@link PredictionContext} if necessary
 */
public class BallProcessor
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private final PredictionContext	context;
	
	/** Instance-variable for performance reasons */
	private int								count;
	
	
	/**
	 * @param context
	 */
	public BallProcessor(PredictionContext context)
	{
		this.context = context;
		count = 0;
	}
	
	
	/**
	 * @param camBalls
	 */
	public void process(List<CamBall> camBalls)
	{
		if (camBalls.isEmpty())
		{
			// Nothing to do here
			return;
		}
		
		// ---perform update and collisiontest for ball
		final WPCamBall visionBall = new WPCamBall(camBalls.get(0));
		final IFilter ballWeKnow = context.ball;
		final double timestamp = context.getLatestCaptureTimestamp();
		// do we know a ball?
		if (ballWeKnow != null)
		{
			// ---For benchmarking purpose
			if (count < 5)
			{
				count++;
			} else
			{
				for (int i = 0; i <= context.stepCount; i++)
				{
					Precision.getInstance().addBall(ballWeKnow.getLookaheadTimestamp(i),
							(BallMotionResult) ballWeKnow.getLookahead(i));
				}
				Precision.getInstance().addCamBall(timestamp, visionBall);
			}
			
			// observe
			ballWeKnow.observation(timestamp, visionBall);
			return;
		}
		
		// ---we have no ball
		UnregisteredBall newBall = context.newBall;
		// do we have a possible ball?
		if (newBall != null)
		{
			newBall.addBall(timestamp, visionBall);
		} else
		// so it's a unknown ball
		{
			newBall = new UnregisteredBall(timestamp, visionBall);
			context.newBall = newBall;
		}
	}
	
	
	/**
	 */
	public void normalizePredictionTime()
	{
		final IFilter ballWeKnow = context.ball;
		// do we know a ball?
		if (ballWeKnow != null)
		{
			final double nowTime = context.getLatestCaptureTimestamp();
			ballWeKnow.updateOffset(nowTime);
		}
	}
	
	
	/**
	 */
	public void performCollisionAwareLookahead()
	{
		final IFilter ballWeKnow = context.ball;
		// do we know a ball?
		if (ballWeKnow != null)
		{
			for (int i = 1; i <= context.stepCount; i++)
			{
				ballWeKnow.performLookahead(i);
				// TODO WP: perform collision control
				// ballWeKnow.collisionControl(i);
			}
		}
	}
}
