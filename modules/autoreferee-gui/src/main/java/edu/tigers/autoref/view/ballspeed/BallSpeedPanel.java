/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 15, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.view.ballspeed;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

import org.apache.commons.lang.NotImplementedException;

import edu.tigers.autoref.view.generic.FixedTimeRangeChartPanel;
import edu.tigers.sumatra.components.BasePanel;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * @author "Lukas Magel"
 */
public class BallSpeedPanel extends BasePanel<IBallSpeedPanelListener> implements ISumatraView
{
	/**  */
	private static final long			serialVersionUID	= -5641196573487059661L;
	
	/** in ns */
	private final long					updatePeriod;
	
	private FixedTimeRangeChartPanel	chartPanel;
	private JSlider						timeRangeSlider;
	private JCheckBox						stopChartCheckbox;
	private JButton						pauseButton			= new JButton("Pause");
	private JButton						resumeButton		= new JButton("Resume");
	
	
	/**
	 * @param timeRange Displayed range in ns
	 * @param updatePeriod update period of the chart in ns
	 */
	public BallSpeedPanel(final long timeRange, final long updatePeriod)
	{
		this.updatePeriod = updatePeriod;
		
		setupUI(timeRange);
	}
	
	
	private void setupUI(final long timeRange)
	{
		chartPanel = new FixedTimeRangeChartPanel(timeRange, true);
		chartPanel.setColor(Color.BLUE);
		chartPanel.clipY(0, 15);
		chartPanel.setXTitle("Time [s]");
		chartPanel.setYTitle("Ball Speed [m/s]");
		setTimeRange(timeRange);
		
		timeRangeSlider = new JSlider(SwingConstants.VERTICAL, 0, 120, (int) TimeUnit.NANOSECONDS.toSeconds(timeRange));
		timeRangeSlider.setPaintTicks(true);
		timeRangeSlider.setPaintLabels(true);
		timeRangeSlider.setMajorTickSpacing(30);
		timeRangeSlider.setMinorTickSpacing(10);
		timeRangeSlider.setBackground(Color.WHITE);
		timeRangeSlider.setToolTipText("Adjust the ball speed time range [s] (Resets the graph!)");
		timeRangeSlider.addChangeListener(e -> {
			if (!timeRangeSlider.getValueIsAdjusting())
			{
				int newSliderValue = Math.max(timeRangeSlider.getValue(), 1);
				informObserver(observer -> observer.timeRangeSliderValueChanged(newSliderValue));
			}
		});
		
		stopChartCheckbox = new JCheckBox("Pause when not RUNNING");
		stopChartCheckbox.setBackground(Color.WHITE);
		stopChartCheckbox.addActionListener(e -> {
			boolean newValue = stopChartCheckbox.isSelected();
			informObserver(observer -> observer.stopChartValueChanged(newValue));
		});
		
		pauseButton.addActionListener(e -> {
			informObserver(observer -> observer.pauseButtonPressed());
		});
		resumeButton.addActionListener(e -> {
			informObserver(observer -> observer.resumeButtonPressed());
		});
		
		JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		southPanel.setBackground(Color.WHITE);
		southPanel.add(pauseButton);
		southPanel.add(resumeButton);
		southPanel.add(stopChartCheckbox);
		
		setLayout(new BorderLayout());
		add(chartPanel, BorderLayout.CENTER);
		add(southPanel, BorderLayout.SOUTH);
		add(timeRangeSlider, BorderLayout.EAST);
	}
	
	
	/**
	 * @param timeRange
	 */
	public void setTimeRange(final long timeRange)
	{
		chartPanel.setRange(timeRange);
		chartPanel.setPointBufferSizeWithPeriod(updatePeriod);
		chartPanel.clear();
	}
	
	
	/**
	 * @param velocity
	 */
	public void setMaxBallVelocityLine(final double velocity)
	{
		chartPanel.setHorizontalLine("Max", Color.RED, velocity);
	}
	
	
	/**
	 * @param time
	 * @param velocity
	 */
	public void addPoint(final long time, final double velocity)
	{
		chartPanel.addPoint(time, velocity);
	}
	
	
	@Override
	public void setPanelEnabled(final boolean enabled)
	{
		throw new NotImplementedException();
	}
	
}
