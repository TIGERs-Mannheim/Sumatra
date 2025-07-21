/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.athena;

import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ids.BotID;
import lombok.Value;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Contains information about added and removed plays and roles from UI.
 */
@Value
public class AthenaGuiInput
{
	List<ARole> roles = new CopyOnWriteArrayList<>();
	List<APlay> plays = new CopyOnWriteArrayList<>();
	Map<EPlay, Set<BotID>> roleMapping = new ConcurrentHashMap<>();

	public AthenaGuiInput copy() {
		AthenaGuiInput copy = new AthenaGuiInput();
		copy.roles.addAll(roles);
		copy.plays.addAll(plays);
		copy.roleMapping.putAll(roleMapping);
		return copy;
	}
}
