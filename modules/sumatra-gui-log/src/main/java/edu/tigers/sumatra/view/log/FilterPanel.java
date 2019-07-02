/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.log;

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

import org.apache.log4j.Priority;

import edu.tigers.sumatra.presenter.log.LogPresenter;
import net.miginfocom.swing.MigLayout;


/**
 * Filter panel, select a user filter here.
 * Multiple words may be separated by a comma.
 */
public class FilterPanel extends JPanel
{
	private final ArrayList<IFilterPanelObserver> observers = new ArrayList<>();

	private final SlidePanel slidePanel;
	private final JTextField text;
	private final JToggleButton freeze;
	private final JLabel lblNumFatals;
	private final JLabel lblNumErrors;
	private final JLabel lblNumWarnings;


	@SuppressWarnings("squid:S1192") // duplicate string constants
	public FilterPanel(final Priority initialLevel)
	{
		setLayout(new MigLayout("fill, inset 0", "[][][]", ""));
		setBorder(BorderFactory.createEmptyBorder());

		slidePanel = new SlidePanel(initialLevel);

		text = new JTextField();
		text.addActionListener(new TextChange());

		final JButton reset = new JButton("Reset");
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


	public SlidePanel getSlidePanel()
	{
		return slidePanel;
	}


	public void addObserver(final IFilterPanelObserver o)
	{
		observers.add(o);
	}


	public void setNumFatals(final int num)
	{
		lblNumFatals.setText(String.valueOf(num));
	}


	public void setNumErrors(final int num)
	{
		lblNumErrors.setText(String.valueOf(num));
	}


	public void setNumWarnings(final int num)
	{
		lblNumWarnings.setText(String.valueOf(num));
	}

	protected class Reset implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			text.setText("");

			for (final IFilterPanelObserver o : observers)
			{
				o.onNewFilter(new ArrayList<>());
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
			final ArrayList<String> allowed = new ArrayList<>(Arrays.asList(text.getText().split(",")));

			for (final IFilterPanelObserver o : observers)
			{
				o.onNewFilter(allowed);
			}
		}
	}
}
