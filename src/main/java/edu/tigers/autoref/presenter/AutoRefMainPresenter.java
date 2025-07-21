/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.presenter;

import edu.tigers.autoref.view.AutoRefMainFrame;
import edu.tigers.autoref.view.ballspeed.BallSpeedView;
import edu.tigers.autoref.view.gamelog.GameLogView;
import edu.tigers.autoref.view.main.AutoRefView;
import edu.tigers.sumatra.AModuliMainPresenter;
import edu.tigers.sumatra.config.ConfigEditorView;
import edu.tigers.sumatra.gui.log.LogView;
import edu.tigers.sumatra.gui.referee.RefereeView;
import edu.tigers.sumatra.gui.visualizer.VisualizerView;
import edu.tigers.sumatra.views.ASumatraView;
import lombok.extern.log4j.Log4j2;

import java.util.List;


@Log4j2
public class AutoRefMainPresenter extends AModuliMainPresenter<AutoRefMainFrame>
{
	public AutoRefMainPresenter()
	{
		super(new AutoRefMainFrame(), createViews(), "auto_ref");

		init();
	}


	private static List<ASumatraView> createViews()
	{
		// gameLogView and AutoRefView must be initialized in this order to ensure that game log is notified
		// about the game log table model
		GameLogView gameLogView = new GameLogView();
		gameLogView.ensureInitialized();
		AutoRefView autoRefView = new AutoRefView();
		autoRefView.ensureInitialized();

		return List.of(
				new LogView(true),
				new VisualizerView(),
				new ConfigEditorView(),
				autoRefView,
				gameLogView,
				new BallSpeedView(),
				new RefereeView()
		);
	}
}
