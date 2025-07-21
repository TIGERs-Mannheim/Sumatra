/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.replay.view;

import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.EGameEvent;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;


/**
 * Observer for {@link ReplayControlPanel}
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IReplayControlPanelObserver
{
	/**
	 * @param speed
	 */
	void onSetSpeed(double speed);


	/**
	 * @param playing true if playing, false if paused
	 */
	void onPlayPause(boolean playing);


	/**
	 * @param time
	 */
	void onChangeAbsoluteTime(long time);


	/**
	 * @param relTime
	 */
	void onChangeRelativeTime(long relTime);


	/**
	 * jump to next frame
	 */
	void onNextFrame();


	/**
	 * jump to prev frame
	 */
	void onPreviousFrame();


	/**
	 * @param enable
	 */
	void onSetSkipStop(final boolean enable);


	/**
	 * Enable ball placement skipping
	 *
	 * @param enable
	 */
	void onSetSkipBallPlacement(final boolean enable);


	/**
	 * Search for the next command of this type
	 *
	 * @param command
	 */
	void onSearchCommand(SslGcRefereeMessage.Referee.Command command);


	/**
	 * Search for the next game event of this type
	 *
	 * @param gameEvent
	 */
	void onSearchGameEvent(final EGameEvent gameEvent);


	/**
	 * Search for the next game state of the type
	 *
	 * @param gameState
	 */
	void onSearchGameState(final EGameState gameState);

	/**
	 * save snapshot to file
	 */
	void onSnapshot();


	/**
	 * Copy snapshot to clipboard
	 */
	void onCopySnapshot();


	/**
	 * Create a cut replay.
	 */
	void cutReplay();
}
