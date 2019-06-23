/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim;

import java.util.List;

import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.botmanager.ABotManager;
import edu.tigers.sumatra.botmanager.basestation.IBaseStation;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.data.RefBoxRemoteControlFactory;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.vision.AVisionFilter;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public final class SimulationHelper
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(SimulationHelper.class.getName());
	
	
	@SuppressWarnings("unused")
	private SimulationHelper()
	{
	}
	
	
	/**
	 * Load a simulation with given parameters
	 * 
	 * @param params
	 * @throws ModuleNotFoundException
	 */
	public static void loadSimulation(final SimulationParameters params)
	{
		log.debug("Loading simulation from " + params);
		SumatraSimulator sim = getSumatraSimulator();
		SumatraBaseStation bs = getSumatraBaseStation();
		AReferee referee = getReferee();
		
		boolean wasRunning = sim.isRunning();
		sim.pause();
		
		// first stop and remove all current bots
		for (SumatraBot sBot : sim.getBots())
		{
			bs.removeBot(sBot.getBotId());
		}
		
		resetSimulation();
		
		// reset ball
		sim.placeBall(params.getInitBall().getPos(), params.getInitBall().getVel());
		
		// add new bots
		params.getInitBots().keySet().forEach(bs::addBot);
		
		sim.getBots().forEach(bot -> bot.setPos(params.getInitBots().get(bot.getBotId()).getPos()));
		sim.getBots().forEach(bot -> bot.setVel(params.getInitBots().get(bot.getBotId()).getVel()));
		
		referee.handleControlRequest(constructRefereeMsg(params));
		
		sim.step();
		if (wasRunning)
		{
			sim.play();
		}
		log.debug("Loaded simulation");
	}
	
	
	/**
	 * Start (play) the simulation, if not already running
	 * 
	 * @throws ModuleNotFoundException
	 */
	public static void startSimulation()
	{
		getSumatraSimulator().play();
	}
	
	
	/**
	 * Reset the current simulation state
	 */
	public static void resetSimulation()
	{
		try
		{
			AVisionFilter vf = SumatraModel.getInstance().getModule(AVisionFilter.class);
			if (vf instanceof SumatraSimulator)
			{
				SumatraSimulator sCam = (SumatraSimulator) vf;
				sCam.reset(0);
			}
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find cam module", e);
		}
		
		try
		{
			AAgent ab = SumatraModel.getInstance().getModule(AAgent.class);
			ab.reset();
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find agent modules", e);
		}
	}
	
	
	private static SSL_RefereeRemoteControlRequest constructRefereeMsg(final SimulationParameters params)
	{
		Command cmd = params.getRefereeCommand();
		
		if ((cmd == Command.BALL_PLACEMENT_BLUE) || (cmd == Command.BALL_PLACEMENT_YELLOW))
		{
			if (cmd == Command.BALL_PLACEMENT_BLUE)
			{
				return RefBoxRemoteControlFactory.fromBallPlacement(ETeamColor.BLUE, params.getBallPlacementPos());
			}
			
			return RefBoxRemoteControlFactory.fromBallPlacement(ETeamColor.YELLOW, params.getBallPlacementPos());
		}
		
		return RefBoxRemoteControlFactory.fromCommand(params.getRefereeCommand());
	}
	
	
	/**
	 * If true, sync AI and skill threads with cam thread.
	 * 
	 * @param sync
	 */
	public static void setProcessAllWorldFrames(final boolean sync)
	{
		AAgent agent = getAgent();
		if (agent != null)
		{
			agent.setProcessAllWorldFrames(sync);
		}
		getSkillSystem().setProcessAllWorldFrames(sync);
	}
	
	
	/**
	 * Simulate with maximum speed by setting the sync flag and a high simulation speed
	 * 
	 * @throws ModuleNotFoundException
	 * @param state
	 */
	public static void setSimulateWithMaxSpeed(final boolean state)
	{
		SimulationHelper.setProcessAllWorldFrames(state);
		if (state)
		{
			getSumatraSimulator().setSimSpeed(100);
		} else
		{
			getSumatraSimulator().setSimSpeed(1);
		}
	}
	
	
	/**
	 * Stop the simulation
	 * 
	 * @throws ModuleNotFoundException
	 */
	public static void stopSimulation()
	{
		getSumatraSimulator().pause();
	}
	
	
	private static SumatraSimulator getSumatraSimulator()
	{
		AVisionFilter vf = SumatraModel.getInstance().getModule(AVisionFilter.class);
		if (vf instanceof SumatraSimulator)
		{
			return (SumatraSimulator) vf;
		}
		throw new ModuleNotFoundException("ACam is not a SumatraCam instance!");
	}
	
	
	private static ABotManager getBotManager()
	{
		return SumatraModel.getInstance().getModule(ABotManager.class);
	}
	
	
	private static SumatraBaseStation getSumatraBaseStation()
	{
		List<IBaseStation> baseStations = getBotManager().getBaseStations();
		for (IBaseStation baseStation : baseStations)
		{
			if (baseStation instanceof SumatraBaseStation)
			{
				return (SumatraBaseStation) baseStation;
			}
		}
		throw new IllegalStateException("No SumatraBaseStation found.");
	}
	
	
	private static AReferee getReferee()
	{
		try
		{
			return SumatraModel.getInstance().getModule(AReferee.class);
			
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find referee module.", e);
		}
		throw new IllegalStateException("No referee module");
	}
	
	
	private static ASkillSystem getSkillSystem()
	{
		try
		{
			return SumatraModel.getInstance().getModule(ASkillSystem.class);
		} catch (ModuleNotFoundException e)
		{
			log.error("skill system module not found.", e);
		}
		throw new IllegalStateException();
	}
	
	
	private static AAgent getAgent()
	{
		try
		{
			return SumatraModel.getInstance().getModule(AAgent.class);
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find AI module.", e);
		}
		return null;
	}
}
