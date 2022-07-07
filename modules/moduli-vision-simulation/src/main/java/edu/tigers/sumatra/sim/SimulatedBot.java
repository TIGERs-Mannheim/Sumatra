/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.botshape.IBotShape;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotAction;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotDynamics;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotDynamicsState;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotState;
import lombok.Setter;


public class SimulatedBot implements ISimulatedObject, ISimBot
{
	private static final double NEAR_BARRIER_TOLERANCE = 2;

	private final SimBotDynamics dynamics = new SimBotDynamics();

	private final BotID botId;
	private SimBotAction action;

	private SimBotState state;
	@Setter
	private IVector2 ballPos;
	@Setter
	private Boolean barrierInterrupted;
	@Setter
	private Long lastUpdate;

	private double center2DribblerDist = 75.0;


	public SimulatedBot(final BotID botId, final SimBotState state)
	{
		this.botId = botId;
		setState(state);
	}


	@Override
	public void dynamics(final double dt)
	{
		final SimBotDynamicsState dynamicsState = dynamics.step(dt, action);

		state = updateState(dynamicsState);
	}


	private SimBotState updateState(SimBotDynamicsState dynamicsState)
	{
		return new SimBotState(
				dynamicsState,
				isBarrierInterrupted(),
				lastUpdate == null ? state.getLastFeedback() : lastUpdate
		);
	}


	private boolean isBarrierInterrupted()
	{
		if (barrierInterrupted != null)
		{
			return barrierInterrupted;
		}
		IBotShape botShape = BotShape.fromFullSpecification(state.getPose().getPos(), Geometry.getBotRadius(),
				center2DribblerDist, state.getPose().getOrientation());
		return botShape.isPointInKickerZone(ballPos, Geometry.getBallRadius() + NEAR_BARRIER_TOLERANCE);
	}


	public void setAction(final SimBotAction action)
	{
		this.action = action;
	}


	public void setState(final SimBotState state)
	{
		this.state = state;
		dynamics.setPose(state.getPose());
		dynamics.setVelGlobal(state.getVel());
	}


	public IVector3 getVelLocal()
	{
		return dynamics.getVelLocal();
	}


	@Override
	public BotID getBotId()
	{
		return botId;
	}


	@Override
	public SimBotState getState()
	{
		return state;
	}


	@Override
	public SimBotAction getAction()
	{
		return action;
	}


	@Override
	public double getMass()
	{
		return 2.5;
	}


	@Override
	public double getCenter2DribblerDist()
	{
		return center2DribblerDist;
	}


	@Override
	public double getRadius()
	{
		return Geometry.getBotRadius();
	}
}
