/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.10.2010
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.BallPossessionCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.TeamClosestToBallCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.EWAI;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.DefenderK1DRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.KeeperK1DRole;


/**
 * Play where a Keeper and a Defender try to defend the goal! omg!<br>
 * <u>First Approach:</u> (currently implemented)
 * <p>
 * We calculate the bisector b of the angle defined by leftPost, Ball, rightPost. Orthogonal to b Keeper and Defender
 * are moved left respectively right. Both have different radii(!) due to the goal area. Who gets left and who gets
 * right is decided in the first Frame, based on their current Positions.
 * <p>
 * <u>Second Approach:</u>
 * <p>
 * We calculate the bisector b of the angle defined by leftPost, Ball, rightPost. With the biesctor within the triangle,
 * 2 new triangles are created. (One left and one right.) The more left bot has to defend the left triangle and the more
 * right bot has to defend the right triangle. This is solved by new bisectors in these new triangles.
 * <p>
 * Requires: 1 {@link KeeperK1DRole Keeper} and 1 {@link DefenderK1DRole Defender}
 * 
 * @author Malte
 * 
 */
public class KeeperPlus1DefenderPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long		serialVersionUID	= 1983710028661755511L;
	
	// Roles
	private KeeperK1DRole			keeper;
	private DefenderK1DRole			defender;
	

	private final Vector2f				goalCenter				= AIConfig.getGeometry().getGoalOur().getGoalCenter();
	
	private Vector2f					defenderPos			= AIConfig.INIT_VECTOR;
	private Vector2f					keeperPos			= AIConfig.INIT_VECTOR;
	private Vector2f					ballPos				= AIConfig.INIT_VECTOR;
	
	/** Line from approximately the middle of the goal to the ball position. */
	private Line						bisector;
	
	private BallPossessionCrit		ballPossCrit		= null;
	private TeamClosestToBallCrit	closestBallCrit	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public KeeperPlus1DefenderPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.KEEPER_PLUS_1_DEFENDER, aiFrame);
		
		ballPossCrit = new BallPossessionCrit(EBallPossession.WE);
		closestBallCrit = new TeamClosestToBallCrit(ETeam.TIGERS);
		addCriterion(ballPossCrit);
		addCriterion(closestBallCrit);
		
		keeper = new KeeperK1DRole();
		defender = new DefenderK1DRole();
		addDefensiveRole(keeper, goalCenter);
		addDefensiveRole(defender, goalCenter);
		
		bisector = new Line();
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Decides whether our keeper/defender is left/right.
	 */
	@Override
	protected void beforeFirstUpdate(AIInfoFrame currentFrame)
	{
		defenderPos = new Vector2f(defender.getPos(currentFrame));
		keeperPos = new Vector2f(keeper.getPos(currentFrame));
		
		if (defenderPos.y > keeperPos.y)
		{
			defender.setLeftOrRight(EWAI.LEFT);
			keeper.setLeftOrRight(EWAI.RIGHT);
		} else
		{
			defender.setLeftOrRight(EWAI.RIGHT);
			keeper.setLeftOrRight(EWAI.LEFT);
		}
	}
	

	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		ballPos = currentFrame.worldFrame.ball.pos;
		//bisector.setPoints(AIMath.calculateBisector(ballPos, goal.getGoalPostRight(), goal.getGoalPostLeft()), ballPos);
		bisector.setPoints(AIConfig.getGeometry().getGoalOur().getGoalCenter(), ballPos);
		defender.updateDangerLine(bisector);
		keeper.updateDangerLine(bisector);
	}
	

	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
