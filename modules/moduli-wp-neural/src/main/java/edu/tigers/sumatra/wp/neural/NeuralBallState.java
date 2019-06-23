/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.04.2015
 * Author(s): KaiE
 * *********************************************************
 */
package edu.tigers.sumatra.wp.neural;

import java.util.ArrayDeque;
import java.util.Deque;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector3f;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.neural.NeuralStaticConfiguration.BallConfigs;


/**
 * This class contains the state of the ball
 * 
 * @author KaiE
 */
public class NeuralBallState implements INeuralState
{
	
	
	// ------<time after which a Ball is no longer represented, when it was no longer in a CamDetectionFrame>------//
	@Configurable
	private static long									throwOutTimeMS	= 1000;
	private NeuralNetworkImpl							ballNetwork		= null;
	private CamBall										lastBall			= null;
	private final Deque<INeuralPredicitonData>	activeBall		= new ArrayDeque<INeuralPredicitonData>(1);
	private NeuralBallPredictionData					lastData			= null;
																					
																					
	static
	{
		ConfigRegistration.registerClass("wp", NeuralBallState.class);
	}
	
	
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
	public void updateState(final ExtendedCamDetectionFrame newframe)
	{
		/*
		 * if (newframe.getBalls().isEmpty())
		 * {
		 * return;
		 * }
		 */
		lastBall = newframe.getBall();
		if (lastData == null)
		{
			lastData = new NeuralBallPredictionData();
		}
		ballNetwork.updateRecurrence(lastBall);
	}
	
	
	@Override
	public void trainNetworks()
	{
		if (ballNetwork != null)
		{
			ballNetwork.train();
		}
	}
	
	
	@Override
	public void performPrediction()
	{
		final double vals[] = ballNetwork.generateOutput();
		if (vals.length == 0)
		{
			return;
		}
		final IVector3 p = new Vector3f(vals[0], vals[1], 0);
		final IVector3 v = new Vector3f(vals[2], vals[3], 0);
		final IVector3 a = new Vector3f(vals[4], vals[5], 0);
		lastData.update(p, v, a, lastBall, lastBall.getTimestamp());
	}
	
	
	@Override
	public Iterable<INeuralPredicitonData> getPredictedObjects()
	{
		activeBall.clear();
		if ((lastData != null) && (lastData.getLastball() != null))
		{
			if ((System.nanoTime() - lastData.getTimestamp()) < (throwOutTimeMS * 1e6))
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
	public ETeamColor getTeamColor()
	{
		return ETeamColor.UNINITIALIZED;
	}
	
}
