/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.09.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.timer;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyFixedViewport;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.util.Range;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.timer.TimerInfo;
import edu.dhbw.mannheim.tigers.sumatra.util.StopWatch.Timing;


/**
 * This panels visualizes the actual timer-data
 * 
 * @author Gero
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class TimerChartPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long					serialVersionUID	= -422217644518603954L;
	
	private static final int					MAX_SIZE				= 200;
	private static final float					NS_TO_MS				= 1000000;
	
	/** Maximum time everything is allowed to take (currently {@value} , 60 FPS) */
	private static final float					MAXIMUM_DURATION	= 16.7f;
	private float									currentMaxY			= 1.3f;
	
	private final Chart2D						chart					= new Chart2D();
	private final Trace2DLtd					maximum				= new Trace2DLtd(MAX_SIZE);
	private final Map<String, Trace2DLtd>	traces				= new HashMap<String, Trace2DLtd>();
	private final ArrayList<Color>			colors				= new ArrayList<Color>();
	private final JTextField					txtMaxSize;
	private final long							timeOffset			= System.nanoTime();
	
	private boolean								freeze				= false;
	private boolean								active				= false;
	private boolean								keepActive			= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TimerChartPanel()
	{
		setLayout(new BorderLayout());
		final JPanel diagramPanel = new JPanel(new MigLayout("fill"));
		final JPanel controlPanel = new JPanel();
		
		colors.add(Color.green);
		colors.add(Color.blue);
		colors.add(Color.orange);
		colors.add(Color.cyan);
		colors.add(Color.magenta);
		colors.add(Color.yellow);
		colors.add(Color.pink);
		colors.add(Color.darkGray);
		colors.add(Color.red);
		
		chart.setBackground(getBackground());
		chart.setForeground(Color.BLACK);
		chart.setDoubleBuffered(true);
		
		chart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(0.0, MAXIMUM_DURATION * 1.3)));
		chart.getAxisY().setAxisTitle(new AxisTitle("Pass [ms]"));
		chart.getAxisX().setAxisTitle(new AxisTitle("t [ms]"));
		
		maximum.setColor(Color.RED);
		maximum.setName("Maximum time");
		chart.addTrace(maximum);
		
		final JButton btnFreeze = new JButton("Freeze");
		controlPanel.add(btnFreeze);
		btnFreeze.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				freeze = !freeze;
			}
		});
		final JCheckBox chkKeepActive = new JCheckBox("Keep active", keepActive);
		controlPanel.add(chkKeepActive);
		chkKeepActive.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				keepActive = chkKeepActive.isSelected();
			}
		});
		
		txtMaxSize = new JTextField("" + MAX_SIZE, 6);
		txtMaxSize.setToolTipText("Max x values");
		controlPanel.add(txtMaxSize);
		txtMaxSize.addKeyListener(new TextFieldKeyListener());
		
		diagramPanel.add(chart, "grow");
		this.add(diagramPanel, BorderLayout.CENTER);
		this.add(controlPanel, BorderLayout.NORTH);
		
		addMouseWheelListener(new ChartMouseWheelListener());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param info
	 */
	public void onNewTimerInfo(TimerInfo info)
	{
		if (isFreeze())
		{
			return;
		}
		final long now = System.nanoTime();
		final float step = (now - timeOffset) / NS_TO_MS;
		
		maximum.addPoint(step, MAXIMUM_DURATION);
		
		for (final Map.Entry<String, Timing> entry : info.getTimings().entrySet())
		{
			final String name = entry.getKey();
			final Timing timing = entry.getValue();
			final float fTiming = timing.duration / NS_TO_MS;
			Trace2DLtd trace = traces.get(name);
			if (trace == null)
			{
				trace = new Trace2DLtd(MAX_SIZE);
				trace.setColor(colors.get(traces.size() % colors.size()));
				trace.setName(name);
				chart.addTrace(trace);
				traces.put(name, trace);
			}
			trace.addPoint(step, fTiming);
		}
	}
	
	
	/**
	 *
	 */
	public void clearChart()
	{
		for (final ITrace2D trace : traces.values())
		{
			trace.removeAllPoints();
		}
		maximum.removeAllPoints();
	}
	
	
	/**
	 * @return
	 */
	public boolean isFreeze()
	{
		return !active || freeze;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setVisible(boolean visible)
	{
		if (!keepActive)
		{
			if (!active)
			{
				clearChart();
			}
			active = visible;
		}
		chart.setVisible(visible);
	}
	
	private class TextFieldKeyListener implements KeyListener
	{
		
		@Override
		public void keyTyped(KeyEvent e)
		{
			try
			{
				final int size = Integer.parseInt(txtMaxSize.getText());
				maximum.setMaxSize(size);
				for (final Trace2DLtd trace : traces.values())
				{
					trace.setMaxSize(size);
				}
			} catch (final NumberFormatException err)
			{
				
			}
		}
		
		
		@Override
		public void keyReleased(KeyEvent e)
		{
		}
		
		
		@Override
		public void keyPressed(KeyEvent e)
		{
		}
	}
	
	private class ChartMouseWheelListener implements MouseWheelListener
	{
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e)
		{
			currentMaxY += e.getWheelRotation() / 5f;
			if (currentMaxY <= (-MAXIMUM_DURATION + 0.1f))
			{
				currentMaxY = -MAXIMUM_DURATION + 0.1f;
			}
			
			chart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(0.0, MAXIMUM_DURATION + currentMaxY)));
		}
		
	}
}
