/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.snapshot;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
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


@Log4j2
public class SnapshotController
{
	private final Component parentComponent;
	private WorldFrameWrapper wfw;
	@Setter
	private boolean saveMoveDestinations;


	public SnapshotController(final Component parentComponent)
	{
		this.parentComponent = parentComponent;
	}


	private Snapshot createSnapshot()
	{
		Map<BotID, SnapObject> snapBots = new HashMap<>();
		Map<BotID, IVector3> moveDestinations = new HashMap<>();
		for (Map.Entry<BotID, ITrackedBot> entry : wfw.getSimpleWorldFrame().getBots().entrySet())
		{
			ITrackedBot bot = entry.getValue();
			snapBots.put(entry.getKey(),
					new SnapObject(Vector3.from2d(bot.getPos(), bot.getOrientation()),
							Vector3.from2d(bot.getVel(), bot.getAngularVel())));
			if (saveMoveDestinations)
			{
				bot.getCurrentTrajectory().map(ITrajectory::getFinalDestination).ifPresent(
						dest -> moveDestinations.put(entry.getKey(), dest)
				);
			}
		}

		ITrackedBall ball = wfw.getSimpleWorldFrame().getBall();
		SnapObject snapBall = new SnapObject(ball.getPos3(), ball.getVel3());

		return Snapshot.builder()
				.bots(snapBots)
				.ball(snapBall)
				.command(wfw.getRefereeMsg().getCommand())
				.stage(wfw.getRefereeMsg().getStage())
				.placementPos(wfw.getRefereeMsg().getBallPlacementPos())
				.moveDestinations(moveDestinations)
				.build();
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

		Snapshot snapshot = createSnapshot();
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
				snapshot.save(file.toPath());
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

		Snapshot snapshot = createSnapshot();
		String snapJson = snapshot.toJSON().toJSONString();

		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection stringSelection = new StringSelection(snapJson);
		clipboard.setContents(stringSelection, null);
	}
}
