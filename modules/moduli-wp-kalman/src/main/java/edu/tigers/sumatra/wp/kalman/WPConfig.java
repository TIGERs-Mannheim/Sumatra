/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.08.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.wp.kalman;


/**
 * This class holds some configuration parameters for the WorldPredictor
 * 
 * @author Gero
 * @author Maren
 * @author Peter
 * @author Massl
 * @author Birgit
 */
public final class WPConfig
{
	/** */
	public static final double	FILTER_CONVERT_NS_TO_INTERNAL_TIME				= 1e-9;
	/** */
	public static final double	FILTER_CONVERT_MM_TO_INTERNAL_UNIT				= 1e0;
	
	/** */
	public static final double	FILTER_CONVERT_M_TO_INTERNAL_UNIT				= 1e3 * FILTER_CONVERT_MM_TO_INTERNAL_UNIT;
	/** */
	public static final double	FILTER_CONVERT_MperS_TO_INTERNAL_V				= (1e-6 * FILTER_CONVERT_MM_TO_INTERNAL_UNIT)
																											/ FILTER_CONVERT_NS_TO_INTERNAL_TIME;
	/** */
	public static final double	FILTER_CONVERT_MperSS_TO_INTERNAL_A				= (1e-15 * FILTER_CONVERT_MM_TO_INTERNAL_UNIT)
																											/ (FILTER_CONVERT_NS_TO_INTERNAL_TIME * FILTER_CONVERT_NS_TO_INTERNAL_TIME);
	/** */
	public static final double	FILTER_CONVERT_RadPerS_TO_RadPerInternal		= 1e-9 / FILTER_CONVERT_NS_TO_INTERNAL_TIME;
	/** */
	public static final double	FILTER_CONVERT_RadPerSS_TO_RadPerInternalSQ	= 1e-18 / (FILTER_CONVERT_NS_TO_INTERNAL_TIME * FILTER_CONVERT_NS_TO_INTERNAL_TIME);
	
	
	/** */
	public static final int		ADD_MIN_FRAMES_BOTS									= 10;
	
	/** [internalTime] */
	public static final double	ADD_MAX_TIME_BOT										= (0.50);
	/** [internalTime] */
	public static final double	ADD_MAX_TIME_BALL										= (0.10);
	
	/** [internalTime] */
	public static final double	REM_MAX_TIME_BOT										= (1.50);
	/** [internalTime] */
	public static final double	REM_MAX_TIME_BALL										= (0.10);
	
	/**
	 * [internalTime] Time that have to be passed before a new observation of a bot is accepted.
	 */
	public static final double	MIN_CAMFRAME_DELAY_TIME								= 0.0001;
	
	/**
	 * internal id shifting:
	 * tigerbots id: 100-199
	 * foodbots id: 200-299
	 * ball id: 0
	 * tiger_id_offset have to be smaller than food_id_offset
	 */
	public static final int		YELLOW_ID_OFFSET										= 100;
	/** */
	public static final int		BLUE_ID_OFFSET											= 200;
	
	
	/*
	 * Possible Selections
	 * 0: extended Kalman Filtering
	 * 1: Particlefiltering
	 */
	
	/** */
	public static final int		BALL_MODULE												= 0;
	/** */
	public static final int		BLUE_MODULE												= 0;
	/** */
	public static final int		YELLOW_MODULE											= 0;
	
	
	private WPConfig()
	{
		
	}
}
