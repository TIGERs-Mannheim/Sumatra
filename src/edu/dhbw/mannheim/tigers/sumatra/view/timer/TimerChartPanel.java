/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.09.2010
 * Author(s): Gero
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
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.timer.ETimerStatistic;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.timer.TimerInfo;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.timer.ETimable;


/**
 * This panels visualizes the actual timer-data
 * 
 * @author Gero
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TimerChartPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long						serialVersionUID		= -422217644518603954L;
	
	/** Maximum time everything is allowed to take (currently {@value} , 60 FPS) */
	private static final float						MAXIMUM_DURATION		= 16.7f;
	private float										currentMaxY				= 1.3f;
	private final int									maxTraces				= ETimable.values().length;
	
	private final JTextField						txtMaxRange;
	private int											maxRange					= 1200;
	
	private final JTextField						txtCombinedFrames;
	private int											combinedFrames			= 10;
	
	private ETimerStatistic							timerStatisticType	= ETimerStatistic.AVG;
	
	private final ArrayList<Color>				colors					= new ArrayList<Color>(maxTraces);
	private final Map<ETimable, Trace2DLtd>	traces					= new HashMap<ETimable, Trace2DLtd>(maxTraces);
	private final Map<ETimable, Long>			maxIds					= new HashMap<ETimable, Long>(maxTraces);
	private final List<ETimable>					displayedTimables		= new CopyOnWriteArrayList<ETimable>();
	
	private boolean									freeze					= false;
	private boolean									active					= false;
	private boolean									keepActive				= false;
	
	private final Chart2D							chart						= new Chart2D();
	private final Trace2DLtd						maximum					= new Trace2DLtd(maxRange);
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TimerChartPanel()
	{
		setLayout(new BorderLayout());
		final JPanel diagramPanel = new JPanel(new MigLayout("fill, inset 0"));
		final JPanel controlPanel = new JPanel(new MigLayout("inset 0, center"));
		
		colors.add(Color.green);
		colors.add(Color.blue);
		colors.add(Color.orange);
		colors.add(Color.cyan);
		colors.add(Color.magenta);
		colors.add(Color.yellow);
		colors.add(Color.pink);
		colors.add(Color.darkGray);
		colors.add(Color.red);
		
		displayedTimables.add(ETimable.WP);
		displayedTimables.add(ETimable.AGENT_Y);
		displayedTimables.add(ETimable.AGENT_B);
		
		chart.setBackground(getBackground());
		chart.setForeground(Color.BLACK);
		chart.setDoubleBuffered(true);
		
		chart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(0.0, MAXIMUM_DURATION * 1.3)));
		chart.getAxisX().setRangePolicy(new RangePolicyDynamicViewport(maxRange));
		chart.getAxisY().setAxisTitle(new AxisTitle("duration [ms]"));
		chart.getAxisX().setAxisTitle(new AxisTitle(""));
		chart.getAxisX().setPaintScale(false);
		
		maximum.setColor(Color.RED);
		maximum.setName("Maximum time");
		chart.addTrace(maximum);
		
		final JButton btnFreeze = new JButton("Freeze");
		controlPanel.add(btnFreeze);
		btnFreeze.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				freeze = !freeze;
			}
		});
		final JCheckBox chkKeepActive = new JCheckBox("Keep active", keepActive);
		controlPanel.add(chkKeepActive);
		chkKeepActive.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				keepActive = chkKeepActive.isSelected();
			}
		});
		
		txtCombinedFrames = new JTextField(String.valueOf(combinedFrames), 3);
		JLabel lblCombinedFrames = new JLabel("Combined frames: ");
		controlPanel.add(lblCombinedFrames);
		controlPanel.add(txtCombinedFrames);
		txtCombinedFrames.addKeyListener(new TextFieldAverageKeyListener());
		
		txtMaxRange = new JTextField(String.valueOf(maxRange), 6);
		JLabel lblMaxRange = new JLabel("Range: ");
		txtMaxRange.setToolTipText("Max x values");
		controlPanel.add(lblMaxRange);
		controlPanel.add(txtMaxRange);
		txtMaxRange.addKeyListener(new TextFieldKeyListener());
		
		JPanel timablePanel = new JPanel(new MigLayout("fill, insets 0 0 13 0"));
		JScrollPane timableScrollPane = new JScrollPane(timablePanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		timableScrollPane.setBorder(BorderFactory.createEmptyBorder());
		for (final ETimable timable : ETimable.values())
		{
			final JCheckBox chkBox = new JCheckBox(timable.name());
			timablePanel.add(chkBox);
			if (displayedTimables.contains(timable))
			{
				chkBox.setSelected(true);
			}
			chkBox.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					if (chkBox.isSelected())
					{
						displayedTimables.add(timable);
					} else
					{
						displayedTimables.remove(timable);
					}
				}
			});
		}
		
		ButtonGroup group = new ButtonGroup();
		
		for (final ETimerStatistic tStat : ETimerStatistic.values())
		{
			final JRadioButton rdb = new JRadioButton(tStat.name());
			group.add(rdb);
			controlPanel.add(rdb);
			if (timerStatisticType.equals(tStat))
			{
				rdb.setSelected(true);
			}
			rdb.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					timerStatisticType = tStat;
				}
			});
		}
		
		diagramPanel.add(chart, "grow");
		this.add(controlPanel, BorderLayout.NORTH);
		this.add(diagramPanel, BorderLayout.CENTER);
		this.add(timableScrollPane, BorderLayout.SOUTH);
		
		addMouseWheelListener(new ChartMouseWheelListener());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param info
	 */
	public void onNewTimerInfo(final TimerInfo info)
	{
		if (isFreeze())
		{
			return;
		}
		
		for (ETimable timable : displayedTimables)
		{
			final SortedMap<Long, Long> timings = info.getCombinedTimings(timable, combinedFrames, timerStatisticType);
			if (timings.isEmpty())
			{
				continue;
			}
			Long maxId = maxIds.get(timable);
			if (maxId == null)
			{
				maxId = timings.firstKey();
			}
			SortedMap<Long, Long> upperTimings = timings.tailMap(maxId);
			upperTimings.remove(maxId);
			
			long frameId = maxId;
			for (Map.Entry<Long, Long> entry : upperTimings.entrySet())
			{
				frameId = entry.getKey();
				double value = entry.getValue() / 1e6;
				Trace2DLtd trace = traces.get(timable);
				if (trace == null)
				{
					trace = new Trace2DLtd(maxRange);
					trace.setColor(colors.get(traces.size() % colors.size()));
					trace.setName(timable.name());
					chart.addTrace(trace);
					traces.put(timable, trace);
				}
				trace.addPoint(frameId, value);
			}
			maximum.addPoint(frameId, MAXIMUM_DURATION);
			maximum.addPoint(0, MAXIMUM_DURATION);
			maxIds.put(timable, frameId);
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
		traces.clear();
		chart.removeAllTraces();
		chart.addTrace(maximum);
		maxIds.clear();
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
	public void setVisible(final boolean visible)
	{
		if (!keepActive)
		{
			active = visible;
		}
		chart.setVisible(visible);
	}
	
	private class TextFieldKeyListener implements KeyListener
	{
		
		@Override
		public void keyTyped(final KeyEvent e)
		{
		}
		
		
		@Override
		public void keyReleased(final KeyEvent e)
		{
		}
		
		
		@Override
		public void keyPressed(final KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_ENTER)
			{
				try
				{
					maxRange = Integer.parseInt(txtMaxRange.getText());
					maximum.setMaxSize(maxRange);
					for (final Trace2DLtd trace : traces.values())
					{
						trace.setMaxSize(maxRange);
					}
					chart.getAxisX().setRangePolicy(new RangePolicyDynamicViewport(maxRange));
				} catch (final NumberFormatException err)
				{
				}
			}
		}
	}
	
	private class TextFieldAverageKeyListener implements KeyListener
	{
		
		@Override
		public void keyTyped(final KeyEvent e)
		{
			
		}
		
		
		@Override
		public void keyReleased(final KeyEvent e)
		{
		}
		
		
		@Override
		public void keyPressed(final KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_ENTER)
			{
				try
				{
					combinedFrames = Integer.parseInt(txtCombinedFrames.getText());
				} catch (final NumberFormatException err)
				{
				}
			}
		}
	}
	
	private class ChartMouseWheelListener implements MouseWheelListener
	{
		
		@Override
		public void mouseWheelMoved(final MouseWheelEvent e)
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
