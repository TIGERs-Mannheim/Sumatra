/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 19, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal;

import org.apache.log4j.Level;
import org.junit.Ignore;
import org.junit.Test;

import Jama.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.BallCorrector;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.BallProcessor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data.PredictionContext;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data.WPCamBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.filter.ExtKalmanFilter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.filter.IFilter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.motionModels.BallMotionModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.motionModels.TigersMotionModel;
import edu.dhbw.mannheim.tigers.sumatra.util.SumatraSetupHelper;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.CSVExporter;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.ExportDataContainer.RawBot;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.ExportDataContainer.WpBall;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class KalmanBallTest
{
	private static final String	FOLDER	= "data/ball/moduli_sim/ballManual/2015-05-23_21-08-13/";
	
	static
	{
		SumatraSetupHelper.setupSumatra();
		SumatraSetupHelper.changeLogLevel(Level.TRACE);
	}
	
	
	/**
	 */
	@Test
	@Ignore
	public void testProcessBall()
	{
		BallProcessor.runOnData(FOLDER);
	}
	
	
	/**
	 */
	@Test
	@Ignore
	public void testCorrectBall()
	{
		BallCorrector.runOnData(FOLDER);
	}
	
	
	/**
	 */
	@Test
	@Ignore
	public void testBallDynamics()
	{
		String baseFolder = "data/ball/ballDynamicsTest/";
		PredictionContext context = new PredictionContext();
		BallMotionModel model = new BallMotionModel(context);
		
		TigersMotionModel botModel = new TigersMotionModel();
		IFilter botFilter = new ExtKalmanFilter();
		IVector3 botPos = new Vector3(1025, 2000, -AngleMath.PI_HALF);
		RawBot rawBot = new RawBot(0, 0, 0, ETeamColor.BLUE, botPos, 0);
		botFilter.init(botModel, context, 0, new WPCamBot(rawBot.toCamRobot()));
		context.getBlueBots().put(0, botFilter);
		
		
		CSVExporter exporterBot = new CSVExporter(baseFolder + "rawBots", false);
		exporterBot.addValues(rawBot.getNumberList());
		exporterBot.close();
		
		
		Matrix state = new Matrix(9, 1);
		state.set(0, 0, -3000);
		state.set(1, 0, -1000);
		state.set(2, 0, 0);
		state.set(3, 0, -1500);
		state.set(4, 0, 900);
		state.set(5, 0, 0);
		state.set(6, 0, 0);
		state.set(7, 0, 0);
		state.set(8, 0, 0);
		
		CSVExporter exporter = new CSVExporter(baseFolder + "simBall", false);
		long dt = (long) (16e6);
		long time = 0;
		for (int i = 0; i < 1000; i++)
		{
			IVector3 pos = new Vector3(state.get(0, 0), state.get(1, 0), state.get(2, 0));
			IVector3 vel = new Vector3(state.get(3, 0), state.get(4, 0), state.get(5, 0));
			IVector3 acc = new Vector3(state.get(6, 0), state.get(7, 0), state.get(8, 0));
			WpBall ball = new WpBall(pos, vel, acc, i, time);
			exporter.addValues(ball.getNumberList());
			
			Matrix newState = model.dynamics(state, null, dt * 1e-9);
			if ((Math.abs(state.get(3, 0) - newState.get(3, 0)) < 0.01)
					&& (Math.abs(state.get(4, 0) - newState.get(4, 0)) < 0.01))
			{
				System.out.println("Converged after " + i + " iterations.");
				break;
			}
			state = newState;
			time += dt;
		}
		exporter.close();
	}
}
