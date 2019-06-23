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

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;


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
	private final JLabel			lblFpsCamWF			= new JLabel();
	private final JLabel			lblFpsAIFYellow	= new JLabel();
	private final JLabel			lblFpsAIFBlue		= new JLabel();
	private final JLabel			lblFpsWF				= new JLabel();
	
	private Float					lastFpsAIFYellow	= -1f;
	private Float					lastFpsAIFBlue		= -1f;
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
		
		setLayout(new MigLayout("fill, inset 0", "[]5[]5[]5[]"));
		this.add(lblFpsCamWF);
		this.add(lblFpsWF);
		this.add(lblFpsAIFYellow);
		this.add(lblFpsAIFBlue);
		setFpsCam(0f);
		setFpsAIF(0f, ETeamColor.YELLOW);
		setFpsAIF(0f, ETeamColor.BLUE);
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
	 * @param teamColor
	 */
	public final void setFpsAIF(final float fps, ETeamColor teamColor)
	{
		Float lastFpsAIF = teamColor == ETeamColor.YELLOW ? lastFpsAIFYellow : lastFpsAIFBlue;
		final JLabel lblFpsAIF = teamColor == ETeamColor.YELLOW ? lblFpsAIFYellow : lblFpsAIFBlue;
		final String colorLetter = teamColor == ETeamColor.YELLOW ? "Y" : "B";
		if (!lastFpsAIF.equals(fps))
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					lblFpsAIF.setText(String.format("AIF" + colorLetter + ":%3.0f", fps));
				}
			});
		}
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
