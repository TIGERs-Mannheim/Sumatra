/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.07.2011
 * Author(s): osteinbrecher
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker;

import java.awt.BasicStroke;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;


/**
 * Fast kicker configuration panel
 * 
 * @author Oliver Steinbrecher
 */
public class KickerConfig extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final long	serialVersionUID	= -1228227784425852684L;
	
	private static final int	SAFETY_VOLTAGE		= 20;
	
	private final BotID			botId;
	
	private JCheckBox				cbAutoload			= null;
	private JTextField			actualCap			= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param botId
	 */
	public KickerConfig(final BotID botId)
	{
		this.botId = botId;
		
		setLayout(new MigLayout("fill", "[80]10[50,fill]"));
		Color color = Color.yellow;
		if (botId.getTeamColor() == ETeamColor.BLUE)
		{
			color = Color.blue;
		}
		Border coloredBorder = BorderFactory.createStrokeBorder(new BasicStroke(), color);
		setBorder(BorderFactory
				.createTitledBorder(coloredBorder, "Bot " + botId.getNumber() + " " + botId.getTeamColor()));
		
		cbAutoload = new JCheckBox();
		cbAutoload.setSelected(true);
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
	/**
	 * @return
	 */
	public BotID getBotId()
	{
		return botId;
	}
	
	
	/**
	 * @return
	 */
	public boolean isAutoloadEnabled()
	{
		return cbAutoload.isSelected();
	}
	
	
	/**
	 * @param chg
	 */
	public void setChargeLvL(final float chg)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				actualCap.setText(Float.toString(chg));
				
				if (chg <= SAFETY_VOLTAGE)
				{
					actualCap.setBackground(Color.GREEN);
				} else
				{
					actualCap.setBackground(Color.RED);
				}
				
			}
		});
	}
	
}
