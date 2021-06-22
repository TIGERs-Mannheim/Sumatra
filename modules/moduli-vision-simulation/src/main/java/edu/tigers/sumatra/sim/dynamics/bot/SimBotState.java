package edu.tigers.sumatra.sim.dynamics.bot;

import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector3;
import lombok.EqualsAndHashCode;
import lombok.Value;


/**
 * State of a simulated bot
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class SimBotState extends SimBotDynamicsState
{
	long lastFeedback;
	boolean barrierInterrupted;


	/**
	 * @param pose [mm,mm,rad]
	 * @param vel  [mm/s,mm/s,rad/s]
	 */
	public SimBotState(Pose pose, IVector3 vel)
	{
		super(pose, vel);
		barrierInterrupted = false;
		lastFeedback = 0;
	}


	/**
	 * @param pose               [mm,mm,rad]
	 * @param vel                [mm/s,mm/s,rad/s]
	 * @param barrierInterrupted
	 * @param lastFeedback
	 */
	public SimBotState(Pose pose, IVector3 vel, Boolean barrierInterrupted, long lastFeedback)
	{
		super(pose, vel);
		this.barrierInterrupted = barrierInterrupted != null && barrierInterrupted;
		this.lastFeedback = lastFeedback;
	}


	public SimBotState(SimBotDynamicsState dynamicsState, Boolean barrierInterrupted, long lastFeedback)
	{
		super(dynamicsState);
		this.barrierInterrupted = barrierInterrupted != null && barrierInterrupted;
		this.lastFeedback = lastFeedback;
	}
}
