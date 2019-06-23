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
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2f;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.neural.NeuralStaticConfiguration.BotConfigs;


/**
 * This is the specialisation for the Robot state
 * 
 * @author KaiE
 */
public class NeuralRobotState implements INeuralState
{
	
	
	// ------<time after which a Bot is no longer represented, when it was no longer in a CamDetectionFrame>------//
	@Configurable
	private static long														throwOutTimeMS			= 1000;
	@Configurable(comment = "this value is always multiplyed by 2 to allow maximum coverage")
	private static int														sslDefaultTeamSize	= 6;
	// Default for HashTables is 0.75; 1 should increase the lookup speed
	@Configurable
	private static float														hashtableLoadFactor	= 1;
	private final Hashtable<Integer, CamRobot>						allBots;
	private final Hashtable<Integer, Long>								lastactive;
	private final Hashtable<Integer, Long>								activity;
	private final Hashtable<Integer, NeuralRobotPredictionData>	lastData;
	private final Deque<INeuralPredicitonData>						activeBots;
	private final Hashtable<Integer, NeuralNetworkImpl>			neuralNetworks;
	final private ETeamColor												color;
	private static final Logger											log						= Logger.getLogger(
																														NeuralRobotState.class
																																.getName());
																																
																																
	static
	{
		ConfigRegistration.registerClass("wp", NeuralRobotState.class);
	}
	
	
	/**
	 * Default c-tor.
	 * 
	 * @param tcolor
	 */
	public NeuralRobotState(final ETeamColor tcolor)
	{
		allBots = new Hashtable<Integer, CamRobot>(2 * sslDefaultTeamSize, hashtableLoadFactor);
		lastactive = new Hashtable<Integer, Long>(2 * sslDefaultTeamSize, hashtableLoadFactor);
		activity = new Hashtable<Integer, Long>(2 * sslDefaultTeamSize, hashtableLoadFactor);
		lastData = new Hashtable<Integer, NeuralRobotPredictionData>(2 * sslDefaultTeamSize, hashtableLoadFactor);
		neuralNetworks = new Hashtable<Integer, NeuralNetworkImpl>(2 * sslDefaultTeamSize, hashtableLoadFactor);
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
	public void updateState(final ExtendedCamDetectionFrame newframe)
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
		lastactive.clear();
		for (CamRobot cr : bots)
		{
			allBots.put(cr.getRobotID(), cr);
			lastactive.put(cr.getRobotID(), cr.getTimestamp());
			activity.put(cr.getRobotID(), cr.getTimestamp());
			neuralNetworks.get(cr.getRobotID()).updateRecurrence(cr);
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
			final NeuralNetworkImpl nni = neuralNetworks.get(etry.getKey());
			if (nni != null)
			{
				nni.train();
			}
		}
	}
	
	
	@Override
	public void performPrediction()
	{
		for (final Entry<Integer, Long> etry : lastactive.entrySet())
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
			final IVector2 pos = new Vector2f(vals[0], vals[1]);
			final IVector2 vel = new Vector2f(vals[2], vals[3]);
			final IVector2 acc = new Vector2f(vals[4], vals[5]);
			double orient = vals[6];
			double orientVel = vals[7];
			double orientAcc = vals[8];
			lastData.get(key).update(id, cr, cr.getTimestamp(), pos, vel, acc, orient, orientVel, orientAcc);
		}
	}
	
	
	@Override
	public Iterable<INeuralPredicitonData> getPredictedObjects()
	{
		activeBots.clear();
		for (Entry<Integer, Long> entry : activity.entrySet())
		{
			if ((System.nanoTime() - entry.getValue()) < (throwOutTimeMS * 1e6))
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
	public ETeamColor getTeamColor()
	{
		return color;
	}
	
}
