/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.10.2010
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move;

import java.awt.Color;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawablePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition.EConditionState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveAndStaySkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * <pre>
 * ----o
 * ---o
 * --o <--r--> Ball
 * ---o
 * ----o
 * 
 * |x - center| = radius   <-- circ equation
 * 
 * <pre>
 * 
 * Bots will always look to Ball!
 * 
 * @author Malte
 * 
 */
public class MoveWithDistanceToPointRole extends ARole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private enum EStateId
	{
		MOVING;
	}
	
	private enum EEvent
	{
		DONE
	}
	
	/** position of the object that is surrounded by the bots */
	private IVector2				center;
	/** radius of the circle */
	private float					radius;
	/** direction center - bot */
	private Vector2				direction;
	
	private final IRoleState	movingState	= new MovingState();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Default constructor
	 */
	public MoveWithDistanceToPointRole()
	{
		this(Vector2.ZERO_VECTOR, AIConfig.getGeometry().getBotToBallDistanceStop(), new Vector2(-1, 0));
	}
	
	
	/**
	 * 
	 * @param center
	 * @param radius
	 * @param direction
	 */
	public MoveWithDistanceToPointRole(IVector2 center, float radius, IVector2 direction)
	{
		super(ERole.MOVE_DISTANCE_BALL);
		updateCirclePos(center, radius, direction);
		setInitialState(movingState);
		addEndTransition(EStateId.MOVING, EEvent.DONE);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Updates center, radius and direction for the MoveRole.
	 * 
	 * @param center
	 * @param radius
	 * @param direction
	 */
	public final void updateCirclePos(IVector2 center, float radius, IVector2 direction)
	{
		this.center = center;
		this.radius = radius;
		this.direction = new Vector2(direction);
	}
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
		// only needed MOVING and this is already set in ARole
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Moves the bot
	 * @author Daniel Andres <andreslopez.daniel@gmail.com>
	 * 
	 */
	public class MovingState implements IRoleState
	{
		private static final float	ANGLE_CORRECTION	= 0.05f;
		
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(new MoveAndStaySkill(getMoveCon()));
		}
		
		
		@Override
		public void onSkillStarted(ASkill skill, BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(ASkill skill, BotID botID)
		{
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			updateDest(direction);
			updateLookAtTarget(getAiFrame().worldFrame.ball.getPos());
			
			float curAngleCor = 0;
			while (checkMovementCondition() == EConditionState.BLOCKED)
			{
				updateDest(direction.turnNew(curAngleCor));
				curAngleCor -= (2 * curAngleCor) - (curAngleCor <= 0 ? ANGLE_CORRECTION : 0);
			}
			
			getAiFrame().addDebugShape(new DrawableCircle(new Circle(center, radius), Color.blue));
			getAiFrame().addDebugShape(new DrawablePoint(getDestination()));
		}
		
		
		@Override
		public EStateId getIdentifier()
		{
			return EStateId.MOVING;
		}
		
		
		private void updateDest(IVector2 direction)
		{
			// adds the vector to the center
			Vector2 destination = center.addNew(direction.scaleToNew(radius));
			updateDestination(destination);
		}
		
		// --------------------------------------------------------------------------
		// --- getter/setter --------------------------------------------------------
		// --------------------------------------------------------------------------
	}
}
