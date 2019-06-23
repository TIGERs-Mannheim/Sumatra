/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botoverview.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.tigers.sumatra.ai.data.BotAiInformation;
import edu.tigers.sumatra.ai.data.EBotInformation;
import edu.tigers.sumatra.botoverview.BotOverviewTableModel;


/**
 * Dataholder for a bot overview column in {@link BotOverviewTableModel}
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotOverviewColumn
{
	private final List<Object> data = new ArrayList<>();
	
	
	/**
	 * @param botAiInformation
	 */
	public BotOverviewColumn(final BotAiInformation botAiInformation)
	{
		Map<EBotInformation, String> map = botAiInformation.getMap();
		for (EBotInformation botInfo : EBotInformation.values())
		{
			Object obj = map.get(botInfo);
			if (obj == null)
			{
				data.add("");
			} else
			{
				data.add(obj);
			}
		}
	}
	
	
	/**
	 * @return
	 */
	public List<Object> getData()
	{
		return data;
	}
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + data.hashCode();
		return result;
	}
	
	
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		BotOverviewColumn other = (BotOverviewColumn) obj;
		return data.equals(other.data);
	}
}
