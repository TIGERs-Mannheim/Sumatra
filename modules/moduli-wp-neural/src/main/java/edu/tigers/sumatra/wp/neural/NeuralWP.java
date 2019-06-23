/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 18.04.2014
 * Author(s): KaiE/ JanE
 * *********************************************************
 */
package edu.tigers.sumatra.wp.neural;

import java.io.File;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.data.TrackedBot;


/**
 * Neural Network as worldpredictor. Each Object on Field will get an own neural Network.
 * The network will be created if there isnt a saved file for the current object.
 */
public class NeuralWP extends AWorldPredictor
{
	
	@SuppressWarnings("unused")
	private static final Logger	log						= Logger.getLogger(NeuralWP.class.getName());
	
	
	@Configurable
	private static boolean			allowTrainingBlue		= true;
	@Configurable
	private static boolean			allowTrainigYellow	= true;
	@Configurable
	private static boolean			allowTrainingBall		= true;
	@Configurable(comment = "allows loading the networks from the filesystem if present")
	private static boolean			allowLoadingFromFile	= true;
	
	
	private INeuralState				yellowState;
	private INeuralState				blueState;
	private INeuralState				ballState;
	
	
	private static final String	foeFileStub				= "foeBot";
	private static final String	tigersFileStub			= "tigerBot";
	
	/** Sumatra/data/neuralsave/ */
	public static final String		baseDirPathToFiles	= System.getProperty("user.dir")
			+ File.separator + "data"
			+ File.separator
			+ "neuralsave"
			+ File.separator;
	
	
	static
	{
		ConfigRegistration.registerClass("wp", NeuralWP.class);
	}
	
	
	/**
	 * @param config
	 */
	public NeuralWP(final SubnodeConfiguration config)
	{
		super(config);
	}
	
	
	private void performTraining()
	{
		trainBots(blueState, allowTrainingBlue);
		trainBots(yellowState, allowTrainigYellow);
		trainBall(ballState);
	}
	
	
	/**
	 * If the yellow Agent is active then the tigers are Yellow even when the blue agent is active too.
	 * If both Agents are not active then Uninitialised is returned which results in loading both teams as foes.
	 */
	private ETeamColor getTigerColor()
	{
		// try
		// {
		// FIXME find another way
		// Agent agentYellow = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_YELLOW);
		// if (agentYellow.isActive())
		// {
		// return ETeamColor.YELLOW;
		// }
		//
		// Agent agentBlue = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_BLUE);
		// if (agentBlue.isActive())
		// {
		// return ETeamColor.BLUE;
		// }
		return ETeamColor.UNINITIALIZED;
		
		// } catch (ModuleNotFoundException err)
		// {
		// log.error("could not get Agents to determine Tigers", err);
		// return ETeamColor.UNINITIALIZED;
		// }
	}
	
	
	private void trainBots(final INeuralState state, final boolean allowTraining)
	{
		if (allowTraining)
		{
			state.trainNetworks();
		}
	}
	
	
	private void trainBall(final INeuralState ballstate)
	{
		if (allowTrainingBall)
		{
			ballstate.trainNetworks();
		}
	}
	
	
	private void loadAllBots()
	{
		ETeamColor tcolor = getTigerColor();
		
		if (tcolor == ETeamColor.YELLOW)
		{
			yellowState.loadNetwork(tigersFileStub);
			blueState.loadNetwork(foeFileStub);
		} else
		{
			yellowState.loadNetwork(foeFileStub);
			blueState.loadNetwork(tigersFileStub);
		}
		
	}
	
	
	@Override
	protected void processCameraDetectionFrame(final ExtendedCamDetectionFrame freshCamDetnFrame)
	{
		if (freshCamDetnFrame == null)
		{
			return;
		}
		
		yellowState.updateState(freshCamDetnFrame);
		blueState.updateState(freshCamDetnFrame);
		ballState.updateState(freshCamDetnFrame);
		
		performTraining();
		
		ballState.performPrediction();
		yellowState.performPrediction();
		blueState.performPrediction();
		
		IBotIDMap<ITrackedBot> bots = packBots(yellowState, blueState);
		TrackedBall ball = packBall(ballState);
		
		final SimpleWorldFrame swf = new SimpleWorldFrame(bots, ball, freshCamDetnFrame.getFrameNumber(),
				freshCamDetnFrame.gettCapture());
		pushFrame(swf);
	}
	
	
	@Override
	protected void start()
	{
		yellowState = new NeuralRobotState(ETeamColor.YELLOW);
		blueState = new NeuralRobotState(ETeamColor.BLUE);
		ballState = new NeuralBallState();
		
		if (allowLoadingFromFile)
		{
			loadAllBots();
			ballState.loadNetwork(baseDirPathToFiles + "ball-1.eg");
		}
	}
	
	
	@Override
	protected void stop()
	{
		if (getTigerColor() == ETeamColor.YELLOW)
		{
			yellowState.saveNetwork(tigersFileStub);
			blueState.saveNetwork(foeFileStub);
		} else
		{
			yellowState.saveNetwork(foeFileStub);
			blueState.saveNetwork(tigersFileStub);
		}
		ballState.saveNetwork("ball");
	}
	
	
	/**
	 * Method to pack the bot-data. the visible as well as the bots not in field.
	 * 
	 * @param yellowState
	 * @param blueState
	 * @return
	 */
	public IBotIDMap<ITrackedBot> packBots(final INeuralState yellowState, final INeuralState blueState)
	{
		IBotIDMap<ITrackedBot> bots = new BotIDMap<>();
		packBotsFromNeural(yellowState, bots);
		packBotsFromNeural(blueState, bots);
		return bots;
	}
	
	
	/**
	 * Method to pack ball-data. Required by the create method
	 * 
	 * @param ballstate
	 * @return
	 */
	private TrackedBall packBall(final INeuralState ballstate)
	{
		for (INeuralPredicitonData inpd : ballstate.getPredictedObjects())
		{
			
			NeuralBallPredictionData data = (NeuralBallPredictionData) inpd;
			if (data != null)
			{
				return new TrackedBall(data.getPos(), data.getVel(), data.getAcc());
			}
		}
		
		return TrackedBall.defaultInstance();
	}
	
	
	/**
	 * Helper Method to generate the TrackedTigerBots from the predicted info from the Network
	 */
	private void packBotsFromNeural(final INeuralState state, final IBotIDMap<ITrackedBot> bots)
	{
		for (INeuralPredicitonData inpd : state.getPredictedObjects())
		{
			NeuralRobotPredictionData data = (NeuralRobotPredictionData) inpd;
			
			final IVector2 pos = data.getPos();
			final IVector2 vel = data.getVel();
			final BotID bi = data.getId();
			double opos = data.getOrient();
			double ovel = data.getOrientVel();
			// FIXME timestamp
			TrackedBot ttb = new TrackedBot(0, bi);
			ttb.setPos(pos);
			ttb.setVel(vel);
			ttb.setAngle(opos);
			ttb.setaVel(ovel);
			bots.put(bi, ttb);
		}
	}
}
