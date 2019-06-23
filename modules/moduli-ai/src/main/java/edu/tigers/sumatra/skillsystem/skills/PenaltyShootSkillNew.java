/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 9, 2014
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.sisyphus.finder.traj.ITrajPathFinder;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.TrajPathFinderInput;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.TrajPathFinderV4;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles.ObstacleGenerator;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.driver.DoNothingDriver;
import edu.tigers.sumatra.skillsystem.driver.TrajPathDriverV2;
import edu.tigers.sumatra.skillsystem.driver.VelocityDriver;
import edu.tigers.sumatra.skillsystem.driver.VelocityDriver.EVelocityMode;
import edu.tigers.sumatra.statemachine.IState;


/**
 * @author ArneS <arne.sachtler@dlr.de>
 *         this is orientated on PenaltyShootSkill but uses BangBangTrajectoryControl
 *         Move to given Destination and Orientation an shoot.
 */
public class PenaltyShootSkillNew extends AMoveSkill
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	@Configurable(comment = "Distance to ball to switch to slow move.")
	private static double			slowMoveDist				= 200;
	
	@Configurable(comment = "Velocity for slow move")
	private static double			slowMoveVel					= 0.1;
	
	@Configurable(comment = "Duration of quick rotation before Kick")
	private static double			timeToShoot					= 50;
	
	@Configurable(comment = "Rotation speed")
	private static double			rotationSpeed				= 20;
	
	@Configurable(comment = "Dribbler speed during kick")
	private static double			dribblerSpeed				= 0;
	
	@Configurable(comment = "Kick speed")
	private static double			kickSpeed					= 8;
	
	@SuppressWarnings("unused")
	private final long				timeout						= 500;
	private ERotateTrajDirection	rotateDirection			= ERotateTrajDirection.LEFT;
	
	private boolean					ready							= true;
	
	private IVector2					rememberedBallPosition	= null;
	
	
	/**
	 * @author ArneS <arne.sachtler@dlr.de>
	 */
	public enum ERotateTrajDirection
	{
		/**  */
		LEFT,
		/**  */
		RIGHT;
	}
	
	
	private enum EState
	{
		/**
		 * Move to ball in specific distance using pathplanning
		 * stay in this state while no normalStart occured
		 */
		PREPOSITIONING,
		/**
		 * slowly move in direction to ball
		 */
		SLOW_MOVE,
		/**
		 * turn and kick
		 */
		PENALTY_KICK,
		/**
		 * stop rotation and do nothing
		 */
		FINISHED;
	}
	
	private enum EEvent
	{
		/**
		 * Preposition distance reaches and normal start sent
		 */
		NORMAL_START,
		/**
		 * Moved slowly, now kick!
		 */
		BALL_REACHED,
		/**
		 * Kick performed
		 */
		KICKED;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Do not use this constructor, if you extend from this class
	 * 
	 * @param rotate
	 */
	public PenaltyShootSkillNew(final ERotateTrajDirection rotate)
	{
		super(ESkill.PENALTY_SHOOT_NEW);
		rotateDirection = rotate;
		setInitialState(new PrepositioningState());
		addTransition(EState.PREPOSITIONING, EEvent.NORMAL_START, new SlowMoveState());
		addTransition(EState.SLOW_MOVE, EEvent.BALL_REACHED, new PenaltyKickState());
		addTransition(EState.PENALTY_KICK, EEvent.KICKED, new FinishedState());
	}
	
	private class PrepositioningState implements IState
	{
		
		TrajPathFinderInput	pathFinderInput;
		ITrajPathFinder		finder		= new TrajPathFinderV4();
		TrajPathDriverV2		pathDriver;
		ObstacleGenerator		obstacleGen;
		
		final double			targetAngle	= 0.0;
		
		
		@Override
		public void doEntryActions()
		{
			pathFinderInput = new TrajPathFinderInput(getWorldFrame().getTimestamp());
			pathFinderInput.setMoveCon(getMoveCon());
			obstacleGen = new ObstacleGenerator();
			pathDriver = new TrajPathDriverV2();
			setPathDriver(pathDriver);
			rememberedBallPosition = getWorldFrame().getBall().getPos();
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			IVector2 targetPosition = rememberedBallPosition.addNew(new Vector2(-slowMoveDist, 0));
			
			if (getWorldFrame().getBot(getBotId()).getPos().equals(targetPosition, 70))
			{
				if (ready)
				{
					triggerEvent(EEvent.NORMAL_START);
				}
			} else
			{
				pathFinderInput
						.setObstacles(
								obstacleGen.generateObstacles(getWorldFrame(), getBotId(), getRefereeMsg(), getGameState()));
				pathFinderInput.setTrackedBot(getTBot());
				pathFinderInput.setDest(targetPosition);
				pathFinderInput.setTargetAngle(targetAngle);
				final TrajPathFinderInput linput = new TrajPathFinderInput(pathFinderInput, getWorldFrame().getTimestamp());
				pathDriver.setPath(finder.calcPath(linput).orElseGet(null), targetPosition, targetAngle);
			}
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			return EState.PREPOSITIONING;
		}
		
	}
	
	
	private class SlowMoveState implements IState
	{
		
		private VelocityDriver velDriver;
		
		
		@Override
		public void doEntryActions()
		{
			velDriver = new VelocityDriver();
			velDriver.setDirection(rememberedBallPosition);
			velDriver.setSpeed(slowMoveVel, 0.0);
			setPathDriver(velDriver);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			if (getTBot().hasBallContact())
			{
				velDriver.setSpeed(0.0, 0.0);
				triggerEvent(EEvent.BALL_REACHED);
			}
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			return EState.SLOW_MOVE;
		}
		
	}
	
	private class PenaltyKickState implements IState
	{
		VelocityDriver	velDriver;
		private long	time;
		
		
		@Override
		public void doEntryActions()
		{
			velDriver = new VelocityDriver(EVelocityMode.WHEEL_VELOCITY);
			velDriver.setTranslationalSpeed(0.0);
			double rotSpeed = 0;
			switch (rotateDirection)
			{
				case LEFT:
					rotSpeed = rotationSpeed;
					break;
				case RIGHT:
					rotSpeed = -rotationSpeed;
					break;
				default:
					rotSpeed = 0.0;
					throw new IllegalArgumentException();
					
			}
			velDriver.setDirection(new Vector2(1, 0));
			velDriver.setTranslationalSpeed(0.2);
			velDriver.setRotationalSpeed(rotSpeed);
			setPathDriver(velDriver);
			time = getWorldFrame().getTimestamp();
		}
		
		
		@Override
		public void doExitActions()
		{
			velDriver.setSpeed(0.0, 0.0);
		}
		
		
		@Override
		public void doUpdate()
		{
			if ((getWorldFrame().getTimestamp() - time) > (timeToShoot * 1e6))
			{
				// Kick now!
				getMatchCtrl().setDribblerSpeed(dribblerSpeed);
				getMatchCtrl().setKick(kickSpeed, EKickerDevice.STRAIGHT, EKickerMode.FORCE);
				triggerEvent(EEvent.KICKED);
			}
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			return EState.PENALTY_KICK;
		}
		
	}
	
	private class FinishedState implements IState
	{
		
		private DoNothingDriver dnd;
		
		
		@Override
		public void doEntryActions()
		{
			dnd = new DoNothingDriver();
			setPathDriver(dnd);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			return EState.FINISHED;
		}
		
	}
	
	
	/**
	 * Skill will go on and Shoot.
	 */
	public void normalStartCalled()
	{
		ready = true;
	}
	
	
	/**
	 * change ShootDirection
	 * 
	 * @param rotate
	 */
	public void setShootDirection(final ERotateTrajDirection rotate)
	{
		rotateDirection = rotate;
	}
}
