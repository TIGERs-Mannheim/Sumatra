/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.geometry;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;


/**
 * @author AndreR
 */
public class BallParameters
{
	@Configurable(defValue = "1000", comment = "If the initial kick velocity is below this the ball is assumed to roll [mm/s]")
	private double	avgKickVelThresholdForAcc	= 1000.0;
	
	@Configurable(defValue = "-3600", comment = "Ball sliding acceleration [mm/s^2]", spezis = { "GRSIM", "SUMATRA",
			"LAB", "TISCH", "ROBOCUP", "ANDRE" })
	private double	accSlide							= -3600.0;
	@Configurable(defValue = "-400", comment = "Ball rolling acceleration [mm/s^2]", spezis = { "GRSIM", "SUMATRA",
			"LAB", "TISCH", "ROBOCUP", "ANDRE" })
	private double	accRoll							= -400.0;
	@Configurable(defValue = "0.62", comment = "Fraction of the initial velocity where the ball starts to roll")
	private double	kSwitch							= 0.62;
	@Configurable(defValue = "2000", comment = "Fixed velocity where the ball starts to roll [mm/s]")
	private double	vSwitch							= 2000;
	
	@Configurable(defValue = "0.75", comment = "Chip kick velocity damping factor in XY direction", spezis = { "GRSIM",
			"SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE" })
	private double	chipDampingXY					= 0.75;
	@Configurable(defValue = "0.6", comment = "Chip kick velocity damping factor in Z direction", spezis = { "GRSIM",
			"SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE" })
	private double	chipDampingZ					= 0.6;
	@Configurable(defValue = "40", comment = "If a chipped ball does not reach this height it is considered rolling [mm]")
	private double	minHopHeight					= 40;
	@Configurable(defValue = "150", comment = "Max. ball height that can be intercepted by robots [mm]")
	private double	maxInterceptableHeight		= 150;
	
	static
	{
		ConfigRegistration.registerClass("geom", BallParameters.class);
	}
	
	
	BallParameters()
	{
	}
	
	
	/**
	 * @return the avgKickVelThresholdForAcc
	 */
	public double getAvgKickVelThresholdForAcc()
	{
		return avgKickVelThresholdForAcc;
	}
	
	
	/**
	 * This value is always negative!
	 * 
	 * @return the accSlide
	 */
	public double getAccSlide()
	{
		return accSlide;
	}
	
	
	/**
	 * This value is always negative!
	 * 
	 * @return the accRoll
	 */
	public double getAccRoll()
	{
		return accRoll;
	}
	
	
	/**
	 * @return the kSwitch
	 */
	public double getkSwitch()
	{
		return kSwitch;
	}
	
	
	/**
	 * @return the chipDampingXY
	 */
	public double getChipDampingXY()
	{
		return chipDampingXY;
	}
	
	
	/**
	 * @return the chipDampingZ
	 */
	public double getChipDampingZ()
	{
		return chipDampingZ;
	}
	
	
	public double getMinHopHeight()
	{
		return minHopHeight;
	}
	
	
	public double getMaxInterceptableHeight()
	{
		return maxInterceptableHeight;
	}
	
	
	/**
	 * @return vswitch
	 */
	public double getvSwitch()
	{
		return vSwitch;
	}
}
