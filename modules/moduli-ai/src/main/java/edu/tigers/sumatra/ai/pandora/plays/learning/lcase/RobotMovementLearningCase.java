/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.learning.lcase;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;


/**
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class RobotMovementLearningCase extends ALearningCase
{
	
	List<IVector2>			movePositionsA	= new ArrayList<IVector2>();
	List<IVector2>			movePositionsB	= new ArrayList<IVector2>();
	private EMoveState	currentState	= EMoveState.BACKWARD;
	private int				counter			= 0;
	
	private enum EMoveState
	{
		FORWARD,
		BACKWARD
	}
	
	
	/**
	 * 
	 */
	public RobotMovementLearningCase()
	{
		getActiveRoleTypes().add(ERole.MOVE);
		getActiveRoleTypes().add(ERole.MOVE);
		getActiveRoleTypes().add(ERole.MOVE);
		getActiveRoleTypes().add(ERole.MOVE);
		getActiveRoleTypes().add(ERole.MOVE);
		getActiveRoleTypes().add(ERole.MOVE);
		
		for (int i = 0; i < 6; i++)
		{
			double length = (Geometry.getFieldLength() / 2.0) - 500;
			double width = (Geometry.getFieldWidth() / 2.0) - 200;
			movePositionsA.add(Vector2.fromXY(-length + 500 + (i * (length / (6.0))), width));
			movePositionsB.add(Vector2.fromXY(-length + 500 + (i * (length / (6.0))), -width));
		}
	}
	
	
	@Override
	public void update(final List<ARole> roles, final AthenaAiFrame frame)
	{
		int i = 0;
		for (ARole role : roles)
		{
			if (i < 6)
			{
				if (role instanceof MoveRole)
				{
					MoveRole mrole = (MoveRole) role;
					switch (currentState)
					{
						case BACKWARD:
							mrole.getMoveCon().updateDestination(movePositionsA.get(i));
							i++;
							if (destinationsReached(roles))
							{
								currentState = EMoveState.FORWARD;
								counter++;
							}
							break;
						case FORWARD:
							mrole.getMoveCon().updateDestination(movePositionsB.get(i));
							i++;
							if (destinationsReached(roles))
							{
								currentState = EMoveState.BACKWARD;
								counter++;
							}
							break;
						default:
							break;
					
					}
				}
			}
		}
	}
	
	
	@Override
	public boolean isFinished(final AthenaAiFrame frame)
	{
		return counter > 6;
	}
	
	
	@Override
	public boolean isReady(final AthenaAiFrame frame, final List<ARole> roles)
	{
		return roles.size() >= 6;
	}
	
	
	@Override
	public String getReadyCriteria()
	{
		return "needed bots: 6\n";
	}
	
	
	private boolean destinationsReached(final List<ARole> roles)
	{
		boolean checker = true;
		switch (currentState)
		{
			case FORWARD:
				for (ARole role : roles)
				{
					boolean insidePosition = false;
					for (int i = 0; i < 6; i++)
					{
						if (VectorMath.distancePP(role.getPos(), movePositionsB.get(i)) < 50)
						{
							insidePosition = true;
						}
					}
					if (!insidePosition)
					{
						checker = false;
					}
				}
				break;
			case BACKWARD:
				for (ARole role : roles)
				{
					boolean insidePosition = false;
					for (int i = 0; i < 6; i++)
					{
						if (VectorMath.distancePP(role.getPos(), movePositionsA.get(i)) < 50)
						{
							insidePosition = true;
						}
					}
					if (!insidePosition)
					{
						checker = false;
					}
				}
				break;
			
			default:
				break;
		}
		return checker;
	}
}
