/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 13, 2012
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays;

/**
 * 
 * RUNNING
 * SUCCEEDED
 * FAILED
 * FINISHED
 * 
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public enum EPlayState
{
	/** */
	RUNNING,
	/** */
	SUCCEEDED,
	/** */
	FAILED,
	/** neutral finished state */
	FINISHED;
	
	@Override
	public String toString()
	{
		return name();
	}
	
}
