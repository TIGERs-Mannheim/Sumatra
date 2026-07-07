package edu.tigers.sumatra.ai.metis.pass;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.ai.metis.kicking.OngoingPass;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.wp.data.WorldFrame;


public class OngoingPassSuccessRater
{
	@Configurable(defValue = "0.35", unit = EConfigUnit.TIME_S)
	private static double maxDurationDif = 0.35;

	@Configurable(defValue = "500.0", unit = EConfigUnit.DISTANCE_MM)
	private static double maxDistanceToPlannedReceivePosition = 500.0;

	static
	{
		ConfigRegistration.registerClass("metis", OngoingPassSuccessRater.class);
	}

	private OngoingPassSuccessRater()
	{
		// hide public constructor
	}


	public static boolean isOngoingPassASuccess(WorldFrame worldFrame, OngoingPass currentPass)
	{
		var plannedReceiver = worldFrame.getTiger(currentPass.getPass().getPass().getReceiver());
		if (plannedReceiver == null)
		{
			return false;
		}
		double distBallToReceiver = plannedReceiver.getBotKickerPos().distanceTo(worldFrame.getBall().getPos());
		double distReceiverToPassDest = plannedReceiver.getPos()
				.distanceTo(currentPass.getPass().getPass().getKick().getTarget());
		double durationDif = Math.abs(
				(currentPass.getKickStartTime() + currentPass.getPass().getPass().getDuration() * 1e9)
						- worldFrame.getTimestamp())
				* 1e-9;
		return distBallToReceiver < Geometry.getBotRadius()
				&& distReceiverToPassDest < maxDistanceToPlannedReceivePosition && durationDif < maxDurationDif;
	}
}
