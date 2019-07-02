/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.eventtypes.ballpossession;

import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.loganalysis.LogEventProtobufMapper;
import edu.tigers.sumatra.loganalysis.eventtypes.IInstantaneousEventType;
import edu.tigers.sumatra.labeler.LogLabels;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import org.apache.log4j.Logger;

import java.util.Optional;


public class BallPossessionEventType implements IInstantaneousEventType
{
	private static final Logger log = Logger.getLogger(BallPossessionEventType.class.getName());

	/** One of the following values: yellow-posses, blue-posses, none */
	private final ETeamColor possessionState;
	
	/** Which robot is in possession (ignored if loose is chosen) */
	private final ITrackedBot robot;
	
	
	public BallPossessionEventType(final ETeamColor possessionState, final ITrackedBot robot)
	{
		this.possessionState = possessionState;
		this.robot = robot;
	}
	
	
	public BallPossessionEventType(final ETeamColor possessionState)
	{
		this.possessionState = possessionState;
		this.robot = null;
	}
	
	
	public ETeamColor getPossessionState()
	{
		return possessionState;
	}
	
	
	public Optional<ITrackedBot> getRobot()
	{
		return Optional.ofNullable(robot);
	}

	@Override
	public void addEventTypeTo(final LogLabels.Labels.Builder labelsBuilder)
	{
		LogEventProtobufMapper mapper = new LogEventProtobufMapper();

		try {
			labelsBuilder.addBallPossessionLabels(mapper.mapBallPossession(this));
		} catch (LogEventProtobufMapper.InconsistentProtobufMapperException e) {
			log.error("Could not add ball possession label", e);
		}
	}
}
