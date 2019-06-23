/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 28, 2016
 * Author(s): Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.event;

import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public interface IGameEventDetector
{
	/**
	 * Will get the active Participant in an specific event.
	 * This is for now defined to be a single bot per event.
	 * If there is no ID returned, then there is no event to be tracked.
	 * 
	 * @param newTacticalField The temporary active Tactical Field
	 * @param baseAiFrame The temporary active base AI frame
	 * @return The BotID of the involved Bot
	 */
	GameEventFrame getActiveParticipant(TacticalField newTacticalField, BaseAiFrame baseAiFrame);
}
