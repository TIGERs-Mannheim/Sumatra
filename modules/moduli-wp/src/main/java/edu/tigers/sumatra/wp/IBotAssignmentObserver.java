/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiType;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IBotAssignmentObserver
{
	/**
	 * @param botID
	 * @param aiType
	 */
	void onBotAssignmentChanged(BotID botID, EAiType aiType);
}
