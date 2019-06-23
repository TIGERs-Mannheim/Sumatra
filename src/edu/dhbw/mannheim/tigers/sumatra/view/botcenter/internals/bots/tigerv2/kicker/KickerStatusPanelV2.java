/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.05.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.kicker;

import java.awt.Color;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;


/**
 * Kicker status panel
 * 
 * @author AndreR
 * 
 */
public class KickerStatusPanelV2 extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= -6057961786996601478L;
	
	private final JTextField	cap;
	private final JTextField	chg;
	private final JTextField	ir;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public KickerStatusPanelV2()
	{
		setLayout(new MigLayout("fill, wrap 3", "[50]10[50,fill][20]"));
		
		cap = new JTextField();
		chg = new JTextField();
		ir = new JTextField();
		
		add(new JLabel("Capacitor Level:"));
		add(cap);
		add(new JLabel("V"));
		add(new JLabel("Charge:"));
		add(chg);
		add(new JLabel("A"));
		add(new JLabel("IR:"));
		add(ir);
		add(new JLabel("V"));
		
		setBorder(BorderFactory.createTitledBorder("Status"));
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param f
	 */
	public void setCap(final float f)
	{
		float green = 1;
		float red = 0;
		
		// increase red level => yellow
		if (f < 125)
		{
			red = f / 125;
		} else
		{
			red = 1;
		}
		
		// decrease green level => red
		if (f > 125)
		{
			green = 1 - ((f - 125) / 125);
		} else
		{
			green = 1;
		}
		
		if (green < 0)
		{
			green = 0;
		}
		
		if (red < 0)
		{
			red = 0;
		}
		
		final float g = green;
		final float r = red;
		
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				cap.setText(Float.toString(f));
				cap.setBackground(new Color(r, g, 0));
			}
		});
	}
	
	
	/**
	 * @param irLvl
	 */
	public void setIrLevel(final float irLvl)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				ir.setText(String.format(Locale.ENGLISH, "%.3f", irLvl));
			}
		});
	}
	
	
	/**
	 * @param f
	 */
	public void setChg(final float f)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				chg.setText(Float.toString(f));
			}
		});
	}
}
