/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.04.2015
 * Author(s): JulianT
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.support;


/**
 * Enum to choose the SupportPosition implementation.
 * 
 * @author JulianT
 */
public enum ESupportPosition
{
	/**
	 * (Pseudo) random based
	 */
	RANDOM(new RandomSupportPosition()),
	/**
	 * RedirectPosGPUCalc based
	 */
	GPUGrid(new GPUGridSupportPosition());
	
	
	private ASupportPosition	supportPosition;
	
	
	private ESupportPosition(final ASupportPosition supportPosition)
	{
		this.supportPosition = supportPosition;
	}
	
	
	/**
	 * @return The SupportPosition
	 */
	public ASupportPosition getSupportPosition()
	{
		return supportPosition;
	}
}
