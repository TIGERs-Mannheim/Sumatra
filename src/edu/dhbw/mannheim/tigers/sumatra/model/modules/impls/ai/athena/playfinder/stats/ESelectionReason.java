/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 16, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.stats;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.LearningPlayFinder;


/**
 * Reason for {@link LearningPlayFinder} selection
 * 
 * Do NOT refactor names! They are stored in DB by name
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public enum ESelectionReason
{
	/** chose random */
	RANDOM,
	/** found successful field on first try */
	SUCCESSFUL_FIRST_TRY,
	/** successful field found, but not on first try */
	SUCCESSFUL_MULTIPLE_TRIES,
	/** successful field found, but there were equal matched before */
	SUCCESSFUL_EQUAL_MATCH,
	/** selected by a referee command */
	REFEREE,
	/** chosen in test mode */
	MANUEL,
	/**  */
	HELPER,
	/** not set yet */
	UNKNOWN,
}
