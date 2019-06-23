/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 30, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyHighestValues;
import info.monitorenter.gui.chart.traces.Trace2DLtd;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplinePair3D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;


/**
 * Display last full spline that is or was executed by bot
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class SplinePanel extends JPanel
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= -1245087876706447851L;
	
	/** [s] */
	private static final float	DELTA_T				= 0.02f;
	/** [s] */
	private static final float	TIME_TO_DISPLAY	= 10;
	
	private SplinePair3D			latestSplinePair	= null;
	private float					timeOffset			= 0;
	private final Chart2D		chartX				= new Chart2D();
	private final Chart2D		chartY				= new Chart2D();
	private final Chart2D		chartW				= new Chart2D();
	
	private boolean				enabled				= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public SplinePanel()
	{
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.LINE_AXIS));
		add(controlPanel);
		
		JButton btnClear = new JButton("Clear");
		btnClear.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				clearTraces();
			}
		});
		controlPanel.add(btnClear);
		
		final JToggleButton btnEnable = new JToggleButton("Capture");
		btnEnable.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				enabled = btnEnable.isSelected();
			}
		});
		controlPanel.add(btnEnable);
		
		chartX.setName("x");
		chartY.setName("y");
		chartW.setName("w");
		chartX.getAxisY().getAxisTitle().setTitle("");
		chartY.getAxisY().getAxisTitle().setTitle("");
		chartW.getAxisY().getAxisTitle().setTitle("");
		
		chartX.getAxisX().setRangePolicy(new RangePolicyHighestValues((int) (TIME_TO_DISPLAY / DELTA_T)));
		chartY.getAxisX().setRangePolicy(new RangePolicyHighestValues((int) (TIME_TO_DISPLAY / DELTA_T)));
		chartW.getAxisX().setRangePolicy(new RangePolicyHighestValues((int) (TIME_TO_DISPLAY / DELTA_T)));
		
		
		add(chartX, "wrap");
		add(chartY, "wrap");
		add(chartW, "wrap");
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private void clearTraces()
	{
		timeOffset = 0;
		chartX.removeAllTraces();
		chartY.removeAllTraces();
		chartW.removeAllTraces();
	}
	
	
	/**
	 * @param splinePair
	 */
	public void showSpline(final SplinePair3D splinePair)
	{
		// note: testing for equality of references is intended here
		if (enabled && (splinePair != latestSplinePair))
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					float timeDiff = 0;
					if (latestSplinePair != null)
					{
						timeDiff = latestSplinePair.getTotalTime() - latestSplinePair.getTrajectoryTime();
						if (timeDiff > 0)
						{
							timeOffset -= timeDiff;
						}
					}
					updateCharts(splinePair);
					latestSplinePair = splinePair;
					timeOffset += splinePair.getTotalTime();
				}
			});
		}
	}
	
	
	private void updateCharts(SplinePair3D splinePair)
	{
		int bufferSize = (int) Math.ceil(TIME_TO_DISPLAY / DELTA_T);
		ITrace2D traceAccX = new Trace2DLtd(bufferSize, "acc");
		ITrace2D traceVelX = new Trace2DLtd(bufferSize, "vel");
		ITrace2D tracePosX = new Trace2DLtd(bufferSize, "pos");
		chartX.addTrace(traceAccX);
		chartX.addTrace(traceVelX);
		chartX.addTrace(tracePosX);
		ITrace2D traceAccY = new Trace2DLtd(bufferSize, "acc");
		ITrace2D traceVelY = new Trace2DLtd(bufferSize, "vel");
		ITrace2D tracePosY = new Trace2DLtd(bufferSize, "pos");
		chartY.addTrace(traceAccY);
		chartY.addTrace(traceVelY);
		chartY.addTrace(tracePosY);
		ITrace2D traceAccW = new Trace2DLtd(bufferSize, "acc");
		ITrace2D traceVelW = new Trace2DLtd(bufferSize, "vel");
		ITrace2D tracePosW = new Trace2DLtd(bufferSize, "pos");
		chartW.addTrace(traceAccW);
		chartW.addTrace(traceVelW);
		chartW.addTrace(tracePosW);
		
		traceAccX.setColor(Color.red);
		traceAccY.setColor(Color.red);
		traceAccW.setColor(Color.red);
		traceVelX.setColor(Color.green);
		traceVelY.setColor(Color.green);
		traceVelW.setColor(Color.green);
		tracePosX.setColor(Color.blue);
		tracePosY.setColor(Color.blue);
		tracePosW.setColor(Color.blue);
		
		for (float t = 0; t < (splinePair.getTotalTime()); t += DELTA_T)
		{
			IVector2 pos = splinePair.getPositionTrajectory().getPosition(t);
			IVector2 vel = splinePair.getPositionTrajectory().getVelocity(t);
			IVector2 acc = splinePair.getPositionTrajectory().getAcceleration(t);
			tracePosX.addPoint(timeOffset + t, pos.x());
			tracePosY.addPoint(timeOffset + t, pos.y());
			tracePosW.addPoint(timeOffset + t, splinePair.getRotationTrajectory().getPosition(t));
			traceVelX.addPoint(timeOffset + t, vel.x());
			traceVelY.addPoint(timeOffset + t, vel.y());
			traceVelW.addPoint(timeOffset + t, splinePair.getRotationTrajectory().getVelocity(t));
			traceAccX.addPoint(timeOffset + t, acc.x());
			traceAccY.addPoint(timeOffset + t, acc.y());
			traceAccW.addPoint(timeOffset + t, splinePair.getRotationTrajectory().getAcceleration(t));
		}
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
