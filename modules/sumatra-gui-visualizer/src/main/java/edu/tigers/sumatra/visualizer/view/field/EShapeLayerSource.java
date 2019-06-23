/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.visualizer.view.field;

import edu.tigers.sumatra.ids.EAiTeam;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EShapeLayerSource
{
	/**  */
	CAM,
	/**  */
	WP,
	/**  */
	AI_YELLOW_PRIMARY,
	/**  */
	AI_BLUE_PRIMARY,
	/** */
	AI_YELLOW_SECONDARY,
	/** */
	AI_BLUE_SECONDARY,
	/**  */
	AUTOREFEREE;
	
	
	/**
	 * @param teamColor
	 * @return
	 */
	public static EShapeLayerSource forAiTeam(final EAiTeam teamColor)
	{
		switch (teamColor)
		{
			case YELLOW_PRIMARY:
				return AI_YELLOW_PRIMARY;
			case BLUE_PRIMARY:
				return AI_BLUE_PRIMARY;
			case YELLOW_SECONDARY:
				return AI_YELLOW_SECONDARY;
			case BLUE_SECONDARY:
				return AI_BLUE_SECONDARY;
			default:
				throw new IllegalArgumentException();
		}
	}
}
