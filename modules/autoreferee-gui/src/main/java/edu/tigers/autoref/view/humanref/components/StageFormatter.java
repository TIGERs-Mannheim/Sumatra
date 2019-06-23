/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 15, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.view.humanref.components;

import javax.swing.JLabel;

import edu.tigers.autoref.view.generic.JFormatLabel.LabelFormatter;
import edu.tigers.sumatra.Referee.SSL_Referee.Stage;


/**
 * @author "Lukas Magel"
 */
public class StageFormatter implements LabelFormatter<Stage>
{
	
	private String getText(final Stage value)
	{
		switch (value)
		{
			case EXTRA_FIRST_HALF:
				return "Extra First Half";
			case EXTRA_FIRST_HALF_PRE:
				return "Extra First Half Pre";
			case EXTRA_HALF_TIME:
				return "Extra Half Time";
			case EXTRA_SECOND_HALF:
				return "Extra Second Half";
			case EXTRA_SECOND_HALF_PRE:
				return "Extra Second Half Pre";
			case EXTRA_TIME_BREAK:
				return "Extra Time Break";
			case NORMAL_FIRST_HALF:
				return "First Half";
			case NORMAL_FIRST_HALF_PRE:
				return "First Half Pre";
			case NORMAL_HALF_TIME:
				return "Half Time";
			case NORMAL_SECOND_HALF:
				return "Second Half";
			case NORMAL_SECOND_HALF_PRE:
				return "Second Half Pre";
			case PENALTY_SHOOTOUT:
				return "Penalty Shootout";
			case PENALTY_SHOOTOUT_BREAK:
				return "Penalty Shootout Break";
			case POST_GAME:
				return "Post Game";
			default:
				return "Unknown Stage";
				
		}
	}
	
	
	@Override
	public void formatLabel(final Stage value, final JLabel label)
	{
		label.setText(getText(value));
	}
	
}
