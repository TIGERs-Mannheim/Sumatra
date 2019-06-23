/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.aicenter;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.InformationPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.botoverview.BotFullOverviewPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview.ModuleControlPanel;


/**
 * This is the main panel of the ai view in sumatra.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class AICenterPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final long		serialVersionUID	= 8132550010453691515L;
	
	private InformationPanel		informationPanel	= null;
	private BotFullOverviewPanel	botPanel				= null;
	private ModuleControlPanel		modulesPanel		= null;
	
	private JScrollPane				scrollPane			= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public AICenterPanel()
	{
		this.setLayout(new BorderLayout());
		
		JPanel combinePanel = new JPanel();
		combinePanel.setLayout(new MigLayout("fill", "", ""));
		
		informationPanel = new InformationPanel();
		botPanel = new BotFullOverviewPanel();
		modulesPanel = new ModuleControlPanel();
		
		combinePanel.add(informationPanel, "wrap");
		combinePanel.add(modulesPanel, "wrap");
		combinePanel.add(botPanel);
		

		scrollPane = new JScrollPane(combinePanel);
		this.add(scrollPane, BorderLayout.CENTER);
		
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public InformationPanel getInformationPanel()
	{
		return informationPanel;
	}
	

	public ModuleControlPanel getModulesPanel()
	{
		return modulesPanel;
	}
	

	public BotFullOverviewPanel getBotOverviewPanel()
	{
		return botPanel;
	}
	

	public void clearView()
	{
		informationPanel.clearView();
		modulesPanel.onStop();
	}
}
