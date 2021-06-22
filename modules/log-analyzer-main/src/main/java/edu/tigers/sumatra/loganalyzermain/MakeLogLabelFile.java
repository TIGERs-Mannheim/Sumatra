/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalyzermain;

import com.github.g3force.configurable.ConfigRegistration;
import edu.tigers.sumatra.cam.ACam;
import edu.tigers.sumatra.cam.LogfileAnalyzerVisionCam;
import edu.tigers.sumatra.gamelog.SSLGameLogLabelFileWriter;
import edu.tigers.sumatra.gamelog.SSLGameLogReader;
import edu.tigers.sumatra.gamelog.proto.LogLabels;
import edu.tigers.sumatra.loganalysis.LogAnalysis;
import edu.tigers.sumatra.model.SumatraModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumMap;
import java.util.Map;


public class MakeLogLabelFile
{
	private static final Logger log = LogManager.getLogger(MakeLogLabelFile.class.getName());


	/**
	 * @param args <inputLabelerFile> <outputLabelFile>
	 */
	public static void main(String[] args)
	{
		if (args.length != 2)
		{
			log.error("Invalid parameters. Pass <inputLabelerFile> <outputLabelFile>");
			System.exit(1);
		}

		String importPathLabelerDataFile = args[0];
		String exportPathLabelFile = args[1];

		try
		{
			ConfigRegistration.setDefPath("../../config/");
			SumatraModel.getInstance()
					.setCurrentModuliConfig("../../modules/log-analyzer-main/config/moduli/makeloglabelfile.xml");
			SumatraModel.getInstance()
					.loadModulesOfConfig("../../modules/log-analyzer-main/config/moduli/makeloglabelfile.xml");
			SumatraModel.getInstance().startModules();
		} catch (Throwable e)
		{
			log.error("Could not start Sumatra.", e);
			System.exit(1);
		}

		final LogfileAnalyzerVisionCam logfileAnalyzerVisionCam = (LogfileAnalyzerVisionCam) SumatraModel.getInstance()
				.getModule(ACam.class);

		log.info("loading log from file");
		SSLGameLogReader reader = importLabelerDataFile(importPathLabelerDataFile);
		log.info("DONE");

		log.info("Start playing log");
		logfileAnalyzerVisionCam.playLog(reader,
				frameId -> SumatraModel.getInstance().getModule(LogAnalysis.class).process(frameId));
		log.info("DONE");

		log.info("Num frames: " + logfileAnalyzerVisionCam.getTimestampToFrameId().size());
		log.info("Min id: " + logfileAnalyzerVisionCam.getTimestampToFrameId().values().stream().min(Long::compareTo));
		log.info("Max id " + logfileAnalyzerVisionCam.getTimestampToFrameId().values().stream().max(Long::compareTo));

		log.info("Writing label to file");
		exportLabelFile(exportPathLabelFile);
		log.info("DONE");

		log.info("Process finished");
	}


	private static SSLGameLogReader importLabelerDataFile(String filePath)
	{
		SSLGameLogReader logReader = new SSLGameLogReader();

		logReader.loadFileBlocking(filePath);

		return logReader;
	}


	private static void exportLabelFile(String filePath)
	{
		LogAnalysis logAnalysis = SumatraModel.getInstance().getModule(LogAnalysis.class);
		LogLabels.Labels labels = logAnalysis.getProtobufMsgLogLabels();
		LogLabels.Labels.Builder newLabels = LogLabels.Labels.newBuilder(labels);

		log.info("dribbling labels: " + newLabels.getDribblingLabelsCount());
		log.info("possession labels: " + newLabels.getBallPossessionLabelsCount());
		log.info("passing labels: " + newLabels.getPassingLabelsCount());
		log.info("goal labels: " + newLabels.getGoalShotLabelsCount());

		int nDribbling = 0;
		for (LogLabels.DribblingLabel dribblingLabel : newLabels.getDribblingLabelsList())
		{
			if (dribblingLabel.getIsDribbling())
			{
				nDribbling++;
			}
		}
		log.info("Dribbling: " + nDribbling + "/" + newLabels.getDribblingLabelsCount());


		Map<LogLabels.BallPossessionLabel.State, Integer> possessionCounter = new EnumMap<>(
				LogLabels.BallPossessionLabel.State.class);
		for (LogLabels.BallPossessionLabel possessionLabel : newLabels.getBallPossessionLabelsList())
		{
			possessionCounter.putIfAbsent(possessionLabel.getState(), 0);
			possessionCounter.compute(possessionLabel.getState(), (k, v) -> v == null ? 1 : v + 1);
		}
		log.info("Possession: " + possessionCounter + " / " + newLabels.getBallPossessionLabelsCount());

		for (LogLabels.PassingLabel passingLabel : newLabels.getPassingLabelsList())
		{
			log.info("Pass frame " + passingLabel.getStartFrame() + ", length: "
					+ (passingLabel.getEndFrame() - passingLabel.getStartFrame()));
		}
		for (LogLabels.GoalShotLabel goalShotLabel : newLabels.getGoalShotLabelsList())
		{
			log.info("Goal frame " + goalShotLabel.getStartFrame() + ", length: "
					+ (goalShotLabel.getEndFrame() - goalShotLabel.getStartFrame()));
		}

		SSLGameLogLabelFileWriter writer = new SSLGameLogLabelFileWriter();
		writer.openPath(filePath);
		writer.write(newLabels.build());
		writer.close();
	}
}
