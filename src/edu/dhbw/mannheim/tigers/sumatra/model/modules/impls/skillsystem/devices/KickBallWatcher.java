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
 * Watch straight kicks
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class KickBallWatcher extends ABallWatcher
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log	= Logger.getLogger(KickBallWatcher.class.getName());
	private final int					duration;
	private final float				goalX;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param duration
	 * @param goalX
	 */
	public KickBallWatcher(final int duration, final float goalX)
	{
		this.duration = duration;
		this.goalX = goalX;
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
		exporter.addValues(duration);
	}
	
	
	@Override
	protected String getFileName()
	{
		return "ballKick/ballKick" + System.currentTimeMillis();
	}
	
	
	@Override
	protected void postProcessing(final String fileName)
	{
		MatlabProxy mp;
		try
		{
			mp = MatlabConnection.getMatlabProxy();
			Object[] res = mp.returningFeval("kickDistApprox", 4, fileName, goalX / 1000);
			double[] kickDists = (double[]) res[0];
			double[] endVel = (double[]) res[1];
			double[] avgVel = (double[]) res[2];
			double[] initVel = (double[]) res[3];
			log.info("duration: " + duration + " kickDist: " + kickDists[0] + " endVel: " + endVel[0] + " avgVel: "
					+ avgVel[0] + " initVel: "
					+ initVel[0]);
			
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
