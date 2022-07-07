/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.athena;

import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ids.BotID;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This stores all tactical information.
 */
@Value
@Builder
public class PlayStrategy implements IPlayStrategy
{
	@Singular
	Set<APlay> activePlays;


	@Override
	public Map<BotID, ARole> getActiveRoles()
	{
		Map<BotID, ARole> roles = new HashMap<>();
		for (APlay play : activePlays)
		{
			for (ARole role : play.getRoles())
			{
				roles.put(role.getBotID(), role);
			}
		}
		return roles;
	}


	@Override
	public List<ARole> getActiveRoles(final ERole... roleTypes)
	{
		final List<ERole> types = Arrays.asList(roleTypes);
		List<ARole> roles = new ArrayList<>();
		for (APlay play : activePlays)
		{
			for (ARole role : play.getRoles())
			{
				if (types.contains(role.getType()))
				{
					roles.add(role);
				}
			}
		}
		return roles;
	}


	@Override
	public List<ARole> getActiveRoles(final EPlay playType)
	{
		List<ARole> roles = new ArrayList<>();
		for (APlay play : activePlays)
		{
			if (play.getType() != playType)
			{
				continue;
			}
			roles.addAll(play.getRoles());
		}
		return roles;
	}
}
