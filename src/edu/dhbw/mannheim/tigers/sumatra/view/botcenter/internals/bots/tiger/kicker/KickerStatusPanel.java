/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.09.2010
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker;

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
public class KickerStatusPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= -6057961786996601478L;
	
	private JTextField cap;
	private JTextField TDiode[];
	private JTextField TIGBT[];
	private JTextField chg;
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public KickerStatusPanel()
	{
		setLayout(new MigLayout("fill, wrap 3", "[50]10[50,fill][20]"));
		
		cap = new JTextField();
		chg = new JTextField();
		TDiode = new JTextField[2];
		TDiode[0] = new JTextField();
		TDiode[1] = new JTextField();
		TIGBT = new JTextField[2];
		TIGBT[0] = new JTextField();
		TIGBT[1] = new JTextField();
		
		add(new JLabel("Capacitor Level:"));
		add(cap);
		add(new JLabel("V"));
		add(new JLabel("Charge:"));
		add(chg);
		add(new JLabel("A"));
		add(new JLabel("T IGBT Straight:"));
		add(TIGBT[0]);
		add(new JLabel("°C"));
		add(new JLabel("T Diode Straight:"));
		add(TDiode[0]);
		add(new JLabel("°C"));
		add(new JLabel("T IGBT Chip:"));
		add(TIGBT[1]);
		add(new JLabel("°C"));
		add(new JLabel("T Diode Chip:"));
		add(TDiode[1]);
		add(new JLabel("°C"));
	
		setBorder(BorderFactory.createTitledBorder("Status"));
	}

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public void setCap(final float f)
	{
		float green = 1;
		float red = 0;
		
		if(f < 200)	// increase red level => yellow
		{
			red = f/200;
		}
		else
		{
			red = 1;
		}
		
		if(f > 200)	// decrease green level => red
		{
			green = 1 - (f-200)/200;
		}
		else
		{
			green = 1;
		}
		
		if(green < 0)
		{
			green = 0;
		}
		
		if(red < 0)
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
	
	public void setTIGBT(final float[] t)
	{
		if(t.length < 2)
		{
			return;
		}
		
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				TIGBT[0].setText(String.format(Locale.ENGLISH, "%.1f", t[0]));
				TIGBT[1].setText(String.format(Locale.ENGLISH, "%.1f", t[1]));
			}
		});
	}

	public void setTDiode(final float[] t)
	{
		if(t.length < 2)
		{
			return;
		}
		
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				TDiode[0].setText(String.format(Locale.ENGLISH, "%.1f", t[0]));
				TDiode[1].setText(String.format(Locale.ENGLISH, "%.1f", t[1]));
			}
		});
	}

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
