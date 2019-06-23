/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.11.2010
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense;

import java.awt.Color;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circlef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.ICircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Linef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ATrackedObject;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveAndStaySkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * Man to Man Marker ("Manndecker").<br>
 * In the second constructor you can pass a value, which defines
 * the maximum height the bot is allowed to go on the field.
 * The vertical line defined by that x-value must not be overrun.<br>
 * Used by: {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.support.ManToManMarkerPlay}
 * 
 * @author Malte
 * 
 */
public class ManToManMarkerRole extends ADefenseRole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/** the maximum height the bot is allowed to go */
	private float		maxLength;
	
	/** gap between bot and target */
	private float		gap;
	/** target foe bot, this role should mark */
	private IVector2	target;
	/** this role may not enter this forbidden circle */
	private Circlef	forbiddenCircle;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Default constructor
	 */
	public ManToManMarkerRole()
	{
		this(Vector2.ZERO_VECTOR);
	}
	
	
	/**
	 * @param target
	 */
	public ManToManMarkerRole(final IVector2 target)
	{
		super(ERole.MAN_TO_MAN_MARKER, false, true);
		
		// the maximum height the bot is allowed to go
		maxLength = AIConfig.getGeometry().getCenter().x();
		this.target = target;
		gap = AIConfig.getGeometry().getBotRadius() * 3;
		
		setInitialState(new DefendState());
		addEndTransition(EStateId.DEFEND, EEvent.DONE);
	}
	
	// --------------------------------------------------------------------------
	// --- states ---------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private enum EStateId
	{
		DEFEND
	}
	
	private enum EEvent
	{
		DONE
	}
	
	private class DefendState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(new MoveAndStaySkill(getMoveCon()));
		}
		
		
		@Override
		public void doUpdate()
		{
			final float fieldWidth = AIConfig.getGeometry().getFieldWidth();
			// line defined by 2 points: target and our goal middle
			final Linef shootLine = new Linef(target, AIConfig.getGeometry().getGoalOur().getGoalCenter()
					.subtractNew(target));
			// bot may not pass this line towards our field half
			final Line criticalLine = new Line(new Vector2(maxLength, -fieldWidth / 2),
					AVector2.Y_AXIS.scaleToNew(fieldWidth));
			getAiFrame().addDebugShape(new DrawableLine(shootLine, Color.blue));
			getAiFrame().addDebugShape(new DrawableLine(criticalLine, Color.red, false));
			
			// vector from our goal to the enemy bot.
			final Vector2 direction = new Vector2(shootLine.directionVector()).multiply(-1);
			// stand a little bit in front of
			direction.scaleTo(direction.getLength2() - gap);
			
			Vector2 destination = AIConfig.getGeometry().getGoalOur().getGoalCenter().addNew(direction);
			
			// if the target is beyond the critical line..
			if (destination.x > maxLength)
			{
				try
				{
					destination = GeoMath.intersectionPoint(criticalLine, shootLine);
				} catch (final MathException err)
				{
					destination.x = maxLength;
				}
			}
			
			// if destination in PenaltyArea, set nearPointOutsite
			
			if (AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(destination))
			{
				destination = new Vector2(AIConfig.getGeometry().getPenaltyAreaOur().nearestPointOutside(destination));
			}
			
			// Checks if the destination is inside the forbidden circle.
			if ((forbiddenCircle != null) && forbiddenCircle.isPointInShape(destination))
			{
				final IVector2 posOutside;
				List<IVector2> intersectionPts = GeoMath.lineCircleIntersections(shootLine, forbiddenCircle);
				if (intersectionPts.isEmpty())
				{
					throw new IllegalStateException(
							"You tell me, destination is in circle, but there is no intersection from dest to goal?!");
				} else if (intersectionPts.size() == 1)
				{
					posOutside = intersectionPts.get(0);
				} else if (intersectionPts.get(0).x() < intersectionPts.get(1).x())
				{
					posOutside = intersectionPts.get(0);
				} else
				{
					posOutside = intersectionPts.get(1);
				}
				destination = new Vector2(posOutside);
				
				getAiFrame().addDebugShape(new DrawableCircle(forbiddenCircle));
			}
			
			updateDestination(destination);
			updateLookAtTarget(target);
		}
		
		
		@Override
		public void doExitActions()
		{
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
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.DEFEND;
		}
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * The enemy is set who shall by covered by this role.
	 * @param target
	 */
	public void updateTarget(ATrackedObject target)
	{
		updateTarget(target.getPos());
	}
	
	
	/**
	 * @param target
	 */
	public void updateTarget(IVector2 target)
	{
		this.target = target;
	}
	
	
	@Override
	protected void updateMoveCon(AIInfoFrame aiFrame)
	{
		final float fieldWidth = AIConfig.getGeometry().getFieldWidth();
		// line defined by 2 points: target and our goal middle
		final Linef shootLine = new Linef(target, AIConfig.getGeometry().getGoalOur().getGoalCenter().subtractNew(target));
		// bot may not pass this line towards our field half
		final Line criticalLine = new Line(new Vector2(maxLength, -fieldWidth / 2),
				AVector2.Y_AXIS.scaleToNew(fieldWidth));
		aiFrame.addDebugShape(new DrawableLine(shootLine, Color.blue));
		aiFrame.addDebugShape(new DrawableLine(criticalLine, Color.red, false));
		
		// vector from our goal to the enemy bot.
		final Vector2 direction = new Vector2(shootLine.directionVector()).multiply(-1);
		// stand a little bit in front of
		direction.scaleTo(direction.getLength2() - gap);
		
		Vector2 destination = AIConfig.getGeometry().getGoalOur().getGoalCenter().addNew(direction);
		
		// if the target is beyond the critical line..
		if (destination.x > maxLength)
		{
			try
			{
				destination = GeoMath.intersectionPoint(criticalLine, shootLine);
			} catch (final MathException err)
			{
				destination.x = maxLength;
			}
		}
		
		// if
		
		// Checks if the destination is inside the forbidden circle.
		if ((forbiddenCircle != null) && forbiddenCircle.isPointInShape(destination))
		{
			final IVector2 posOutside;
			List<IVector2> intersectionPts = GeoMath.lineCircleIntersections(shootLine, forbiddenCircle);
			if (intersectionPts.isEmpty())
			{
				throw new IllegalStateException(
						"You tell me, destination is in circle, but there is no intersection from dest to goal?!");
			} else if (intersectionPts.size() == 1)
			{
				posOutside = intersectionPts.get(0);
			} else if (intersectionPts.get(0).x() < intersectionPts.get(1).x())
			{
				posOutside = intersectionPts.get(0);
			} else
			{
				posOutside = intersectionPts.get(1);
			}
			destination = new Vector2(posOutside);
			
			aiFrame.addDebugShape(new DrawableCircle(forbiddenCircle));
		}
		
		updateDestination(destination);
		updateLookAtTarget(target);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Set forbiddenCircle. Bot radius will be considered, so do not add it your self!
	 * 
	 * @param forbiddenCircle
	 */
	public void setForbiddenCircle(ICircle forbiddenCircle)
	{
		final Circle circle = new Circle(forbiddenCircle.center(), forbiddenCircle.radius()
				+ AIConfig.getGeometry().getBotRadius());
		this.forbiddenCircle = new Circlef(circle);
	}
	
	
	/**
	 * gap between bot and target
	 * 
	 * @param gap
	 */
	public void setGap(float gap)
	{
		this.gap = gap;
	}
	
	
	/**
	 * @return
	 */
	public float getMaxLength()
	{
		return maxLength;
	}
	
	
	/**
	 * the maximum height the bot is allowed to go
	 * 
	 * @param maxLength
	 */
	public void setMaxLength(float maxLength)
	{
		this.maxLength = maxLength;
	}
	
	
	/**
	 * @return
	 */
	public IVector2 getTarget()
	{
		return target;
	}
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
	}
}
