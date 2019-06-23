/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.10.2010
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.other.MoveRole;


/**
 * All 5 Robots shall move on a circle around the ball-position.
 * @see MoveRole
 * @author Malte
 * 
 */
public class AroundTheBallPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= -5521150672629446483L;
	
	private final Goal			goal					= AIConfig.getGeometry().getGoalOur();
	
	private Vector2				ballPos;
	private float					radius				= AIConfig.getPlays().getAroundTheBall().getRadius();
	
	private Vector2				direction;
	
	private MoveRole				role1;
	private MoveRole				role2;
	private MoveRole				role3;
	private MoveRole				role4;
	private MoveRole				role5;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public AroundTheBallPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.AROUND_THE_BALL, aiFrame);
		Vector2f bp = aiFrame.worldFrame.ball.pos;
		role1 = new MoveRole();
		addAggressiveRole(role1, bp);
		role2 = new MoveRole();
		addAggressiveRole(role2, bp);
		role3 = new MoveRole();
		addAggressiveRole(role3, bp);
		role4 = new MoveRole();
		addAggressiveRole(role4, bp);
		role5 = new MoveRole();
		addAggressiveRole(role5, bp);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		ballPos = new Vector2(currentFrame.worldFrame.ball.pos);
		
		// direction: vector from ball to the middle of the goal!
		direction = goal.getGoalCenter().subtractNew(ballPos);
		role1.updateCirclePos(ballPos, radius, direction);
		
		// some little changes to the first direction
		role2.updateCirclePos(ballPos, radius, direction.turnNew(AIMath.PI / 4));
		role3.updateCirclePos(ballPos, radius, direction.turnNew(AIMath.PI / 2));
		role4.updateCirclePos(ballPos, radius, direction.turnNew(-AIMath.PI / 4));
		role5.updateCirclePos(ballPos, radius, direction.turnNew(-AIMath.PI / 2));
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
