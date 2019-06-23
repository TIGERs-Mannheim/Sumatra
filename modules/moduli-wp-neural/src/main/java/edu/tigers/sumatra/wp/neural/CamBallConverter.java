/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 26.12.2014
 * Author(s): KaiE
 * *********************************************************
 */
package edu.tigers.sumatra.wp.neural;

import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;

import edu.tigers.sumatra.cam.data.ACamObject;
import edu.tigers.sumatra.cam.data.ACamObject.ECamObjectType;
import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.wp.neural.NeuralStaticConfiguration.BallConfigs;
import edu.tigers.sumatra.wp.neural.NeuralStaticConfiguration.BotConfigs;
import edu.tigers.sumatra.wp.neural.NeuralStaticConfiguration.NeuralWPConfigs;


/**
 * This Class is like the {@link CamRobotConverter} but is used for the Ball
 * 
 * @author KaiE
 */
public class CamBallConverter implements IACamObjectConverter
{
	
	private static final double[]	emptydummy	= new double[0];
	private double[]					lastOutput	= new double[BallConfigs.ConvertedDataArray];
	
	
	@Override
	public double[] convertOutput(final double[] dataToConvert, final ACamObject newest)
	{
		if (newest.implementation() != ECamObjectType.Ball)
		{
			return emptydummy;
		}
		CamBall rob = (CamBall) newest;
		
		double[] ret = new double[BallConfigs.ConvertedDataArray];
		ret[0] = dataToConvert[1] + rob.getPos().x();
		ret[1] = dataToConvert[2] + rob.getPos().y();
		if (dataToConvert[0] == 0)
		{
			ret[2] = lastOutput[2];
			ret[3] = lastOutput[3];
			ret[4] = lastOutput[4];
			ret[5] = lastOutput[5];
		} else
		{
			ret[2] = dataToConvert[3];
			ret[3] = dataToConvert[4];
			ret[4] = (ret[2] - lastOutput[2]) / dataToConvert[0];
			ret[5] = (ret[3] - lastOutput[3]) / dataToConvert[0];
		}
		lastOutput = Arrays.copyOf(ret, BotConfigs.ConvertedDataArray);
		return ret;
	}
	
	
	@Override
	public double[] convertInput(final Collection<ACamObject> data,
			final double lookahead)
	{
		if (data.size() != NeuralWPConfigs.LastNFrames)
		{
			throw new IllegalArgumentException("frames.size was not NeuralWPConfigs.LastNFrames");
		}
		double[] ret = new double[BallConfigs.InputLayer];
		Vector<ACamObject> frm = new Vector<>(data);
		
		for (int i = 0; i < (frm.size() - 1); ++i)
		{
			final CamBall fnew = (CamBall) frm.get(i + 1);
			final CamBall fold = (CamBall) frm.get(i);
			final int offsetCnst = BallConfigs.OutputLayer;
			// Convert ns to ms
			ret[(i * offsetCnst) + 0] = (fnew.getTimestamp() - fold.getTimestamp()) / 1000000.0;
			ret[(i * offsetCnst) + 1] = fnew.getPos().x() - fold.getPos().x();
			ret[(i * offsetCnst) + 2] = fnew.getPos().y() - fold.getPos().y();
			if (ret[(i * offsetCnst) + 0] != 0)
			{
				ret[(i * offsetCnst) + 3] = ret[(i * offsetCnst) + 1]
						/ ret[(i * offsetCnst) + 0];
				ret[(i * offsetCnst) + 4] = ret[(i * offsetCnst) + 2]
						/ ret[(i * offsetCnst) + 0];
			}
		}
		ret[BallConfigs.InputLayer - 1] = lookahead;
		return ret;
	}
	
	
	@Override
	public double[] createIdealOutput(final Collection<ACamObject> frames, final ACamObject referenceItem,
			final double timestep)
	{
		if (referenceItem.implementation() != ECamObjectType.Ball)
		{
			throw new IllegalArgumentException("expected Camobject of type Ball got "
					+ referenceItem.implementation().toString());
		}
		Vector<ACamObject> frm = new Vector<>(frames);
		double[] ret = new double[BallConfigs.OutputLayer];
		CamBall fnew = (CamBall) referenceItem;
		CamBall fold = (CamBall) frm.get(frm.size() - 1);
		ret[0] = timestep;
		ret[1] = fnew.getPos().x() - fold.getPos().x();
		ret[2] = fnew.getPos().y() - fold.getPos().y();
		ret[3] = ret[1] / ret[0];
		ret[4] = ret[2] / ret[0];
		return ret;
	}
}
