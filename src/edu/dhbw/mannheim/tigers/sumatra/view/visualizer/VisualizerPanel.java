/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.08.2010
 * Author(s): BernhardP
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.main.ISumatraView;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.OptionsPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.RobotsPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.FieldPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.replay.ReplayLoadPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.replay.ReplayOptionsPanel;


/**
 * Visualizes the current game situation.
 * It also allows the user to set a robot at a determined position.
 * 
 * @author BernhardP, OliverS
 * 
 */
public class VisualizerPanel extends JPanel implements ISumatraView
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long			serialVersionUID		= 2686191777355388548L;
	
	private static final int			ID							= 4;
	private static final String		TITLE						= "Visualizer";
	
	private final FieldPanel			fieldPanel;
	
	private final JTextField			teamColor;
	private final RobotsPanel			robotsPanel;
	private final OptionsPanel			optionsPanel;
	private final ReplayOptionsPanel	replayOptionsPanel;
	private final ReplayLoadPanel		replayLoadPanel;
	
	private Boolean						lastTigersAreYellow	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public VisualizerPanel()
	{
		// --- set layout ---
		setLayout(new MigLayout("fill, inset 0", "[min!][left][]", "[top]"));
		
		// --- left panel ---
		final JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
		// --- right panel ---
		final JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));
		
		
		// team color panel
		teamColor = new JTextField();
		teamColor.setEditable(false);
		teamColor.setBackground(Color.WHITE);
		final JPanel teamColorPanel = new JPanel(new MigLayout("fill", "[fill]", "[fill]"));
		
		final TitledBorder border = BorderFactory.createTitledBorder("Team");
		border.setTitleJustification(TitledBorder.CENTER);
		teamColorPanel.setBorder(border);
		teamColorPanel.add(teamColor);
		
		
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
		leftPanel.add(teamColorPanel, "grow, top, wrap");
		leftPanel.add(robotsPanel, "grow, top");
		leftPanel.add(Box.createVerticalGlue());
		rightPanel.add(optionsPanel);
		rightPanel.add(replayOptionsPanel);
		rightPanel.add(replayLoadPanel);
		rightPanel.add(Box.createVerticalGlue());
		
		add(leftPanel);
		add(fieldPanel, "grow, top");
		add(rightPanel);
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- ISumatraView ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
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
	 * 
	 * @return
	 */
	public IFieldPanel getFieldPanel()
	{
		return fieldPanel;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public RobotsPanel getRobotsPanel()
	{
		return robotsPanel;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public OptionsPanel getOptionsPanel()
	{
		return optionsPanel;
	}
	
	
	/**
	 * 
	 * @param tigersAreYellow
	 */
	public void setTigersAreYellow(boolean tigersAreYellow)
	{
		if ((lastTigersAreYellow == null) || (lastTigersAreYellow != tigersAreYellow))
		{
			if (tigersAreYellow)
			{
				teamColor.setText("YELLOW");
				teamColor.setBackground(Color.YELLOW);
			} else
			{
				teamColor.setText("BLUE");
				teamColor.setBackground(Color.BLUE);
			}
			
			// Sub-panels
			getRobotsPanel().setTigersAreYellow(tigersAreYellow);
			
			lastTigersAreYellow = tigersAreYellow;
		}
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
}
