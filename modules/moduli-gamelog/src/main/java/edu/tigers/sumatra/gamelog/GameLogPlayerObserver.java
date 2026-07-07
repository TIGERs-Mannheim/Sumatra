package edu.tigers.sumatra.gamelog;

public interface GameLogPlayerObserver
{
	void onNewGameLogMessage(GameLogMessage message, int index);

	void onGameLogTimeJump();
}
