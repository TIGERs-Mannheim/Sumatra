/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.10.2010
 * Author(s): DanielAl
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.rectangle.Rectanglef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole.EMoveBehavior;


/**
 * All available Robots shall move on a circle around the ball-position.
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class CheeringPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final Vector2f		center		= AIConfig.getGeometry().getCenter();
	private final Rectanglef	field			= AIConfig.getGeometry().getField();
	private final float			radius		= AIConfig.getGeometry().getBotToBallDistanceStop();
	
	private static final int	REPEAT		= 4;
	
	private int						currentStep	= 0;
	private CheeringPhase		state			= CheeringPhase.START;
	
	private enum CheeringPhase
	{
		START,
		GROW,
		ROTATE,
		CENTER,
		END
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public CheeringPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		setTimeout(Long.MAX_VALUE);
		
		float angleStep = AngleMath.PI_TWO / numAssignedRoles;
		IVector2 startOnCircle = new Vector2f(center.x(), center.y() + radius);
		for (int i = 0; i < getNumAssignedRoles(); i++)
		{
			IVector2 dest = GeoMath.stepAlongCircle(startOnCircle, center, angleStep * i);
			ARole role = new MoveRole(EMoveBehavior.NORMAL);
			addAggressiveRole(role, dest);
			role.updateLookAtTarget(center);
			role.getMoveCon().setOptimizationWanted(false);
		}
		state = CheeringPhase.GROW;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		if (checkConditions())
		{
			switch (state)
			{
				case START:
					grow(0.6f);
					state = CheeringPhase.GROW;
					break;
				case GROW:
					grow(1.5f);
					state = CheeringPhase.ROTATE;
					break;
				case ROTATE:
					rotate(AngleMath.PI_HALF);
					state = CheeringPhase.END;
					break;
				case END:
					if (currentStep < REPEAT)
					{
						state = CheeringPhase.START;
						currentStep++;
					} else
					{
						changeToFinished();
					}
					break;
				default:
					break;
			}
		}
	}
	
	
	private boolean checkConditions()
	{
		int counterTrue = 0;
		for (final ARole role : getRoles())
		{
			switch (role.checkMovementCondition())
			{
				case BLOCKED:
				case CRASHED:
				case DISABLED:
				case FULFILLED:
					counterTrue++;
					break;
				case NOT_CHECKED:
				case PENDING:
					break;
			}
		}
		if (counterTrue >= 4)
		{
			return true;
		}
		return false;
	}
	
	
	private void grow(float factor)
	{
		for (final ARole role : getRoles())
		{
			final MoveRole moveRole = (MoveRole) role;
			if (field.isPointInShape(moveRole.getPos()))
			{
				moveRole.updateDestination(moveRole.getDestination().multiplyNew(factor));
			}
		}
	}
	
	
	private void rotate(float angle)
	{
		for (final ARole role : getRoles())
		{
			final MoveRole moveRole = (MoveRole) role;
			if (field.isPointInShape(moveRole.getPos()))
			{
				IVector2 dest = GeoMath.stepAlongCircle(moveRole.getPos(), center, angle);
				moveRole.updateDestination(dest);
			}
		}
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		// nothing todo
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
