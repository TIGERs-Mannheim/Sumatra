/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.06.2013
 * Author(s): rYan
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bootloader;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.Bootloader.EBootloaderState;


/** */
public class FirmwareBotPanel extends JPanel
{
	/**  */
	private static final long	serialVersionUID	= 3866974299385225909L;
	
	private JCheckBox				chkEnabled			= new JCheckBox();
	private JTextField			state					= new JTextField();
	private JProgressBar			progress				= new JProgressBar(0, 100);
	private JTextField			botName				= new JTextField();
	
	
	/** */
	public FirmwareBotPanel()
	{
		setLayout(new MigLayout("wrap 4", "[20]10[100,fill]10[100,fill]10[210,fill]"));
		
		chkEnabled.setSelected(true);
		add(chkEnabled);
		add(botName);
		add(state);
		add(progress);
	}
	
	
	/**
	 * @param enabled
	 */
	public void setChkEnabled(final boolean enabled)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				chkEnabled.setSelected(enabled);
			}
		});
	}
	
	
	/**
	 * 
	 * @param s
	 */
	public void setState(final EBootloaderState s)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				state.setText(s.name());
			}
		});
	}
	
	
	/**
	 * 
	 * @param current
	 * @param total
	 */
	public void setProgress(long current, long total)
	{
		final float percentage = (current * 100f) / total;
		
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				progress.setValue((int) percentage);
			}
		});
	}
	
	
	/**
	 * 
	 * @param name
	 */
	public void setBotName(final String name)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				botName.setText(name);
			}
		});
	}
	
	
	/**
	 * @return the chkEnabled
	 */
	public final boolean getChkEnabled()
	{
		return chkEnabled.isSelected();
	}
}