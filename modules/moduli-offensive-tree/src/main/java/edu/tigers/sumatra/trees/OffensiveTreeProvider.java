/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trees;

import edu.tigers.moduli.AModule;
import edu.tigers.sumatra.ids.ETeamColor;

import java.util.EnumMap;
import java.util.Map;


public class OffensiveTreeProvider extends AModule
{
	private final Map<ETeamColor, OffensiveActionTreeMap> treeMaps = new EnumMap<>(ETeamColor.class);


	public void updateTree(OffensiveActionTreeMap map, final ETeamColor color)
	{
		treeMaps.put(color, map);
	}


	public OffensiveActionTreeMap getTreeMap(ETeamColor color)
	{
		return treeMaps.computeIfAbsent(color, c -> new OffensiveActionTreeMap());
	}
}
