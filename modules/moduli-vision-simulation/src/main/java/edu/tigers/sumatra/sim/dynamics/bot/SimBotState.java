package edu.tigers.sumatra.sim.dynamics.bot;

import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector3;


/**
 * State of a simulated bot
 */
public class SimBotState extends SimBotDynamicsState
{
	private final boolean barrierInterrupted;


	/**
	 * @param pose [mm,mm,rad]
	 * @param vel [mm/s,mm/s,rad/s]
	 */
	public SimBotState(final Pose pose, final IVector3 vel)
	{
		super(pose, vel);
		barrierInterrupted = false;
	}


	/**
	 * @param pose [mm,mm,rad]
	 * @param vel [mm/s,mm/s,rad/s]
	 * @param barrierInterrupted
	 */
	public SimBotState(final Pose pose, final IVector3 vel, final boolean barrierInterrupted)
	{
		super(pose, vel);
		this.barrierInterrupted = barrierInterrupted;
	}


	public SimBotState(final SimBotDynamicsState dynamicsState, final boolean barrierInterrupted)
	{
		super(dynamicsState);
		this.barrierInterrupted = barrierInterrupted;
	}


	public boolean isBarrierInterrupted()
	{
		return barrierInterrupted;
	}
}
