/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.dhbw.mannheim.tigers.sumatra.view.timer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.timer.ETimerStatistic;
import edu.tigers.sumatra.timer.TimerInfo;
import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyFixedViewport;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.util.Range;
import net.miginfocom.swing.MigLayout;


/**
 * This panels visualizes the actual timer-data
 * 
 * @author Gero
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TimerChartPanel extends JPanel
{
	private static final Logger				log						= Logger.getLogger(TimerChartPanel.class.getName());
	private static final long					serialVersionUID		= -422217644518603954L;
	
	/** Maximum time everything is allowed to take (currently {@value} , 60 FPS) */
	private static final double				MAXIMUM_DURATION		= 16.7;
	private double									currentMaxY				= 1.3;
	
	private final JTextField					txtMaxRange;
	private int										maxRange					= 1200;
	
	private final JTextField					txtCombinedFrames;
	private int										combinedFrames			= 10;
	
	private ETimerStatistic						timerStatisticType	= ETimerStatistic.AVG;
	
	private final ArrayList<Color>			colors					= new ArrayList<>();
	private final Map<String, Trace2DLtd>	traces					= new LinkedHashMap<>();
	private final Map<String, Long>			maxIds					= new HashMap<>();
	private final Set<String>					knownTimables			= new HashSet<>();
	private final Set<String>					displayedTimables		= new LinkedHashSet<>();
	
	private boolean								freeze					= false;
	private boolean								active					= true;
	private boolean								keepActive				= false;
	
	private final Chart2D						chart						= new Chart2D();
	private final Trace2DLtd					maximum					= new Trace2DLtd(maxRange);
	
	private final JMenu							mTimeables;
	
	
	/**
	 * Default
	 */
	public TimerChartPanel()
	{
		setLayout(new BorderLayout());
		final JPanel diagramPanel = new JPanel(new MigLayout("fill, inset 0"));
		
		colors.add(Color.green);
		colors.add(Color.blue);
		colors.add(Color.orange);
		colors.add(Color.cyan);
		colors.add(Color.yellow);
		colors.add(Color.magenta);
		colors.add(Color.pink);
		colors.add(Color.darkGray);
		colors.add(Color.red);
		
		chart.setBackground(getBackground());
		chart.setForeground(Color.BLACK);
		chart.setDoubleBuffered(true);
		
		chart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(0.0, MAXIMUM_DURATION * 1.3)));
		chart.getAxisX().setRangePolicy(new RangePolicyDynamicViewport(maxRange));
		chart.getAxisY().setAxisTitle(new AxisTitle("duration [ms]"));
		chart.getAxisX().setAxisTitle(new AxisTitle(""));
		chart.getAxisX().setPaintScale(false);
		
		displayedTimables.add("AI_YELLOW_PRIMARY");
		displayedTimables.add("AI_YELLOW_SECONDARY");
		displayedTimables.add("AI_BLUE_PRIMARY");
		displayedTimables.add("AI_BLUE_SECONDARY");
		
		maximum.setColor(Color.RED);
		maximum.setName("Maximum time");
		chart.addTrace(maximum);
		
		JMenuBar menuBar = new JMenuBar();
		JMenu mAction = new JMenu("TimerPanel");
		final JMenuItem btnFreeze = new JMenuItem("Freeze");
		mAction.add(btnFreeze);
		menuBar.add(mAction);
		btnFreeze.addActionListener(e -> freeze = !freeze);
		final JCheckBox chkKeepActive = new JCheckBox("Keep active", keepActive);
		mAction.add(chkKeepActive);
		chkKeepActive.addActionListener(e -> keepActive = chkKeepActive.isSelected());
		
		txtCombinedFrames = new JTextField(String.valueOf(combinedFrames), 3);
		JMenu mFrames = new JMenu("Combined frames");
		menuBar.add(mFrames);
		mFrames.add(txtCombinedFrames);
		txtCombinedFrames.addKeyListener(new TextFieldAverageKeyListener());
		
		txtMaxRange = new JTextField(String.valueOf(maxRange), 6);
		txtMaxRange.setToolTipText("Max x values");
		JMenu mRange = new JMenu("Range");
		menuBar.add(mRange);
		mRange.add(txtMaxRange);
		txtMaxRange.addKeyListener(new TextFieldKeyListener());
		
		mTimeables = new JMenu("Timeables");
		menuBar.add(mTimeables);
		final JCheckBox chkBox = new JCheckBox("All");
		mTimeables.add(chkBox);
		chkBox.addActionListener(e -> {
			
			if (chkBox.isSelected())
			{
				for (String t : knownTimables)
				{
					displayedTimables.add(t);
				}
				chkBox.setName("None");
			} else
			{
				for (String t : knownTimables)
				{
					displayedTimables.remove(t);
				}
				chkBox.setName("All");
			}
		});
		
		ButtonGroup group = new ButtonGroup();
		JMenu mPlots = new JMenu("Aggregate");
		menuBar.add(mPlots);
		for (final ETimerStatistic tStat : ETimerStatistic.values())
		{
			final JRadioButton rdb = new JRadioButton(tStat.name());
			group.add(rdb);
			mPlots.add(rdb);
			if (timerStatisticType.equals(tStat))
			{
				rdb.setSelected(true);
			}
			rdb.addActionListener(e -> timerStatisticType = tStat);
		}
		
		diagramPanel.add(chart, "grow");
		this.add(menuBar, BorderLayout.PAGE_START);
		this.add(diagramPanel, BorderLayout.CENTER);
		
		addMouseWheelListener(new ChartMouseWheelListener());
	}
	
	
	private void addTimeableMenu(String timable)
	{
		final JCheckBox chkBox = new JCheckBox(timable);
		mTimeables.add(chkBox);
		if (displayedTimables.contains(timable))
		{
			chkBox.setSelected(true);
		}
		
		chkBox.addActionListener(e -> {
			if (chkBox.isSelected())
			{
				displayedTimables.add(timable);
			} else
			{
				displayedTimables.remove(timable);
			}
		});
	}
	
	
	private void addTimeableIfNotPresent(final TimerInfo info)
	{
		for (String s : info.getAllTimables())
		{
			if (!knownTimables.contains(s))
			{
				addTimeableMenu(s);
				knownTimables.add(s);
			}
		}
	}
	
	
	/**
	 * @param info
	 */
	public void onNewTimerInfo(final TimerInfo info)
	{
		if (isFreeze())
		{
			return;
		}
		addTimeableIfNotPresent(info);
		
		for (String timable : displayedTimables)
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
					trace.setName(timable);
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
	 * Clear full chart
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
			// nothing to do
		}
		
		
		@Override
		public void keyReleased(final KeyEvent e)
		{
			// nothing to do
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
					log.error("Could not parse range: " + txtMaxRange.getText(), err);
				}
			}
		}
	}
	
	private class TextFieldAverageKeyListener implements KeyListener
	{
		
		@Override
		public void keyTyped(final KeyEvent e)
		{
			// nothing to do
		}
		
		
		@Override
		public void keyReleased(final KeyEvent e)
		{
			// nothing to do
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
					log.error("Could not parse frames: " + txtCombinedFrames.getText(), err);
				}
			}
		}
	}
	
	private class ChartMouseWheelListener implements MouseWheelListener
	{
		@Override
		public void mouseWheelMoved(final MouseWheelEvent e)
		{
			currentMaxY += e.getWheelRotation() / 5.0;
			if (currentMaxY <= (-MAXIMUM_DURATION + 0.1))
			{
				currentMaxY = -MAXIMUM_DURATION + 0.1;
			}
			
			chart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(0.0, MAXIMUM_DURATION + currentMaxY)));
		}
	}
}
