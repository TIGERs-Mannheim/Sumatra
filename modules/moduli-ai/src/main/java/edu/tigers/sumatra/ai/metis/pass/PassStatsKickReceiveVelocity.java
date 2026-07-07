package edu.tigers.sumatra.ai.metis.pass;

import edu.tigers.sumatra.ai.metis.pass.rating.EPassRating;
import edu.tigers.sumatra.math.vector.IVector2;

import java.util.Map;


public record PassStatsKickReceiveVelocity(IVector2 planned,
                                           IVector2 actual,
                                           boolean success,
                                           double passDurationPlanned,
                                           double passDurationActual,
                                           boolean redirect,
                                           Map<EPassRating, Double> scores)
{

}
