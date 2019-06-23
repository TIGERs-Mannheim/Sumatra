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

import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.skills.test.PositionSkill;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * Move to a given destination and orientation with PositionController
 * and make an epic shoot :P
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class PenaltyShootSkill extends PositionSkill
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private EState					state									= EState.Prepositioning_1;
	
	/**
	 * This value adjusts the angle to shoot but to low or to high number
	 * will cause the skill to fail, so be very careful here.
	 */
	@Configurable(comment = "This value adjusts the angle to shoot but to low or to high number will cause the skill to fail, so be very careful here.")
	private static long			timeToShoot							= 50;
	
	@Configurable(comment = "dribbleSpeed")
	private static int			dribbleSpeed						= 0;
	
	@Configurable(comment = "correction Distance: distance to ball")
	private static double		correctionDist						= 0;
	
	@Configurable(comment = "speed to move closer to the ball, big numbers mean slower movement")
	private static long			stepTimeForSlowMove				= 150;
	
	private final long			timeout								= 500;
	private long					time									= 0;
	private ERotateDirection	rotateDirection					= ERotateDirection.LEFT;
	
	private long					stepTimeCounter					= 0;
	
	@Configurable
	private static double		slowMoveDist						= 200;
	
	private double					slowMoveDistCounter				= 0;
	
	private boolean				ready									= false;
	private boolean				normalStartBeforePosReached	= false;
	
	private double					earlyNormalStartFix				= 0;
	
	
	private IVector2				ballPos								= null;
	
	/**
	 * @author MarkG
	 */
	public enum ERotateDirection
	{
		/**  */
		LEFT,
		/**  */
		RIGHT;
	}
	
	private enum EState
	{
		Prepositioning_1,
		Prepositioning_2,
		Turn,
		WRAPPER_TMP;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Do not use this constructor, if you extend from this class
	 * 
	 * @param rotate
	 */
	public PenaltyShootSkill(final ERotateDirection rotate)
	{
		super(ESkill.PENALTY_SHOOT);
		rotateDirection = rotate;
		slowMoveDistCounter = slowMoveDist;
		setInitialState(new WrapperTmpState());
	}
	
	
	private class WrapperTmpState implements IState
	{
		
		@Override
		public void doEntryActions()
		{
			stepTimeCounter = getWorldFrame().getTimestamp();
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			IVector2 destination = null;
			IVector2 orientation = new Vector2(0, 0);
			
			if (ready && (slowMoveDistCounter > 0))
			{
				earlyNormalStartFix = 20;
				normalStartBeforePosReached = true;
			}
			if (ready && (slowMoveDistCounter <= 0))
			{
				if (normalStartBeforePosReached)
				{
					if (slowMoveDistCounter <= (0 - earlyNormalStartFix))
					{
						state = EState.Turn;
					}
				} else
				{
					state = EState.Turn;
				}
			}
			
			switch (state)
			{
				case Prepositioning_1:
					destination = getWorldFrame().getBall().getPos().addNew(new Vector2(-200, 0));
					if (GeoMath.distancePP(destination, getPos()) < 70)
					{
						state = EState.Prepositioning_2;
					}
					orientation = ballPos.subtractNew(getPos());
					break;
				case Prepositioning_2:
					if ((slowMoveDistCounter > (0 - earlyNormalStartFix))
							&& ((getWorldFrame().getTimestamp() - stepTimeCounter) > stepTimeForSlowMove))
					{
						stepTimeCounter = getWorldFrame().getTimestamp();
						slowMoveDistCounter = slowMoveDistCounter - 3;
					} else if (slowMoveDistCounter < (0 - earlyNormalStartFix))
					{
						slowMoveDistCounter = 0 - earlyNormalStartFix;
					}
					
					destination = getWorldFrame()
							.getBall()
							.getPos()
							.addNew(
									new Vector2(
											(-getBot().getCenter2DribblerDist()
													- Geometry.getBallRadius() - correctionDist - slowMoveDistCounter),
											0));
					orientation = ballPos.subtractNew(getPos());
					
					
					break;
				case Turn:
					switch (rotateDirection)
					{
						case LEFT:
							orientation = ballPos.addNew(new Vector2(0, 100)).subtractNew(getPos())
									.multiplyNew(-1);
							break;
						case RIGHT:
							orientation = ballPos.addNew(new Vector2(0, -100))
									.subtractNew(getPos())
									.multiplyNew(-1);
							break;
					}
					destination = getPos().addNew(new Vector2(50, 0));
					if (time == 0)
					{
						time = getWorldFrame().getTimestamp();
					} else if ((getWorldFrame().getTimestamp() - time) > timeout)
					{
						ready = false;
						state = EState.Prepositioning_1;
						return;
					} else if ((getWorldFrame().getTimestamp() - time) > timeToShoot)
					{
						getMatchCtrl().setKick(8, EKickerDevice.STRAIGHT, EKickerMode.FORCE);
						getMatchCtrl().setDribblerSpeed(dribbleSpeed);
					}
					break;
				case WRAPPER_TMP:
					throw new IllegalStateException();
			}
			
			setDestination(destination);
			setOrientation(orientation.getAngle());
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			return EState.WRAPPER_TMP;
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
	public void setShootDirection(final ERotateDirection rotate)
	{
		rotateDirection = rotate;
	}
	
	
	@Override
	protected void beforeStateUpdate()
	{
		super.beforeStateUpdate();
		
		if (ballPos == null)
		{
			ballPos = getWorldFrame().getBall().getPos();
		}
	}
}
