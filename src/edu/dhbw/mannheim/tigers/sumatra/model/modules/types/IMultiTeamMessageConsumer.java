/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.07.2015
 * Author(s): JulianT
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.MultiTeamMessage;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;


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
