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
	private Map<ETeamColor, OffensiveActionTreeMap> treeMaps = new EnumMap<>(ETeamColor.class);
	
	
	@Override
	public void initModule()
	{
		treeMaps.put(ETeamColor.YELLOW, new OffensiveActionTreeMap());
		treeMaps.put(ETeamColor.BLUE, new OffensiveActionTreeMap());
	}
	
	
	@Override
	public void startModule()
	{
		// nothing to do
	}
	
	
	@Override
	public void stopModule()
	{
		// nothing to do
	}
	
	
	@Override
	public void deinitModule()
	{
		// nothing to do
	}
	
	
	public void updateTree(OffensiveActionTreeMap map, final ETeamColor color)
	{
		treeMaps.put(color, map);
	}
	
	
	public OffensiveActionTreeMap getTreeMap(ETeamColor color)
	{
		return treeMaps.get(color);
	}
	
}
