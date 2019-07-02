/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.vision.data;

import edu.tigers.sumatra.vision.kick.estimators.IBallModelIdentResult;


/**
 * Notifications for kicks with identified model parameters.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public interface IBallModelIdentificationObserver
{
	default void onBallModelIdentificationResult(final IBallModelIdentResult ident)
	{
	}
}