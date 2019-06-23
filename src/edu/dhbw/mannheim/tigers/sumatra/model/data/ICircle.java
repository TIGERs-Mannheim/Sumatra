/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.04.2011
 * Author(s): Malte
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

/**
 * Circle interface.
 * 
 * @author Malte
 * 
 */
public interface ICircle extends I2DShape
{
	
	public float radius();
	
	public IVector2 center();
}
