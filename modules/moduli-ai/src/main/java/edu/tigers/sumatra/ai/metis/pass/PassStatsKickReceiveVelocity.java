/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass;

import edu.tigers.sumatra.math.vector.IVector2;


public record PassStatsKickReceiveVelocity(IVector2 planned, IVector2 actual, boolean success,
                                           double passDurationPlanned, double passDurationActual, boolean redirect)
{

}
