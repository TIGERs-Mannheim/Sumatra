/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.07.2015
 * Author(s): JulianT
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.observer;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.MultiTeamMessage;


/**
 * @author JulianT
 */
public interface IMultiTeamMessageObserver
{
	/**
	 * @param message
	 */
	void onNewMultiTeamMessage(MultiTeamMessage message);
}
