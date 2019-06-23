/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 3, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplinePair3D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplineTrajectoryGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.EventStatePair;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IStateMachine;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.StateMachine;
import edu.dhbw.mannheim.tigers.sumatra.util.units.DistanceUnit;


/**
 * Pulls the ball backwards
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class PullBackAndTurnSkill extends AMoveSkill
{
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final float		PULL_BACK_DIST	= 100;
	private IVector2					pullDest;
	private IStateMachine<IState>	stateMachine	= new StateMachine<IState>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * When turning the bot during pull, the ball will most likely roll away from
	 * the bot in pull back direction
	 */
	public PullBackAndTurnSkill()
	{
		super(ESkillName.PULL_BACK_AND_TURN);
		IState getballState = new GetBallState();
		IState pullState = new PullState();
		stateMachine.setInitialState(getballState);
		stateMachine.getTransititions().put(new EventStatePair(EEvent.GOT_BALL, EStateId.GET_BALL), pullState);
		stateMachine.getTransititions().put(new EventStatePair(EEvent.LOST_BALL, EStateId.PULL_BACK), getballState);
	}
	
	// --------------------------------------------------------------------------
	// --- states ---------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private enum EStateId
	{
		GET_BALL,
		PULL_BACK,
		TURN
	}
	
	private enum EEvent
	{
		LOST_BALL,
		GOT_BALL,
	}
	
	private class GetBallState implements IState
	{
		
		@Override
		public void doEntryActions()
		{
			List<IVector2> nodes = new LinkedList<IVector2>();
			nodes.add(GeoMath.stepAlongLine(getWorldFrame().ball.getPos(), getBot().getPos(), AIConfig.getGeometry()
					.getBotRadius() / 2));
			createSpline(getBot(), nodes, getWorldFrame().ball.getPos(), getGen(getBot()));
		}
		
		
		@Override
		public void doUpdate()
		{
			if (getBot().hasBallContact())
			{
				stateMachine.nextState(EEvent.GOT_BALL);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.GET_BALL;
		}
	}
	
	private class PullState implements IState
	{
		private long	expectedTimePullDone	= 0;
		
		
		@Override
		public void doEntryActions()
		{
			pullDest = GeoMath.stepAlongLine(getBot().getPos(), getWorldFrame().ball.getPos(), -PULL_BACK_DIST);
			IVector2 turnDest = pullDest.addNew(getBot().getPos().subtractNew(pullDest).turn(AngleMath.PI_HALF)
					.scaleTo(AIConfig.getGeometry().getBotRadius() * 7));
			
			List<IVector2> nodes = new LinkedList<IVector2>();
			nodes.add(DistanceUnit.MILLIMETERS.toMeters(getBot().getPos()));
			nodes.add(DistanceUnit.MILLIMETERS.toMeters(pullDest));
			
			IVector2 finalVel = pullDest.subtractNew(getBot().getPos()).scaleTo(1f);
			SplinePair3D pair = getGen(getBot()).create(nodes, getBot().getVel(), finalVel, getBot().getAngle(),
					getBot().getAngle(), getBot().getaVel(), 0f);
			expectedTimePullDone = System.nanoTime()
					+ TimeUnit.MILLISECONDS.toNanos((int) (pair.getPositionTrajectory().getTotalTime() * 1000));
			
			List<IVector2> nodes2 = new LinkedList<IVector2>();
			nodes2.add(DistanceUnit.MILLIMETERS.toMeters(pullDest));
			nodes2.add(DistanceUnit.MILLIMETERS.toMeters(turnDest));
			// SplinePair3D pair2 = getGen(getBot()).create(nodes2, finalVel, Vector2.ZERO_VECTOR, getBot().getAngle(),
			// getBot().getAngle() - AngleMath.PI_HALF, getBot().getaVel(), 0f);
			
			// pair.append(pair2);
			
			setNewTrajectory(pair, System.nanoTime());
			List<IVector2> allNodes = new LinkedList<IVector2>();
			allNodes.add(getBot().getPos());
			allNodes.add(pullDest);
			allNodes.add(turnDest);
			visualizePath(getBot().getId(), allNodes, pair);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (!getBot().hasBallContact() && ((System.nanoTime() - expectedTimePullDone) < 0))
			{
				stateMachine.nextState(EEvent.LOST_BALL);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.PULL_BACK;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void periodicProcess(TrackedTigerBot bot, List<ACommand> cmds)
	{
		stateMachine.update();
		if ((bot.getAngle() > AngleMath.PI_HALF) && (bot.getAngle() < (AngleMath.PI_HALF * 3)))
		{
			getDevices().disarm(cmds);
		} else
		{
			getDevices().chipStop(cmds, 2000, 0);
		}
	}
	
	
	@Override
	protected List<ACommand> doCalcEntryActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		getDevices().dribble(cmds, true);
		stateMachine.update();
		return cmds;
	}
	
	
	@Override
	protected List<ACommand> doCalcExitActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		getDevices().dribble(cmds, false);
		getDevices().disarm(cmds);
		return cmds;
	}
	
	
	private SplineTrajectoryGenerator getGen(TrackedTigerBot bot)
	{
		SplineTrajectoryGenerator gen = createDefaultGenerator(bot);
		return gen;
	}
	
	
	@Override
	protected boolean isComplete(TrackedTigerBot bot)
	{
		if ((stateMachine.getCurrentState().getIdentifier() == EStateId.GET_BALL) && super.isComplete(bot))
		{
			stateMachine.nextState(EEvent.GOT_BALL);
		} else if ((stateMachine.getCurrentState().getIdentifier() == EStateId.PULL_BACK) && super.isComplete(bot))
		{
			return true;
		} else if ((stateMachine.getCurrentState().getIdentifier() == EStateId.TURN) && super.isComplete(bot))
		{
			return true;
		}
		return false;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
