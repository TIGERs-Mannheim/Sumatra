/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.match;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseGroup;
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


/**
 * The defense play manages all defenders
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class DefensePlay extends APlay
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(DefensePlay.class.getName());
	
	
	private GroupSet currentGroupSet = new GroupSet();
	
	
	/**
	 * Default
	 */
	public DefensePlay()
	{
		super(EPlay.DEFENSIVE);
	}
	
	
	@Override
	protected ARole onRemoveRole(final MetisAiFrame frame)
	{
		return getLastRole();
	}
	
	
	@Override
	protected ARole onAddRole()
	{
		return new DefenderPlaceholderRole();
	}
	
	
	@Override
	public void updateBeforeRoles(final AthenaAiFrame aiFrame)
	{
		super.updateBeforeRoles(aiFrame);
		GroupSet newGroupSet = createGroupSet(aiFrame);
		updateRoleAssignment(newGroupSet.allGroups);
		currentGroupSet.allGroups.forEach(group -> group.updateRoles(aiFrame));
		currentGroupSet = newGroupSet;
	}
	
	
	private GroupSet createGroupSet(final AthenaAiFrame currentFrame)
	{
		GroupSet newGroupSet = new GroupSet();
		Map<BotID, ARole> roleMap = getRoles().stream().collect(Collectors.toMap(ARole::getBotID, Function.identity()));
		List<DefenseThreatAssignment> threatAssignments = currentFrame.getTacticalField().getDefenseThreatAssignments();
		assignCenterBackGroups(threatAssignments, newGroupSet, roleMap);
		assignManToManMarkerGroups(threatAssignments, newGroupSet, roleMap);
		newGroupSet.penAreaGroup.setThreatAssignments(threatAssignments);
		for (ARole role : roleMap.values())
		{
			newGroupSet.penAreaGroup.addRole(role);
		}
		return newGroupSet;
	}
	
	
	private void assignManToManMarkerGroups(final List<DefenseThreatAssignment> threatAssignments,
			final GroupSet newGroupSet,
			final Map<BotID, ARole> roleMap)
	{
		for (DefenseThreatAssignment assignment : threatAssignments)
		{
			if (assignment.getDefenseGroup() != EDefenseGroup.MAN_TO_MAN_MARKER)
			{
				continue;
			}
			ADefenseGroup group = new Man2ManMarkerGroup(assignment.getObjectID(), assignment.getThreat());
			newGroupSet.addGroup(group);
			for (BotID botID : assignment.getBotIds())
			{
				ARole role = roleMap.remove(botID);
				if (role != null)
				{
					group.addRole(role);
				}
			}
		}
	}
	
	
	private void assignCenterBackGroups(final List<DefenseThreatAssignment> threatAssignments,
			final GroupSet newGroupSet,
			final Map<BotID, ARole> roleMap)
	{
		for (DefenseThreatAssignment assignment : threatAssignments)
		{
			if (assignment.getDefenseGroup() != EDefenseGroup.CENTER_BACK)
			{
				continue;
			}
			ADefenseGroup group = getDefendingGroup(assignment, newGroupSet);
			for (BotID botID : assignment.getBotIds())
			{
				ARole role = roleMap.remove(botID);
				if (role != null)
				{
					group.addRole(role);
				}
			}
		}
	}
	
	
	private ADefenseGroup getDefendingGroup(final DefenseThreatAssignment assignment,
			final GroupSet groupSet)
	{
		ADefenseGroup group = new CenterBackGroup(assignment.getObjectID(), assignment.getThreat());
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
		final PenAreaGroup penAreaGroup = new PenAreaGroup(BotID.noBot());
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
