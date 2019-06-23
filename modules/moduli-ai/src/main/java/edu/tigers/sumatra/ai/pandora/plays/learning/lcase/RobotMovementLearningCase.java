/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 3, 2015
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays.learning.lcase;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.wp.data.Geometry;


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
		BACKWARD;
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
			movePositionsA.add(new Vector2(-length + 500 + (i * (length / (6.0))), width));
			movePositionsB.add(new Vector2(-length + 500 + (i * (length / (6.0))), -width));
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
		if (counter > 6)
		{
			return true;
		}
		return false;
	}
	
	
	@Override
	public boolean isReady(final AthenaAiFrame frame, final List<ARole> roles)
	{
		if (roles.size() >= 6)
		{
			return true;
		}
		return false;
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
						if (GeoMath.distancePP(role.getPos(), movePositionsB.get(i)) < 50)
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
						if (GeoMath.distancePP(role.getPos(), movePositionsA.get(i)) < 50)
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
