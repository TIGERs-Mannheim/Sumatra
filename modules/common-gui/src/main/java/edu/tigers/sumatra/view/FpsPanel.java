/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 11, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.view;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;


/**
 * Panel for displaying FPS of Worldframe and AIInfoFrame
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class FpsPanel extends JPanel
{
	private static final long		serialVersionUID	= -4915659461230793676L;
	
	private Map<EFpsType, JLabel>	labelMap				= new EnumMap<>(EFpsType.class);
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public enum EFpsType
	{
		/**  */
		CAM("Cam: "),
		/**  */
		WP("WP: "),
		/**  */
		AI_Y("AIY: "),
		/**  */
		AI_B("AIB: "), ;
		final String	prefix;
		
		
		private EFpsType(final String prefix)
		{
			this.prefix = prefix;
		}
	}
	
	
	/**
	 * New FpsPanel
	 */
	public FpsPanel()
	{
		// --- border ---
		final TitledBorder border = BorderFactory.createTitledBorder("fps");
		setBorder(border);
		setLayout(new MigLayout("fill, inset 0", "[]5[]5[]5[]5[]"));
		
		int width = 60;
		for (EFpsType ft : EFpsType.values())
		{
			JLabel lbl = new JLabel(ft.prefix + "-");
			labelMap.put(ft, lbl);
			add(lbl);
			lbl.setMinimumSize(new Dimension(width, 0));
		}
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param type
	 * @param fps
	 */
	public void setFps(final EFpsType type, final double fps)
	{
		final String txt = String.format("%s%03.0f", type.prefix, fps);
		final JLabel lbl = labelMap.get(type);
		if (!lbl.getText().equals(txt))
		{
			EventQueue.invokeLater(() -> lbl.setText(txt));
		}
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public void clearFps()
	{
		for (EFpsType ft : EFpsType.values())
		{
			setFps(ft, 0);
		}
	}
}
