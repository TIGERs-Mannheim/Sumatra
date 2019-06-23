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

import edu.dhbw.mannheim.tigers.sumatra.model.data.ATrackedObject;
import edu.dhbw.mannheim.tigers.sumatra.model.data.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Circlef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.ICircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Linef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.ManToManMarkerPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.EWAI;


/**
 * Man to Man Marker ("Manndecker").<br>
 * In the second constructor you can pass a value, which defines
 * the maximum height the bot is allowed to go on the field.
 * The vertical line defined by that x-value must not be overrun.<br>
 * Used by: {@link ManToManMarkerPlay}, {@link KickOfThemMarkerPlay}
 * 
 * @author Malte
 * 
 */
public class ManToManMarkerRole extends ADefenseRole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= 6965155468894095085L;
	
	private final Goal			goal					= AIConfig.getGeometry().getGoalOur();
	
	private final Vector2		target;
	
	// the maximum height to bot is allowed to go
	private float					maxLength;
	
	/** gap between bot and target */
	private float					gap;
	
	private Circlef				forbiddenCircle;
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public ManToManMarkerRole(EWAI type)
	{
		super(ERole.MAN_TO_MAN_MARKER);
		
		target = new Vector2();
		
		// everything is allowed!
		forbiddenCircle = null;
		maxLength = 42000;
		gap = AIConfig.getGeometry().getBotRadius() * 3;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	/**
	 * The enemy is set who shall by covered by this role.
	 */
	public void updateTarget(ATrackedObject target)
	{
		updateTarget(target.pos);
	}
	

	public void updateTarget(IVector2 target)
	{
		this.target.set(target);
	}
	

	@Override
	public void update(AIInfoFrame currentFrame)
	{
		// line defined by 2 points: target and our goal middle
		final Linef shootLine = new Linef(target, target.subtractNew(goal.getGoalCenter()));
		
		// vector from our goal to the enemy bot.
		final Vector2 direction = new Vector2(shootLine.directionVector());
		// stand a little bit in front of
		direction.scaleTo(direction.getLength2() - gap);
		
		Vector2 destination = goal.getGoalCenter().addNew(direction);
		
		// if the target is beyond the critical line..
		if (destination.x > maxLength)
		{
			Line verticalLine = new Line(new Vector2(maxLength - 150, 0), AVector2.Y_AXIS);
			
			try
			{
				destination = AIMath.intersectionPoint(verticalLine, shootLine);
			} catch (MathException err)
			{
				destination = null;
			}
		}
		
		if(destination.x > -100)
		{
			destination.x = -100;
		}
		
		// Checks if the destination is inside the forbidden circle.
		if (forbiddenCircle != null && forbiddenCircle.isPointInShape(destination))
		{
			Circle tmp = new Circle(forbiddenCircle.center(), forbiddenCircle.radius()
					+ AIConfig.getGeometry().getBotRadius());
			destination = tmp.nearestPointOutside(destination);
		}
		
		if(AIConfig.getGeometry().getFakeOurPenArea().isPointInShape(destination))
		{
			destination = AIConfig.getGeometry().getFakeOurPenArea().nearestPointOutside(destination);
		}
		
		destCon.updateDestination(destination);
		lookAtCon.updateTarget(target);
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public void setForbiddenCircle(ICircle forbiddenCircle)
	{
		this.forbiddenCircle = new Circlef(forbiddenCircle);
	}
	

	public void setGap(float gap)
	{
		this.gap = gap;
	}
	

	public float getMaxLength()
	{
		return maxLength;
	}
	

	public void setMaxLength(float maxLength)
	{
		this.maxLength = maxLength;
	}
	
	
	public IVector2 getTarget()
	{
		return target;
	}
}
