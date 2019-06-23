/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 18.04.2014
 * Author(s): KaiE/ JanE
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.neural;

import java.io.File;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.Agent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.timer.ETimable;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.timer.SumatraTimer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.AWorldPredictorImplementationBluePrint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.MergedCamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.neural.neuralimpl.INeuralState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.neural.neuralimpl.NeuralBallState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.neural.neuralimpl.NeuralRobotState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.neural.neuralimpl.NeuralWorldFramePacker;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ATimer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor.PredictorKey;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Neural Network as worldpredictor. Each Object on Field will get an own neural Network.
 * The network will be created if there isnt a saved file for the current object.
 */
public class NeuralWP extends AWorldPredictorImplementationBluePrint
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger				log						= Logger
																							.getLogger(NeuralWP.class
																									.getName());
	
	
	@Configurable
	private static boolean						allowTrainingBlue		= true;
	@Configurable
	private static boolean						allowTrainigYellow	= true;
	@Configurable
	private static boolean						allowTrainingBall		= true;
	@Configurable(comment = "allows loading the networks from the filesystem if present")
	private static boolean						allowLoadingFromFile	= true;
	
	
	private MergedCamDetectionFrame			freshCamDetnFrame;
	private INeuralState							yellowState;
	private INeuralState							blueState;
	private INeuralState							ballState;
	// _____________________
	private final NeuralWorldFramePacker	packer					= new NeuralWorldFramePacker();
	private SumatraTimer							timer						= null;
	
	
	private static final String				foeFileStub				= "foeBot";
	private static final String				tigersFileStub			= "tigerBot";
	
	/** Sumatra/data/neuralsave/ */
	public static final String					baseDirPathToFiles	= System.getProperty("user.dir")
																							+ File.separator + "data"
																							+ File.separator
																							+ "neuralsave"
																							+ File.separator;
	
	
	/**
	 * @param predictor
	 */
	public NeuralWP(final AWorldPredictor predictor)
	{
		super(PredictorKey.Neural, predictor);
		try
		{
			timer = (SumatraTimer) SumatraModel.getInstance().getModule(ATimer.MODULE_ID);
		} catch (ModuleNotFoundException err)
		{
			log.error("No timer found");
		}
	}
	
	
	@Override
	public void predict()
	{
		freshCamDetnFrame = pollLatestMergedCamFrame();
		if (freshCamDetnFrame == null)
		{
			return;
		}
		final long nextFrameNR = getNextFrameNumber();
		
		if (timer != null)
		{
			timer.start(ETimable.WP_Neural, nextFrameNR);
		}
		
		yellowState.updateState(freshCamDetnFrame);
		blueState.updateState(freshCamDetnFrame);
		ballState.updateState(freshCamDetnFrame);
		
		performTraining();
		
		ballState.performPrediction();
		yellowState.performPrediction();
		blueState.performPrediction();
		
		packer.packBots(yellowState, blueState);
		packer.packBall(ballState);
		
		SimpleWorldFrame swf = packer.create(getNextFrameNumber());
		setReturnFrame(swf);
		pushPredictedFrameToWorldPredictor();
		
		if (timer != null)
		{
			timer.stop(ETimable.WP_Neural, nextFrameNR);
		}
	}
	
	
	@Override
	public void onFacadeInitModule()
	{
		System.out.println("INIT NEUROWP");
	}
	
	
	@Override
	public void onFacadeDeinitModule()
	{
		
	}
	
	
	@Override
	public void onFacadeStartModule()
	{
		yellowState = new NeuralRobotState(ETeamColor.YELLOW);
		blueState = new NeuralRobotState(ETeamColor.BLUE);
		ballState = new NeuralBallState();
		
		if (allowLoadingFromFile)
		{
			loadAllBots();
			ballState.loadNetwork(baseDirPathToFiles + "ball-1.eg");
		}
		
		startThisThread();
		
		
	}
	
	
	@Override
	public void onFacadeStopModule()
	{
		
		if (getTigerColor() == ETeamColor.YELLOW)
		{
			yellowState.saveNetwork(tigersFileStub);
			blueState.saveNetwork(foeFileStub);
		}
		else
		{
			yellowState.saveNetwork(foeFileStub);
			blueState.saveNetwork(tigersFileStub);
		}
		ballState.saveNetwork("ball");
		yellowState.reset();
		blueState.reset();
		ballState.reset();
		
		stopThisThread();
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
		try
		{
			Agent agentYellow = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_YELLOW);
			if (agentYellow.isActive())
			{
				return ETeamColor.YELLOW;
			}
			
			Agent agentBlue = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_BLUE);
			if (agentBlue.isActive())
			{
				return ETeamColor.BLUE;
			}
			return ETeamColor.UNINITIALIZED;
			
		} catch (ModuleNotFoundException err)
		{
			log.error("could not get Agents to determine Tigers", err);
			return ETeamColor.UNINITIALIZED;
		}
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
		}
		else
		{
			yellowState.loadNetwork(foeFileStub);
			blueState.loadNetwork(tigersFileStub);
		}
		
	}
}
