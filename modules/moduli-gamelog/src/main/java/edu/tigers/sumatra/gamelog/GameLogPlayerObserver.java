/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gamelog;

public interface GameLogPlayerObserver
{
	void onNewGameLogMessage(GameLogMessage message, int index);

	void onGameLogTimeJump();
}
