/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 20, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.statistics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import edu.dhbw.mannheim.tigers.sumatra.view.main.ISumatraView;
import edu.dhbw.mannheim.tigers.sumatra.view.statistics.internals.ITimeSliderObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.statistics.internals.history.HistoryTableModel;
import edu.dhbw.mannheim.tigers.sumatra.view.statistics.internals.summary.SummaryTableModel;


/**
 * shows the statistics of the play finder
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
public class PlayFinderStatisticsPanel extends JPanel implements ISumatraView
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long				serialVersionUID	= -8385440725186953626L;
	
	
	private static final int				ID						= 14;
	private static final String			TITLE					= "Play Finder Statistics";
	private static final int				INIT_DIVIDER_LOC	= 150;
	
	private final JTable						historyTable;
	private final JTable						summaryTable;
	private final JSlider					timeSlider;
	private final JButton					loadButton;
	
	private long								offset;
	
	private List<ITimeSliderObserver>	observers			= new LinkedList<ITimeSliderObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param historyTableModel
	 * @param summaryTableModel
	 * 
	 */
	public PlayFinderStatisticsPanel(HistoryTableModel historyTableModel, SummaryTableModel summaryTableModel)
	{
		setLayout(new BorderLayout());
		final JPanel summary = new JPanel(new BorderLayout());
		summaryTable = new JTable();
		summaryTable.setModel(summaryTableModel);
		summaryTable.setAutoCreateRowSorter(true);
		JScrollPane scrollPane = new JScrollPane(summaryTable);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		add(scrollPane, BorderLayout.CENTER);
		summary.add(scrollPane);
		
		final JPanel historyLog = new JPanel(new BorderLayout());
		historyTable = new JTable();
		historyTable.setModel(historyTableModel);
		historyTable.setAutoCreateRowSorter(true);
		scrollPane = new JScrollPane(historyTable);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		add(scrollPane, BorderLayout.CENTER);
		historyLog.add(scrollPane);
		
		final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, summary, historyLog);
		splitPane.setDividerLocation(INIT_DIVIDER_LOC);
		add(splitPane);
		
		timeSlider = new JSlider();
		timeSlider.setMinimum(0);
		timeSlider.setValue(0);
		timeSlider.setPaintLabels(true);
		timeSlider.setPaintTicks(true);
		timeSlider.setMajorTickSpacing(60);
		timeSlider.setMinorTickSpacing(10);
		timeSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				notifyOnTimeSlide();
			}
		});
		add(timeSlider, BorderLayout.SOUTH);
		
		loadButton = new JButton("Load statistics...");
		loadButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				notifyOnFileLoad();
			}
		});
		add(loadButton, BorderLayout.NORTH);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return selected time
	 */
	public long getTime()
	{
		return offset + (new Long(timeSlider.getValue()).longValue() * 1000); // Convert seconds back to timestamp in
																										// milliseconds and return
	}
	
	
	/**
	 * Fits the timeSlider
	 * 
	 * @param min
	 * @param max
	 */
	public void setTimeSlider(long min, long max)
	{
		timeSlider.setMaximum(new Long((max - min) / 1000).intValue()); // Times are in milliseconds, slider shows seconds
		offset = min - 500; // Subtract a half second so the play which starts at 0s can also be displayed
	}
	
	
	/**
	 * 
	 * Disables/enables the loadButton
	 * 
	 * @param b
	 */
	public void enableFileLoad(boolean b)
	{
		loadButton.setEnabled(b);
	}
	
	
	@Override
	public int getId()
	{
		return ID;
	}
	
	
	@Override
	public String getTitle()
	{
		return TITLE;
	}
	
	
	@Override
	public Component getViewComponent()
	{
		return this;
	}
	
	
	@Override
	public List<JMenu> getCustomMenus()
	{
		return null;
	}
	
	
	/**
	 * Adds an ITimeSliderObserver
	 * 
	 * @param iso
	 */
	public void addITimeSliderObserver(ITimeSliderObserver iso)
	{
		synchronized (observers)
		{
			observers.add(iso);
		}
	}
	
	
	/**
	 * Removes an ITimeSliderObserver
	 * 
	 * @param iso
	 */
	public void removeITimeSliderObserver(ITimeSliderObserver iso)
	{
		synchronized (observers)
		{
			observers.remove(iso);
		}
	}
	
	
	/**
	 * Notifies all ITimeSliderObservers that timeSlider has been changed
	 */
	public void notifyOnTimeSlide()
	{
		for (ITimeSliderObserver iso : observers)
		{
			iso.onTimeSlide();
		}
	}
	
	
	/**
	 * Notifies all ITimeSliderObservers that new file is going to be loaded
	 * 
	 */
	public void notifyOnFileLoad()
	{
		JFileChooser filechooser = new JFileChooser("logs/matchstats/");
		filechooser.setFileFilter(new FileFilter()
		{
			
			@Override
			public boolean accept(File arg0)
			{
				if (arg0.getName().endsWith(".odb"))
				{
					return true;
				}
				return false;
			}
			
			
			@Override
			public String getDescription()
			{
				return "Object Data Bases";
			}
			
		});
		int state = filechooser.showOpenDialog(this);
		String filename = "fullStats";
		if (state == JFileChooser.APPROVE_OPTION)
		{
			filename = filechooser.getSelectedFile().getName().split(".odb")[0];
			
			for (ITimeSliderObserver iso : observers)
			{
				iso.onFileLoad(filename);
			}
		}
	}
	
	
	@Override
	public void onShown()
	{
		
	}
	
	
	@Override
	public void onHidden()
	{
		
	}
	
	
	@Override
	public void onFocused()
	{
		
	}
	
	
	@Override
	public void onFocusLost()
	{
		
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
