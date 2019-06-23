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

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.BallPossessionCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.OpponentApproximateScoringChanceCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.TeamClosestToBallCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.EWAI;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.DefenderK2DRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.KeeperK2DRole;


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
 * Requires: 1 {@link KeeperK2DRole Keeper} and 2 {@link DefenderK2DRole Defender}
 * 
 * @author Malte
 * 
 */
public class KeeperPlus2DefenderPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long							serialVersionUID	= 6215145269269607838L;
	
	private final Goal									goal					= AIConfig.getGeometry().getGoalOur();
	
	private KeeperK2DRole								keeper;
	
	private DefenderK2DRole								leftDefender;
	private DefenderK2DRole								rightDefender;
	
	private BallPossessionCrit							ballPossCrit		= null;
	private TeamClosestToBallCrit						closestBallCrit	= null;
	private OpponentApproximateScoringChanceCrit	oppScoChance		= null;
	
	/**
	 * Target wherefrom the ball could be shot directly towards the goal.
	 * Usually this is the current ball position.
	 */
	private Vector2f										protectAgainst;
	
	private boolean										criticalAngle		= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public KeeperPlus2DefenderPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.KEEPER_PLUS_2_DEFENDER, aiFrame);
		
		ballPossCrit = new BallPossessionCrit(EBallPossession.THEY);
		closestBallCrit = new TeamClosestToBallCrit(ETeam.OPPONENTS);
		oppScoChance = new OpponentApproximateScoringChanceCrit(true);
		addCriterion(ballPossCrit);
		addCriterion(closestBallCrit);
		addCriterion(oppScoChance);
		
		keeper = new KeeperK2DRole();
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
		protectAgainst = currentFrame.worldFrame.ball.pos;
		float angle = protectAgainst.subtractNew(AIConfig.getGeometry().getGoalOur().getGoalCenter()).getAngle();
		if (angle > AIMath.PI_QUART || angle < -AIMath.PI_QUART)
		{
			criticalAngle = true;
		} else
		{
			criticalAngle = false;
		}
		keeper.setCriticalAngle(criticalAngle);
		leftDefender.setCriticalAngle(criticalAngle);
		rightDefender.setCriticalAngle(criticalAngle);
		
		
		// keeper position is calculated.
		Vector2f keeperPos = new Vector2f(keeper.calcDestination(currentFrame));
		
		// keeper position is passed to the defenders.
		keeper.setKeeperPos(keeperPos);
		leftDefender.updateKeeperPos(keeperPos);
		rightDefender.updateKeeperPos(keeperPos);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
}
