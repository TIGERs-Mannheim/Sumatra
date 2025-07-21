/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.kick.presenter;

import edu.tigers.sumatra.gui.kick.presenter.sample.BallSamplePresenter;
import edu.tigers.sumatra.gui.kick.presenter.sample.KickSamplePresenter;
import edu.tigers.sumatra.gui.kick.presenter.sample.RedirectSamplePresenter;
import edu.tigers.sumatra.gui.kick.view.BallKickIdentPanel;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.modelidentification.ModelIdentificationModule;
import edu.tigers.sumatra.modelidentification.kickspeed.data.KickDatabase;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import edu.tigers.sumatra.vision.kick.estimators.EBallModelIdentType;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.SwingUtilities;
import java.awt.event.ItemEvent;
import java.util.EnumMap;
import java.util.Map;


/**
 * Presenter for ball kick identification UI.
 */
@Log4j2
public class BallKickIdentPresenter implements ISumatraViewPresenter
{
	@Getter
	private BallKickIdentPanel viewPanel = new BallKickIdentPanel();

	private KickSamplePresenter kickPresenter;
	private RedirectSamplePresenter redirectPresenter;
	private Map<EBallModelIdentType, BallSamplePresenter> ballPresenters = new EnumMap<>(EBallModelIdentType.class);


	public BallKickIdentPresenter()
	{
		viewPanel.getChkCollectKickIdents().addItemListener(this::onCollectKickIdentsItemEvent);
	}


	@Override
	public void onModuliStarted()
	{
		SumatraModel.getInstance().getModuleOpt(ModelIdentificationModule.class).ifPresent(modelIdentificationModule -> {
			modelIdentificationModule.getKickSpeedObserver().getOnDatabaseChanged()
					.subscribe(getClass().getCanonicalName(), this::onDatabaseChanged);
			var database = modelIdentificationModule.getKickSpeedObserver().getDatabase();

			kickPresenter = new KickSamplePresenter(database.getKickSamples());
			redirectPresenter = new RedirectSamplePresenter(database.getRedirectSamples());

			viewPanel.getTabs().add("Kick Model", kickPresenter.getContainer());
			viewPanel.getTabs().add("Redirect Model", redirectPresenter.getContainer());

			for (EBallModelIdentType type : EBallModelIdentType.values())
			{
				BallSamplePresenter presenter = new BallSamplePresenter(
						database.getBallSamplesByIdentType().get(type), type);
				ballPresenters.put(type, presenter);
				viewPanel.getTabs().add(type.toString(), presenter.getContainer());
			}

			viewPanel.getChkCollectKickIdents().setSelected(modelIdentificationModule.getKickSpeedObserver().isEnabled());
		});
	}


	private void onCollectKickIdentsItemEvent(ItemEvent e)
	{
		SumatraModel.getInstance().getModuleOpt(ModelIdentificationModule.class)
				.ifPresent(m -> m.getKickSpeedObserver().setEnabled(e.getStateChange() == ItemEvent.SELECTED));
	}


	@Override
	public void onModuliStopped()
	{
		SumatraModel.getInstance().getModuleOpt(ModelIdentificationModule.class).ifPresent(identModule -> {
			identModule.getKickSpeedObserver().getOnDatabaseChanged().unsubscribe(getClass().getCanonicalName());

			SwingUtilities.invokeLater(() -> viewPanel.getTabs().removeAll());

			kickPresenter = null;
			redirectPresenter = null;
			ballPresenters.clear();

			viewPanel.getChkCollectKickIdents().removeItemListener(
					l -> identModule.getKickSpeedObserver().setEnabled(l.getStateChange() == ItemEvent.SELECTED)
			);
		});
	}


	private void onDatabaseChanged(KickDatabase kickDatabase)
	{
		kickPresenter.update();
		redirectPresenter.update();
		for (BallSamplePresenter presenter : ballPresenters.values())
		{
			presenter.update();
		}
	}
}
