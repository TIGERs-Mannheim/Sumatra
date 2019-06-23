/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.05.2010
 * Authors:
 * Maren Künemund <Orphen@fantasymail.de>,
 * Peter Birkenkampf <birkenkampf@web.de>,
 * Marcel Sauer <sauermarcel@yahoo.de>,
 * Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.benchmarking.Precision;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.BallMotionResult;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.PredictionContext;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.UnregisteredBall;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.WPCamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.filter.IFilter;


/**
 * Prepares all new data from the incoming {@link CamDetectionFrame} concerning the ball(s), and add it to the
 * {@link PredictionContext} if necessary
 */
public class BallProcessor
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	

	private final PredictionContext	context;
	
	/** Instance-variable for performance reasons */
	// private final List<Integer>		frameBallIDs	= new ArrayList<Integer>();
	private int								count;
	
	
	public BallProcessor(PredictionContext context)
	{
		this.context = context;
		count = 0;
	}
	

	public void process(List<CamBall> camBalls)
	{
		if (camBalls.isEmpty())
		{
			return; // Nothing to do here
		}
		
		// ---perform update and collisiontest for ball
		WPCamBall visionBall = new WPCamBall(camBalls.get(0));
		IFilter ballWeKnow = context.ball;
		double timestamp = context.getLatestCaptureTimestamp();
		if (ballWeKnow != null) // do we know a ball?
		{
			// ---For benchmarking purpose
			if (count < 5)
				count++;
			else
			{
				for (int i = 0; i <= context.stepCount; i++) 
				{
					Precision.getInstance().addBall(ballWeKnow.getLookaheadTimestamp(i),
							(BallMotionResult) ballWeKnow.getLookahead(i));
				}
				Precision.getInstance().addCamBall(timestamp, visionBall);
			}
			
			ballWeKnow.observation(timestamp, visionBall); // observe
			return;
		}
		
		// ---we have no ball
		UnregisteredBall newBall = context.newBall;
		if (newBall != null) // do we have a possible ball?
		{
			newBall.addBall(timestamp, visionBall);
		} else
		// so it's a unknown ball
		{
			newBall = new UnregisteredBall(timestamp, visionBall);
			context.newBall = newBall;
		}
		// Cleanup
		//frameBallIDs.clear();
	}
	
	public void normalizePredictionTime()
	{
		IFilter ballWeKnow = context.ball;
		if (ballWeKnow != null) // do we know a ball?
		{
			double nowTime = context.getLatestCaptureTimestamp();
			ballWeKnow.updateOffset(nowTime);
		}
	}
	
	public void performCollisionAwareLookahead()
	{
		IFilter ballWeKnow = context.ball;
		if (ballWeKnow != null) // do we know a ball?
		{
			for (int i = 1; i <= context.stepCount; i++)
			{
				ballWeKnow.performLookahead(i);
				//TODO WP: perform collision control
				//ballWeKnow.collisionControl(i);
			}
		}
	}
}
