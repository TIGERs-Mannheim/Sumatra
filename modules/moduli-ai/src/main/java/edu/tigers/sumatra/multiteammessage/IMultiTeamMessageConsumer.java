/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.07.2015
 * Author(s): JulianT
 * *********************************************************
 */
package edu.tigers.sumatra.multiteammessage;

import edu.tigers.sumatra.ai.data.MultiTeamMessage;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * @author JulianT
 */
public interface IMultiTeamMessageConsumer
{
	/**
	 * @param message
	 */
	void onNewMultiTeamMessage(MultiTeamMessage message);
	
	
	/**
	 * @return
	 */
	ETeamColor getTeamColor();
}
