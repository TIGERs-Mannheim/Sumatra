/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim.collision.bot;

import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.sim.ISimBot;
import edu.tigers.sumatra.sim.SimulatedBot;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotDynamicsState;

import java.util.List;


/**
 * Handle the collisions in the Sumatra simulator:
 * It checks the available bots for collisions and updates their position, so that they are not colliding anymore
 */
public class BotCollisionHandler
{

	// dampFactor = 1 -> perfect inelastic collision
	// dampFactor = 0 -> perfect elastic collision
	private static final double VEL_DAMP_FACTOR = 0.8;
	// influenceFactor = 0 -> impulse orthogonal to the collision will not change at all
	//	influenceFactor = 1 -> impulse orthogonal to the collision will behave as if the collision was 90° shifted
	private static final double INFLUENCE_FACTOR = 0.1;


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

		var velocitiesAfterCollision = velocitiesAfterCollision(bot1, bot2, diff.normalizeNew());
		IVector3 vel1 = Vector3.from2d(velocitiesAfterCollision.v1, bot1.getState().getVel().z());
		IVector3 vel2 = Vector3.from2d(velocitiesAfterCollision.v2, bot2.getState().getVel().z());

		final Pose pose1 = Pose.from(newPos1, bot1.getState().getPose().getOrientation());
		final Pose pose2 = Pose.from(newPos2, bot2.getState().getPose().getOrientation());
		bot1.setState(new SimBotDynamicsState(pose1, vel1));
		bot2.setState(new SimBotDynamicsState(pose2, vel2));
	}


	private VelocitiesAfterCol velocitiesAfterCollision(ISimBot bot1, ISimBot bot2, IVector2 collisionDirection)
	{
		var normalX = collisionDirection.normalizeNew();
		var normalY = normalX.getNormalVector();

		var v1x = normalX.scalarProduct(bot1.getState().getVel().getXYVector());
		var v2x = normalX.scalarProduct(bot2.getState().getVel().getXYVector());

		var v1y = normalY.scalarProduct(bot1.getState().getVel().getXYVector());
		var v2y = normalY.scalarProduct(bot2.getState().getVel().getXYVector());

		var m1 = bot1.getMass();
		var m2 = bot2.getMass();

		var v1xAfterCol = v1AfterCol(m1, v1x, m2, v2x);
		var v2xAfterCol = v2AfterCol(m1, v1x, m2, v2x);

		var v1yAfterCol = v1y * (1 - INFLUENCE_FACTOR) + INFLUENCE_FACTOR * v1AfterCol(m1, v1y, m2, v2y);
		var v2yAfterCol = v2y * (1 - INFLUENCE_FACTOR) + INFLUENCE_FACTOR * v2AfterCol(m1, v1y, m2, v2y);

		var v1Final = normalX.scaleToNew(v1xAfterCol).add(normalY.scaleToNew(v1yAfterCol));
		var v2Final = normalX.scaleToNew(v2xAfterCol).add(normalY.scaleToNew(v2yAfterCol));

		return new VelocitiesAfterCol(v1Final, v2Final);
	}


	private double v1AfterCol(double m1, double v1, double m2, double v2)
	{
		return (m1 * v1 + m2 * v2 - m2 * (v1 - v2) * (1 - VEL_DAMP_FACTOR)) / (m1 + m2);
	}


	private double v2AfterCol(double m1, double v1, double m2, double v2)
	{
		return (m1 * v1 + m2 * v2 - m2 * (v2 - v1) * (1 - VEL_DAMP_FACTOR)) / (m1 + m2);
	}


	private record VelocitiesAfterCol(IVector2 v1, IVector2 v2)
	{
	}

}
