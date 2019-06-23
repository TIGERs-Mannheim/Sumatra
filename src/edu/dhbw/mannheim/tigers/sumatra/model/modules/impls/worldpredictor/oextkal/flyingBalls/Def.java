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

import edu.dhbw.mannheim.tigers.sumatra.test.junit.model.modules.impls.worldpredictor.oextkal.flyingBalls.cases.ATestCase;


/**
 * Some Data for initial
 * 
 * @author Birgit
 * 
 */
public class Def
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	//parameter for test
	public static double eps = 12e-12;
	public static double hund = 0.01;
	public static double zehn = 0.1;
	
	//parameter for run

	//Parameter 
	public static final double	MIN_FLY_HEIGHT						= 300;//mm
	public static final double	MIN_FLY_LENGTH						= 400;//mm
	public static final double START_BOT2BALL_MAX_ANGLE 		= Math.PI/3;
	public static final double RUN_BOT2BALL_MAX_ANGLE 			= Math.PI/6;
	public static final double BALL2BALL_MIN_DISTANCE			= 30;//mm
	
	//feste Konstanten
	public static final double KICK_RADIUS_AROUND_BOT 			= 100;//mm
	public static final double BOT_RADIUS							= 70;//mm
	public static final double DUMMY 								= Double.NEGATIVE_INFINITY;
	public static final int		MAX_NUMBER_FLYS					= 3;
	public static final int		MAX_NUMBER_BALLS_GO_BACK		= 2;
	

	
	public static boolean debug = false;
	public static boolean debugCam = false;
	public static boolean debugFlyHeight = false;
	
	public static boolean isParametersSet = false;
	public static double CamHeight = Def.DUMMY;
	public static double CamNullX = Def.DUMMY;
	public static double CamNullY = Def.DUMMY;
	public static double CamOneX = Def.DUMMY;
	public static double CamOneY = Def.DUMMY;
	public static int	CamIDNull = -1;
	public static int CamIDOne = -1;
	
	public static void setParameter(
			  double aCamHeight,
			  double aCamNullX,
			  double aCamNullY,
			  int aNullCamID,
			  double aCamOneX,
			  double aCamOneY,
			  int aOneCamID)
	{	
		CamHeight = aCamHeight;
		CamNullX  = aCamNullX;
		CamNullY  = aCamNullY;
		CamIDNull = aNullCamID;
		CamOneX  = aCamOneX;
		CamOneY  = aCamOneY;
		CamIDOne = aOneCamID;
		
		isParametersSet = true;
		
		if(Def.debug)
		{
		System.err.println("Parameter gesetzt: ");
		System.out.println("CamHeight"+aCamHeight);
		System.out.println("CamNullX "+aCamNullX );
		System.out.println("CamNullY "+aCamNullY );
		System.out.println("NullID"+aNullCamID);
		System.out.println("CamOneX "+aCamOneX );
		System.out.println("CamOneY "+aCamOneY );
		System.out.println("OneCamID"+aOneCamID);
		}
	}
	


	//public static ATestCase t = new TestCaseFour();
	public static ATestCase t = null;
	

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	//help-functions
	public static boolean equals(double x, double y, double eps)
	{
		if(Math.abs(x-y) < eps)
		{
			return true;
		}
		return false;
	}
	
	
	public static String toString(Jama.Matrix ma)
	{
		String str = "";
		
		int i = -1;
		int j = -1;
		// for all rows
		for (i = 0; i < ma.getRowDimension(); i++)
		{
			for (j = 0; j < ma.getColumnDimension(); j++)
			{
				str += "[";
				str += String.format("% 10.4e ", ma.get(i,j));
				str += "]";
			}
			str += "\n";
		}
		return str;	
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
