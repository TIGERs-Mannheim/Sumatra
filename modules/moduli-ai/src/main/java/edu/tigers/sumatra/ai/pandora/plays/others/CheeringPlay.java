/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.others;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.CircleMath;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * All available Robots shall move on a circle around the ball-position.
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class CheeringPlay extends APlay
{
	private final IVector2 center = Geometry.getCenter();
	private final IRectangle field = Geometry.getField();
	private final double radius = Geometry.getCenterCircle().radius();
	
	private CheeringPhase state = CheeringPhase.START;
	
	private int numRolesLastTime = 0;
	
	
	private enum CheeringPhase
	{
		START,
		GROW,
		ROTATE,
		CENTER,
		END,
		SHRINK,
		ROTATEAGAIN,
		BACKTOMID
	}
	
	
	/**
	 * Default
	 */
	public CheeringPlay()
	{
		super(EPlay.CHEERING);
	}
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame currentFrame)
	{
		if (getRoles().size() < 3)
		{
			return; // minimum bots 3
		}
		
		if (numRolesLastTime != getRoles().size())
		{
			updateRoles();
			numRolesLastTime = getRoles().size();
		}
		
		performAction();
	}
	
	
	private void performAction()
	{
		final int direction = 1;
		if (checkConditions())
		{
			switch (state)
			{
				case START:
					setRadius(1f);
					state = CheeringPhase.ROTATE;
					break;
				case ROTATE:
					rotate((direction * AngleMath.PI_TWO) / getRoles().size());
					state = CheeringPhase.GROW;
					break;
				case GROW:
					setRadius(1.5f);
					state = CheeringPhase.BACKTOMID;
					break;
				case BACKTOMID:
					setRadius(2f);
					state = CheeringPhase.ROTATEAGAIN;
					break;
				case ROTATEAGAIN:
					rotate((direction * AngleMath.PI_TWO) / getRoles().size());
					state = CheeringPhase.SHRINK;
					break;
				case SHRINK:
					setRadius(0.5f);
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
	
	
	private boolean checkConditions()
	{
		int counterTrue = 0;
		for (final ARole role : getRoles())
		{
			MoveRole moveRole = (MoveRole) role;
			if (moveRole.getMoveCon().getDestination() != null
					&& moveRole.getPos().isCloseTo(moveRole.getMoveCon().getDestination(), 100))
			{
				counterTrue++;
			}
		}
		return counterTrue >= (getRoles().size() - 1);
	}
	
	
	private void setRadius(final double factor)
	{
		for (final ARole role : getRoles())
		{
			final MoveRole moveRole = (MoveRole) role;
			if (field.isPointInShape(moveRole.getPos()))
			{
				moveRole.getMoveCon().updateDestination(
						moveRole.getMoveCon().getDestination()
								.multiplyNew((factor * radius) / moveRole.getMoveCon().getDestination().getLength()));
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
				IVector2 dest = CircleMath.stepAlongCircle(moveRole.getMoveCon().getDestination(), center, angle);
				moveRole.getMoveCon().updateDestination(dest);
			}
		}
	}
	
	
	@Override
	protected ARole onRemoveRole(final MetisAiFrame frame)
	{
		return getLastRole();
	}
	
	
	@Override
	protected ARole onAddRole()
	{
		MoveRole newRole = new MoveRole();
		newRole.getMoveCon().setBallObstacle(false);
		return newRole;
	}
	
	
	private void updateRoles()
	{
		double angleStep = AngleMath.PI_TWO / getRoles().size();
		IVector2 startOnCircle = Vector2f.fromXY(center.x(), center.y() + radius);
		int i = 0;
		for (ARole role : getRoles())
		{
			MoveRole moveRole = (MoveRole) role;
			IVector2 dest = CircleMath.stepAlongCircle(startOnCircle, center, angleStep * i);
			moveRole.getMoveCon().updateDestination(dest);
			
			moveRole.getMoveCon().updateLookAtTarget(center);
			i++;
		}
		state = CheeringPhase.GROW;
	}
}
