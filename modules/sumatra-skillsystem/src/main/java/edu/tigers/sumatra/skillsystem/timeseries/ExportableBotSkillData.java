/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.timeseries;

import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.data.collector.IExportable;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.TrajTrackingQuality;
import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Value
@Builder
public class ExportableBotSkillData implements IExportable
{
	BotID botID;
	long timestamp;
	State bufferedTrajState;
	TrajTrackingQuality trackingQuality;


	@Override
	public List<Number> getNumberList()
	{
		List<Number> numbers = new ArrayList<>();
		numbers.add(botID.getNumber());
		numbers.addAll(botID.getTeamColor().getNumberList());
		numbers.add(timestamp);
		numbers.addAll(Optional.ofNullable(bufferedTrajState).orElse(State.nan()).getNumberList());
		numbers.add(trackingQuality.getCurDistance());
		return numbers;
	}


	@Override
	public List<String> getHeaders()
	{
		return List.of(
				"id",
				"color",
				"timestamp",
				"buffered_pos_x", "buffered_pos_y", "buffered_pos_z",
				"buffered_vel_x", "buffered_vel_y", "buffered_vel_z",
				"dist2Traj"
		);
	}
}
