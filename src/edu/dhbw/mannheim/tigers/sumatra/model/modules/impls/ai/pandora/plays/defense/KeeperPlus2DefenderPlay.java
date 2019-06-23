/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.11.2010
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.field.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.EWAI;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.DefenderK2DRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.KeeperSoloRole;


/**
 * Standard defense Play. We have 1 Keeper and 2 Defenders close to him
 * forming a triangle and trying to cover the goal as good as possible.
 * 
 * <pre>
 * ----|      |-----
 *        K
 *      D   D
 * </pre>
 * 
 * Requires: 1 {@link KeeperSoloRole Keeper} and 2 {@link DefenderK2DRole Defender}
 * 
 * @author Malte
 * 
 */
public class KeeperPlus2DefenderPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final Goal				goal				= AIConfig.getGeometry().getGoalOur();
	
	private final KeeperSoloRole	keeper;
	
	private final DefenderK2DRole	leftDefender;
	private final DefenderK2DRole	rightDefender;
	
	/**
	 * Target wherefrom the ball could be shot directly towards the goal.
	 * Usually this is the current ball position.
	 */
	private IVector2					protectAgainst;
	
	private boolean					criticalAngle	= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public KeeperPlus2DefenderPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		setTimeout(Long.MAX_VALUE);
		
		keeper = new KeeperSoloRole();
		rightDefender = new DefenderK2DRole(EWAI.RIGHT);
		leftDefender = new DefenderK2DRole(EWAI.LEFT);
		
		addDefensiveRole(keeper, goal.getGoalCenter());
		addDefensiveRole(rightDefender, goal.getGoalPostRight());
		addDefensiveRole(leftDefender, goal.getGoalPostLeft());
		
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		protectAgainst = currentFrame.worldFrame.ball.getPos();
		final float angle = protectAgainst.subtractNew(AIConfig.getGeometry().getGoalOur().getGoalCenter()).getAngle();
		if ((angle > AngleMath.PI_QUART) || (angle < -AngleMath.PI_QUART))
		{
			criticalAngle = true;
		} else
		{
			criticalAngle = false;
		}
		leftDefender.setCriticalAngle(criticalAngle);
		rightDefender.setCriticalAngle(criticalAngle);
		
		
		// keeper position is calculated.
		final Vector2f keeperPos = new Vector2f(keeper.getDestination());
		
		// keeper position is passed to the defenders.
		leftDefender.updateKeeperPos(keeperPos);
		rightDefender.updateKeeperPos(keeperPos);
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		// nothing todo
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
}
