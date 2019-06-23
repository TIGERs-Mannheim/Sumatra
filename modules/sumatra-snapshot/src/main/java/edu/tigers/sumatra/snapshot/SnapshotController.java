/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.snapshot;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


public class SnapshotController
{
	private static final Logger log = Logger.getLogger(SnapshotController.class.getName());
	private final Component parentComponent;
	private WorldFrameWrapper wfw;
	
	
	public SnapshotController(final Component parentComponent)
	{
		this.parentComponent = parentComponent;
	}
	
	
	/**
	 * @param worldFrame
	 * @return
	 */
	private static Snapshot createSnapshot(final SimpleWorldFrame worldFrame)
	{
		Map<BotID, SnapObject> snapBots = new HashMap<>();
		for (Map.Entry<BotID, ITrackedBot> entry : worldFrame.getBots())
		{
			ITrackedBot bot = entry.getValue();
			snapBots.put(entry.getKey(),
					new SnapObject(Vector3.from2d(bot.getPos(), bot.getOrientation()),
							Vector3.from2d(bot.getVel(), bot.getAngularVel())));
		}
		
		ITrackedBall ball = worldFrame.getBall();
		SnapObject snapBall = new SnapObject(ball.getPos3(), ball.getVel3());
		
		return new Snapshot(snapBots, snapBall);
	}
	
	
	/**
	 * @param wfw
	 */
	public void updateWorldFrame(WorldFrameWrapper wfw)
	{
		this.wfw = wfw;
	}
	
	
	/**
	 * save snapshot to file
	 */
	public void onSnapshot()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		
		SimpleWorldFrame worldFrame = wfw.getSimpleWorldFrame();
		Snapshot snapshot = createSnapshot(worldFrame);
		String defaultFilename = "data/snapshots/" + sdf.format(new Date()) + ".snap";
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File("data/snapshots"));
		fileChooser.setSelectedFile(new File(defaultFilename));
		FileNameExtensionFilter filter = new FileNameExtensionFilter("snapshot files", "snap");
		fileChooser.setFileFilter(filter);
		if (fileChooser.showSaveDialog(parentComponent) == JFileChooser.APPROVE_OPTION)
		{
			File file = fileChooser.getSelectedFile();
			// save to file
			try
			{
				snapshot.save(file.getAbsolutePath());
			} catch (IOException e)
			{
				log.error("Could not save snapshot file", e);
			}
		}
	}
	
	
	/**
	 * Copy snapshot to clipboard
	 */
	public void onCopySnapshot()
	{
		if (wfw == null)
		{
			return;
		}
		
		SimpleWorldFrame worldFrame = wfw.getSimpleWorldFrame();
		Snapshot snapshot = createSnapshot(worldFrame);
		String snapJson = snapshot.toJSON().toJSONString();
		
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection stringSelection = new StringSelection(snapJson);
		clipboard.setContents(stringSelection, null);
	}
}
