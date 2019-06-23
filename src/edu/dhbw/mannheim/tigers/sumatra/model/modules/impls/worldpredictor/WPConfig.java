/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.08.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor;


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
	// :::>>> (export)
	/** */
	public static final double		FILTER_CONVERT_NS_TO_INTERNAL_TIME				= 1e-9;
	/** */
	public static final double		FILTER_CONVERT_MM_TO_INTERNAL_UNIT				= 1e0;
	// :::<<<
	
	/** */
	public static final double		FILTER_CONVERT_M_TO_INTERNAL_UNIT				= 1e3 * FILTER_CONVERT_MM_TO_INTERNAL_UNIT;
	/** */
	public static final double		FILTER_CONVERT_MperS_TO_INTERNAL_V				= (1e-6 * FILTER_CONVERT_MM_TO_INTERNAL_UNIT)
																												/ FILTER_CONVERT_NS_TO_INTERNAL_TIME;
	/** */
	public static final double		FILTER_CONVERT_MperSS_TO_INTERNAL_A				= (1e-15 * FILTER_CONVERT_MM_TO_INTERNAL_UNIT)
																												/ (FILTER_CONVERT_NS_TO_INTERNAL_TIME * FILTER_CONVERT_NS_TO_INTERNAL_TIME);
	/** */
	public static final double		FILTER_CONVERT_RadPerS_TO_RadPerInternal		= 1e-9 / FILTER_CONVERT_NS_TO_INTERNAL_TIME;
	/** */
	public static final double		FILTER_CONVERT_RadPerSS_TO_RadPerInternalSQ	= 1e-18 / (FILTER_CONVERT_NS_TO_INTERNAL_TIME * FILTER_CONVERT_NS_TO_INTERNAL_TIME);
	
	/** = System.nanoTime() */
	private static long				filterTimeOffset;
	
	
	/** */
	public static final long		S_IN_NS													= 1000000000l;
	
	/**
	 * [internalTime] How often the
	 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.TrackingManager} should check
	 * the data in the
	 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.PredictionContext}
	 */
	public static final double		TRACKING_CHECK_INTERVAL								= (0.08 * S_IN_NS * FILTER_CONVERT_NS_TO_INTERNAL_TIME);
	
	/** */
	public static final int			ADD_MIN_FRAMES_BOTS									= 10;
	/** */
	public static final int			ADD_MIN_FRAMES_BALL									= 2;
	
	/** [internalTime] */
	public static final double		ADD_MAX_TIME_BOT										= (0.50 * S_IN_NS * FILTER_CONVERT_NS_TO_INTERNAL_TIME);
	/** [internalTime] */
	public static final double		ADD_MAX_TIME_BALL										= (0.10 * S_IN_NS * FILTER_CONVERT_NS_TO_INTERNAL_TIME);
	
	/** [internalTime] */
	public static final double		REM_MAX_TIME_BOT										= (2.00 * S_IN_NS * FILTER_CONVERT_NS_TO_INTERNAL_TIME);
	/** [internalTime] */
	public static final double		REM_MAX_TIME_BALL										= (0.10 * S_IN_NS * FILTER_CONVERT_NS_TO_INTERNAL_TIME);
	
	/**
	 * [internalTime] Time that have to be passed before a new observation of a bot is accepted.
	 */
	// 1ms
	public static final double		MIN_CAMFRAME_DELAY_TIME								= 0.001
																												* S_IN_NS
																												* FILTER_CONVERT_NS_TO_INTERNAL_TIME;
	
	// :::>>> (export)
	
	/**
	 * internal id shifting:
	 * tigerbots id: 100-199
	 * foodbots id: 200-299
	 * ball id: 0
	 * 
	 * tiger_id_offset have to be smaller than food_id_offset
	 */
	public static final int			TIGER_ID_OFFSET										= 100;
	/** */
	public static final int			FOOD_ID_OFFSET											= 200;
	
	/** */
	public static final boolean	DEBUG														= false;
	/** */
	public static final int			DEBUGID													= 0;
	/** */
	public static final String		DEBUGFILE												= "D:\\log.txt";
	
	/** */
	public static final boolean	DEBUG_FLYING											= false;
	/** */
	public static final String		FLYING_DEBUGFILE										= "D:\\logFlying.txt";
	
	/** */
	public static final boolean	ADD_NOISE												= false;
	/** maximal noise (mm) */
	public static final float		NOISE_S													= 10.0f;
	/** maximal noise (rad) */
	public static final float		NOISE_R													= 0.09f;
	// :::<<<
	
	
	/*
	 * Possible Selections
	 * 0: extended Kalman Filtering
	 * 1: Particlefiltering
	 */
	
	// :::>>> (export)
	/** */
	public static final int			BALL_MODULE												= 0;
	/** */
	public static final int			FOOD_MODULE												= 0;
	/** */
	public static final int			TIGER_MODULE											= 0;
	// :::<<<
	
	// enable BallCorrector Module
	/** */
	public static final boolean	CORRECT_BALL_DATA										= true;
	
	
	private WPConfig()
	{
		
	}
	
	
	/**
	 * @return the filterTimeOffset
	 */
	public static long getFilterTimeOffset()
	{
		return filterTimeOffset;
	}
	
	
	/**
	 * @param filterTimeOffset the filterTimeOffset to set
	 */
	public static void setFilterTimeOffset(long filterTimeOffset)
	{
		WPConfig.filterTimeOffset = filterTimeOffset;
	}
}
