/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.04.2015
 * Author(s): KaiE
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.neural.neuralimpl;

import java.util.ArrayDeque;
import java.util.Deque;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.MergedCamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.neural.neuralimpl.NeuralStaticConfiguration.BallConfigs;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * This class contains the state of the ball
 * 
 * @author KaiE
 */
public class NeuralBallState implements INeuralState
{
	// ------<time after which a Ball is no longer represented, when it was no longer in a CamDetectionFrame>------//
	@Configurable
	private static long							throwOutTimeMS		= 1000;
	private NeuralNetworkImpl					ballNetwork			= null;
	private long									newestCaptureTime	= 0;
	private CamBall								lastBall				= null;
	private Deque<INeuralPredicitonData>	activeBall			= new ArrayDeque<INeuralPredicitonData>(1);
	private NeuralBallPredictionData			lastData				= null;
	
	
	/**
	 * c-tor
	 */
	public NeuralBallState()
	{
		ballNetwork = new NeuralNetworkImpl(BallConfigs.InputLayer, BallConfigs.HiddenLayer,
				BallConfigs.NeuronsPerHidden, BallConfigs.OutputLayer, BallConfigs.ID,
				new CamBallConverter());
	}
	
	
	@Override
	public void loadNetwork(final String filenameStub)
	{
		ballNetwork.loadNeuralConfig(filenameStub);
	}
	
	
	@Override
	public void saveNetwork(final String filenameStub)
	{
		ballNetwork.saveNeuralConfig(filenameStub);
	}
	
	
	@Override
	public void updateState(final MergedCamDetectionFrame newframe)
	{
		if (newframe.getBalls().isEmpty())
		{
			return;
		}
		newestCaptureTime = newframe.getBalls().get(0).getTimestamp();
		lastBall = newframe.getBalls().get(0);
		if (lastData == null)
		{
			lastData = new NeuralBallPredictionData();
		}
		ballNetwork.updateRecurrence(lastBall, newestCaptureTime);
	}
	
	
	@Override
	public void trainNetworks()
	{
		ballNetwork.train();
	}
	
	
	@Override
	public void performPrediction()
	{
		final double vals[] = ballNetwork.generateOutput();
		if (vals.length == 0)
		{
			return;
		}
		final IVector3 p = new Vector3f((float) vals[0], (float) vals[1], 0f);
		final IVector3 v = new Vector3f((float) vals[2], (float) vals[3], 0f);
		final IVector3 a = new Vector3f((float) vals[4], (float) vals[5], 0f);
		lastData.update(p, v, a, lastBall, newestCaptureTime);
	}
	
	
	@Override
	public Iterable<INeuralPredicitonData> getPredictedObjects()
	{
		activeBall.clear();
		if ((lastData != null) && (lastData.getLastball() != null))
		{
			if ((newestCaptureTime - lastData.getTimestamp()) < (throwOutTimeMS * 1e6))
			{
				activeBall.add(lastData);
			} else
			{
				ballNetwork.interruptRecurrence();
			}
		}
		return activeBall;
	}
	
	
	@Override
	public void reset()
	{
		ballNetwork = null;
		lastData = null;
		activeBall.clear();
	}
	
	
	@Override
	public ETeamColor getTeamColor()
	{
		return ETeamColor.UNINITIALIZED;
	}
	
}
