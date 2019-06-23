/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 7, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.general;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.tigers.sumatra.ai.data.LedControl;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ids.BotID;


/**
 * @author MarkG
 */
public class LedCalc extends ACalculator
{
	private long							toggleTime			= 550_000_000L;
	private long							toogleTimer			= 0;
	
	private Map<BotID, LedControl>	ledData				= null;
	
	private int								timeoutLedStatus	= 0;
	private boolean						toogler				= false;
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		if (toogleTimer == 0)
		{
			toogleTimer = baseAiFrame.getWorldFrame().getTimestamp();
		}
		ledData = new HashMap<>();
		if (baseAiFrame.getPrevFrame() != null)
		{
			List<ARole> roles = new ArrayList<ARole>(
					baseAiFrame.getPrevFrame().getPlayStrategy().getActiveRoles().values());
			Collections.sort(roles, Comparator.comparing(e -> e.getBotID()));
			int i = 0;
			for (ARole role : roles)
			{
				LedControl control = null;
				if (role.getType().toString() == ERole.OFFENSIVE.toString())
				{
					control = new LedControl(false, true, false, true);
				} else if (role.getType().toString() == ERole.SUPPORT.toString())
				{
					control = new LedControl(true, true, true, true);
				} else if (role.getType().toString() == ERole.DEFENDER.toString())
				{
					control = new LedControl(true, false, true, false);
				} else
				{
					control = new LedControl(false, false, false, false);
				}
				switch (newTacticalField.getGameState())
				{
					case TIMEOUT_THEY:
					case TIMEOUT_WE:
					case HALTED:
						if (timeoutLedStatus == i)
						{
							if (!toogler)
							{
								toogler = true;
								control = new LedControl(false, false, false, true);
							} else
							{
								toogler = false;
								control = new LedControl(false, true, false, false);
							}
						} else
						{
							control = new LedControl(false, false, false, false);
						}
						if (((baseAiFrame.getWorldFrame().getTimestamp() - toogleTimer) > toggleTime) && (toogler == false))
						{
							timeoutLedStatus++;
							if (timeoutLedStatus == baseAiFrame.getPrevFrame().getPlayStrategy().getActiveRoles().size())
							{
								timeoutLedStatus = 0;
							}
							toogleTimer = baseAiFrame.getWorldFrame().getTimestamp();
						}
						break;
					default:
						break;
				}
				ledData.put(role.getBotID(), control);
				i++;
			}
		}
		newTacticalField.setLedData(ledData);
	}
}
