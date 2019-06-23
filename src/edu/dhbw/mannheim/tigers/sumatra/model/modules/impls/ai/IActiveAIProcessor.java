/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.08.2010
 * Author(s): Gero
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai;

import edu.moduli.AModule;

/**
 * This interface allows extended control over the AI-sub-modules
 * 
 * @author Gero
 * 
 */
public interface IActiveAIProcessor
{
	/**
	 * The equivalent to {@link AModule#startModule()} in the moduli!
	 */
	public void start();
	
	/**
	 * The equivalent to {@link AModule#stopModule()} in the moduli!
	 */
	public void stop();
}
