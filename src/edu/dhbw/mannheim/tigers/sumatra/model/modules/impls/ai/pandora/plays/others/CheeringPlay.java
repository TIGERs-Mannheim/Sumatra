/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.10.2010
 * Author(s): DanielAl
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.rectangle.Rectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition.EConditionState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole.EMoveBehavior;


/**
 * All available Robots shall move on a circle around the ball-position.
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class CheeringPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final Vector2f	center				= AIConfig.getGeometry().getCenter();
	private final Rectangle	field					= AIConfig.getGeometry().getField();
	private final float		radius				= AIConfig.getGeometry().getBotToBallDistanceStop();
	
	// private static final int REPEAT = 4;
	// private int currentStep = 0;
	private CheeringPhase	state					= CheeringPhase.START;
	
	private int					numRolesLastTime	= 0;
	
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
	 */
	public CheeringPlay()
	{
		super(EPlay.CHEERING);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected void doUpdate(final AthenaAiFrame currentFrame)
	{
		if (numRolesLastTime != getRoles().size())
		{
			updateRoles();
			numRolesLastTime = getRoles().size();
		}
		
		if (checkConditions(currentFrame))
		{
			switch (state)
			{
				case START:
					grow(0.5f);
					state = CheeringPhase.GROW;
					break;
				case GROW:
					grow(2.0f);
					state = CheeringPhase.ROTATE;
					break;
				case ROTATE:
					rotate(AngleMath.PI_HALF);
					state = CheeringPhase.END;
					break;
				case END:
					// if (currentStep < REPEAT)
					// {
					state = CheeringPhase.START;
					// currentStep++;
					// }
					// else
					// {
					// changeToFinished();
					// }
					break;
				default:
					break;
			}
		}
	}
	
	
	private boolean checkConditions(final AthenaAiFrame frame)
	{
		int counterTrue = 0;
		for (final ARole role : getRoles())
		{
			MoveRole moveRole = (MoveRole) role;
			if (moveRole.getMoveCon().checkCondition(frame.getWorldFrame(), role.getBotID()) == EConditionState.FULFILLED)
			{
				counterTrue++;
			}
		}
		if (counterTrue >= getRoles().size())
		{
			return true;
		}
		return false;
	}
	
	
	private void grow(final float factor)
	{
		for (final ARole role : getRoles())
		{
			final MoveRole moveRole = (MoveRole) role;
			if (field.isPointInShape(moveRole.getPos()))
			{
				moveRole.getMoveCon().updateDestination(
						moveRole.getMoveCon().getDestCon().getDestination().multiplyNew(factor));
			}
		}
	}
	
	
	private void rotate(final float angle)
	{
		for (final ARole role : getRoles())
		{
			final MoveRole moveRole = (MoveRole) role;
			if (field.isPointInShape(moveRole.getPos()))
			{
				IVector2 dest = GeoMath.stepAlongCircle(moveRole.getPos(), center, angle);
				moveRole.getMoveCon().updateDestination(dest);
			}
		}
	}
	
	
	@Override
	protected ARole onRemoveRole()
	{
		ARole role = getLastRole();
		return role;
	}
	
	
	@Override
	protected ARole onAddRole()
	{
		ARole newRole = new MoveRole(EMoveBehavior.NORMAL);
		return newRole;
	}
	
	
	@Override
	protected void onGameStateChanged(final EGameState gameState)
	{
	}
	
	
	private void updateRoles()
	{
		float angleStep = AngleMath.PI_TWO / getRoles().size();
		IVector2 startOnCircle = new Vector2f(center.x(), center.y() + radius);
		int i = 0;
		for (ARole role : getRoles())
		{
			MoveRole moveRole = (MoveRole) role;
			IVector2 dest = GeoMath.stepAlongCircle(startOnCircle, center, angleStep * i);
			moveRole.getMoveCon().updateDestination(dest);
			moveRole.getMoveCon().updateLookAtTarget(center);
			moveRole.getMoveCon().setOptimizationWanted(false);
			i++;
		}
		state = CheeringPhase.GROW;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
