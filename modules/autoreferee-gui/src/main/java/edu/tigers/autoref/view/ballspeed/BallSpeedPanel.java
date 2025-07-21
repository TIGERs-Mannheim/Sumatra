/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.view.ballspeed;

import edu.tigers.autoref.view.generic.FixedTimeRangeChartPanel;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.io.Serial;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


/**
 * @author "Lukas Magel"
 */
public class BallSpeedPanel extends JPanel
{
	@Serial
	private static final long serialVersionUID = -5641196573487059661L;

	/**
	 * in ns
	 */
	private final long updatePeriod;

	private final List<IBallSpeedPanelListener> observers = new CopyOnWriteArrayList<>();
	private FixedTimeRangeChartPanel chartPanel;
	private JButton pauseButton = new JButton("Pause");
	private JButton resumeButton = new JButton("Resume");
	private JLabel lineInitialDescription = new JLabel("initial ball speed");
	private JLabel lineSpeedDescription = new JLabel("ball speed");
	private JLabel lineMaxDescription = new JLabel("maximum ball speed");


	/**
	 * @param timeRange    Displayed range in ns
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
		chartPanel.clipY(0, 7.5);
		chartPanel.yTicks(1.0);
		chartPanel.setXTitle("Time [s]");
		chartPanel.setYTitle("Ball Speed [m/s]");
		setTimeRange(timeRange);

		JSlider timeRangeSlider = new JSlider(SwingConstants.VERTICAL, 0, 120,
				(int) TimeUnit.NANOSECONDS.toSeconds(timeRange));
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

		JCheckBox stopChartCheckbox = new JCheckBox("Pause when not RUNNING");
		stopChartCheckbox.setSelected(true);
		stopChartCheckbox.setBackground(Color.WHITE);
		stopChartCheckbox.addActionListener(e -> {
			boolean newValue = stopChartCheckbox.isSelected();
			informObserver(observer -> observer.stopChartValueChanged(newValue));
		});

		pauseButton.addActionListener(e -> informObserver(IBallSpeedPanelListener::pauseButtonPressed));
		resumeButton.addActionListener(e -> informObserver(IBallSpeedPanelListener::resumeButtonPressed));

		lineMaxDescription.setForeground(Color.RED);
		lineInitialDescription.setForeground(Color.GREEN);
		lineSpeedDescription.setForeground(Color.BLUE);

		JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		southPanel.add(lineMaxDescription);
		southPanel.add(lineInitialDescription);
		southPanel.add(lineSpeedDescription);
		southPanel.setBackground(Color.WHITE);
		southPanel.add(pauseButton);
		southPanel.add(resumeButton);
		southPanel.add(stopChartCheckbox);

		setLayout(new BorderLayout());
		add(chartPanel, BorderLayout.CENTER);
		add(southPanel, BorderLayout.SOUTH);
		add(timeRangeSlider, BorderLayout.EAST);
	}


	public void addObserver(final IBallSpeedPanelListener observer)
	{
		this.observers.add(observer);
	}


	public void removeObserver(final IBallSpeedPanelListener observer)
	{
		this.observers.remove(observer);
	}


	private void informObserver(final Consumer<IBallSpeedPanelListener> consumer)
	{
		observers.forEach(consumer);
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


	/**
	 * @param time
	 * @param velocity
	 */
	public void addInitialVelPoint(final long time, final double velocity)
	{
		chartPanel.addInitialVelPoint(time, velocity);
	}
}
