/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 26, 2012
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.analyze;

/**
 * configuration types for the testing of the path planning, determines which parameter or parameter combination should
 * be tested
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
public enum ETuneableParameter
{
	/** */
	pGoal,
	/** */
	stepSize,
	/** */
	maxIterations,
	/** */
	probabilities
}
