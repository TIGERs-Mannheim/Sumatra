/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.control.GcEventFactory;
import edu.tigers.sumatra.snapshot.Snapshot;
import edu.tigers.sumatra.vision.AVisionFilter;
import edu.tigers.sumatra.wp.AWorldPredictor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;


/**
 * Helper methods for controlling simulation.
 */
@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimulationHelper
{
	/**
	 * Load a simulation with given parameters
	 *
	 * @param snapshot
	 */
	public static void loadSimulation(final Snapshot snapshot)
	{
		log.debug("Loading simulation from {}", snapshot);
		SumatraSimulator sim = getSumatraSimulator();

		boolean wasRunning = sim.isRunning();
		sim.pause();

		resetSimulation();

		getReferee().initGameController();

		initSimulation(snapshot);

		sim.stepBlocking();
		if (wasRunning)
		{
			sim.resume();
		}
		log.debug("Loaded simulation");
	}


	/**
	 * Initializing the simulation, assuming that is was just started up and is in its initial state
	 *
	 * @param snapshot
	 */
	public static void initSimulation(final Snapshot snapshot)
	{
		SumatraSimulator sim = getSumatraSimulator();

		// reset ball
		sim.placeBall(snapshot.getBall().getPos(), snapshot.getBall().getVel().multiplyNew(1e3));

		// add new bots
		snapshot.getBots().forEach((k, v) -> registerBot(sim, k, v.getPos(), v.getVel()));

		if (snapshot.getStage() != null)
		{
			getReferee().sendGameControllerEvent(GcEventFactory.stage(snapshot.getStage()));
		}
		if (snapshot.getPlacementPos() != null)
		{
			getReferee().sendGameControllerEvent(
					GcEventFactory.ballPlacement(snapshot.getPlacementPos().multiplyNew(1e-3)));
		}
		if (snapshot.getCommand() != null)
		{
			getReferee().sendGameControllerEvent(GcEventFactory.command(snapshot.getCommand()));
		}
	}


	private static void registerBot(SumatraSimulator sim, BotID botID, IVector3 pos, IVector3 vel)
	{
		Pose pose = Pose.from(pos);
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
		getSumatraSimulator().resume();
	}


	/**
	 * Reset the current simulation state
	 */
	private static void resetSimulation()
	{
		SumatraModel.getInstance().getModule(SumatraSimulator.class).reset();
		SumatraModel.getInstance().getModule(AWorldPredictor.class).reset();
		SumatraModel.getInstance().getModule(AAgent.class).reset();
	}


	/**
	 * Simulate with maximum speed by setting the sync flag and a high simulation speed
	 *
	 * @param state
	 * @throws ModuleNotFoundException
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
		return SumatraModel.getInstance().getModule(AReferee.class);
	}
}
