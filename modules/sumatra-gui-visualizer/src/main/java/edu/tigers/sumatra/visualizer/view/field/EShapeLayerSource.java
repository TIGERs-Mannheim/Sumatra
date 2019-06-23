/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 14, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.visualizer.view.field;

import edu.tigers.sumatra.ids.ETeamColor;


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
	AI_YELLOW,
	/**  */
	AI_BLUE,
	/**  */
	AUTOREFEREE;
	
	
	/**
	 * @param teamColor
	 * @return
	 */
	public static EShapeLayerSource forTeamColor(final ETeamColor teamColor)
	{
		switch (teamColor)
		{
			case BLUE:
				return EShapeLayerSource.AI_BLUE;
			case YELLOW:
				return EShapeLayerSource.AI_YELLOW;
			case UNINITIALIZED:
			default:
				throw new IllegalArgumentException();
		}
	}
}
