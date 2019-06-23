/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 12, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botoverview;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.BotAiInformation;
import edu.dhbw.mannheim.tigers.sumatra.presenter.botoverview.BotOverviewTableModel;


/**
 * Dataholder for a bot overview column in {@link BotOverviewTableModel}
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotOverviewColumn
{
	/**  */
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	public static final int		ROWS	= 13;
	
	private final List<Object>	data	= new ArrayList<Object>(ROWS);
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param botAiInformation
	 */
	public BotOverviewColumn(final BotAiInformation botAiInformation)
	{
		addData(String.format("%.1fV Bat.", botAiInformation.getBattery()));
		addData(String.format("%.1fV Cond.", botAiInformation.getKickerCharge()));
		addData("Vel:" + botAiInformation.getVel());
		addData(botAiInformation.isBallContact() ? "ballcont." : "no ballcont.");
		addData(botAiInformation.isPathPlanning() ? "PP running" : "no PP");
		addData(botAiInformation.getNumPaths() + " paths");
		addData(botAiInformation.getPlay());
		addData(botAiInformation.getRole());
		addData(botAiInformation.getRoleState());
		addData(botAiInformation.getSkill());
		for (String cond : botAiInformation.getConditions())
		{
			addData(cond);
		}
		int size = data.size();
		for (int i = 0; i < (ROWS - size); i++)
		{
			addData(null);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private void addData(final Object obj)
	{
		if (obj == null)
		{
			data.add("");
		} else
		{
			data.add(obj);
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
		result = (prime * result) + ((data == null) ? 0 : data.hashCode());
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
		if (data == null)
		{
			if (other.data != null)
			{
				return false;
			}
		} else if (!data.equals(other.data))
		{
			return false;
		}
		return true;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
