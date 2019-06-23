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

import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.field.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Linef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.EWAI;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveAndStaySkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * Defender Role for
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.KeeperPlus2DefenderPlay}<br>
 * That role is assigned exactly two times (a left and a right defender).
 * 
 * This is an old role, just used for backup
 * 
 * @author Malte
 * 
 */
public class DefenderK2DRole extends ADefenseRole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger	log						= Logger.getLogger(DefenderK2DRole.class.getName());
	
	/** Current keeper position */
	private IVector2					keeperPos;
	
	/** Direction [Goal -> protectAgainst] */
	private Vector2					direction;
	
	/** Left or Right defender? */
	private final EWAI				type;
	
	/** Where to go? */
	private final Vector2			destination;
	
	/** 'vertical' gap between defender and keeper */
	private final float				SPACE_BEFORE_KEEPER	= AIConfig.getRoles().getDefenderK2D().getSpaceBeforeKeeper();
	
	/** 'horizontal' gap between defender and keeper */
	private final float				SPACE_BESIDE_KEEPER	= AIConfig.getRoles().getDefenderK2D().getSpaceBesideKeeper();
	
	private boolean					criticalAngle			= false;
	
	private Vector2					target					= new Vector2(GeoMath.INIT_VECTOR);
	private final Vector2f			goalCenterTheir		= AIConfig.getGeometry().getGoalTheir().getGoalCenter();
	
	private final Goal				goal						= AIConfig.getGeometry().getGoalOur();
	
	private enum EStateId
	{
		NORMAL
	}
	
	private enum EEvent
	{
		DONE
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Default constructor
	 */
	public DefenderK2DRole()
	{
		this(EWAI.LEFT);
	}
	
	
	/**
	 * @param type
	 */
	public DefenderK2DRole(EWAI type)
	{
		super(ERole.DEFENDER_K2D, false, true);
		
		setInitialState(new NormalDefendState());
		addEndTransition(EStateId.NORMAL, EEvent.DONE);
		this.type = type;
		keeperPos = GeoMath.INIT_VECTOR;
		
		destination = new Vector2(GeoMath.INIT_VECTOR);
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
	public void updateMoveCon(AIInfoFrame currentFrame)
	{
		
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
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- InnerClass --------------------------------------------------------
	// --------------------------------------------------------------------------
	private class NormalDefendState implements IRoleState
	{
		
		@Override
		public void onSkillStarted(ASkill skill, BotID botID)
		{
			
		}
		
		
		@Override
		public void onSkillCompleted(ASkill skill, BotID botID)
		{
			
		}
		
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(new MoveAndStaySkill(getMoveCon()));
		}
		
		
		@Override
		public void doExitActions()
		{
			
		}
		
		
		@Override
		public void doUpdate()
		{
			final IVector2 protectAgainst = getAiFrame().worldFrame.ball.getPos();
			
			if (!getAiFrame().tacticalInfo.isBallInOurPenArea())
			{
				if (!criticalAngle)
				{
					// Wird der ball auf unser Tor geschossen?
					final TrackedBall ball = getAiFrame().worldFrame.ball;
					Vector2f interceptPoint;
					if (ball.getVel().x() < -0.3)
					{
						final Linef ballShootLine = new Linef(ball.getPos(), ball.getVel());
						try
						{
							interceptPoint = new Vector2f(GeoMath.intersectionPoint(ballShootLine,
									new Line(goal.getGoalCenter(), AVector2.Y_AXIS)));
						} catch (final MathException err)
						{
							interceptPoint = new Vector2f(0, 99999);
						}
						
						// Ball wird auf unser Tor geschossen!
						
						if ((interceptPoint.y() < (goal.getGoalPostLeft().y() + 50))
								&& (interceptPoint.y() > (goal.getGoalPostRight().y() - 50)))
						{
							// TODO PhilippP maybe eigenenState mit block
							destination.set(GeoMath.leadPointOnLine(getPos(), ballShootLine));
						}
					} else
					{
						// First we take keeperPos at start Position
						destination.set(keeperPos);
						
						direction = protectAgainst.subtractNew(keeperPos);
						direction.scaleTo(SPACE_BEFORE_KEEPER);
						destination.add(direction);
						direction.turn(AngleMath.PI_HALF);
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
					direction.turn(AngleMath.PI_HALF);
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
					target = new Vector2(getAiFrame().worldFrame.ball.getPos());
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
				direction.turn(AngleMath.PI_HALF);
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
			updateDestination(destination);
			updateLookAtTarget(target);
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.NORMAL;
		}
	}
	
}
