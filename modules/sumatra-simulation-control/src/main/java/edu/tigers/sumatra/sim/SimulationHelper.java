/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim;

import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.control.Event;
import edu.tigers.sumatra.referee.control.GcEventFactory;
import edu.tigers.sumatra.vision.AVisionFilter;
import edu.tigers.sumatra.wp.AWorldPredictor;


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

		boolean wasRunning = sim.isRunning();
		sim.pause();

		resetSimulation();

		AReferee referee = getReferee();
		referee.initGameController();

		initSimulation(params);

		sim.step();
		if (wasRunning)
		{
			sim.play();
		}
		log.debug("Loaded simulation");
	}


	/**
	 * Initializing the simulation, assuming that is was just started up and is in its initial state
	 *
	 * @param params
	 */
	public static void initSimulation(final SimulationParameters params)
	{
		SumatraSimulator sim = getSumatraSimulator();

		// reset ball
		sim.placeBall(params.getInitBall().getPos(), params.getInitBall().getVel().multiplyNew(1e3));

		// add new bots
		params.getInitBots().keySet().forEach(b -> registerBot(sim, b, params));

		getReferee().sendGameControllerEvent(constructRefereeMsg(params));
	}


	private static void registerBot(SumatraSimulator sim, BotID botID, SimulationParameters params)
	{
		Pose pose = Pose.from(params.getInitBots().get(botID).getPos());
		IVector3 vel = params.getInitBots().get(botID).getVel();
		IVector3 velConverted = Vector3.from2d(vel.getXYVector().multiplyNew(1e3), vel.z());
		sim.registerBot(botID, pose, velConverted);
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
	private static void resetSimulation()
	{
		SumatraModel.getInstance().getModule(SumatraSimulator.class).reset(1);
		SumatraModel.getInstance().getModule(AWorldPredictor.class).reset();
		SumatraModel.getInstance().getModule(AAgent.class).reset();
	}


	private static Event constructRefereeMsg(final SimulationParameters params)
	{
		Command cmd = params.getRefereeCommand();

		if ((cmd == Command.BALL_PLACEMENT_BLUE) || (cmd == Command.BALL_PLACEMENT_YELLOW))
		{
			if (cmd == Command.BALL_PLACEMENT_BLUE)
			{
				return GcEventFactory.ballPlacement(ETeamColor.BLUE, params.getBallPlacementPos());
			}

			return GcEventFactory.ballPlacement(ETeamColor.YELLOW, params.getBallPlacementPos());
		}

		return GcEventFactory.command(params.getRefereeCommand());
	}


	/**
	 * Simulate with maximum speed by setting the sync flag and a high simulation speed
	 *
	 * @throws ModuleNotFoundException
	 * @param state
	 */
	public static void setSimulateWithMaxSpeed(final boolean state)
	{
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
	public static void pauseSimulation()
	{
		getSumatraSimulator().pause();
	}


	/**
	 * Set if simulation should handle the bot count
	 *
	 * @param state the value that should be set
	 * @throws ModuleNotFoundException
	 */
	public static void setHandleBotCount(boolean state)
	{
		getSumatraSimulator().setManageBotCount(state);
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
}
