/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.match;

import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseThreatType;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.plays.match.defense.ADefenseGroup;
import edu.tigers.sumatra.ai.pandora.plays.match.defense.CenterBackGroup;
import edu.tigers.sumatra.ai.pandora.plays.match.defense.Man2ManMarkerGroup;
import edu.tigers.sumatra.ai.pandora.plays.match.defense.PenAreaGroup;
import edu.tigers.sumatra.ai.pandora.plays.match.defense.SwitchableDefenderRole;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderPlaceholderRole;
import edu.tigers.sumatra.ids.BotID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * The defense play manages all defenders.
 */
public class DefensePlay extends APlay
{
	public DefensePlay()
	{
		super(EPlay.DEFENSIVE);
	}


	@Override
	protected ARole onAddRole()
	{
		return new DefenderPlaceholderRole();
	}


	@Override
	protected void doUpdateBeforeRoles()
	{
		super.doUpdateBeforeRoles();
		var newGroupSet = createGroupSet();
		updateRoleAssignment(newGroupSet.allGroups);
		newGroupSet.allGroups.forEach(group -> group.updateRoles(getAiFrame()));
	}


	private GroupSet createGroupSet()
	{
		var newGroupSet = new GroupSet();
		var roleMap = getRoles().stream().collect(Collectors.toMap(ARole::getBotID, Function.identity()));
		assignOuterGroups(newGroupSet, roleMap);
		assignPenAreaGroup(newGroupSet, roleMap);
		return newGroupSet;
	}


	private void assignOuterGroups(GroupSet newGroupSet, Map<BotID, ARole> roleMap)
	{
		for (var assignment : getTacticalField().getDefenseOuterThreatAssignments())
		{
			var roles = assignment.getBotIds().stream()
					.map(roleMap::get)
					.filter(Objects::nonNull)
					.toList();

			if (roles.isEmpty())
			{
				continue;
			}

			ADefenseGroup group = getDefendingGroup(assignment, newGroupSet);

			for (var role : roles)
			{
				group.addRole(role);
				roleMap.remove(role.getBotID());
			}
		}
	}


	private void assignPenAreaGroup(GroupSet newGroupSet, Map<BotID, ARole> roleMap)
	{
		for (var assignment : getTacticalField().getDefensePenAreaPositionAssignments())
		{
			var role = roleMap.get(assignment.botID());
			if (role != null)
			{
				newGroupSet.penAreaGroup.addRole(role);
				roleMap.remove(assignment.botID());
			}
		}
	}


	private ADefenseGroup getDefendingGroup(final DefenseThreatAssignment assignment,
			final GroupSet groupSet)
	{
		ADefenseGroup group;
		if (assignment.getThreat().getType() == EDefenseThreatType.BOT_M2M)
		{
			group = new Man2ManMarkerGroup(assignment.getThreat());
		} else
		{
			group = new CenterBackGroup(assignment.getThreat());
		}
		groupSet.addGroup(group);
		return group;
	}


	private void updateRoleAssignment(final List<ADefenseGroup> groups)
	{
		for (ADefenseGroup group : groups)
		{
			group.assignRoles();
			for (SwitchableDefenderRole sRole : group.getRoles())
			{
				if (sRole.getOriginalRole() != sRole.getNewRole())
				{
					switchRoles(sRole.getOriginalRole(), sRole.getNewRole());
				}
			}
		}
	}


	private static class GroupSet
	{
		final PenAreaGroup penAreaGroup = new PenAreaGroup();
		final List<ADefenseGroup> allGroups = new ArrayList<>();


		GroupSet()
		{
			allGroups.add(penAreaGroup);
		}


		void addGroup(final ADefenseGroup group)
		{
			allGroups.add(group);
		}
	}
}
