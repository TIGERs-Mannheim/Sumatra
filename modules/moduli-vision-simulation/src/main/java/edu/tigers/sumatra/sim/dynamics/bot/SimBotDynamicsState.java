package edu.tigers.sumatra.sim.dynamics.bot;

import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector3;


public class SimBotDynamicsState
{
	// [mm,mm,rad]
	private final Pose pose;
	// [mm/s,mm/s,rad/s]
	private final IVector3 vel;
	
	
	public SimBotDynamicsState(final Pose pose, final IVector3 vel)
	{
		this.pose = pose;
		this.vel = vel;
	}
	
	
	public SimBotDynamicsState(final SimBotDynamicsState state)
	{
		this.pose = state.getPose();
		this.vel = state.getVel();
	}
	
	
	public Pose getPose()
	{
		return pose;
	}
	
	
	public IVector3 getVel()
	{
		return vel;
	}
}
