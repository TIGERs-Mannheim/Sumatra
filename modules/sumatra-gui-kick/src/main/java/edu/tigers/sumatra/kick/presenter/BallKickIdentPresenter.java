/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.kick.presenter;

import java.awt.Component;
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

import org.apache.log4j.Logger;

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
import edu.tigers.sumatra.kick.view.MainPanel;
import edu.tigers.sumatra.kick.view.MainPanel.IMainPanelObserver;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.skillsystem.ISkillSystemObserver;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;
import edu.tigers.sumatra.vision.AVisionFilter;
import edu.tigers.sumatra.vision.IVisionFilterObserver;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;
import edu.tigers.sumatra.vision.data.IKickEvent;
import edu.tigers.sumatra.vision.kick.estimators.EBallModelIdentType;
import edu.tigers.sumatra.vision.kick.estimators.IBallModelIdentResult;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class BallKickIdentPresenter extends ASumatraViewPresenter
		implements ISumatraView, IVisionFilterObserver, ISkillSystemObserver, IMainPanelObserver
{
	private static final String DATABASE_FILE = "data/kickDatabase.json";
	
	private static final Logger log = Logger
			.getLogger(BallKickIdentPresenter.class.getName());
	
	private KickDatabase database;
	
	private MainPanel mainPanel = new MainPanel();
	
	private List<IKickEvent> kickEvents = new ArrayList<>();
	private Map<BotID, KickerDribblerCommands> botCmdMap = new HashMap<>();
	
	private KickSamplePresenter kickPresenter;
	private Map<EBallModelIdentType, BallSamplePresenter> ballPresenters = new EnumMap<>(EBallModelIdentType.class);
	
	private AVisionFilter visionFilter;
	private ASkillSystem skillSystem;
	
	
	@Override
	public Component getComponent()
	{
		return mainPanel;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return this;
	}
	
	
	@Override
	public void onStart()
	{
		loadDatabase();
		
		kickPresenter = new KickSamplePresenter(database.getKickSamples());
		
		mainPanel.getTabs().add("Kick Model", kickPresenter.getComponent());
		
		for (EBallModelIdentType type : EBallModelIdentType.values())
		{
			BallSamplePresenter presenter = new BallSamplePresenter(database.getBallSamples(), type);
			ballPresenters.put(type, presenter);
			mainPanel.getTabs().add(type.toString(), presenter.getComponent());
		}
		
		try
		{
			visionFilter = SumatraModel.getInstance().getModule(AVisionFilter.class);
			visionFilter.addObserver(this);
			visionFilter.setModelIdentification(mainPanel.isModelIdentificationEnabled());
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
		
		mainPanel.addObserver(this);
	}
	
	
	@Override
	public void onStop()
	{
		saveDatabase();
		
		mainPanel.removeObserver(this);
		mainPanel.getTabs().removeAll();
		
		kickPresenter = null;
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
		if (filteredVisionFrame.getKickEvent().isPresent())
		{
			kickEvents.add(filteredVisionFrame.getKickEvent().get());
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
		
		Optional<IKickEvent> closestKickEvent = kickEvents.stream()
				.min(Comparator.comparingLong(e -> Math.abs(e.getTimestamp() - ident.getKickTimestamp())));
		
		if (!closestKickEvent.isPresent())
		{
			return;
		}
		
		BotID kickBot = closestKickEvent.get().getKickingBot();
		if (botCmdMap.containsKey(kickBot))
		{
			KickerDribblerCommands kd = botCmdMap.get(kickBot);
			if ((ident.getKickVelocity().z() > 0) == (kd.getDevice() == EKickerDevice.CHIP))
			{
				database.getKickSamples()
						.add(new KickModelSample(kickBot.getSaveableString(), ident.getKickVelocity().getLength() * 1e-3,
								kd.getKickSpeed() * 1e3, kd.getDribblerSpeed(), ident.getKickVelocity().z() > 0));
				
				kickPresenter.update();
			}
		}
		
		kickEvents.clear();
		botCmdMap.clear();
	}
	
	
	@Override
	public void onCommandSent(final ABot bot, final long timestamp)
	{
		KickerDribblerCommands kd = bot.getMatchCtrl().getSkill().getKickerDribbler();
		if (kd.getMode().equals(EKickerMode.ARM_TIME))
		{
			botCmdMap.put(bot.getBotId(), kd);
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
