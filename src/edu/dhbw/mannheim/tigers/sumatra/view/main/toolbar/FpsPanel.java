/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 11, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.main.toolbar;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;


/**
 * Panel for displaying FPS of Worldframe and AIInfoFrame
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class FpsPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final long	serialVersionUID	= -4915659461230793676L;
	private static final int	H_GAP					= 10;
	private static final int	V_GAP					= 0;
	private static final int	MAX_WIDTH			= 180;
	private static final int	MAX_HEIGHT			= 100;
	private final JLabel			lblFpsCamWF			= new JLabel();
	private final JLabel			lblFpsAIF			= new JLabel();
	private final JLabel			lblFpsWF				= new JLabel();
	
	private Float					lastFpsAIF			= -1f;
	private Float					lastFpsCam			= -1f;
	private Float					lastFpsWF			= -1f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * New FpsPanel
	 */
	public FpsPanel()
	{
		// --- border ---
		final TitledBorder border = BorderFactory.createTitledBorder("fps");
		setBorder(border);
		setMaximumSize(new Dimension(MAX_WIDTH, MAX_HEIGHT));
		
		setLayout(new BorderLayout(H_GAP, V_GAP));
		this.add(lblFpsCamWF, BorderLayout.WEST);
		this.add(lblFpsWF, BorderLayout.CENTER);
		this.add(lblFpsAIF, BorderLayout.EAST);
		setFpsCam(0f);
		setFpsAIF(0f);
		setFpsWF(0f);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Set the cam fps
	 * 
	 * @param fps
	 */
	public final void setFpsCam(final float fps)
	{
		if (!lastFpsCam.equals(fps))
		{
			lastFpsCam = fps;
			final String camText;
			if (fps == -1.0f)
			{
				camText = "Cam: Vision Signal Lost";
			} else
			{
				camText = String.format("Cam:%3.0f", fps);
			}
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					lblFpsCamWF.setText(camText);
				}
			});
		}
	}
	
	
	/**
	 * Set the worldframe fps
	 * 
	 * @param fps
	 */
	public final void setFpsWF(final float fps)
	{
		if (!lastFpsWF.equals(fps))
		{
			lastFpsWF = fps;
			final String text;
			if (fps == -1.0f)
			{
				text = "WF: 0";
			} else
			{
				text = String.format("WF:%3.0f", fps);
			}
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					lblFpsWF.setText(text);
				}
			});
		}
	}
	
	
	/**
	 * Set the AIInfoFrame fps
	 * 
	 * @param fps
	 */
	public final void setFpsAIF(final float fps)
	{
		if (!lastFpsAIF.equals(fps))
		{
			lastFpsAIF = fps;
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					lblFpsAIF.setText(String.format("AIF:%3.0f", fps));
				}
			});
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
