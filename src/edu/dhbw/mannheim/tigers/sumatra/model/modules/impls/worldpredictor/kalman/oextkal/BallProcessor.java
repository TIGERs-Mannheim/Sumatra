/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.05.2010
 * Authors:
 * Maren K�nemund <Orphen@fantasymail.de>,
 * Peter Birkenkampf <birkenkampf@web.de>,
 * Marcel Sauer <sauermarcel@yahoo.de>,
 * Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamRobot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.MergedCamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data.BallMotionResult;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data.PredictionContext;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data.WPCamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data.WPCamBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.filter.ExtKalmanFilter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.filter.IFilter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.motionModels.BallMotionModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.motionModels.TigersMotionModel;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.RealTimeClock;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.StaticSimulationClock;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.CSVExporter;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.ExportDataContainer;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.ExportDataContainer.RawBall;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.ExportDataContainer.RawBot;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.ExportDataContainer.WpBall;


/**
 * Prepares all new data from the incoming
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamDetectionFrame} concerning the ball(s), and add it
 * to the {@link PredictionContext} if necessary
 */
public class BallProcessor
{
	private final PredictionContext	context;
	
	
	/**
	 * @param context
	 */
	public BallProcessor(final PredictionContext context)
	{
		this.context = context;
	}
	
	
	/**
	 * @param frame
	 */
	public void process(final MergedCamDetectionFrame frame)
	{
		CamBall ball = frame.getBall();
		final WPCamBall visionBall = new WPCamBall(ball);
		context.getBall().observation(ball.getTimestamp(), visionBall);
	}
	
	
	/**
	 */
	public void performCollisionAwareLookahead()
	{
		final IFilter ballWeKnow = context.getBall();
		for (int i = 1; i <= context.getStepCount(); i++)
		{
			ballWeKnow.performLookahead(i);
		}
	}
	
	
	/**
	 * @param folder
	 */
	public static void runOnData(final String folder)
	{
		List<RawBall> allRawBalls = ExportDataContainer.readRawBall(folder, "rawBalls");
		List<RawBall> rawBalls = ExportDataContainer.readRawBall(folder, "rawBall");
		List<RawBot> yellowBots = ExportDataContainer.readRawBots(folder, "rawBots", ETeamColor.YELLOW);
		List<RawBot> blueBots = ExportDataContainer.readRawBots(folder, "rawBots", ETeamColor.BLUE);
		
		StaticSimulationClock clock = new StaticSimulationClock();
		clock.setNanoTime(rawBalls.get(0).getTimestamp());
		SumatraClock.setClock(clock);
		
		PredictionContext context = new PredictionContext();
		ExtKalmanFilter filter = new ExtKalmanFilter();
		filter.init(new BallMotionModel(context), context, rawBalls.get(0).getTimestamp(), new WPCamBall(rawBalls.get(0)
				.toCamBall()));
		context.setBall(filter);
		
		BallProcessor ballProcessor = new BallProcessor(context);
		
		List<WpBall> wpBalls = new ArrayList<>();
		
		for (int i = 0; i < rawBalls.size(); i++)
		{
			RawBall rawBall = rawBalls.get(i);
			CamBall ball = rawBall.toCamBall();
			clock.setNanoTime(ball.getTimestamp());
			
			List<CamBall> frameBalls = allRawBalls.stream().filter(b -> b.getFrameId() == rawBall.getFrameId())
					.map(rb -> rb.toCamBall()).collect(Collectors.toList());
			List<CamRobot> frameBotsY = yellowBots.stream().filter(b -> b.getFrameId() == rawBall.getFrameId())
					.map(rb -> rb.toCamRobot()).collect(Collectors.toList());
			List<CamRobot> frameBotsB = blueBots.stream().filter(b -> b.getFrameId() == rawBall.getFrameId())
					.map(rb -> rb.toCamRobot()).collect(Collectors.toList());
			
			context.getBlueBots().clear();
			context.getYellowBots().clear();
			for (CamRobot bot : frameBotsB)
			{
				TigersMotionModel botModel = new TigersMotionModel();
				IFilter botFilter = new ExtKalmanFilter();
				botFilter.init(botModel, context, 0, new WPCamBot(bot));
				context.getBlueBots().put(bot.getRobotID(), botFilter);
			}
			for (CamRobot bot : frameBotsY)
			{
				TigersMotionModel botModel = new TigersMotionModel();
				IFilter botFilter = new ExtKalmanFilter();
				botFilter.init(botModel, context, 0, new WPCamBot(bot));
				context.getYellowBots().put(bot.getRobotID(), botFilter);
			}
			
			MergedCamDetectionFrame frame = new MergedCamDetectionFrame(i, frameBalls, frameBotsY, frameBotsB, ball);
			ballProcessor.process(frame);
			
			final IFilter filteredBall = context.getBall();
			final BallMotionResult motion = (BallMotionResult) filteredBall.getLookahead(context.getStepCount());
			TrackedBall trackedBall = TrackedBall.motionToTrackedBall(motion);
			
			wpBalls.add(new WpBall(trackedBall.getPos3(), trackedBall.getVel3(), trackedBall.getAcc3(), i, ball
					.getTimestamp()));
		}
		
		SumatraClock.setClock(new RealTimeClock());
		
		CSVExporter.exportList(folder, "wpBallTest", wpBalls.stream().map(c -> c));
	}
}
