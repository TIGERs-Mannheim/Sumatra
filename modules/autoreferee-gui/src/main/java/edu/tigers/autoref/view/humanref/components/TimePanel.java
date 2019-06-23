/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 15, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.view.humanref.components;

import java.awt.Font;
import java.time.Duration;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import edu.tigers.autoref.view.generic.JFormatLabel;
import edu.tigers.sumatra.Referee.SSL_Referee.Stage;


/**
 * @author "Lukas Magel"
 */
public class TimePanel extends JPanel
{
	
	/**  */
	private static final long			serialVersionUID	= -5276347698642647549L;
	
	private JFormatLabel<Stage>		stageLabel;
	private JFormatLabel<Duration>	timeLabel;
	
	
	/**
	 * @param font
	 */
	public TimePanel(final Font font)
	{
		setupUI(font);
	}
	
	
	private void setupUI(final Font font)
	{
		stageLabel = new JFormatLabel<>(new StageFormatter());
		stageLabel.setAlignmentX(SwingConstants.CENTER);
		stageLabel.setText("No Stage");
		
		timeLabel = new JFormatLabel<>(null);
		timeLabel.setFormatter(new DurationFormatter(Duration.ofSeconds(30), timeLabel.getForeground()));
		
		timeLabel.setAlignmentX(SwingConstants.CENTER);
		timeLabel.setText("00:00");
		
		setTextFont(font);
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(stageLabel);
		add(timeLabel);
	}
	
	
	/**
	 * @param font
	 */
	public void setTextFont(final Font font)
	{
		stageLabel.setFont(font.deriveFont(font.getSize() / 2.0f));
		timeLabel.setFont(font);
	}
	
	
	/**
	 * @param duration
	 */
	public void setTimeLeft(final Duration duration)
	{
		timeLabel.setValue(duration);
	}
	
	
	/**
	 * @param stage
	 */
	public void setStage(final Stage stage)
	{
		stageLabel.setValue(stage);
	}
	
}
