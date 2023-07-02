/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.kicking.OngoingPass;
import edu.tigers.sumatra.export.CSVExporter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * Track the ongoing pass by matching passes with the ball kick fit state.
 */
@Log4j2
@RequiredArgsConstructor
public class PassStatisticsExporterCalc extends ACalculator
{
	private final Supplier<Optional<OngoingPass>> ongoingPass;

	private OngoingPass currentPass;

	private CSVExporter exporter = null;

	@Configurable(defValue = "false")
	private static boolean activateRecording = false;


	@Override
	protected void doCalc()
	{
		if (!activateRecording && exporter == null)
		{
			return;
		} else if (!activateRecording)
		{
			log.info("Closing pass data recorder");
			exporter.close();
			exporter = null;
			return;
		}

		if (exporter == null)
		{
			initExporter();
		}

		var ongoingPassOpt = ongoingPass.get();
		if (ongoingPassOpt.isPresent())
		{
			if (currentPass != null && ongoingPassOpt.get().getKickStartTime() != currentPass.getKickStartTime())
			{
				// new pass started.
				fillAndWriteData();
			}
			currentPass = ongoingPassOpt.get();
		} else if (currentPass != null)
		{
			// old pass vanished
			fillAndWriteData();
			currentPass = null;
		}
	}


	private void fillAndWriteData()
	{
		List<Number> values = new ArrayList<>();
		values.add(currentPass.getKickStartTime());
		values.add(currentPass.getPass().getKick().getSource().x());
		values.add(currentPass.getPass().getKick().getSource().y());
		values.add(currentPass.getOriginatingZone().ordinal());
		values.add(currentPass.getPass().getKick().getTarget().x());
		values.add(currentPass.getPass().getKick().getTarget().y());
		values.add(currentPass.getTargetZone().ordinal());

		if (OngoingPassSuccessRater.isOngoingPassASuccess(getWFrame(), currentPass))
		{
			values.add(1);
		} else
		{
			values.add(0);
		}

		exporter.addValues(values);
	}


	private void initExporter()
	{
		String name = "./pass_data_" + getWFrame().getTeamColor().toString();
		log.info("Started recording pass data: " + name);
		exporter = new CSVExporter("./data/pass/", name, CSVExporter.EMode.AUTO_INCREMENT_FILE_NAME);

		List<String> headers = new ArrayList<>();
		headers.add("KickStartTime");
		headers.add("sourceX");
		headers.add("sourceY");
		headers.add("sourceZone");
		headers.add("targetX");
		headers.add("targetY");
		headers.add("targetZone");
		headers.add("success");

		exporter.setHeader(headers);
	}

}
