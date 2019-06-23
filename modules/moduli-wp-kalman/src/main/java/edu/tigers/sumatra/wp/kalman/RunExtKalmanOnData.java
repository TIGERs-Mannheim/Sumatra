/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 26, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.kalman;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import Jama.Matrix;
import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.cam.data.CamDetectionFrame;
import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.wp.ExportDataContainer;
import edu.tigers.sumatra.wp.ExportDataContainer.FrameInfo;
import edu.tigers.sumatra.wp.ExportDataContainer.WpBall;
import edu.tigers.sumatra.wp.ExportDataContainer.WpBot;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.kalman.data.ABotMotionResult;
import edu.tigers.sumatra.wp.kalman.filter.ExtKalmanFilter;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RunExtKalmanOnData
{
	@SuppressWarnings("unused")
	private static final Logger	log	= Logger.getLogger(RunExtKalmanOnData.class.getName());
	
	private final ExtKalman			kalman;
	
	
	/**
	 * 
	 */
	public RunExtKalmanOnData()
	{
		kalman = new ExtKalman(null);
		kalman.start();
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
		
		CSVExporter exp = new CSVExporter("/tmp/testControl", false);
		CSVExporter exp2 = new CSVExporter("/tmp/state", false);
		
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
				// if (!camBalls.isEmpty())
				// {
				// System.out.println("t=" + t);
				// }
				CamDetectionFrame cFrame = new CamDetectionFrame(tCapture, tSent, camId,
						frameId, camBalls, camBotsY, camBotsB);
				ExtendedCamDetectionFrame eFrame = kalman.processCamDetectionFrame(cFrame);
				SimpleWorldFrame swf = kalman.predictSimpleWorldFrame(eFrame);
				
				BotID botID = BotID.createBotId(1, ETeamColor.BLUE);
				final ExtKalmanFilter existingBot = (ExtKalmanFilter) kalman.getContext().getBlueBots()
						.get(botID.getNumber() + WPConfig.BLUE_ID_OFFSET);
				if ((existingBot != null) && !cFrame.getRobotsBlue().isEmpty())
				{
					CamRobot bot = cFrame.getRobotsBlue().get(0);
					final ABotMotionResult s = (ABotMotionResult) existingBot
							.getPrediction(swf.getTimestamp());
					Matrix contr = existingBot.getContr();
					if (contr == null)
					{
						contr = new Matrix(6, 1);
					}
					Matrix state = existingBot.getState()[0];
					
					exp2.addValues(
							state.get(0, 0), state.get(1, 0), // x,y
							AngleMath.normalizeAngle(state.get(2, 0)), // orientation,
							AngleMath.normalizeAngle(state.get(3, 0)), // dir
							state.get(4, 0) / 1000, // v
							state.get(5, 0), state.get(6, 0), // omega, eta
							contr.get(0, 0), contr.get(0, 0), contr.get(1, 0));
					
					ITrackedBot tBot = s.motionToTrackedBot(swf.getTimestamp(), botID);
					exp.addValues(cFrame.gettCapture(),
							tBot.getPos().x(), tBot.getPos().y(), tBot.getAngle(),
							tBot.getVel().x(), tBot.getVel().y(), tBot.getaVel(),
							bot.getPos().x(), bot.getPos().y(), bot.getOrientation(),
							0, 0, 0,
							contr.get(0, 0), contr.get(0, 0), contr.get(1, 0));
				}
				
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
		
		exp.close();
		exp2.close();
		
		CSVExporter.exportList(folder, "wpBotsTest", outBots.stream().map(c -> c));
		CSVExporter.exportList(folder, "wpBallTest", wpBalls.stream().map(c -> c));
	}
	
	
	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		RunExtKalmanOnData run = new RunExtKalmanOnData();
		// run.runOnData("/home/geforce/git/Sumatra/modules/sumatra-main/data/vision/moduli_lab/manual/2016-03-04_19-52-29");
		run.runOnData(
				"/home/geforce/git/Sumatra/modules/sumatra-main/data/vision/moduli_sumatra/manual/2016-03-04_22-23-49");
		run.runOnData(
				"/home/geforce/git/Sumatra/modules/sumatra-main/data/vision/moduli_lab/manual/2016-03-02_19-17-46");
	}
}
