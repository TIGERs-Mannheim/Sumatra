package edu.tigers.autoref.presenter;

import edu.tigers.autoref.view.AutoRefMainFrame;
import edu.tigers.sumatra.AModuliMainPresenter;
import edu.tigers.sumatra.config.ConfigEditorPresenter;
import edu.tigers.sumatra.gui.log.presenter.LogPresenter;
import edu.tigers.sumatra.gui.referee.presenter.RefereePresenter;
import edu.tigers.sumatra.gui.visualizer.presenter.VisualizerPresenter;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.SumatraView;
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


	private static List<SumatraView> createViews()
	{
		// gameLogView and AutoRefView must be initialized in this order to ensure that game log is notified
		// about the game log table model
		SumatraView gameLogView = new SumatraView(ESumatraViewType.AUTOREFEREE_GAME_LOG, GameLogPresenter::new);
		gameLogView.ensureInitialized();
		SumatraView autoRefView = new SumatraView(ESumatraViewType.AUTOREFEREE, AutoRefPresenter::new);
		autoRefView.ensureInitialized();

		return List.of(
				new SumatraView(ESumatraViewType.LOG, () -> new LogPresenter(true)),
				new SumatraView(ESumatraViewType.VISUALIZER, VisualizerPresenter::new),
				new SumatraView(ESumatraViewType.CONFIG_EDITOR, ConfigEditorPresenter::new),
				autoRefView,
				gameLogView,
				new SumatraView(ESumatraViewType.BALL_SPEED, BallSpeedPresenter::new),
				new SumatraView(ESumatraViewType.REFEREE, RefereePresenter::new)
		);
	}
}
