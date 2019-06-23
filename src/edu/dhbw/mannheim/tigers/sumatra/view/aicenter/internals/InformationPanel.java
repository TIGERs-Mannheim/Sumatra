/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EMatchBehavior;


/**
 * This panel gives an overview of the general information within the ai-module.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class InformationPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID				= 2855627682384616285L;
	
	private JTextField			playBehavior					= null;
	private EMatchBehavior		lastBehavior					= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public InformationPanel()
	{
		setLayout(new MigLayout("fill"));
		setBorder(BorderFactory.createTitledBorder("General AI information"));
		
		playBehavior = new JTextField();
		playBehavior.setEditable(false);
		playBehavior.setBackground(Color.WHITE);
		
		JPanel playBehaviorPanel = new JPanel(new MigLayout("fill", "[60,fill]10[100,fill]"));
		playBehaviorPanel.add(new JLabel("Play behavior:"));
		playBehaviorPanel.add(playBehavior);
		
		add(playBehaviorPanel);
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public void setPlayBehavior(EMatchBehavior behavior)
	{
		if (lastBehavior != behavior)
		{
			playBehavior.setText(behavior.toString());
			lastBehavior = behavior;
		}
	}	

	public void clearView()
	{
		playBehavior.setText("");
	}
}
