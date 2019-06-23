/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 29, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawablePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.field.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.field.PenaltyArea;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole.EMoveBehavior;


/**
 * Simulate the defenders of the technical shoot challenge 2013
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class TecShootChalTestPlay extends APlay
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final float				INITIAL_SPACE	= 200;
	private final ETeam						teamSide			= AIConfig.getPlays().getTecShootCalPlay().getGoalOfTeam();
	
	private final MoveRole					keeper;
	private final Map<MoveRole, Float>	moveRoles		= new HashMap<MoveRole, Float>();
	private final PenaltyArea				penArea;
	private final Goal						goal;
	private final Random						rnd				= new Random(System.nanoTime());
	
	private final IVector2					goalLeft;
	private final IVector2					goalRight;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public TecShootChalTestPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		if (teamSide == ETeam.TIGERS)
		{
			penArea = AIConfig.getGeometry().getPenaltyAreaOur();
			goal = AIConfig.getGeometry().getGoalOur();
			goalLeft = goal.getGoalPostLeft().addNew(new Vector2(90, -100));
			goalRight = goal.getGoalPostRight().addNew(new Vector2(90, 100));
		} else
		{
			penArea = AIConfig.getGeometry().getPenaltyAreaTheir();
			goal = AIConfig.getGeometry().getGoalTheir();
			goalLeft = goal.getGoalPostLeft().addNew(new Vector2(-90, -100));
			goalRight = goal.getGoalPostRight().addNew(new Vector2(-90, 100));
		}
		
		keeper = new MoveRole(EMoveBehavior.NORMAL);
		keeper.setPenaltyAreaAllowed(true);
		addDefensiveRole(keeper, goal.getGoalCenter());
		
		if (getNumAssignedRoles() == 2)
		{
			float length = penArea.getPerimeterFrontCurve() / 2;
			MoveRole role = new MoveRole(EMoveBehavior.NORMAL);
			role.setPenaltyAreaAllowed(true);
			moveRoles.put(role, length);
			IVector2 newDest = new Vector2(50, 0).add(penArea.stepAlongPenArea(length));
			addDefensiveRole(role, newDest);
		} else
		{
			float perimeter = penArea.getPerimeterFrontCurve();
			float step = (perimeter - (3 * INITIAL_SPACE)) / (getNumAssignedRoles() - 2);
			for (int i = 0; i < (getNumAssignedRoles() - 1); i++)
			{
				MoveRole role = new MoveRole(EMoveBehavior.NORMAL);
				role.setPenaltyAreaAllowed(true);
				float length = INITIAL_SPACE + (step * i);
				moveRoles.put(role, length);
				IVector2 newDest = new Vector2(50, 0).add(penArea.stepAlongPenArea(length));
				addDefensiveRole(role, newDest);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private void reduceSpeed(ARole role)
	{
		final float speed = AIConfig.getPlays().getTecShootCalPlay().getSpeed();
		if (!SumatraMath.isEqual(speed, role.getMoveCon().getSpeed()))
		{
			if (teamSide == ETeam.TIGERS)
			{
				role.updateTargetAngle(0f);
			} else
			{
				role.updateTargetAngle(AngleMath.PI);
			}
			role.getMoveCon().setSpeed(speed);
		}
	}
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
		for (Map.Entry<MoveRole, Float> entry : moveRoles.entrySet())
		{
			if (entry.getKey().checkMoveCondition(frame.worldFrame))
			{
				float length = entry.getValue() + rnd.nextInt(400);
				IVector2 newDest = new Vector2(50, 0).add(penArea.stepAlongPenArea(length));
				frame.addDebugShape(new DrawablePoint(newDest));
				entry.getKey().updateDestination(newDest);
				reduceSpeed(entry.getKey());
			}
			frame.addDebugShape(new DrawablePoint(penArea.stepAlongPenArea(entry.getValue())));
		}
		
		if (keeper.checkMoveCondition(frame.worldFrame))
		{
			if (keeper.getDestination().equals(goalLeft))
			{
				keeper.updateDestination(goalRight);
			} else
			{
				keeper.updateDestination(goalLeft);
			}
			reduceSpeed(keeper);
		}
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
