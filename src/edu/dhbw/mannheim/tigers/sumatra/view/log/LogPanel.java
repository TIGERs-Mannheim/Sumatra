/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.08.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.log;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Priority;

import edu.dhbw.mannheim.tigers.sumatra.view.log.internals.FilterPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.log.internals.SlidePanel;
import edu.dhbw.mannheim.tigers.sumatra.view.log.internals.TextPane;
import edu.dhbw.mannheim.tigers.sumatra.view.log.internals.TreePanel;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraView;


/**
 * ( @see {@link edu.dhbw.mannheim.tigers.sumatra.presenter.log.LogPresenter})
 * 
 * @author AndreR
 * 
 */
public class LogPanel extends JPanel implements ISumatraView
{
	private static final long	serialVersionUID	= 1L;
	
	private static final int	INIT_DIVIDER_LOC	= 150;
	
	
	private final TextPane		textPane;
	private final FilterPanel	filterPanel;
	private final TreePanel		treePanel;
	
	private final JMenu			logMenu;
	
	
	/**
	 * @param maxCapacity
	 * @param initialLevel
	 */
	public LogPanel(int maxCapacity, Priority initialLevel)
	{
		setLayout(new MigLayout("fill, inset 0", "", ""));
		
		textPane = new TextPane(maxCapacity);
		filterPanel = new FilterPanel(initialLevel);
		treePanel = new TreePanel();
		
		logMenu = new JMenu("Log");
		final JCheckBoxMenuItem item = new JCheckBoxMenuItem("Autoscrolling");
		item.setSelected(true);
		item.addActionListener(new ChangeAutoscrolling());
		logMenu.add(item);
		
		
		final JPanel filter = new JPanel(new MigLayout("fill", "", ""));
		filter.add(treePanel, "push, grow, wrap");
		filter.setMinimumSize(new Dimension(0, 0));
		
		final JPanel display = new JPanel(new MigLayout("fill", "", ""));
		display.add(textPane, "push, grow, wrap");
		display.add(filterPanel, "growx");
		
		final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, filter, display);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(0);
		splitPane.setLastDividerLocation(INIT_DIVIDER_LOC);
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		
		add(splitPane, "grow");
	}
	
	
	/**
	 * @return
	 */
	public TextPane getTextPane()
	{
		return textPane;
	}
	
	
	/**
	 * @return
	 */
	public FilterPanel getFilterPanel()
	{
		return filterPanel;
	}
	
	
	/**
	 * @return
	 */
	public SlidePanel getSlidePanel()
	{
		return filterPanel.getSlidePanel();
	}
	
	
	/**
	 * @return
	 */
	public TreePanel getTreePanel()
	{
		return treePanel;
	}
	
	
	@Override
	public List<JMenu> getCustomMenus()
	{
		final ArrayList<JMenu> customMenu = new ArrayList<JMenu>();
		customMenu.add(logMenu);
		return customMenu;
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
	
	protected class ChangeAutoscrolling implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			final JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
			setAutoScrolling(item.isSelected());
		}
	}
	
	
	/**
	 * @param active
	 */
	public void setAutoScrolling(boolean active)
	{
		textPane.setAutoscroll(active);
	}
}
