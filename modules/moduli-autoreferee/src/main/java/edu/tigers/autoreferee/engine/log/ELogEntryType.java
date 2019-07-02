package edu.tigers.autoreferee.engine.log;

import java.awt.Color;


public enum ELogEntryType
{
	/** Describes a change in the game state e.g. RUNNING or STOP */
	GAME_STATE(new Color(50, 50, 250)),

	/** Game event from this autoRef */
	DETECTED_GAME_EVENT(new Color(250, 17, 228)),

	/** New referee message received */
	RECEIVED_REFEREE_MSG(new Color(50, 50, 50)),

	/** new game event received */
	RECEIVED_GAME_EVENT(new Color(0, 180, 0)),

	;

	private final Color color;


	ELogEntryType(final Color color)
	{
		this.color = color;
	}


	public Color getColor()
	{
		return color;
	}
}
