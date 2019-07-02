/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.match;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.metis.defense.DefenseConstants;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseThreatType;
import edu.tigers.sumatra.ai.metis.defense.data.IDefenseThreat;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.plays.match.defense.ADefenseGroup;
import edu.tigers.sumatra.ai.pandora.plays.match.defense.CenterBackGroup;
import edu.tigers.sumatra.ai.pandora.plays.match.defense.Man2ManMarkerGroup;
import edu.tigers.sumatra.ai.pandora.plays.match.defense.PenAreaGroup;
import edu.tigers.sumatra.ai.pandora.plays.match.defense.SwitchableDefenderRole;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderPlaceholderRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.v2.ILineSegment;


/**
 * The defense play manages all defenders.
 */
public class DefensePlay extends APlay
{
	private GroupSet currentGroupSet = new GroupSet();


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
		newGroupSet.allGroups.forEach(group -> group.updateRoles(aiFrame));
	}


	private GroupSet createGroupSet(final AthenaAiFrame currentFrame)
	{
		GroupSet newGroupSet = new GroupSet();
		Map<BotID, ARole> roleMap = getRoles().stream().collect(Collectors.toMap(ARole::getBotID, Function.identity()));
		List<DefenseThreatAssignment> threatAssignments = currentFrame.getTacticalField().getDefenseThreatAssignments();
		assignOuterGroups(threatAssignments, newGroupSet, roleMap);
		for (ARole role : roleMap.values())
		{
			newGroupSet.penAreaGroup.addRole(role);
		}
		return newGroupSet;
	}


	private void assignOuterGroups(final List<DefenseThreatAssignment> threatAssignments,
			final GroupSet newGroupSet,
			final Map<BotID, ARole> roleMap)
	{
		for (DefenseThreatAssignment assignment : threatAssignments)
		{
			if (stayOnPenArea(assignment.getThreat()))
			{
				continue;
			}
			ADefenseGroup group = getDefendingGroup(assignment, newGroupSet);
			for (BotID botID : assignment.getBotIds())
			{
				ARole role = roleMap.get(botID);
				if (role != null && canMoveToProtectionLine(assignment, role))
				{
					group.addRole(role);
					roleMap.remove(botID);
				}
			}
		}
	}


	private boolean stayOnPenArea(final IDefenseThreat threat)
	{
		if (!threat.getProtectionLine().isPresent())
		{
			// protection not possible -> penalty area is only the fallback here
			return true;
		}

		if (threat.getType() == EDefenseThreatType.BALL_TO_GOAL)
		{
			// only stay on penArea during ball placement
			return getAiFrame().getGamestate().isBallPlacement();
		}

		if (threat.getType() == EDefenseThreatType.BOT_TO_GOAL)
		{
			return getAiFrame().getGamestate().isStoppedGame()
					|| getAiFrame().getGamestate().isStandardSituationForThem();
		}

		if (threat.getType() == EDefenseThreatType.BALL_TO_BOT)
		{
			return false;
		}

		throw new IllegalArgumentException("Unknown threat type: " + threat.getType());
	}


	private boolean canMoveToProtectionLine(final DefenseThreatAssignment assignment, final ARole role)
	{
		if (assignment.getThreat().getType() == EDefenseThreatType.BALL_TO_BOT)
		{
			return true;
		}

		final ILineSegment protectionLine = assignment.getThreat().getProtectionLine()
				.orElseThrow(IllegalStateException::new);
		final double distToProtectionLine = protectionLine.distanceTo(role.getPos());

		boolean roleIsInPenAreaGroup = currentGroupSet.penAreaGroup.getRoles().stream()
				.anyMatch(sdr -> sdr.getOriginalRole().getBotID() == role.getBotID());
		double goOutOffset = DefenseConstants.getMinGoOutDistance()
				+ (Geometry.getBotRadius() * 2)
				+ (roleIsInPenAreaGroup ? -50.0 : 50.0);

		double onPenaltyAreaOffset = Geometry.getPenaltyAreaMargin() + Geometry.getBotRadius() * 3
				+ (roleIsInPenAreaGroup ? 50.0 : -50.0);

		final boolean outsideOfPenArea = !Geometry.getPenaltyAreaOur()
				.withMargin(onPenaltyAreaOffset)
				.isPointInShape(role.getPos());
		return distToProtectionLine < goOutOffset || outsideOfPenArea;
	}


	private ADefenseGroup getDefendingGroup(final DefenseThreatAssignment assignment,
			final GroupSet groupSet)
	{
		ADefenseGroup group;
		if (assignment.getThreat().getType() == EDefenseThreatType.BALL_TO_BOT)
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
