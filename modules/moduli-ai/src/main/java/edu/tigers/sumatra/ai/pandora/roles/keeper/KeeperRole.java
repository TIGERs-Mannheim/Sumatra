/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.keeper.KeeperStateCalc;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.keeper.states.BallGoalInterceptState;
import edu.tigers.sumatra.ai.pandora.roles.keeper.states.BallPlacementState;
import edu.tigers.sumatra.ai.pandora.roles.keeper.states.CatchRedirectState;
import edu.tigers.sumatra.ai.pandora.roles.keeper.states.ChipFastState;
import edu.tigers.sumatra.ai.pandora.roles.keeper.states.GoOutState;
import edu.tigers.sumatra.ai.pandora.roles.keeper.states.MoveInsidePenaltyState;
import edu.tigers.sumatra.ai.pandora.roles.keeper.states.NormalBlockState;
import edu.tigers.sumatra.ai.pandora.roles.keeper.states.PullBackState;
import edu.tigers.sumatra.geometry.Geometry;


/**
 * The Keeper protect the Goal and tries to catch every shoot into the goal.
 * 
 * @author ChrisC
 */
public class KeeperRole extends ARole
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	@Configurable(comment = "Max acceleration of Keeper", defValue = "4.5")
	private static double keeperAcc = 4.5;
	
	@Configurable(comment = "Keeper's normal movement is circular", defValue = "true")
	private static boolean isKeepersNormalMovementCircular = true;
	
	@Configurable(comment = "Angle of Keeper towards the ball in NormalBlockState", defValue = "0.0")
	private static double turnAngleOfKeeper = 0.0;
	
	@Configurable(comment = "Dist [mm] - Distance around the penalty area, inside the bot will pass controlled, outside the bot will just shoot", defValue = "-180.0")
	private static double safetyAroundPenaltyKickType = -2 * Geometry.getBotRadius();
	
	@Configurable(comment = "Dist [mm] to GoalCenter in NormalBlockState", defValue = "500.0")
	private static double distToGoalCenter = 500;
	
	@Configurable(comment = "Keeper goes out if redirecting bot is behind this margin added to the penaulty area", defValue = "-2022.5")
	private static double goOutWhileRedirectMargin = Geometry
			.getGoalOur().getCenter().x() / 2;
	
	@Configurable(comment = "Min X value for a valid pass target", defValue = "0.0")
	private static double minXPosOfPossiblePassTarget = 0.0;
	
	@Configurable(comment = "Speed limit of keeper while dribbling", defValue = "0.08")
	private static double keeperDribblingVelocity = 0.08;
	
	@Configurable(comment = "Foe ball dist to chill", defValue = "1000.0")
	private static double minFOEBotDistToChill = 1000;
	
	@Configurable(comment = "Minimum chip score for passTargets", defValue = "0.4")
	private static double minChipScore = 0.4;
	
	@Configurable(comment = "if checked: PassTarget will be set", defValue = "true")
	private static boolean isPassTargetSet = true;
	
	@Configurable(comment = "check if long chipKick passes in opponent half to passTargets are desired", defValue = "false")
	private static boolean chipFarToPassTarget = false;
	
	
	/**
	 * Initialization of the state machine
	 */
	public KeeperRole()
	{
		this(ERole.KEEPER);
	}
	
	
	protected KeeperRole(ERole role)
	{
		super(role);
		setInitialState(new MoveInsidePenaltyState(this));
		
		addTransition(KeeperStateCalc.EKeeperState.NORMAL, new NormalBlockState(this));
		addTransition(KeeperStateCalc.EKeeperState.INTERCEPT_BALL,
				new BallGoalInterceptState(this));
		addTransition(KeeperStateCalc.EKeeperState.DEFEND_REDIRECT, new CatchRedirectState(this));
		addTransition(KeeperStateCalc.EKeeperState.BALL_PLACEMENT, new BallPlacementState(this));
		addTransition(KeeperStateCalc.EKeeperState.CHIP_FAST, new ChipFastState(this));
		addTransition(KeeperStateCalc.EKeeperState.PULL_BACK, new PullBackState(this));
		addTransition(KeeperStateCalc.EKeeperState.GO_OUT, new GoOutState(this));
		addTransition(KeeperStateCalc.EKeeperState.MOVE_TO_PENALTY_AREA, new MoveInsidePenaltyState(this));
		addTransition(KeeperStateCalc.EKeeperState.STOPPED, new NormalBlockState(this));
	}
	
	
	/**
	 * @return the distToGoalCenter
	 */
	public static double getDistToGoalCenter()
	{
		return distToGoalCenter;
	}
	
	
	public static double getKeeperAcc()
	{
		return keeperAcc;
	}
	
	
	public static double getTurnAngleOfKeeper()
	{
		return turnAngleOfKeeper;
	}
	
	
	public static double getSafetyAroundPenaltyKickType()
	{
		return safetyAroundPenaltyKickType;
	}
	
	
	public static double getGoOutWhileRedirectMargin()
	{
		return goOutWhileRedirectMargin;
	}
	
	
	public static double getMinXPosOfPossiblePassTarget()
	{
		return minXPosOfPossiblePassTarget;
	}
	
	
	public static boolean getIsKeepersNormalMovementCircular()
	{
		return isKeepersNormalMovementCircular;
	}
	
	
	public static double getKeeperDribblingVelocity()
	{
		return keeperDribblingVelocity;
	}
	
	
	public static double getMinFOEBotDistToChill()
	{
		return minFOEBotDistToChill;
	}
	
	
	public static boolean isKeepersNormalMovementCircular()
	{
		return isKeepersNormalMovementCircular;
	}
	
	
	public static double getMaxChipScore()
	{
		return minChipScore;
	}
	
	
	public static boolean isPassTargetSet()
	{
		return isPassTargetSet;
	}
	
	
	public static boolean isChipFarToPassTarget()
	{
		return chipFarToPassTarget;
	}
	
	
	@Override
	protected void beforeUpdate()
	{
		super.beforeUpdate();
		if (!getAiFrame().getTacticalField().getKeeperState()
				.equals(getAiFrame().getPrevFrame().getTacticalField().getKeeperState()))
		{
			triggerEvent(getAiFrame().getTacticalField().getKeeperState());
		}
	}
	
}