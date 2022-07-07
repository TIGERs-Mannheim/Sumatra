/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.kick.presenter;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerMode;
import edu.tigers.sumatra.botmanager.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.kick.data.BallModelSample;
import edu.tigers.sumatra.kick.data.KickDatabase;
import edu.tigers.sumatra.kick.data.KickModelSample;
import edu.tigers.sumatra.kick.data.RedirectModelSample;
import edu.tigers.sumatra.kick.view.BallKickIdentPanel;
import edu.tigers.sumatra.kick.view.BallKickIdentPanel.IMainPanelObserver;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.skillsystem.ISkillSystemObserver;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import edu.tigers.sumatra.vision.AVisionFilter;
import edu.tigers.sumatra.vision.IVisionFilterObserver;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;
import edu.tigers.sumatra.vision.data.FilteredVisionKick;
import edu.tigers.sumatra.vision.kick.estimators.EBallModelIdentType;
import edu.tigers.sumatra.vision.kick.estimators.IBallModelIdentResult;
import edu.tigers.sumatra.vision.kick.estimators.straight.FlatKickSolverNonLin3Factor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Presenter for ball kick identification UI.
 */
@Log4j2
public class BallKickIdentPresenter
		implements ISumatraViewPresenter, IVisionFilterObserver, ISkillSystemObserver, IMainPanelObserver
{
	private static final String DATABASE_FILE = "data/kickDatabase.json";

	private KickDatabase database;

	@Getter
	private BallKickIdentPanel viewPanel = new BallKickIdentPanel();

	private List<FilteredVisionKick> kickEvents = new ArrayList<>();
	private Map<BotID, KickerDribblerCommands> botCmdMapArmTime = new HashMap<>();
	private Map<BotID, KickerDribblerCommands> botCmdMapArmSpeed = new HashMap<>();
	private long lastFilteredVisionKickTimestamp = 0;

	private KickSamplePresenter kickPresenter;
	private RedirectSamplePresenter redirectPresenter;
	private Map<EBallModelIdentType, BallSamplePresenter> ballPresenters = new EnumMap<>(EBallModelIdentType.class);

	private AVisionFilter visionFilter;
	private ASkillSystem skillSystem;


	@Override
	public void onStartModuli()
	{
		loadDatabase();

		kickPresenter = new KickSamplePresenter(database.getKickSamples());
		redirectPresenter = new RedirectSamplePresenter(database.getRedirectSamples());

		viewPanel.getTabs().add("Kick Model", kickPresenter.getComponent());
		viewPanel.getTabs().add("Redirect Model", redirectPresenter.getComponent());

		for (EBallModelIdentType type : EBallModelIdentType.values())
		{
			BallSamplePresenter presenter = new BallSamplePresenter(database.getBallSamples(), type);
			ballPresenters.put(type, presenter);
			viewPanel.getTabs().add(type.toString(), presenter.getComponent());
		}

		try
		{
			visionFilter = SumatraModel.getInstance().getModule(AVisionFilter.class);
			visionFilter.addObserver(this);
			visionFilter.setModelIdentification(viewPanel.isModelIdentificationEnabled());
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find vision filter", err);
		}

		try
		{
			skillSystem = SumatraModel.getInstance().getModule(ASkillSystem.class);
			skillSystem.addObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find skill system", err);
		}

		viewPanel.addObserver(this);
	}


	@Override
	public void onStopModuli()
	{
		saveDatabase();

		viewPanel.removeObserver(this);
		viewPanel.getTabs().removeAll();

		kickPresenter = null;
		redirectPresenter = null;
		ballPresenters.clear();

		if (visionFilter != null)
		{
			visionFilter.removeObserver(this);
		}

		if (skillSystem != null)
		{
			skillSystem.removeObserver(this);
		}
	}


	@Override
	public void onNewFilteredVisionFrame(final FilteredVisionFrame filteredVisionFrame)
	{
		if (filteredVisionFrame.getKick().isPresent())
		{
			var kick = filteredVisionFrame.getKick().orElseThrow();

			if (kick.getKickTimestamp() != lastFilteredVisionKickTimestamp)
			{
				lastFilteredVisionKickTimestamp = kick.getKickTimestamp();
				kickEvents.add(kick);
			}
		}
	}


	@Override
	public void onBallModelIdentificationResult(final IBallModelIdentResult ident)
	{
		database.getBallSamples().add(new BallModelSample(ident.getType(), ident.getModelParameters()));

		for (BallSamplePresenter pres : ballPresenters.values())
		{
			pres.update();
		}

		Optional<FilteredVisionKick> closestKickEvent = kickEvents.stream()
				.min(Comparator.comparingLong(e -> Math.abs(e.getKickTimestamp() - ident.getKickTimestamp())));

		if (closestKickEvent.isEmpty())
		{
			return;
		}

		BotID kickBot = closestKickEvent.get().getKickingBot();
		if (botCmdMapArmTime.containsKey(kickBot))
		{
			KickerDribblerCommands kd = botCmdMapArmTime.get(kickBot);
			if ((ident.getKickVelocity().z() > 0) == (kd.getDevice() == EKickerDevice.CHIP))
			{
				database.getKickSamples()
						.add(new KickModelSample(kickBot.getSaveableString(), ident.getKickVelocity().getLength() * 1e-3,
								kd.getKickSpeed() * 1e3, kd.getDribblerSpeed(), ident.getKickVelocity().z() > 0));

				kickPresenter.update();
			}

			botCmdMapArmTime.clear();
		}

		while (kickEvents.size() > 10)
		{
			kickEvents.remove(0);
		}

		if (ident.getType() == EBallModelIdentType.REDIRECT && botCmdMapArmSpeed.containsKey(kickBot))
		{
			KickerDribblerCommands kd = botCmdMapArmSpeed.get(kickBot);

			var redirectIdent = (FlatKickSolverNonLin3Factor.RedirectModelIdentResult) ident;

			database.getRedirectSamples().add(new RedirectModelSample(redirectIdent.getInVelocity().multiplyNew(1e-3),
					redirectIdent.getOutVelocity().multiplyNew(1e-3),
					redirectIdent.getSpinFactor(), kd.getKickSpeed()));

			redirectPresenter.update();

			botCmdMapArmSpeed.clear();
		}
	}


	@Override
	public void onCommandSent(final ABot bot, final long timestamp)
	{
		KickerDribblerCommands kd = bot.getMatchCtrl().getSkill().getKickerDribbler();
		if (kd.getMode().equals(EKickerMode.ARM_TIME))
		{
			botCmdMapArmTime.put(bot.getBotId(), kd);
		}

		if (kd.getMode().equals(EKickerMode.ARM) || kd.getMode().equals(EKickerMode.ARM_AIM))
		{
			botCmdMapArmSpeed.put(bot.getBotId(), kd);
		}
	}


	private void saveDatabase()
	{
		if (database == null)
		{
			return;
		}

		File file = Paths.get(DATABASE_FILE).toFile();

		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.setDefaultPrettyPrinter(new DefaultPrettyPrinter()
				.withObjectIndenter(new DefaultIndenter().withLinefeed("\n"))
				.withoutSpacesInObjectEntries());

		try
		{
			mapper.writeValue(file, database);
		} catch (IOException e)
		{
			log.error("", e);
		}
	}


	private void loadDatabase()
	{
		File file = Paths.get(DATABASE_FILE).toFile();
		if (!file.exists())
		{
			log.info("Initializing empty kick database");
			database = new KickDatabase();
		} else
		{
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

			try
			{
				database = mapper.readValue(file, KickDatabase.class);
			} catch (IOException e)
			{
				log.error("", e);
			}
		}
	}


	@Override
	public void onEnableIdentification(final boolean enable)
	{
		if (visionFilter != null)
		{
			visionFilter.setModelIdentification(enable);
		}
	}
}
