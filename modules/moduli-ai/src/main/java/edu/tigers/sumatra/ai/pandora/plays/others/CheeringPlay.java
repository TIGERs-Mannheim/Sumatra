/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.10.2010
 * Author(s): DanielAl
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays.others;

import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole.EMoveBehavior;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2f;
import edu.tigers.sumatra.shapes.rectangle.Rectangle;
import edu.tigers.sumatra.wp.data.Geometry;


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
	
	private final IVector2	center				= Geometry.getCenter();
	private final Rectangle	field					= Geometry.getField();
	private final double		radius				= Geometry.getBotToBallDistanceStop();
	
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
					state = CheeringPhase.START;
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
			if (moveRole.isDestinationReached())
			{
				counterTrue++;
			}
		}
		if (counterTrue >= (getRoles().size() - 1))
		{
			return true;
		}
		return false;
	}
	
	
	private void grow(final double factor)
	{
		for (final ARole role : getRoles())
		{
			final MoveRole moveRole = (MoveRole) role;
			if (field.isPointInShape(moveRole.getPos()))
			{
				moveRole.getMoveCon().updateDestination(
						moveRole.getMoveCon().getDestination().multiplyNew(factor));
			}
		}
	}
	
	
	private void rotate(final double angle)
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
	protected ARole onRemoveRole(final MetisAiFrame frame)
	{
		ARole role = getLastRole();
		return role;
	}
	
	
	@Override
	protected ARole onAddRole(final MetisAiFrame frame)
	{
		MoveRole newRole = new MoveRole(EMoveBehavior.NORMAL);
		newRole.getMoveCon().setBallObstacle(false);
		newRole.getMoveCon().setRefereeStop(true);
		return newRole;
	}
	
	
	@Override
	protected void onGameStateChanged(final EGameStateTeam gameState)
	{
	}
	
	
	private void updateRoles()
	{
		double angleStep = AngleMath.PI_TWO / getRoles().size();
		IVector2 startOnCircle = new Vector2f(center.x(), center.y() + radius);
		int i = 0;
		for (ARole role : getRoles())
		{
			MoveRole moveRole = (MoveRole) role;
			IVector2 dest = GeoMath.stepAlongCircle(startOnCircle, center, angleStep * i);
			moveRole.getMoveCon().updateDestination(dest);
			moveRole.getMoveCon().updateLookAtTarget(center);
			i++;
		}
		state = CheeringPhase.GROW;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
