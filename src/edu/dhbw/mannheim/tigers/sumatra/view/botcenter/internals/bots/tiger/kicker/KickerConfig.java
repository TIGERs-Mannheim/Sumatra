/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.07.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;


/**
 * Fast kicker configuration panel
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class KickerConfig extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final long	serialVersionUID	= -1228227784425852684L;
	
	private static final int	SAFETY_VOLTAGE		= 20;
	
	private final int				botId;
	
	private JCheckBox				cbAutoload			= null;
	private JTextField			actualCap			= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public KickerConfig(int botId)
	{
		this.botId = botId;
		
		setLayout(new MigLayout("fill", "[80]10[50,fill]"));
		setBorder(BorderFactory.createTitledBorder("Bot " + botId));
		
		cbAutoload = new JCheckBox();
		actualCap = new JTextField();
		
		setChargeLvL(0);
		
		add(new JLabel("AutoLoad"));
		add(cbAutoload);
		add(new JLabel("ActualCap"));
		add(actualCap);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public int getBotId()
	{
		return botId;
	}
	

	public boolean isAutoloadEnabled()
	{
		return cbAutoload.isSelected();
	}
	

	public void setChargeLvL(final float chg)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				KickerConfig.this.actualCap.setText(Float.toString(chg));
				
				if (chg <= SAFETY_VOLTAGE)
				{
					KickerConfig.this.actualCap.setBackground(Color.GREEN);
				} else
				{
					KickerConfig.this.actualCap.setBackground(Color.RED);
				}
				
			}
		});
	}
	
}
