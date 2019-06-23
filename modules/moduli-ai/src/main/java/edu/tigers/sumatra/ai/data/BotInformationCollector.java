/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.data;

import java.util.EnumMap;
import java.util.Map;


/**
 * Collect and format all bot information
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotInformationCollector
{
	
	private final Map<EBotInformation, String> map = new EnumMap<>(EBotInformation.class);
	
	
	public Map<EBotInformation, String> getMap()
	{
		return map;
	}
	
}
