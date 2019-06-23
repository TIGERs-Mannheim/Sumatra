/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 3, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver;

/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IKickPathDriver extends IPathDriver
{
	/**
	 * @return
	 */
	boolean isReceiving();
	
	
	/**
	 * @return
	 */
	boolean armKicker();
	
	
	/**
	 * @param shootSpeed
	 */
	default void setShootSpeed(final float shootSpeed)
	{
	}
	
	
	/**
	 * @param allowed
	 */
	void setPenAreaAllowed(boolean allowed);
}
