/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 8, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.util.MatlabConnection;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.CSVExporter;


/**
 * Watch chip kicks and capture data
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ChipBallWatcher extends ABallWatcher
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private static final Logger	log	= Logger.getLogger(ChipBallWatcher.class.getName());
	private final ChipParams		chipParams;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param chipValues
	 */
	public ChipBallWatcher(final ChipParams chipValues)
	{
		chipParams = chipValues;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected boolean checkIsFailed(final IVector2 ballPos)
	{
		return super.checkIsFailed(ballPos);
	}
	
	
	@Override
	protected boolean checkIsDone(final IVector2 ballPos)
	{
		return super.checkIsDone(ballPos);
	}
	
	
	@Override
	protected void export(final CSVExporter exporter)
	{
		exporter.addValues(chipParams.getDribbleSpeed(), chipParams.getDuration());
	}
	
	
	@Override
	protected String getFileName()
	{
		return "ballChip/ballChip" + System.currentTimeMillis();
	}
	
	
	@Override
	protected void postProcessing(final String fileName)
	{
		MatlabProxy mp;
		try
		{
			mp = MatlabConnection.getMatlabProxy();
			Object[] res = mp.returningFeval("chipDistApprox", 2, fileName);
			double[] chipDists = (double[]) res[0];
			double[] rollDists = (double[]) res[1];
			log.info("ChipDist: " + chipDists[0] + " RollDist: " + rollDists[0]);
		} catch (MatlabConnectionException err)
		{
			// ignore
		} catch (MatlabInvocationException err)
		{
			log.error("Error evaluating matlab function: " + err.getMessage(), err);
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
