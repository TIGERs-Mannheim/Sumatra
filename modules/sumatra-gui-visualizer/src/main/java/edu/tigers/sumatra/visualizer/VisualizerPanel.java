/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.visualizer;

import edu.tigers.sumatra.views.ISumatraView;
import lombok.extern.log4j.Log4j2;
import net.miginfocom.swing.MigLayout;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


/**
 * Visualizes the current game situation.
 * It also allows the user to set a robot at a determined position.
 *
 * @author BernhardP, OliverS
 */
@Log4j2
public class VisualizerPanel extends JPanel implements ISumatraView, IMediaRecorderListener
{
	private static final long serialVersionUID = 2686191777355388548L;

	private final FieldPanel fieldPanel;
	private final RobotsPanel robotsPanel;
	private final VisualizerOptionsMenu menuBar;
	private final JPanel panel;

	private static final String BASE_SCREENCAST_PATH = "data/screencast/";


	/**
	 * Default
	 */
	public VisualizerPanel()
	{
		setLayout(new BorderLayout());
		menuBar = new VisualizerOptionsMenu();
		menuBar.setMediaRecordingListener(this);
		panel = new JPanel();
		add(menuBar, BorderLayout.PAGE_START);
		add(panel, BorderLayout.CENTER);

		// --- set layout ---
		panel.setLayout(new MigLayout("fill, inset 0, gap 0", "[min!][max][right]", "[top]"));

		// --- init panels ---
		robotsPanel = new RobotsPanel();
		fieldPanel = new FieldPanel();

		panel.add(robotsPanel);
		panel.add(fieldPanel, "grow, top");
	}


	/**
	 * Remove the robots panel
	 */
	public void removeRobotsPanel()
	{
		remove(panel);
		add(fieldPanel);
	}


	/**
	 * @return
	 */
	public FieldPanel getFieldPanel()
	{
		return fieldPanel;
	}


	/**
	 * @return
	 */
	public RobotsPanel getRobotsPanel()
	{
		return robotsPanel;
	}


	/**
	 * @return
	 */
	public VisualizerOptionsMenu getOptionsMenu()
	{
		return menuBar;
	}


	@Override
	public void setMediaParameters(final int w, final int h, EMediaOption mediaOption)
	{
		fieldPanel.setMediaParameters(w, h, mediaOption);
	}


	@Override
	public void takeScreenshot()
	{

		Path path = newFilePath("screenshot", ".png");
		fieldPanel.saveScreenshot(path.toAbsolutePath().toString());
	}


	@Override
	public boolean startRecordingVideo()
	{
		Path path = newFilePath("video", ".mp4");
		if (fieldPanel.startRecordingVideo(path.toAbsolutePath().toString()))
		{
			log.info("Started recording video to: " + path.toAbsolutePath().toString());
			return true;
		}
		log.error("Could not start recording to: " + path.toAbsolutePath().toString());
		return false;
	}


	@Override
	public void stopRecordingVideo()
	{
		fieldPanel.stopRecordingVideo();
	}


	private static Path newFilePath(String prefix, String ending)
	{
		File base = new File(BASE_SCREENCAST_PATH);
		if (!base.exists() && !base.mkdirs())
		{
			log.error("Could not create screencast directory");
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		sdf.setTimeZone(TimeZone.getDefault());
		String filename = "/" + prefix + "_" + sdf.format(new Date()) + ending;
		return new File(base.getAbsolutePath() + filename).toPath();
	}
}
