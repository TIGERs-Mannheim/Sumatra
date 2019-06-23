/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.10.2014
 * Author(s): KaiE
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.neural.neuralimpl;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.AWorldFramePacker;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.FieldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.WorldFramePrediction;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;


/**
 * This Class is the packer for the SimpleWorldFrames of the NeuralWp
 * 
 * @author KaiE
 */
public class NeuralWorldFramePacker extends AWorldFramePacker
{
	private IBotIDMap<TrackedTigerBot>	bots	= new BotIDMap<TrackedTigerBot>();
	private TrackedBall						ball;
	
	
	/**
	 * Method to pack the bot-data. the visible as well as the bots not in field.
	 * 
	 * @param yellowState
	 * @param blueState
	 */
	public void packBots(final INeuralState yellowState, final INeuralState blueState)
	{
		packBotsFromNeural(yellowState);
		packBotsFromNeural(blueState);
	}
	
	
	/**
	 * Method to pack ball-data. Required by the create method
	 * 
	 * @param ballstate
	 */
	public void packBall(final INeuralState ballstate)
	{
		for (INeuralPredicitonData inpd : ballstate.getPredictedObjects())
		{
			
			NeuralBallPredictionData data = (NeuralBallPredictionData) inpd;
			if (data != null)
			{
				ball = new TrackedBall(data.getPos(), data.getVel(), data.getAcc(), data.getLastball().getConfidence(),
						false);
				return;
			}
		}
		
		ball = new TrackedBall(AVector3.ZERO_VECTOR, AVector3.ZERO_VECTOR, AVector3.ZERO_VECTOR, 0, false);
		
	}
	
	
	/**
	 * Method to create SimpleWorldFrame from the packed data that are created by calling packB...()-Method
	 * 
	 * @param nextFrameID
	 * @return
	 */
	public SimpleWorldFrame create(final long nextFrameID)
	{
		WorldFramePrediction wfp = new FieldPredictor(bots.values(), ball).create();
		
		setVisibility(bots.getContentMap());
		setVisibilityBotmanager(bots.getContentMap());
		
		final SimpleWorldFrame swf = new SimpleWorldFrame(bots, ball, nextFrameID, wfp);
		bots = new BotIDMap<TrackedTigerBot>();
		return swf;
	}
	
	
	/**
	 * Helper Method to generate the TrackedTigerBots from the predicted info from the Network
	 */
	private void packBotsFromNeural(final INeuralState state)
	{
		
		for (INeuralPredicitonData inpd : state.getPredictedObjects())
		{
			NeuralRobotPredictionData data = (NeuralRobotPredictionData) inpd;
			
			botLastVisible.put(data.getId(), SumatraClock.nanoTime());
			
			final IVector2 pos = data.getPos();
			final IVector2 vel = data.getVel();
			final IVector2 acc = data.getAcc();
			final BotID bi = data.getId();
			float opos = (float) data.getOrient();
			float ovel = (float) data.getOrientVel();
			float oacc = (float) data.getOrientAcc();
			final TrackedTigerBot ttb = new TrackedTigerBot(bi, pos, vel, acc, 0, opos, ovel,
					oacc, data.getLastData().getConfidence(), botManager.getAllBots().get(bi), bi.getTeamColor());
			bots.put(bi, ttb);
		}
	}
}
