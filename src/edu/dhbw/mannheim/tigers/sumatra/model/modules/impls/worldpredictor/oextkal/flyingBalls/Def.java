/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 17, 2011
 * Author(s): Birgit
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamCalibration;


/**
 * Some Data for initial
 * 
 * @author Birgit
 * 
 */
public final class Def
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger		log								= Logger.getLogger(Def.class.getName());
	
	// parameter for test
	/** */
	public static double					eps								= 12e-12;
	/** */
	public static final double			HUND								= 0.01;
	/** */
	public static final double			ZEHN								= 0.1;
	
	// parameter for run
	
	// Parameter
	/** mm */
	public static final double			MIN_FLY_HEIGHT					= 300;
	/** mm */
	public static final double			MIN_FLY_LENGTH					= 400;
	/** */
	public static final double			START_BOT2BALL_MAX_ANGLE	= Math.PI / 3;
	/** */
	public static final double			RUN_BOT2BALL_MAX_ANGLE		= Math.PI / 6;
	/** mm */
	public static final double			BALL2BALL_MIN_DISTANCE		= 30;
	
	// feste Konstanten
	/** mm */
	public static final double			KICK_RADIUS_AROUND_BOT		= 100;
	/** mm */
	public static final double			BOT_RADIUS						= 70;
	/** */
	public static final double			DUMMY								= Double.NEGATIVE_INFINITY;
	/** */
	public static final int				MAX_NUMBER_FLYS				= 3;
	/** */
	public static final int				MAX_NUMBER_BALLS_GO_BACK	= 2;
	
	
	/** */
	public static final boolean		DEBUG								= false;
	/** */
	public static final boolean		DEBUG_CAM						= false;
	/** */
	public static final boolean		DEBUG_FLY_HEIGHT				= false;
	
	/** */
	public static boolean				isParametersSet				= false;
	/** */
	public static double					camHeight						= Def.DUMMY;
	
	/** */
	public static Map<Integer, Cam>	cams								= new HashMap<Integer, Cam>();
	
	/**
	 */
	public static class Cam
	{
		int		id	= -1;
		double	x	= Def.DUMMY;
		double	y	= Def.DUMMY;
	}
	
	
	private Def()
	{
	}
	
	
	/**
	 * @param aCamHeight
	 * @param camCalibration
	 */
	public static void setParameter(double aCamHeight, List<CamCalibration> camCalibration)
	{
		for (CamCalibration camCal : camCalibration)
		{
			Cam cam = new Cam();
			cam.id = camCal.cameraId;
			cam.x = camCal.derivedCameraWorldTx;
			cam.y = camCal.derivedCameraWorldTy;
			cams.put(camCal.cameraId, cam);
		}
		camHeight = aCamHeight;
		
		isParametersSet = true;
		
		if (Def.DEBUG)
		{
			Level old = log.getLevel();
			log.setLevel(Level.INFO);
			log.info("Parameter gesetzt: ");
			log.info("camHeight" + aCamHeight);
			log.setLevel(old);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	// help-functions
	/**
	 * @param x
	 * @param y
	 * @param eps
	 * @return
	 */
	public static boolean equals(double x, double y, double eps)
	{
		if (Math.abs(x - y) < eps)
		{
			return true;
		}
		return false;
	}
	
	
	/**
	 * @param ma
	 * @return
	 */
	public static String jamaMatrixToString(Jama.Matrix ma)
	{
		final StringBuilder str = new StringBuilder();
		
		int i = -1;
		int j = -1;
		// for all rows
		for (i = 0; i < ma.getRowDimension(); i++)
		{
			for (j = 0; j < ma.getColumnDimension(); j++)
			{
				str.append("[");
				str.append(String.format("% 10.4e ", ma.get(i, j)));
				str.append("]");
			}
			str.append("\n");
		}
		return str.toString();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
