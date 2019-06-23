/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 26, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.kalman2;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.cam.data.CamDetectionFrame;
import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.wp.ExportDataContainer;
import edu.tigers.sumatra.wp.ExportDataContainer.FrameInfo;
import edu.tigers.sumatra.wp.ExportDataContainer.WpBall;
import edu.tigers.sumatra.wp.ExportDataContainer.WpBot;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RunKalmanOnData
{
	@SuppressWarnings("unused")
	private static final Logger			log	= Logger.getLogger(RunKalmanOnData.class.getName());
															
	private final KalmanWorldPredictor	kalman;
													
													
	/**
	 * 
	 */
	public RunKalmanOnData()
	{
		kalman = new KalmanWorldPredictor(null);
	}
	
	
	/**
	 * @param folder
	 */
	public void runOnData(final String folder)
	{
		List<FrameInfo> frameInfo = ExportDataContainer.readFrameInfo(folder, "frameInfo");
		List<CamBall> allRawBalls = ExportDataContainer.readRawBall(folder, "rawBalls");
		List<CamRobot> yellowBots = ExportDataContainer.readRawBots(folder, "rawBots", ETeamColor.YELLOW);
		List<CamRobot> blueBots = ExportDataContainer.readRawBots(folder, "rawBots", ETeamColor.BLUE);
		
		long frameId = Long.MAX_VALUE;
		long tStart = Long.MAX_VALUE;
		if (!frameInfo.isEmpty())
		{
			frameId = frameInfo.get(0).getFrameId();
			tStart = frameInfo.get(0).gettCapture();
		} else
		{
			if (!allRawBalls.isEmpty())
			{
				frameId = Math.min(allRawBalls.get(0).getFrameId(), frameId);
				tStart = Math.min(allRawBalls.get(0).gettCapture(), tStart);
			}
			if (!yellowBots.isEmpty())
			{
				frameId = Math.min(yellowBots.get(0).getFrameId(), frameId);
				tStart = Math.min(yellowBots.get(0).gettCapture(), tStart);
			}
			if (!blueBots.isEmpty())
			{
				frameId = Math.min(blueBots.get(0).getFrameId(), frameId);
				tStart = Math.min(blueBots.get(0).gettCapture(), tStart);
			}
			if (frameId == Long.MAX_VALUE)
			{
				log.error("Now data");
				return;
			}
		}
		
		
		List<WpBot> outBots = new ArrayList<>();
		List<WpBall> wpBalls = new ArrayList<>();
		
		while (!yellowBots.isEmpty() || !blueBots.isEmpty() || !allRawBalls.isEmpty())
		{
			List<CamRobot> camBotsY = new ArrayList<>();
			List<CamRobot> camBotsB = new ArrayList<>();
			List<CamBall> camBalls = new ArrayList<>();
			long tCapture = -1;
			long tSent = -1;
			int camId = -1;
			if (!frameInfo.isEmpty())
			{
				FrameInfo info = frameInfo.remove(0);
				tCapture = info.gettCapture();
				tSent = info.gettSent();
				camId = info.getCamId();
			}
			while (!yellowBots.isEmpty() && (yellowBots.get(0).getFrameId() <= frameId))
			{
				assert frameId == yellowBots.get(0).getFrameId();
				assert (tCapture < 0) || (tCapture == yellowBots.get(0).gettCapture());
				tCapture = yellowBots.get(0).gettCapture();
				assert (tSent < 0) || (tSent == yellowBots.get(0).gettSent());
				tSent = yellowBots.get(0).gettSent();
				assert (camId < 0) || (camId == yellowBots.get(0).getCameraId());
				camId = yellowBots.get(0).getCameraId();
				camBotsY.add(yellowBots.remove(0));
			}
			while (!blueBots.isEmpty() && (blueBots.get(0).getFrameId() <= frameId))
			{
				assert frameId == blueBots.get(0).getFrameId();
				assert (tCapture < 0) || (tCapture == blueBots.get(0).gettCapture());
				tCapture = blueBots.get(0).gettCapture();
				assert (tSent < 0) || (tSent == blueBots.get(0).gettSent());
				tSent = blueBots.get(0).gettSent();
				assert (camId < 0) || (camId == blueBots.get(0).getCameraId());
				camId = blueBots.get(0).getCameraId();
				camBotsB.add(blueBots.remove(0));
			}
			while (!allRawBalls.isEmpty() && (allRawBalls.get(0).getFrameId() <= frameId))
			{
				assert frameId == allRawBalls.get(0).getFrameId();
				assert (tCapture < 0) || (tCapture == allRawBalls.get(0).gettCapture());
				tCapture = allRawBalls.get(0).gettCapture();
				assert (tSent < 0) || (tSent == allRawBalls.get(0).gettSent());
				tSent = allRawBalls.get(0).gettSent();
				assert (camId < 0) || (camId == allRawBalls.get(0).getCameraId());
				camId = allRawBalls.get(0).getCameraId();
				camBalls.add(allRawBalls.remove(0));
			}
			if (tCapture > 0)
			{
				// double t = (tCapture - tStart) / 1e9;
				// if (t > 4)
				// {
				// System.out.println("t=" + t);
				// }
				CamDetectionFrame cFrame = new CamDetectionFrame(tCapture, tSent, camId,
						frameId, camBalls, camBotsY, camBotsB);
				ExtendedCamDetectionFrame eFrame = kalman.processCamDetectionFrame(cFrame);
				SimpleWorldFrame swf = kalman.predictSimpleWorldFrame(eFrame);
				
				for (ITrackedBot bot : swf.getBots().values())
				{
					outBots.add(ExportDataContainer.trackedBot2WpBot(bot, frameId, swf.getTimestamp()));
				}
				wpBalls
						.add(new WpBall(swf.getBall().getPos3(), swf.getBall().getVel3(), swf.getBall().getAcc3(), frameId,
								swf.getTimestamp(),
								swf.getBall().getConfidence()));
			}
			
			frameId++;
		}
		
		
		CSVExporter.exportList(folder, "wpBotsTest", outBots.stream().map(c -> c));
		CSVExporter.exportList(folder, "wpBallTest", wpBalls.stream().map(c -> c));
	}
	
	
	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		RunKalmanOnData run = new RunKalmanOnData();
		run.runOnData("/home/geforce/git/Sumatra/modules/sumatra-main/data/vision/moduli_lab/manual/2016-03-04_19-52-29");
		// run.runOnData(
		// "/home/geforce/git/Sumatra/modules/sumatra-main/data/vision/moduli_sumatra/manual/2016-03-04_22-23-49");
	}
}
