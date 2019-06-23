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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

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
	// Logger
	private static final Logger	log							= Logger.getLogger(InformationPanel.class.getName());
	
	private static final long		serialVersionUID			= 2855627682384616285L;
	
	private JTextField				playBehavior				= null;
	private EMatchBehavior			lastBehavior				= EMatchBehavior.NOT_DEFINED;
	
	private JTextArea					aIExceptionText			= null;
	private JTextField				aIExceptionCounterField	= null;
	private String						lastAIException			= "";
	private int							aIExceptionCounter		= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public InformationPanel()
	{
		setLayout(new MigLayout("fill"));
		setBorder(BorderFactory.createTitledBorder("General AI information"));
		
		// playBehavior parts
		playBehavior = new JTextField();
		playBehavior.setEditable(false);
		playBehavior.setBackground(Color.WHITE);
		
		final JPanel playBehaviorPanel = new JPanel(new MigLayout("fill", "[60,fill]10[100,fill]"));
		playBehaviorPanel.add(new JLabel("Play behavior:"));
		playBehaviorPanel.add(playBehavior);
		
		// label, counter and clear button
		final JLabel aIExceptionLabel = new JLabel("AI Exception: ");
		
		aIExceptionCounterField = new JTextField();
		aIExceptionCounterField.setEditable(false);
		aIExceptionCounterField.setBackground(Color.WHITE);
		aIExceptionCounterField.setPreferredSize(new Dimension(30, 20));
		aIExceptionCounterField.setText(String.valueOf(aIExceptionCounter));
		
		JButton clearAIExceptionButton = new JButton("Clear Textarea");
		clearAIExceptionButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				clearAIExceptionArea();
			}
		});
		
		// panels for the label, counter and clear button
		
		final JPanel aIExceptionLCPanel = new JPanel(new MigLayout("fill", "[30,fill]0[30,fill]"));
		aIExceptionLCPanel.add(aIExceptionLabel);
		aIExceptionLCPanel.add(aIExceptionCounterField);
		
		
		final JPanel aIExceptionLCCBPanel = new JPanel(new MigLayout("fill"));
		aIExceptionLCCBPanel.add(aIExceptionLCPanel, "wrap");
		aIExceptionLCCBPanel.add(clearAIExceptionButton);
		
		
		// textArea and scrollpane for the exception message
		aIExceptionText = new JTextArea();
		aIExceptionText.setEditable(false);
		aIExceptionText.setBackground(Color.WHITE);
		
		final JScrollPane scrollPane = new JScrollPane(aIExceptionText);
		scrollPane.setPreferredSize(new Dimension(200, 50));
		
		
		// putting the panels for the Exception together
		final JPanel aIExceptionPanel = new JPanel(new MigLayout("fill", "[60,fill]10[340,fill]", "[fill]"));
		aIExceptionPanel.add(aIExceptionLCCBPanel);
		aIExceptionPanel.add(scrollPane, "push, grow, wrap");
		
		add(playBehaviorPanel);
		add(aIExceptionPanel);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param behavior
	 */
	public void setPlayBehavior(final EMatchBehavior behavior)
	{
		if ((behavior != null) && !behavior.equals(lastBehavior))
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					playBehavior.setText(behavior.toString());
					lastBehavior = behavior;
				}
			});
		}
	}
	
	
	/**
	 * @param ex
	 */
	public void setAIException(final Exception ex)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				if (!lastAIException.equals(ex.getMessage()))
				{
					log.error(String.valueOf(ex.getMessage()), ex);
					aIExceptionText.append(ex.getMessage() + "\n");
					lastAIException = String.valueOf(ex.getMessage());
					aIExceptionText.setBackground(Color.RED);
					aIExceptionCounter++;
					aIExceptionCounterField.setText(String.valueOf(aIExceptionCounter));
				} else
				{
					aIExceptionCounter++;
					aIExceptionCounterField.setText(String.valueOf(aIExceptionCounter));
				}
			}
		});
	}
	
	
	/**
	 */
	public void clearView()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				playBehavior.setText("");
				clearAIExceptionArea();
			}
		});
	}
	
	
	private void clearAIExceptionArea()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				aIExceptionText.setText("");
				aIExceptionText.setBackground(Color.WHITE);
				aIExceptionCounterField.setText("");
				aIExceptionCounter = 0;
			}
		});
	}
}
