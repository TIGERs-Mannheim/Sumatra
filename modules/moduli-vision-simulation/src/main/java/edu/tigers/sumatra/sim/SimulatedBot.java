package edu.tigers.sumatra.sim;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.botshape.IBotShape;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotAction;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotDynamics;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotDynamicsState;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotState;


public class SimulatedBot implements ISimulatedObject, ISimBot
{
	private static final double NEAR_BARRIER_TOLERANCE = 2;
	
	private final SimBotDynamics dynamics = new SimBotDynamics();
	
	private final BotID botId;
	private final ISimBall ball;
	private SimBotAction action;
	
	private SimBotState state;
	
	private double center2DribblerDist = 75.0;
	
	
	public SimulatedBot(final BotID botId, final SimBotState state, final ISimBall ball)
	{
		this.botId = botId;
		this.ball = ball;
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
		return new SimBotState(dynamicsState, isBarrierInterrupted());
	}
	
	
	private boolean isBarrierInterrupted()
	{
		IBotShape botShape = BotShape.fromFullSpecification(state.getPose().getPos(), Geometry.getBotRadius(),
				center2DribblerDist, state.getPose().getOrientation());
		return botShape.isPointInKickerZone(ball.getState().getPos().getXYVector(),
				Geometry.getBallRadius() + NEAR_BARRIER_TOLERANCE);
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
