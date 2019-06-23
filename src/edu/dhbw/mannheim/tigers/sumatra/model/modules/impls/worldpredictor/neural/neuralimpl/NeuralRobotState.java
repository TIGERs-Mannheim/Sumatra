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
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamRobot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.MergedCamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.neural.NeuralWP;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.neural.neuralimpl.NeuralStaticConfiguration.BotConfigs;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * This is the specialisation for the Robot state
 * 
 * @author KaiE
 */
public class NeuralRobotState implements INeuralState
{
	
	// ------<time after which a Bot is no longer represented, when it was no longer in a CamDetectionFrame>------//
	@Configurable
	private static long												throwOutTimeMS			= 1000;
	@Configurable(comment = "this value is always multiplyed by 2 to allow maximum coverage")
	private static int												sslDefaultTeamSize	= 6;
	// Default for HashTables is 0.75; 1 should increase the lookup speed
	@Configurable
	private static float												hashtableLoadFactor	= 1f;
	private long														newestCaptureTime		= 0;
	private Hashtable<Integer, CamRobot>						allBots;
	private Hashtable<Integer, Long>								lastactive;
	private Hashtable<Integer, NeuralRobotPredictionData>	lastData;
	private Deque<INeuralPredicitonData>						activeBots;
	private Hashtable<Integer, NeuralNetworkImpl>			neuralNetworks;
	final private ETeamColor										color;
	private static final Logger									log						= Logger.getLogger(
																												NeuralRobotState.
																												class.getName());
	
	
	/**
	 * Default c-tor.
	 * 
	 * @param tcolor
	 */
	public NeuralRobotState(final ETeamColor tcolor)
	{
		allBots = new Hashtable<Integer, CamRobot>
				(2 * sslDefaultTeamSize, hashtableLoadFactor);
		lastactive = new Hashtable<Integer, Long>
				(2 * sslDefaultTeamSize, hashtableLoadFactor);
		lastData = new Hashtable<Integer, NeuralRobotPredictionData>
				(2 * sslDefaultTeamSize, hashtableLoadFactor);
		neuralNetworks = new Hashtable<Integer, NeuralNetworkImpl>
				(2 * sslDefaultTeamSize, hashtableLoadFactor);
		color = tcolor;
		activeBots = new ArrayDeque<INeuralPredicitonData>(2 * sslDefaultTeamSize);
		
		for (int i = 0; i < (2 * sslDefaultTeamSize); ++i)
		{
			NeuralNetworkImpl newNetwork = new NeuralNetworkImpl(BotConfigs.InputLayer, BotConfigs.HiddenLayer,
					BotConfigs.NeuronsPerHidden, BotConfigs.OutputLayer, i,
					new CamRobotConverter());
			neuralNetworks.put(i, newNetwork);
		}
	}
	
	
	@Override
	public void updateState(final MergedCamDetectionFrame newframe)
	{
		List<CamRobot> bots = newframe.getRobotsBlue();
		if (color == ETeamColor.YELLOW)
		{
			bots = newframe.getRobotsYellow();
		}
		
		if (bots.isEmpty())
		{
			return;
		}
		// TODO use independent capture time for each robot!
		newestCaptureTime = bots.get(0).getTimestamp();
		for (CamRobot cr : bots)
		{
			allBots.put(cr.getRobotID(), cr);
			lastactive.put(cr.getRobotID(), newestCaptureTime);
			neuralNetworks.get(cr.getRobotID()).updateRecurrence(cr, newestCaptureTime);
			if (!lastData.containsKey(cr.getRobotID()))
			{
				lastData.put(cr.getRobotID(), new NeuralRobotPredictionData());
			}
		}
		
		
	}
	
	
	@Override
	public void trainNetworks()
	{
		for (final Entry<Integer, Long> etry : lastactive.entrySet())
		{
			if (etry.getValue() == newestCaptureTime)
			{
				final NeuralNetworkImpl nni = neuralNetworks.get(etry.getKey());
				nni.train();
			}
		}
	}
	
	
	@Override
	public void performPrediction()
	{
		for (final Entry<Integer, Long> etry : lastactive.entrySet())
		{
			if (etry.getValue() == newestCaptureTime)
			{
				final Integer key = etry.getKey();
				final NeuralNetworkImpl nni = neuralNetworks.get(key);
				final double[] vals = nni.generateOutput();
				if (vals.length == 0)
				{
					return;
				}
				final BotID id = BotID.createBotId(key, color);
				final CamRobot cr = allBots.get(key);
				final long time = newestCaptureTime;
				final IVector2 pos = new Vector2f((float) vals[0], (float) vals[1]);
				final IVector2 vel = new Vector2f((float) vals[2], (float) vals[3]);
				final IVector2 acc = new Vector2f((float) vals[4], (float) vals[5]);
				double orient = vals[6];
				double orientVel = vals[7];
				double orientAcc = vals[8];
				lastData.get(key).update(id, cr, time, pos, vel, acc, orient, orientVel, orientAcc);
			}
		}
	}
	
	
	@Override
	public Iterable<INeuralPredicitonData> getPredictedObjects()
	{
		activeBots.clear();
		for (Entry<Integer, Long> entry : lastactive.entrySet())
		{
			if ((newestCaptureTime - entry.getValue()) < (throwOutTimeMS * 1e6))
			{
				final NeuralRobotPredictionData d = lastData.get(entry.getKey());
				if (d.getLastData() != null)
				{
					activeBots.add(d);
				}
			} else
			{
				final NeuralNetworkImpl nni = neuralNetworks.get(entry.getKey());
				nni.interruptRecurrence();
			}
			
		}
		return activeBots;
	}
	
	
	@Override
	public void loadNetwork(final String filenameStub)
	{
		for (int i = 0; i < (2 * sslDefaultTeamSize); ++i)
		{
			String filePath = NeuralWP.baseDirPathToFiles + filenameStub + i + ".eg";
			
			log.info("Load neural network from file:" + filePath + " for " + color.toString());
			
			neuralNetworks.get(i).loadNeuralConfig(filePath);
		}
	}
	
	
	@Override
	public void saveNetwork(final String filenameStub)
	{
		for (NeuralNetworkImpl nni : neuralNetworks.values())
		{
			nni.saveNeuralConfig(filenameStub);
		}
	}
	
	
	@Override
	public void reset()
	{
		allBots.clear();
		neuralNetworks.clear();
	}
	
	
	@Override
	public ETeamColor getTeamColor()
	{
		return color;
	}
	
}
