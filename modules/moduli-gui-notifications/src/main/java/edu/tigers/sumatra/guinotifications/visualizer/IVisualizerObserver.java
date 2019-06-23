/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.guinotifications.visualizer;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public interface IVisualizerObserver
{
	/**
	 * Called on new move commands.
	 *
	 * @param botID The bot
	 * @param pos The destination
	 */
	void onMoveClick(BotID botID, IVector2 pos);

	/**
	 * Called on bot selection.
	 *
	 * @param botID The bot's id
	 */
	void onRobotClick(BotID botID);

	/**
	 * Called if a bot gets hidden from rcm.
	 * @param botID The bot
	 * @param hide The new state
	 */
	void onHideFromRcm(BotID botID, boolean hide);
}
