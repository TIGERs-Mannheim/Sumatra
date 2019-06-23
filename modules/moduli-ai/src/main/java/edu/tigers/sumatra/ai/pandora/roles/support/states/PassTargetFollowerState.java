/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.support.states;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.ai.pandora.roles.support.SupportRole;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This class follows the current Best PassTarget
 * 
 * @author chris
 */
public class PassTargetFollowerState extends ASupporterState
{
	private AMoveToSkill moveToSkill;
	
	@Configurable
	private static double minDistanceFollowingBestPassTarget = 1500;
	
	static
	{
		ConfigRegistration.registerClass("roles", PassTargetFollowerState.class);
	}
	
	
	/**
	 * @param role the parent of the states
	 */
	public PassTargetFollowerState(final SupportRole role)
	{
		super(role);
	}
	
	
	@Override
	public void doEntryActions()
	{
		
		moveToSkill = AMoveToSkill.createMoveToSkill();
		setNewSkill(moveToSkill);
	}
	
	
	@Override
	public void doUpdate()
	{
		SupportRole.EEvent globalEvent = parent.getGlobalEvent();
		if (globalEvent != null && globalEvent != SupportRole.EEvent.LOCAL_OPTIMIZATION)
		{
			triggerEvent(globalEvent);
			return;
		}
		
		List<IPassTarget> passTargets = getAiFrame().getTacticalField().getPassTargetsRanked();
		if (getGlobalPosition() != null)
		{
			if (!MoveToGlobalPosState.isNearGlobalPosition(getGlobalPosition(), getPos()))
			{
				triggerEvent(SupportRole.EEvent.MOVE);
				return;
			}
			passTargets = passTargets.stream()
					.filter(target -> MoveToGlobalPosState.isNearGlobalPosition(getGlobalPosition(), target.getBotPos()))
					.collect(Collectors.toList());
		}
		Optional<IPassTarget> bestLocalPassTarget = passTargets.stream()
				.filter(target -> target.getBotId() == getBotID()).findFirst();
		if (bestLocalPassTarget.isPresent())
		{
			List<ITrackedBot> bots = getWFrame().getTigerBotsVisible().values().stream()
					.sorted((a, b) -> (int) (b.getPos().distanceTo(getPos()) - a.getPos().distanceTo(getPos())))
					.collect(Collectors.toList());
			if (!bots.isEmpty()
					&& bots.get(0).getPos().distanceTo(getPos()) > minDistanceFollowingBestPassTarget)
			{
				moveToSkill.getMoveCon().updateDestination(bestLocalPassTarget.get().getBotPos());
			} else
			{
				IVector2 destination = LineMath.stepAlongLine(bots.get(0).getPos(), bestLocalPassTarget.get().getBotPos(),
						minDistanceFollowingBestPassTarget * 2);
				moveToSkill.getMoveCon().updateDestination(destination);
			}
			moveToSkill.getMoveCon().updateLookAtTarget(getWFrame().getBall());
		}
	}
}
