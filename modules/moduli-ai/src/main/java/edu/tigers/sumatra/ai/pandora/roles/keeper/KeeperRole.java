/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.keeper.EKeeperState;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.keeper.states.ChipFastState;
import edu.tigers.sumatra.ai.pandora.roles.keeper.states.CriticalKeeperState;
import edu.tigers.sumatra.ai.pandora.roles.keeper.states.KeeperStoppedState;
import edu.tigers.sumatra.ai.pandora.roles.keeper.states.MoveInsidePenaltyState;
import edu.tigers.sumatra.ai.pandora.roles.keeper.states.PullBackState;


/**
 * The Keeper protect the Goal and tries to catch every shoot into the goal.
 *
 * @author ChrisC
 */
public class KeeperRole extends ARole
{

	@Configurable(comment = "Min distance of possible PassTargets to GoalCenter for ChipFast", defValue = "5000.0")
	private static double passCircleRadius = 5000.0;

	@Configurable(comment = "Speed limit of keeper while dribbling", defValue = "0.25")
	private static double keeperDribblingVelocity = 0.25;

	@Configurable(comment = "Foe ball dist to chill", defValue = "1000.0")
	private static double minFOEBotDistToChill = 1000;

	@Configurable(comment = "use runUpChipSkill in ChipFastState", defValue = "false")
	private static boolean useRunUpChipSkill = false;


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
		addTransition(EKeeperState.STOPPED, new KeeperStoppedState(this));
		addTransition(EKeeperState.CHIP_FAST, new ChipFastState(this));
		addTransition(EKeeperState.PULL_BACK, new PullBackState(this));
		addTransition(EKeeperState.CRITICAL, new CriticalKeeperState(this));
		addTransition(EKeeperState.MOVE_TO_PENALTY_AREA, new MoveInsidePenaltyState(this));
	}


	public static double getPassCircleRadius()
	{
		return passCircleRadius;
	}


	public static double getKeeperDribblingVelocity()
	{
		return keeperDribblingVelocity;
	}


	public static double getMinFOEBotDistToChill()
	{
		return minFOEBotDistToChill;
	}


	public static boolean isUseRunUpChipSkill()
	{
		return useRunUpChipSkill;
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
