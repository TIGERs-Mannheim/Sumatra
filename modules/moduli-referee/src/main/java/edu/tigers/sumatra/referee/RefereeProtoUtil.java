/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee;

import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;


/**
 * Utility class for operations on protobuf classes that can not be integrated into the generated classes.
 * Please do NOT add non-protobuf related methods here.
 */
@SuppressWarnings("squid:CallToDeprecatedMethod") // support of deprecated methods still desired
public final class RefereeProtoUtil
{

	private RefereeProtoUtil()
	{
	}


	/**
	 * Get the team for the given command
	 *
	 * @param command
	 * @return
	 */
	@SuppressWarnings("squid:MethodCyclomaticComplexity") // false-positive on enum switch
	public static ETeamColor teamForCommand(SslGcRefereeMessage.Referee.Command command)
	{
		switch (command)
		{
			case HALT:
			case STOP:
			case NORMAL_START:
			case FORCE_START:
				return ETeamColor.NEUTRAL;
			case PREPARE_KICKOFF_BLUE:
			case PREPARE_PENALTY_BLUE:
			case DIRECT_FREE_BLUE:
			case INDIRECT_FREE_BLUE:
			case TIMEOUT_BLUE:
			case BALL_PLACEMENT_BLUE:
			case GOAL_BLUE:
				return ETeamColor.BLUE;
			case PREPARE_KICKOFF_YELLOW:
			case PREPARE_PENALTY_YELLOW:
			case DIRECT_FREE_YELLOW:
			case INDIRECT_FREE_YELLOW:
			case TIMEOUT_YELLOW:
			case BALL_PLACEMENT_YELLOW:
			case GOAL_YELLOW:
				return ETeamColor.YELLOW;
			default:
				throw new IllegalStateException("Unknown command: " + command);
		}
	}
}
