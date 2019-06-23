/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 26, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.view.humanref.components;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.Border;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.util.RoundedCornerBorder;


/**
 * @author "Lukas Magel"
 */
public class JTeamLabel extends JLabel
{
	
	/**  */
	private static final long serialVersionUID = 7688950271552459510L;
	
	
	/**
	 * Create new instance
	 */
	public JTeamLabel()
	{
		setOpaque(true);
		
		Border teamLabelBorder = new RoundedCornerBorder(10, 5, Color.BLACK);
		setBorder(teamLabelBorder);
	}
	
	
	/**
	 * @param color
	 */
	public void setTeam(final ETeamColor color)
	{
		setColoring(color);
		setText(getTeamText(color));
	}
	
	
	/**
	 * @param id
	 */
	public void setRobot(final BotID id)
	{
		ETeamColor color = id.getTeamColor();
		String text = getText(id);
		setColoring(color);
		setText(text);
	}
	
	
	private String getText(final BotID id)
	{
		if (id.isUninitializedID())
		{
			return "Unknown";
		}
		return getTeamText(id.getTeamColor()) + " " + id.getNumber();
	}
	
	
	private void setColoring(final ETeamColor color)
	{
		Color textColor = Color.BLACK;
		Color backgroundColor = Color.WHITE;
		
		switch (color)
		{
			case BLUE:
				textColor = Color.WHITE;
				backgroundColor = Color.BLUE;
				break;
			case YELLOW:
				textColor = null;
				backgroundColor = Color.YELLOW;
				break;
			default:
				break;
		}
		
		setBackground(backgroundColor);
		setForeground(textColor);
	}
	
	
	private String getTeamText(final ETeamColor color)
	{
		switch (color)
		{
			case BLUE:
				return "Blue";
			case YELLOW:
				return "Yellow";
			case UNINITIALIZED:
				return "Uninitialized";
			default:
				return "";
		}
	}
}
