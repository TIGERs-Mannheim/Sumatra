/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.keeper.EKeeperState;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.keeper.states.BallPlacementState;
import edu.tigers.sumatra.ai.pandora.roles.keeper.states.ChipFastState;
import edu.tigers.sumatra.ai.pandora.roles.keeper.states.CriticalKeeperState;
import edu.tigers.sumatra.ai.pandora.roles.keeper.states.MoveInsidePenaltyState;
import edu.tigers.sumatra.ai.pandora.roles.keeper.states.PullBackState;


/**
 * The Keeper protect the Goal and tries to catch every shoot into the goal.
 * 
 * @author ChrisC
 */
public class KeeperRole extends ARole
{
	@Configurable(comment = "Max acceleration of Keeper", defValue = "4.5")
	private static double keeperAcc = 4.5;
	
	@Configurable(comment = "Angle of Keeper towards the ball in NormalBlockState", defValue = "0.0")
	private static double turnAngleOfKeeper = 0.0;
	
	@Configurable(comment = "Min X value for a valid pass target", defValue = "0.0")
	private static double minXPosOfPossiblePassTarget = 0.0;
	
	@Configurable(comment = "Speed limit of keeper while dribbling", defValue = "0.25")
	private static double keeperDribblingVelocity = 0.25;
	
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
		addTransition(EKeeperState.BALL_PLACEMENT, new BallPlacementState(this));
		addTransition(EKeeperState.CHIP_FAST, new ChipFastState(this));
		addTransition(EKeeperState.PULL_BACK, new PullBackState(this));
		addTransition(EKeeperState.CRITICAL, new CriticalKeeperState(this));
		addTransition(EKeeperState.MOVE_TO_PENALTY_AREA, new MoveInsidePenaltyState(this));
		addTransition(EKeeperState.STOPPED, new BallPlacementState(this));
	}
	
	
	public static double getKeeperAcc()
	{
		return keeperAcc;
	}
	
	
	public static double getTurnAngleOfKeeper()
	{
		return turnAngleOfKeeper;
	}
	
	
	public static double getMinXPosOfPossiblePassTarget()
	{
		return minXPosOfPossiblePassTarget;
	}
	
	
	public static double getKeeperDribblingVelocity()
	{
		return keeperDribblingVelocity;
	}
	
	
	public static double getMinFOEBotDistToChill()
	{
		return minFOEBotDistToChill;
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
		if (!getAiFrame().getTacticalField().getKeeperState().name()
				.equals(getCurrentState().getIdentifier()))
		{
			triggerEvent(getAiFrame().getTacticalField().getKeeperState());
		}
	}
}