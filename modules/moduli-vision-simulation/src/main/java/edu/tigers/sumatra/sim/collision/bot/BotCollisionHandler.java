/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim.collision.bot;

import java.util.List;

import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.sim.ISimBot;
import edu.tigers.sumatra.sim.SimulatedBot;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotState;


/**
 * Handle the collisions in the Sumatra simulator:
 * It checks the available bots for collisions and updates their position, so that they are not colliding anymore
 */
public class BotCollisionHandler
{
	public void process(final List<SimulatedBot> botsInPlay)
	{
		for (int i = 0; i < botsInPlay.size(); i++)
		{
			for (int j = i + 1; j < botsInPlay.size(); j++)
			{
				SimulatedBot bot1 = botsInPlay.get(i);
				SimulatedBot bot2 = botsInPlay.get(j);
				
				if (twoBotsAreColliding(bot1, bot2))
				{
					adjustBotStates(bot1, bot2);
				}
			}
		}
	}
	
	
	private boolean twoBotsAreColliding(final ISimBot bot1, final ISimBot bot2)
	{
		final IVector2 pos1 = bot1.getState().getPose().getPos();
		final IVector2 pos2 = bot2.getState().getPose().getPos();
		
		double botDistanceSqr = pos1.distanceToSqr(pos2);
		double radius = bot1.getRadius() + bot2.getRadius();
		double radiusSqr = radius * radius;
		return botDistanceSqr < radiusSqr;
	}
	
	
	private void adjustBotStates(final SimulatedBot bot1, final SimulatedBot bot2)
	{
		final IVector2 pos1 = bot1.getState().getPose().getPos();
		final IVector2 pos2 = bot2.getState().getPose().getPos();
		
		final IVector2 diff = pos1.subtractNew(pos2);
		final IVector2 center = pos2.addNew(diff.multiplyNew(0.5));
		
		IVector2 newPos1 = center.addNew(diff.scaleToNew(bot1.getRadius()));
		IVector2 newPos2 = center.addNew(diff.scaleToNew(-bot2.getRadius()));
		
		final IVector2 velocityAfterCollision = velocityAfterCollision(bot1, bot2);
		IVector3 vel1 = Vector3.from2d(velocityAfterCollision, bot1.getState().getVel().z());
		IVector3 vel2 = Vector3.from2d(velocityAfterCollision, bot2.getState().getVel().z());
		
		final Pose pose1 = Pose.from(newPos1, bot1.getState().getPose().getOrientation());
		final Pose pose2 = Pose.from(newPos2, bot2.getState().getPose().getOrientation());
		bot1.setState(new SimBotState(pose1, vel1));
		bot2.setState(new SimBotState(pose2, vel2));
	}
	
	
	private IVector2 velocityAfterCollision(final ISimBot bot1, final ISimBot bot2)
	{
		final IVector2 v1 = bot1.getState().getVel().getXYVector().multiplyNew(bot1.getMass());
		final IVector2 v2 = bot2.getState().getVel().getXYVector().multiplyNew(bot2.getMass());
		double m = bot1.getMass() + bot2.getMass();
		return v1.addNew(v2).multiply(1 / m);
	}
}
