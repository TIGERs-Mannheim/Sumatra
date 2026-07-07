package edu.tigers.sumatra.vision.kick.estimators;

import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.vision.data.KickSolverResult;

import java.util.List;
import java.util.Optional;


/**
 * @author AndreR <andre@ryll.cc>
 */
@FunctionalInterface
public interface IKickSolver
{
	/**
	 * Take a list of records and somehow try to find a 3D kick velocity.
	 *
	 * @param records
	 * @return Kick velocity, if the solver found one.
	 */
	Optional<KickSolverResult> solve(final List<CamBall> records);
}
