/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 3, 2015
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.learning.lcase;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;


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
			float length = (AIConfig.getGeometry().getFieldLength() / 2f) - 500f;
			float width = (AIConfig.getGeometry().getFieldWidth() / 2f) - 200f;
			movePositionsA.add(new Vector2(-length + 500 + (i * (length / (6))), width));
			movePositionsB.add(new Vector2(-length + 500 + (i * (length / (6))), -width));
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
