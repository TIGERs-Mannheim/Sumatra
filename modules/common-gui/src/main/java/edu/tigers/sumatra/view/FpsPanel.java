/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view;

import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.util.EnumMap;
import java.util.Map;


/**
 * Panel for displaying FPS of Worldframe and AIInfoFrame
 */
public class FpsPanel extends JPanel
{
	private static final long serialVersionUID = -4915659461230793676L;
	private static final int LABEL_WIDTH = 60;

	private final Map<EFpsType, JLabel> labelMap = new EnumMap<>(EFpsType.class);

	public enum EFpsType
	{
		CAM("Cam: ", "Camera FPS"),
		WP("WP: ", "World Predictor FPS"),
		AI_Y("AIY: ", "Yellow Team AI FPS"),
		AI_B("AIB: ", "Blue Team AI FPS"),
		;
		final String prefix;
		final String desc;


		EFpsType(final String prefix, final String desc)
		{
			this.prefix = prefix;
			this.desc = desc;
		}
	}


	/**
	 * New FpsPanel
	 */
	public FpsPanel()
	{
		final TitledBorder border = BorderFactory.createTitledBorder("fps");
		setBorder(border);
		setLayout(new MigLayout("fill, inset 0", "[]5[]5[]5[]5[]"));

		for (EFpsType ft : EFpsType.values())
		{
			JLabel lbl = new JLabel(ft.prefix + "-");
			lbl.setToolTipText(ft.desc);
			labelMap.put(ft, lbl);
			add(lbl);
			lbl.setMinimumSize(new Dimension(LABEL_WIDTH, 0));
		}
	}


	/**
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


	public void clearFps()
	{
		for (EFpsType ft : EFpsType.values())
		{
			setFps(ft, 0);
		}
	}
}
