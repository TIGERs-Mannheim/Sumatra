/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.athena;

import edu.tigers.sumatra.ai.metis.MetisAiFrame;


/**
 * Base class for athena adapter. Adapters are used to react differently
 * depending on the current mode, e.g. Match, Test, Emergency
 */
public interface IAthenaAdapter
{
	PlayStrategy process(final MetisAiFrame metisAiFrame, final AthenaGuiInput athenaGuiInput);

	default void stop(final AthenaGuiInput athenaGuiInput)
	{
		// nothing to do by default
	}
}
