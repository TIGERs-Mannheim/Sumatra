/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 1, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenterv2;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.FusionCtrlPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.SelectControllerPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.StructurePanel;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BcBotControllerCfgPanel extends JPanel
{
	/**  */
	private static final long				serialVersionUID			= 2164531377681154919L;
	
	private final SelectControllerPanel	selectControllerPanel	= new SelectControllerPanel();
	private final FusionCtrlPanel			fusionCtrlPanel			= new FusionCtrlPanel();
	private final StructurePanel			structurePanel				= new StructurePanel();
	
	
	/**
	 * 
	 */
	public BcBotControllerCfgPanel()
	{
		setLayout(new BorderLayout());
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("general", setupScrollPane(selectControllerPanel));
		tabbedPane.addTab("fusion", setupScrollPane(fusionCtrlPanel));
		tabbedPane.addTab("structure", setupScrollPane(structurePanel));
		add(tabbedPane, BorderLayout.CENTER);
	}
	
	
	private Component setupScrollPane(final Component comp)
	{
		JScrollPane scrollPane = new JScrollPane(comp);
		scrollPane.setPreferredSize(new Dimension(0, 0));
		return scrollPane;
	}
	
	
	/**
	 * @return the selectControllerPanel
	 */
	public SelectControllerPanel getSelectControllerPanel()
	{
		return selectControllerPanel;
	}
	
	
	/**
	 * @return the fusionCtrlPanel
	 */
	public FusionCtrlPanel getFusionCtrlPanel()
	{
		return fusionCtrlPanel;
	}
	
	
	/**
	 * @return the structurePanel
	 */
	public StructurePanel getStructurePanel()
	{
		return structurePanel;
	}
}
