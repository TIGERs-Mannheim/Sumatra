/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.log;

import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import org.apache.log4j.Priority;

import edu.dhbw.mannheim.tigers.sumatra.presenter.log.LogPresenter;
import net.miginfocom.swing.MigLayout;


/**
 * Filter panel, select a user filter here.
 * Multiple words may be separated by a comma.
 * 
 * @author AndreR
 */
public class FilterPanel extends JPanel
{
	private static final long								serialVersionUID	= 1L;
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final ArrayList<IFilterPanelObserver>	observers			= new ArrayList<IFilterPanelObserver>();
	
	private final SlidePanel								slidePanel;
	private final JTextField								text;
	private final JButton									reset;
	private final JToggleButton							freeze;
	private final JLabel										lblNumFatals;
	private final JLabel										lblNumErrors;
	private final JLabel										lblNumWarnings;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param initialLevel
	 */
	public FilterPanel(final Priority initialLevel)
	{
		setLayout(new MigLayout("fill, inset 0", "[][][]", ""));
		setBorder(BorderFactory.createEmptyBorder());
		
		slidePanel = new SlidePanel(initialLevel);
		
		text = new JTextField();
		text.addActionListener(new TextChange());
		
		reset = new JButton("Reset");
		reset.addActionListener(new Reset());
		reset.setFont(new Font("", Font.PLAIN, 10));
		reset.setMargin(new Insets(0, 5, 0, 5));
		
		freeze = new JToggleButton("Freeze");
		freeze.addActionListener(new Freeze());
		freeze.setFont(new Font("", Font.PLAIN, 10));
		freeze.setMargin(new Insets(0, 5, 0, 5));
		
		lblNumFatals = new JLabel("0");
		lblNumFatals.setForeground(LogPresenter.DEFAULT_COLOR_FATAL);
		lblNumErrors = new JLabel("0");
		lblNumErrors.setForeground(LogPresenter.DEFAULT_COLOR_ERROR);
		lblNumWarnings = new JLabel("0");
		lblNumWarnings.setForeground(LogPresenter.DEFAULT_COLOR_WARN);
		
		add(slidePanel);
		add(new JLabel("Filter: "));
		add(text, "growx, push, gapright 5");
		add(reset, "gapright 5");
		add(freeze, "gapright 5");
		add(lblNumWarnings, "gapright 5");
		add(lblNumErrors, "gapright 5");
		add(lblNumFatals);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return
	 */
	public SlidePanel getSlidePanel()
	{
		return slidePanel;
	}
	
	
	/**
	 * @param o
	 */
	public void addObserver(final IFilterPanelObserver o)
	{
		observers.add(o);
	}
	
	
	/**
	 * @param o
	 */
	public void removeObserver(final IFilterPanelObserver o)
	{
		observers.remove(o);
	}
	
	
	/**
	 * @param num
	 */
	public void setNumFatals(final int num)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				lblNumFatals.setText(String.valueOf(num));
			}
		});
	}
	
	
	/**
	 * @param num
	 */
	public void setNumErrors(final int num)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				lblNumErrors.setText(String.valueOf(num));
			}
		});
	}
	
	
	/**
	 * @param num
	 */
	public void setNumWarnings(final int num)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				lblNumWarnings.setText(String.valueOf(num));
			}
		});
	}
	
	// --------------------------------------------------------------------------
	// --- action listener ------------------------------------------------------
	// --------------------------------------------------------------------------
	protected class Reset implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			text.setText("");
			
			for (final IFilterPanelObserver o : observers)
			{
				o.onNewFilter(new ArrayList<String>());
			}
		}
	}
	
	protected class Freeze implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			for (final IFilterPanelObserver o : observers)
			{
				o.onFreeze(freeze.isSelected());
			}
		}
	}
	
	protected class TextChange implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			final ArrayList<String> allowed = new ArrayList<String>(Arrays.asList(text.getText().split(",")));
			
			for (final IFilterPanelObserver o : observers)
			{
				o.onNewFilter(allowed);
			}
		}
	}
}
