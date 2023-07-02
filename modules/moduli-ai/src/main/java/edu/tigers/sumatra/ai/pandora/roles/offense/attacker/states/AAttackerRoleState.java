/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker.states;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.pandora.roles.RoleStateExtern;
import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.function.Supplier;


@Log4j2
public abstract class AAttackerRoleState<T extends AMoveToSkill> extends RoleStateExtern<T>
{
	@Getter
	private AttackerRole role;

	@Configurable(defValue = "PROTECT")
	private static EAttackerState forcedState = EAttackerState.PROTECT;

	@Configurable(comment = "if true, forces state set in forcedState to be active", defValue = "false")
	private static boolean forceAttackerState = false;

	@Getter
	private final EAttackerState stateEnum;

	static
	{
		ConfigRegistration.registerClass("roles", AAttackerRoleState.class);
	}

	protected AAttackerRoleState(Supplier<T> supplier, AttackerRole role, EAttackerState stateEnum)
	{
		super(supplier, role);
		this.role = role;
		this.stateEnum = stateEnum;
		role.addTransition(stateEnum, this);
	}


	@Override
	protected void beforeUpdate()
	{
		role.getStateMachine().setEnableTransitions(!forceAttackerState);
	}


	@Override
	protected void onInit()
	{
		if (getRole().isPhysicalObstaclesOnly())
		{
			skill.getMoveCon().physicalObstaclesOnly();
		}
	}


	@Override
	protected void onUpdate()
	{
		if (isNecessaryDataAvailable())
		{
			doStandardUpdate();
		} else
		{
			doFallbackUpdate();
			if (!forceAttackerState)
			{
				log.warn("AttackerRoleState is not forced, but fallback has still been called. This is probably an error");
			}
		}
		if (forceAttackerState && forcedState != this.stateEnum)
		{
			role.triggerEvent(forcedState);
		}
	}


	protected boolean isNecessaryDataAvailable()
	{
		return true;
	}


	protected void doStandardUpdate()
	{
		// do nothing
	}


	protected void doFallbackUpdate()
	{
		// do nothing
	}


	protected IVector2 getProtectionTarget()
	{
		if (getRole().getAction().getDribbleToPos() != null && getRole().getAction().getDribbleToPos().getProtectFromPos() != null)
		{
			return getRole().getAction().getDribbleToPos().getProtectFromPos();
		}
		var closestEnemyBotID = getRole().getAiFrame().getTacticalField().getOpponentClosestToBall().getBotId();
		IVector2 opponentPos;
		if (getRole().getWFrame().getOpponentBots().containsKey(closestEnemyBotID))
		{
			opponentPos = getRole().getWFrame().getOpponentBot(closestEnemyBotID).getPos();
		} else
		{
			opponentPos = Geometry.getGoalTheir().getCenter();
		}
		return opponentPos;
	}
}
