/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.athena;

import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.plays.test.GuiTestPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ids.BotID;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Test mode
 */
public class TestModeAthenaAdapter implements IAthenaAdapter
{
	private final RoleAssigner roleAssigner = new RoleAssigner();
	private final Set<APlay> activePlays = new HashSet<>();
	private final GuiTestPlay guiTestPlay = new GuiTestPlay();


	@Override
	public PlayStrategy process(final MetisAiFrame metisAiFrame, final AthenaGuiInput athenaGuiInput)
	{
		processRoles(athenaGuiInput.getRoles());
		processPlays(athenaGuiInput.getPlays());
		processRoleMapping(metisAiFrame, athenaGuiInput.getRoleMapping());

		Set<APlay> newActivePlays = new HashSet<>(activePlays);
		if (!guiTestPlay.getRoles().isEmpty())
		{
			newActivePlays.add(guiTestPlay);
		}
		return new PlayStrategy(Collections.unmodifiableSet(newActivePlays));
	}


	@Override
	public void stop(final AthenaGuiInput athenaGuiInput)
	{
		athenaGuiInput.getRoleMapping().clear();
		athenaGuiInput.getPlays().clear();
		athenaGuiInput.getRoles().clear();
		guiTestPlay.removeRoles(guiTestPlay.getRoles().size());
		activePlays.forEach(play -> play.removeRoles(play.getRoles().size()));
		activePlays.clear();
	}


	private void processRoles(final List<ARole> roles)
	{
		// remove vanished test roles
		for (ARole role : guiTestPlay.getRoles())
		{
			if (!roles.contains(role) || role.isCompleted())
			{
				roles.remove(role);
				guiTestPlay.removeRole(role);
			}
		}

		// already assigned to a non-GUI play? -> remove
		roles.removeIf(role -> botIdAssigned(role.getBotID()));

		// add new test roles
		for (ARole role : roles)
		{
			// check if bot is assigned to a role already and unassign the role
			guiTestPlay.getRoles().stream()
					.filter(r -> r != role)
					.filter(r -> r.getBotID().equals(role.getBotID()))
					.forEach(r -> {
						guiTestPlay.removeRole(r);
						roles.remove(r);
					});
			if (!guiTestPlay.getRoles().contains(role))
			{
				guiTestPlay.addNewRole(role);
			}
		}
	}


	private void processPlays(final List<APlay> plays)
	{
		// remove duplicate plays
		removeDuplicatePlays(plays);

		// Synchronize plays from aiControl with activePlays
		activePlays.removeIf(play -> !plays.contains(play));
		activePlays.addAll(plays);
	}


	private void processRoleMapping(final MetisAiFrame metisAiFrame, final Map<EPlay, Set<BotID>> roleMapping)
	{
		// remove bot ids that are not visible and do not belong to the current team
		for (Set<BotID> ids : roleMapping.values())
		{
			ids.removeIf(id -> !metisAiFrame.getWorldFrame().getBots().containsKey(id));
		}

		// assign role mappings to active plays (excluding guiTestPlay)
		roleAssigner.assignRoles(activePlays, roleMapping);
	}


	private void removeDuplicatePlays(final List<APlay> plays)
	{
		for (APlay play1 : plays)
		{
			for (APlay play2 : plays)
			{
				if (play1 != play2 && play1.getClass() == play2.getClass())
				{
					play1.removeRoles(play1.getRoles().size());
					plays.remove(play1);
					break;
				}
			}
		}
	}


	private boolean botIdAssigned(final BotID botID)
	{
		for (APlay play : activePlays)
		{
			for (ARole playRole : play.getRoles())
			{
				if (playRole.getBotID().equals(botID))
				{
					return true;
				}
			}
		}
		return false;
	}
}
