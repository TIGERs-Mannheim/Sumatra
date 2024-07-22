/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.movementlimits;

import edu.tigers.sumatra.bot.EDribbleTractionState;
import edu.tigers.sumatra.data.ITimestampBased;
import edu.tigers.sumatra.data.TimestampBasedBuffer;
import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.KickedBall;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.extern.log4j.Log4j2;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Log4j2
public class KickSpeedObserver implements IWorldFrameObserver
{
	private final Map<BotID, TimestampBasedBuffer<KickInfo>> kickInfoBuffers = new HashMap<>();
	private CSVExporter csvExporter;

	private KickedBall lastKickedBall;


	public void start()
	{
		csvExporter = new CSVExporter(
				Path.of("data", "kickSpeed"),
				"kickSpeed",
				CSVExporter.EMode.APPEND_DATE);
		csvExporter.setHeader(List.of(
				"timestamp",
				"botId",
				"chipped",
				"dribbleTractionState",
				"plannedKickSpeed",
				"actualKickSpeed"
		));
	}


	public void stop()
	{
		csvExporter.close();
	}


	@Override
	public void onNewWorldFrame(WorldFrameWrapper wFrameWrapper)
	{
		wFrameWrapper.getSimpleWorldFrame().getBots().values().stream()
				.map(bot -> new KickInfo(
								bot.getTimestamp(),
								bot.getBotId(),
								bot.getRobotInfo().getKickSpeed(),
								bot.getRobotInfo().isChip(),
								bot.getRobotInfo().getDribbleTraction()
						)
				).forEach(kickInfo ->
						kickInfoBuffers.computeIfAbsent(kickInfo.botID, id -> new TimestampBasedBuffer<>(3)).add(kickInfo));

		var currentKickedball = wFrameWrapper.getSimpleWorldFrame().getKickedBall().orElse(null);
		if (!Objects.equals(currentKickedball, lastKickedBall))
		{
			if (lastKickedBall != null && lastKickedBall.getKickingBot().isBot())
			{

				List<KickInfo> samples = kickInfoBuffers.get(lastKickedBall.getKickingBot()).getData().stream()
						.filter(kickInfo -> Math.abs(kickInfo.timestamp - lastKickedBall.getKickTimestamp()) / 1e9 < 0.5)
						.filter(kickInfo -> kickInfo.kickSpeed > 0)
						.toList();
				if (!samples.isEmpty())
				{
					KickInfo kickInfo = samples.getLast();
					report(lastKickedBall, kickInfo);
				}
			}
			lastKickedBall = currentKickedball;
		}
	}


	private void report(KickedBall kickedBall, KickInfo kickInfo)
	{
		log.debug("Kicked ball: {} {}", kickInfo, kickedBall.getAbsoluteKickSpeed());

		csvExporter.addValues(
				List.of(
						kickInfo.getTimestamp(),
						kickInfo.botID.getSaveableString(),
						kickInfo.chipped,
						kickInfo.dribbleTractionState,
						kickInfo.kickSpeed,
						kickedBall.getAbsoluteKickSpeed()
				)
		);
	}


	record KickInfo(
			long timestamp,
			BotID botID,
			double kickSpeed,
			boolean chipped,
			EDribbleTractionState dribbleTractionState
	) implements ITimestampBased
	{
		@Override
		public long getTimestamp()
		{
			return timestamp;
		}
	}
}
