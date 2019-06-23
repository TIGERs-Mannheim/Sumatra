/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.08.2010
 * Author(s): BernhardP
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer;

import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.OptionsPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.RobotsPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.FieldPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.replay.ReplayLoadPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.replay.ReplayOptionsPanel;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraView;


/**
 * Visualizes the current game situation.
 * It also allows the user to set a robot at a determined position.
 * 
 * @author BernhardP, OliverS
 */
public class VisualizerPanel extends JPanel implements ISumatraView
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long			serialVersionUID	= 2686191777355388548L;
	
	private final FieldPanel			fieldPanel;
	
	private final RobotsPanel			robotsPanel;
	private final OptionsPanel			optionsPanel;
	private final ReplayOptionsPanel	replayOptionsPanel;
	private final ReplayLoadPanel		replayLoadPanel;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public VisualizerPanel()
	{
		// --- set layout ---
		setLayout(new MigLayout("fill, inset 0", "[min!][max][right]", "[top]"));
		
		// --- right panel ---
		final JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));
		
		// --- init panels ---
		robotsPanel = new RobotsPanel();
		fieldPanel = new FieldPanel();
		optionsPanel = new OptionsPanel();
		replayOptionsPanel = new ReplayOptionsPanel();
		replayLoadPanel = new ReplayLoadPanel();
		
		optionsPanel.setAlignmentX(LEFT_ALIGNMENT);
		replayOptionsPanel.setAlignmentX(LEFT_ALIGNMENT);
		replayLoadPanel.setAlignmentX(LEFT_ALIGNMENT);
		
		// --- set panels ---
		rightPanel.add(optionsPanel);
		rightPanel.add(replayOptionsPanel);
		rightPanel.add(replayLoadPanel);
		rightPanel.add(Box.createVerticalGlue());
		rightPanel.setMinimumSize(new Dimension(0, 0));
		
		final JSplitPane splitPane = new CustomSplitPane(JSplitPane.HORIZONTAL_SPLIT, fieldPanel, rightPanel);
		splitPane.setOneTouchExpandable(true);
		splitPane.setEnabled(true);
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		
		add(robotsPanel);
		add(splitPane, "grow, top");
	}
	
	
	// --------------------------------------------------------------------------
	// --- ISumatraView ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public List<JMenu> getCustomMenus()
	{
		return null;
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
	
	/**
	 * @return
	 */
	public IFieldPanel getFieldPanel()
	{
		return fieldPanel;
	}
	
	
	/**
	 * @return
	 */
	public RobotsPanel getRobotsPanel()
	{
		return robotsPanel;
	}
	
	
	/**
	 * @return
	 */
	public OptionsPanel getOptionsPanel()
	{
		return optionsPanel;
	}
	
	
	/**
	 * @return the replayOptionsPanel
	 */
	public final ReplayOptionsPanel getReplayOptionsPanel()
	{
		return replayOptionsPanel;
	}
	
	
	/**
	 * @return the replayOptionsPanel
	 */
	public final ReplayLoadPanel getReplayLoadPanel()
	{
		return replayLoadPanel;
	}
	
	private static class CustomSplitPane extends JSplitPane
	{
		/** */
		private static final long	serialVersionUID	= -8505574271276379357L;
		
		
		/**
		 * @param newOrientation
		 * @param newLeftComponent
		 * @param newRightComponent
		 */
		public CustomSplitPane(final int newOrientation, final Component newLeftComponent,
				final Component newRightComponent)
		{
			super(newOrientation, newLeftComponent, newRightComponent);
		}
		
		
		@Override
		public int getDividerLocation()
		{
			return Math.max(super.getDividerLocation(), getWidth() - getRightComponent().getPreferredSize().width);
		}
		
		
		@Override
		public int getLastDividerLocation()
		{
			return getDividerLocation();
		}
	}
}
