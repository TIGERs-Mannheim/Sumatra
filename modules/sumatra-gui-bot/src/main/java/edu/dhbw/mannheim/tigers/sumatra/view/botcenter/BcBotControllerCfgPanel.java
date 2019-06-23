/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 1, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.bots.SelectControllerPanel;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BcBotControllerCfgPanel extends JPanel
{
	/**  */
	private static final long				serialVersionUID			= 2164531377681154919L;
	
	private final SelectControllerPanel	selectControllerPanel	= new SelectControllerPanel();
	
	
	/**
	 * 
	 */
	public BcBotControllerCfgPanel()
	{
		setLayout(new BorderLayout());
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("general", setupScrollPane(selectControllerPanel));
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
}
