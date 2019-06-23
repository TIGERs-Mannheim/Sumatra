/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 26.12.2014
 * Author(s): KaiE
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.neural.neuralimpl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.ACamObject;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.ACamObject.ECamObjectType;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamRobot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.neural.neuralimpl.NeuralStaticConfiguration.BotConfigs;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.neural.neuralimpl.NeuralStaticConfiguration.NeuralWPConfigs;


/**
 * Creates the data set from the CamRobot
 * 
 * @author KaiE
 */
public class CamRobotConverter implements IACamObjectConverter
{
	private double						lastData[]	= new double[BotConfigs.ConvertedDataArray];
	private static final double[]	emptydummy	= new double[0];
	
	
	@Override
	public double[] convertOutput(final double[] dataToConvert, final ACamObject newest)
	{
		if (newest.implementation() != ECamObjectType.Robot)
		{
			return emptydummy;
		}
		CamRobot rob = (CamRobot) newest;
		double[] ret = new double[BotConfigs.ConvertedDataArray];
		ret[0] = dataToConvert[1] + rob.getPos().x(); // delta x + last pos
		ret[1] = dataToConvert[2] + rob.getPos().y(); // delta y + last pos
		ret[6] = dataToConvert[5] + rob.getOrientation(); // orientation
		if (dataToConvert[0] != 0)
		{
			ret[2] = dataToConvert[3]; // vel x
			ret[3] = dataToConvert[4]; // vel y
			ret[7] = dataToConvert[6]; // orient vel
			ret[4] = (ret[2] - lastData[2]) / dataToConvert[0]; // acc x
			ret[5] = (ret[3] - lastData[3]) / dataToConvert[0]; // acc y
			ret[8] = (ret[7] - lastData[7]) / dataToConvert[0]; // acc orient
		} else
		{ // if the time prediction is zero then the output vel and acc
			// are taken from the old prediction
			ret[2] = lastData[2];
			ret[3] = lastData[3];
			ret[7] = lastData[7];
			ret[4] = lastData[4];
			ret[5] = lastData[5];
			ret[8] = lastData[8];
			
		}
		
		lastData = Arrays.copyOf(ret, BotConfigs.ConvertedDataArray);
		return ret;
	}
	
	
	@Override
	public double[] convertInput(final Collection<ACamObject> data, final Collection<Long> timestamps,
			final double lookahead)
	{
		if (data.size() != NeuralWPConfigs.LastNFrames)
		{
			throw new IllegalArgumentException("frames.size was not NeuralWPConfigs.LastNFrames");
		}
		double[] ret = new double[BotConfigs.InputLayer];
		Vector<ACamObject> frm = new Vector<>(data);
		Vector<Long> time = new Vector<>(timestamps);
		for (int i = 0; i < (frm.size() - 1); ++i)
		{
			final CamRobot fnew = (CamRobot) frm.get(i + 1);
			final CamRobot fold = (CamRobot) frm.get(i);
			// Convert ns to ms
			ret[(i * BotConfigs.OutputLayer) + 0] = (time.get(i + 1) - time.get(i)) / 1000000.0;
			// pos x
			ret[(i * BotConfigs.OutputLayer) + 1] = fnew.getPos().x() - fold.getPos().x();
			// pos y
			ret[(i * BotConfigs.OutputLayer) + 2] = fnew.getPos().y() - fold.getPos().y();
			// orientation
			ret[(i * BotConfigs.OutputLayer) + 5] = fnew.getOrientation() - fold.getOrientation();
			if (ret[(i * BotConfigs.OutputLayer) + 0] != 0)
			{
				// vel x
				ret[(i * BotConfigs.OutputLayer) + 3] = ret[(i * BotConfigs.OutputLayer) + 1]
						/ ret[(i * BotConfigs.OutputLayer) + 0];
				// vel y
				ret[(i * BotConfigs.OutputLayer) + 4] = ret[(i * BotConfigs.OutputLayer) + 2]
						/ ret[(i * BotConfigs.OutputLayer) + 0];
				// orient vel in rad/s
				ret[(i * BotConfigs.OutputLayer) + 6] = ret[(i * BotConfigs.OutputLayer) + 5]
						/ (ret[(i * BotConfigs.OutputLayer) + 0] * 1000.0);
			}
			
		}
		ret[BotConfigs.InputLayer - 1] = lookahead;
		return ret;
	}
	
	
	@Override
	public double[] createIdealOutput(final Collection<ACamObject> frames, final ACamObject referenceItem,
			final double timestep)
	{
		Vector<ACamObject> frm = new Vector<>(frames);
		CamRobot newer = (CamRobot) referenceItem;
		CamRobot older = (CamRobot) frm.get(frm.size() - 1);
		
		double[] ret = new double[BotConfigs.OutputLayer];
		ret[0] = (timestep);
		if (ret[0] != 0)
		{
			ret[1] = newer.getPos().x() - older.getPos().x();
			ret[2] = newer.getPos().y() - older.getPos().y();
			ret[3] = ret[1] / ret[0];
			ret[4] = ret[2] / ret[0];
			ret[5] = newer.getOrientation() - older.getOrientation();
			ret[6] = ret[5] / ret[0];
		}
		return ret;
	}
}
