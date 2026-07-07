package edu.tigers.sumatra.ai.metis.kicking;

import edu.tigers.sumatra.ai.metis.offense.situation.zone.EOffensiveZone;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;


/**
 * Stores information about a pass that is already on its way.
 */
@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class OngoingPass
{
	@NonNull
	RatedPass pass;
	long kickStartTime;

	EOffensiveZone originatingZone;
	EOffensiveZone targetZone;

	boolean passLineBeenReachedByReceiver;
}
