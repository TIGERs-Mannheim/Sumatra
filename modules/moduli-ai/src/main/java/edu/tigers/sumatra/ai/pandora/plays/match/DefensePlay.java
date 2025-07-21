/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.match;

import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseThreatType;
import edu.tigers.sumatra.ai.metis.defense.data.IDefenseThreat;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.plays.match.defense.ADefenseGroup;
import edu.tigers.sumatra.ai.pandora.plays.match.defense.CenterBackGroup;
import edu.tigers.sumatra.ai.pandora.plays.match.defense.Man2ManMarkerGroup;
import edu.tigers.sumatra.ai.pandora.plays.match.defense.PassDisruptionGroup;
import edu.tigers.sumatra.ai.pandora.plays.match.defense.PenAreaGroup;
import edu.tigers.sumatra.ai.pandora.plays.match.defense.SwitchableDefenderRole;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderPlaceholderRole;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.BotID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
	private Map<AObjectID, CenterBackGroup.CenterBackGroupState> centerBackGroupStates = new HashMap<>();


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
		var groupSet = createNewGroupSet();
		setCenterBackGroupStates(groupSet);
		updateRoleAssignment(groupSet);
		groupSet.forEach(group -> group.updateRoles(getAiFrame()));
	}


	private List<ADefenseGroup> createNewGroupSet()
	{
		var newGroupSet = new ArrayList<ADefenseGroup>();
		var roleMap = getRoles().stream().collect(Collectors.toMap(ARole::getBotID, Function.identity()));
		assignPassDisruptorGroup(newGroupSet, roleMap);
		assignOuterGroups(newGroupSet, roleMap);
		assignPenAreaGroup(newGroupSet, roleMap);
		return Collections.unmodifiableList(newGroupSet);
	}


	private void assignPassDisruptorGroup(List<ADefenseGroup> groupSet, Map<BotID, ARole> roleMap)
	{

		var disruptionAssignment = getAiFrame().getTacticalField().getDefensePassDisruptionAssignment();
		if (disruptionAssignment != null)
		{
			var defender = roleMap.get(disruptionAssignment.getDefenderId());
			if (defender != null)
			{
				var disruptionGroup = new PassDisruptionGroup();
				groupSet.add(disruptionGroup);
				disruptionGroup.addRole(defender);
				roleMap.remove(defender.getBotID());
			}
		}

	}


	private void assignOuterGroups(List<ADefenseGroup> groupSet, Map<BotID, ARole> roleMap)
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

			ADefenseGroup group = getDefendingGroup(assignment, groupSet);

			for (var role : roles)
			{
				group.addRole(role);
				roleMap.remove(role.getBotID());
			}
		}
	}


	private void assignPenAreaGroup(List<ADefenseGroup> groupSet, Map<BotID, ARole> roleMap)
	{
		var penAreaGroup = new PenAreaGroup(getTacticalField().getDefensePenAreaPositionAssignments());
		groupSet.add(penAreaGroup);
		for (var assignment : getTacticalField().getDefensePenAreaPositionAssignments())
		{
			var role = roleMap.get(assignment.botID());
			if (role != null)
			{
				penAreaGroup.addRole(role);
				roleMap.remove(assignment.botID());
			}
		}
	}


	private ADefenseGroup getDefendingGroup(DefenseThreatAssignment assignment, List<ADefenseGroup> groupSet)
	{
		ADefenseGroup group;
		if (assignment.getThreat().getType() == EDefenseThreatType.BOT_M2M)
		{
			group = new Man2ManMarkerGroup(assignment.getThreat());
		} else
		{
			group = new CenterBackGroup(assignment.getThreat());
		}
		groupSet.add(group);
		return group;
	}


	private void updateRoleAssignment(List<ADefenseGroup> groups)
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


	private void setCenterBackGroupStates(List<ADefenseGroup> groupSet)
	{
		var centerBackGroups = groupSet.stream()
				.filter(CenterBackGroup.class::isInstance)
				.map(CenterBackGroup.class::cast)
				.toList();

		var threats = centerBackGroups.stream()
				.map(CenterBackGroup::getThreat)
				.map(IDefenseThreat::getObjectId)
				.collect(Collectors.toUnmodifiableSet());

		centerBackGroupStates.entrySet().removeIf(entry -> !threats.contains(entry.getKey()));

		for (var centerBackGroup : centerBackGroups)
		{
			centerBackGroup.setState(
					centerBackGroupStates.computeIfAbsent(
							centerBackGroup.getThreat().getObjectId(),
							k -> new CenterBackGroup.CenterBackGroupState()
					)
			);
		}
	}
}
