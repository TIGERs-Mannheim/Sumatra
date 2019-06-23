/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.01.2011
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types;

/**
 * This interface should be used by objects which
 * contain a scoring criteria/value.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public interface IValueObject
{
	/**
	 * 
	 * returns scoring criteria/value of this object.
	 * 
	 * @return scoring value
	 */
	public float getValue();
	

	/**
	 * 
	 * set scoring criteria/value of this object.
	 * 
	 * @param scoring value
	 */
	public void setValue(float value);
	
}
