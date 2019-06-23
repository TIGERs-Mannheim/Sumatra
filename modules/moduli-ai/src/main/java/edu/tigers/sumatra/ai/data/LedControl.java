/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.07.2016
 * Author(s): kisle
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data;

/**
 * @author MarkG
 */
public class LedControl
{
	private boolean	leftGreen	= false;
	private boolean	leftRed		= false;
	private boolean	rightGreen	= false;
	private boolean	rightRed		= false;
	
	private boolean	insane		= false;
	
	
	/**
	 * 
	 */
	public LedControl()
	{
		
	}
	
	
	/**
	 * @param lg
	 * @param lr
	 * @param rg
	 * @param rr
	 */
	public LedControl(final boolean lg, final boolean lr, final boolean rg, final boolean rr)
	{
		leftGreen = lg;
		leftRed = lr;
		rightGreen = rg;
		rightRed = rr;
	}
	
	
	/**
	 * @return the leftGreen
	 */
	public boolean isLeftGreen()
	{
		return leftGreen;
	}
	
	
	/**
	 * @return the leftRed
	 */
	public boolean isLeftRed()
	{
		return leftRed;
	}
	
	
	/**
	 * @return the rightGreen
	 */
	public boolean isRightGreen()
	{
		return rightGreen;
	}
	
	
	/**
	 * @return the rightRed
	 */
	public boolean isRightRed()
	{
		return rightRed;
	}
	
	
	/**
	 * @param leftGreen the leftGreen to set
	 */
	public void setLeftGreen(final boolean leftGreen)
	{
		this.leftGreen = leftGreen;
	}
	
	
	/**
	 * @param leftRed the leftRed to set
	 */
	public void setLeftRed(final boolean leftRed)
	{
		this.leftRed = leftRed;
	}
	
	
	/**
	 * @param rightGreen the rightGreen to set
	 */
	public void setRightGreen(final boolean rightGreen)
	{
		this.rightGreen = rightGreen;
	}
	
	
	/**
	 * @param rightRed the rightRed to set
	 */
	public void setRightRed(final boolean rightRed)
	{
		this.rightRed = rightRed;
	}
	
	
	/**
	 * @return the insane
	 */
	public boolean isInsane()
	{
		return insane;
	}
	
	
	/**
	 * @param insane the insane to set
	 */
	public void setInsane(final boolean insane)
	{
		this.insane = insane;
	}
}
