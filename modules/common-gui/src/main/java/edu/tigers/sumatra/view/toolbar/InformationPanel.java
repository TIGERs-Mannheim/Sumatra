/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.tigers.sumatra.view.toolbar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import net.miginfocom.swing.MigLayout;


/**
 * This panel gives an overview of the general information within the ai-module.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 */
public class InformationPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log							= Logger.getLogger(InformationPanel.class.getName());
	private static final int		TEXT_BOX_WIDTH				= 400;
	
	private static final long		serialVersionUID			= 2855627682384616285L;
	
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
		setLayout(new MigLayout("inset 1", "", "[center]"));
		
		aIExceptionCounterField = new JTextField(3);
		aIExceptionCounterField.setEditable(false);
		aIExceptionCounterField.setBackground(Color.WHITE);
		aIExceptionCounterField.setText(String.valueOf(aIExceptionCounter));
		
		JButton clearAIExceptionButton = new JButton("Clear");
		clearAIExceptionButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent arg0)
			{
				clearAIExceptionArea();
			}
		});
		
		// textArea and scrollpane for the exception message
		aIExceptionText = new JTextArea();
		aIExceptionText.setEditable(false);
		aIExceptionText.setBackground(Color.WHITE);
		
		final JScrollPane scrollPane = new JScrollPane(aIExceptionText, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(TEXT_BOX_WIDTH, 0));
		
		add(clearAIExceptionButton);
		add(aIExceptionCounterField);
		add(scrollPane, "growy");
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param ex
	 */
	public void setAIException(final Throwable ex)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				if (!lastAIException.equals(ex.getMessage()))
				{
					log.error(String.valueOf(ex.getMessage()), ex);
					String text = ex.getMessage();
					if ((text == null) || text.isEmpty())
					{
						text = ex.getClass().getName();
					}
					if (!aIExceptionText.getText().isEmpty())
					{
						aIExceptionText.append("\n");
					}
					aIExceptionText.append(text);
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
				aIExceptionCounter = 0;
				aIExceptionCounterField.setText(String.valueOf(aIExceptionCounter));
			}
		});
	}
}
