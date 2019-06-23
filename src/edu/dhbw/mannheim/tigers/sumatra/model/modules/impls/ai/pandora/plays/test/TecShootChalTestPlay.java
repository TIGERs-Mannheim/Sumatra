/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 29, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import edu.dhbw.mannheim.tigers.sumatra.model.data.area.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.area.PenaltyArea;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawablePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole.EMoveBehavior;


/**
 * Simulate the defenders of the technical shoot challenge 2013
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TecShootChalTestPlay extends APlay
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final float				INITIAL_SPACE	= 200;
	private final ETeam						teamSide			= ETeam.OPPONENTS;
	
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
	 */
	public TecShootChalTestPlay()
	{
		super(EPlay.STUPID_DEFENDERS);
		
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
		keeper.getMoveCon().setPenaltyAreaAllowed(true);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private void reduceSpeed(final MoveRole role)
	{
		final float speed = 1.0f;
		if (!SumatraMath.isEqual(speed, role.getMoveCon().getSpeed()))
		{
			if (teamSide == ETeam.TIGERS)
			{
				role.getMoveCon().updateTargetAngle(0f);
			} else
			{
				role.getMoveCon().updateTargetAngle(AngleMath.PI);
			}
			role.getMoveCon().setSpeed(speed);
		}
	}
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame frame)
	{
		for (Map.Entry<MoveRole, Float> entry : moveRoles.entrySet())
		{
			if (entry.getKey().checkMoveCondition())
			{
				float length = entry.getValue() + rnd.nextInt(400);
				IVector2 newDest = new Vector2(50, 0).add(penArea.stepAlongPenArea(length));
				frame.addDebugShape(new DrawablePoint(newDest));
				entry.getKey().getMoveCon().updateDestination(newDest);
				reduceSpeed(entry.getKey());
			}
			frame.addDebugShape(new DrawablePoint(penArea.stepAlongPenArea(entry.getValue())));
		}
		
		if (keeper.hasBeenAssigned() && keeper.checkMoveCondition())
		{
			if (keeper.getMoveCon().getDestCon().getDestination().equals(goalLeft))
			{
				keeper.getMoveCon().updateDestination(goalRight);
			} else
			{
				keeper.getMoveCon().updateDestination(goalLeft);
			}
			reduceSpeed(keeper);
		}
	}
	
	
	@Override
	protected ARole onRemoveRole()
	{
		ARole role = getLastRole();
		moveRoles.remove(role);
		return role;
	}
	
	
	@Override
	protected ARole onAddRole()
	{
		if (getRoles().isEmpty())
		{
			return keeper;
		} else if (getRoles().size() == 1)
		{
			float length = penArea.getPerimeterFrontCurve() / 2;
			MoveRole role = new MoveRole(EMoveBehavior.NORMAL);
			role.getMoveCon().setPenaltyAreaAllowed(true);
			moveRoles.put(role, length);
			IVector2 newDest = new Vector2(50, 0).add(penArea.stepAlongPenArea(length));
			role.getMoveCon().updateDestination(newDest);
			return role;
		} else
		{
			float perimeter = penArea.getPerimeterFrontCurve();
			float step = (perimeter - (3 * INITIAL_SPACE)) / (getRoles().size() - 2);
			MoveRole role = new MoveRole(EMoveBehavior.NORMAL);
			role.getMoveCon().setPenaltyAreaAllowed(true);
			float length = INITIAL_SPACE + (step * (getRoles().size() - 1));
			moveRoles.put(role, length);
			IVector2 newDest = new Vector2(50, 0).add(penArea.stepAlongPenArea(length));
			role.getMoveCon().updateDestination(newDest);
			return role;
		}
	}
	
	
	@Override
	protected void onGameStateChanged(final EGameState gameState)
	{
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
