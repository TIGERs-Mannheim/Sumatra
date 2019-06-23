/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.11.2010
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense;

import edu.dhbw.mannheim.tigers.sumatra.model.data.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Linef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.KeeperPlus2DefenderPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.EWAI;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillFacade;


/**
 * Defender Role for {@link KeeperPlus2DefenderPlay}<br>
 * That role is assigned exactly two times (a left and a right defender).
 * 
 * @author Malte
 * 
 */
public class DefenderK2DRole extends ADefenseRole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long	serialVersionUID		= 6240720493178226795L;
	
	/** Current keeper position */
	private IVector2				keeperPos;
	
	/** Direction [Goal -> protectAgainst] */
	private Vector2				direction;
	
	/** Left or Right defender? */
	private final EWAI			type;
	
	/** Where to go? */
	private final Vector2		destination;
	
	/** 'vertical' gap between defender and keeper */
	private final float			SPACE_BEFORE_KEEPER	= AIConfig.getRoles().getDefenderK2D().getSpaceBeforeKeeper();
	
	/** 'horizontal' gap between defender and keeper */
	private final float			SPACE_BESIDE_KEEPER	= AIConfig.getRoles().getDefenderK2D().getSpaceBesideKeeper();
	
	private boolean				criticalAngle			= false;
	
	private Vector2				target					= new Vector2(AIConfig.INIT_VECTOR);
	private Vector2f				goalCenterTheir		= AIConfig.getGeometry().getGoalTheir().getGoalCenter();
	
	private Goal goal = AIConfig.getGeometry().getGoalOur();
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public DefenderK2DRole(EWAI type)
	{
		super(ERole.DEFENDER_K2D);
		this.type = type;
		keeperPos = AIConfig.INIT_VECTOR;
		
		destination = new Vector2(AIConfig.INIT_VECTOR);
		direction = new Vector2();
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * New keeper Position is set here.
	 * This is needed because all calculation for this role
	 * are based on the current keeper position.
	 * 
	 * @param keeperPos
	 */
	public void updateKeeperPos(IVector2 keeperPos)
	{
		this.keeperPos = keeperPos;
	}
	

	@Override
	public void update(AIInfoFrame currentFrame)
	{
		Vector2f protectAgainst = currentFrame.worldFrame.ball.pos;
		
		if (!currentFrame.tacticalInfo.isBallInOurPenArea())
		{
			if (!criticalAngle)
			{
				//Wird der ball auf unser Tor geschossen?
				TrackedBall ball = currentFrame.worldFrame.ball;
				Vector2f interceptPoint;
				if(ball.vel.x() < -0.3)
				{
					Linef ballShootLine = new Linef(ball.pos, ball.vel);

					try
					{
						interceptPoint = new Vector2f(AIMath.intersectionPoint(ballShootLine,
								new Line(goal.getGoalCenter(), AVector2.Y_AXIS)));
					} catch (MathException err)
					{
						interceptPoint = new Vector2f(0, 99999);
					}
					
					//Ball wird auf unser Tor geschossen!
					
					if(interceptPoint.y() <  goal.getGoalPostLeft().y()+50
						&& interceptPoint.y() >  goal.getGoalPostRight().y()-50)
					{
					destination.set(AIMath.leadPointOnLine(this.getPos(currentFrame), ballShootLine));
					}
				}
				else{
				// First we take keeperPos at start Position
					destination.set(keeperPos);
					
					direction = protectAgainst.subtractNew(keeperPos);
					direction.scaleTo(SPACE_BEFORE_KEEPER);
					destination.add(direction);
					direction.turn(AIMath.PI_HALF);
					direction.scaleTo(SPACE_BESIDE_KEEPER);
					

					if (type == EWAI.RIGHT)
					{
						destination.subtract(direction);
					} else if (type == EWAI.LEFT)
					{
						destination.add(direction);
					} else
					{
						log.warn("Role called with non-suiting EWAI");
					}
				}

				target = new Vector2(goalCenterTheir);

			} else
			{
				// First we take keeperPos at start Position
				destination.set(keeperPos);
				
				direction = protectAgainst.subtractNew(keeperPos);
				direction.scaleTo(SPACE_BEFORE_KEEPER);
				destination.add(direction);
				direction.turn(AIMath.PI_HALF);
				direction.scaleTo(SPACE_BESIDE_KEEPER - 100);
				

				if (type == EWAI.RIGHT)
				{
					destination.subtract(direction);
				} else if (type == EWAI.LEFT)
				{
					destination.add(direction);
				} else
				{
					log.warn("Role called with non-suiting EWAI");
				}
				target = new Vector2(currentFrame.worldFrame.ball.pos);
			}
			
			
		}

		// Ball is inside our penalty area
		else
		{
			// First we take keeperPos at start Position
			destination.set(keeperPos);
			
			direction = protectAgainst.subtractNew(keeperPos);
			direction.scaleTo(SPACE_BEFORE_KEEPER);
			// HERE IT IS SUBTRACT
			destination.subtract(direction);
			direction.turn(AIMath.PI_HALF);
			direction.scaleTo(SPACE_BESIDE_KEEPER);
			

			if (type == EWAI.RIGHT)
			{
				destination.subtract(direction);
			} else if (type == EWAI.LEFT)
			{
				destination.add(direction);
			} else
			{
				log.warn("Role called with non-suiting EWAI");
			}
		}
		destCon.updateDestination(destination);
		lookAtCon.updateTarget(target);
	}
	

	@Override
	public void calculateSkills(WorldFrame wFrame, SkillFacade skills)
	{
		super.calculateSkills(wFrame, skills);
		float angle = wFrame.tigerBots.get(getBotID()).angle;
		if (angle < AIMath.PI_QUART && angle > -AIMath.PI_QUART)
		{
			skills.kickArm();
		} else
		{
			skills.disarm();
		}
	}
	

	/**
	 * @param criticalAngle the criticalAngle to set
	 */
	public void setCriticalAngle(boolean criticalAngle)
	{
		this.criticalAngle = criticalAngle;
	}
	

	/**
	 * @return the criticalAngle
	 */
	public boolean isCriticalAngle()
	{
		return criticalAngle;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
