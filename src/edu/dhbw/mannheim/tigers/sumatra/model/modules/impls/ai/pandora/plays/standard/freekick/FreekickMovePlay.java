/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 09.04.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.freekick;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circlef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.OptimizedRoleAssigner;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.AStandardPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveWithDistanceToPointRole;


/**
 * Handles n offensive bots who will stand between ball and our goal
 * 
 * @author Malte
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class FreekickMovePlay extends AStandardPlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final float								SPACE_BETWEEN_BOTS			= 1;
	private static final float								BALL_TO_INIT_POS_TRESHOLD	= 100;
	private static final float								RADIUS_TOLERANCE				= 10;
	
	private final float										botRadius						= AIConfig.getGeometry().getBotRadius();
	private final float										stopRadius						= AIConfig.getGeometry()
																												.getBotToBallDistanceStop()
																												+ botRadius
																												+ AIConfig.getGeometry()
																														.getBallRadius()
																												+ RADIUS_TOLERANCE;
	
	private final IVector2									ballInitPos;
	private final List<MoveWithDistanceToPointRole>	roles								= new ArrayList<MoveWithDistanceToPointRole>();
	
	private final OptimizedRoleAssigner					roleAssigner					= new OptimizedRoleAssigner();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public FreekickMovePlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		for (int i = 0; i < getNumAssignedRoles(); i++)
		{
			MoveWithDistanceToPointRole role = new MoveWithDistanceToPointRole(aiFrame.worldFrame.ball.getPos(),
					stopRadius, getDirection(aiFrame, i));
			roles.add(role);
			addAggressiveRole(role, aiFrame.worldFrame.ball.getPos());
		}
		
		ballInitPos = aiFrame.worldFrame.ball.getPos();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Generate a direction for the role according to the role number
	 * 
	 * @param aiFrame
	 * @param botNo
	 * @return
	 */
	private IVector2 getDirection(final AIInfoFrame aiFrame, final int botNo)
	{
		final int turn = (botNo - getNumAssignedRoles()) + 1 + (botNo);
		
		final float turnAngle = (float) Math.acos(((2 * stopRadius * stopRadius) - (Math.pow((2 * botRadius)
				+ SPACE_BETWEEN_BOTS, 2)))
				/ (2 * stopRadius * stopRadius));
		
		// vector from ball to the middle of the goal
		final Vector2f goalCenterOur = AIConfig.getGeometry().getGoalOur().getGoalCenter();
		final Vector2 direction = goalCenterOur.subtractNew(aiFrame.worldFrame.ball.getPos());
		
		aiFrame.addDebugShape(new DrawableLine(new Line(aiFrame.worldFrame.ball.getPos(), direction), Color.blue));
		
		return direction.turnNew((turn * turnAngle) / 2);
	}
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		// map: pos->dir
		Map<IVector2, IVector2> dirs = new HashMap<IVector2, IVector2>();
		for (int i = 0; i < roles.size(); i++)
		{
			final IVector2 dir = getDirection(currentFrame, i);
			final IVector2 pos = currentFrame.worldFrame.ball.getPos().addNew(dir.scaleToNew(stopRadius));
			dirs.put(pos, dir);
		}
		
		Map<ARole, IVector2> assignedRoles = roleAssigner.assign(getRoles(), new ArrayList<IVector2>(dirs.keySet()),
				currentFrame);
		
		for (Map.Entry<ARole, IVector2> entry : assignedRoles.entrySet())
		{
			MoveWithDistanceToPointRole role = (MoveWithDistanceToPointRole) entry.getKey();
			final IVector2 pos = entry.getValue();
			final IVector2 dir = dirs.get(pos);
			role.updateCirclePos(currentFrame.worldFrame.ball.getPos(), stopRadius, dir);
		}
		currentFrame.addDebugShape(new DrawableCircle(new Circlef(currentFrame.worldFrame.ball.getPos(), AIConfig
				.getGeometry().getBotToBallDistanceStop()), Color.red));
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		if (GeoMath.distancePP(currentFrame.worldFrame.ball.getPos(), ballInitPos) > BALL_TO_INIT_POS_TRESHOLD)
		{
			changeToFinished();
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
