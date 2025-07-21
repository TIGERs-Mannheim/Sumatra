/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.modelidentification.kickspeed;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.Separators;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import edu.tigers.sumatra.bot.BotLastKickState;
import edu.tigers.sumatra.botmanager.ACommandBasedBotManager;
import edu.tigers.sumatra.botmanager.basestation.BotCommand;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerMode;
import edu.tigers.sumatra.botmanager.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchCtrl;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.tigers.sumatra.data.TimeLimitedBuffer;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.modelidentification.kickspeed.data.BallModelSample;
import edu.tigers.sumatra.modelidentification.kickspeed.data.KickDatabase;
import edu.tigers.sumatra.modelidentification.kickspeed.data.KickModelSample;
import edu.tigers.sumatra.modelidentification.kickspeed.data.RedirectModelSample;
import edu.tigers.sumatra.observer.EventDistributor;
import edu.tigers.sumatra.vision.AVisionFilter;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;
import edu.tigers.sumatra.vision.data.FilteredVisionKick;
import edu.tigers.sumatra.vision.kick.estimators.EBallModelIdentType;
import edu.tigers.sumatra.vision.kick.estimators.IBallModelIdentResult;
import edu.tigers.sumatra.vision.kick.estimators.straight.FlatKickSolverNonLin3Factor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import static com.fasterxml.jackson.core.PrettyPrinter.DEFAULT_SEPARATORS;


@Log4j2
public class KickSpeedObserver implements IWorldFrameObserver
{
	private static final String DATABASE_FILE = "data/kickDatabase.json";

	private static final double TIME_LIMITED_BUFFER_DURATION = 30.0;

	@Getter
	private final EventDistributor<KickDatabase> onDatabaseChanged = new EventDistributor<>();

	private final Queue<FilteredVisionKick> kickEvents = new CircularFifoQueue<>(10);
	private final Map<BotID, TimeLimitedBuffer<KickerDribblerCommands>> botCmdMapArmTime = new ConcurrentHashMap<>();
	private final Map<BotID, TimeLimitedBuffer<KickerDribblerCommands>> botCmdMapArmSpeed = new ConcurrentHashMap<>();
	private final Map<BotID, TimeLimitedBuffer<BotLastKickState>> botMapLastKickState = new ConcurrentHashMap<>();
	private final Map<BotID, Integer> botMapKickCounter = new ConcurrentHashMap<>();

	@Getter
	private KickDatabase database;

	private long lastFilteredVisionKickTimestamp = 0;
	private long lastFilteredVisionTimestamp = 0;


	public void setEnabled(final boolean enabled)
	{
		SumatraModel.getInstance().setUserProperty(KickSpeedObserver.class, "enabled", enabled);
	}


	public boolean isEnabled()
	{
		return SumatraModel.getInstance().getUserProperty(KickSpeedObserver.class, "enabled", false);
	}


	public void start()
	{
		loadDatabase();

		SumatraModel.getInstance().getModuleOpt(AVisionFilter.class).ifPresent(visionFilter -> {
			visionFilter.getFilteredVisionFrame()
					.subscribe(getClass().getCanonicalName(), this::onNewFilteredVisionFrame);
			visionFilter.getFilteredVisionFrame()
					.subscribeClear(getClass().getCanonicalName(), this::onClearFilteredVisionFrame);
			visionFilter.getBallModelIdentResult()
					.subscribe(getClass().getCanonicalName(), this::onBallModelIdentificationResult);
		});

		SumatraModel.getInstance().getModuleOpt(ACommandBasedBotManager.class).ifPresent(bm -> {
			bm.getOnIncomingBotCommand()
					.subscribe(getClass().getCanonicalName(), this::onIncomingBotCommand);
			bm.getOnOutgoingBotCommand()
					.subscribe(getClass().getCanonicalName(), this::onOutgoingBotCommand);
		});
	}


	public void stop()
	{
		saveDatabase();

		SumatraModel.getInstance().getModuleOpt(AVisionFilter.class).ifPresent(visionFilter -> {
			visionFilter.getFilteredVisionFrame().unsubscribe(getClass().getCanonicalName());
			visionFilter.getFilteredVisionFrame().unsubscribeClear(getClass().getCanonicalName());
			visionFilter.getBallModelIdentResult().unsubscribe(getClass().getCanonicalName());
		});

		SumatraModel.getInstance().getModuleOpt(ACommandBasedBotManager.class).ifPresent(bm -> {
			bm.getOnIncomingBotCommand().unsubscribe(getClass().getCanonicalName());
			bm.getOnOutgoingBotCommand().unsubscribe(getClass().getCanonicalName());
		});
	}


	private void onNewFilteredVisionFrame(final FilteredVisionFrame filteredVisionFrame)
	{
		lastFilteredVisionTimestamp = filteredVisionFrame.getTimestamp();
		filteredVisionFrame.getKick().ifPresent(kick -> {
			if (kick.getKickTimestamp() != lastFilteredVisionKickTimestamp)
			{
				lastFilteredVisionKickTimestamp = kick.getKickTimestamp();
				kickEvents.add(kick);
			}
		});
	}


	private void onClearFilteredVisionFrame()
	{
		lastFilteredVisionKickTimestamp = 0;
		lastFilteredVisionTimestamp = 0;
		kickEvents.clear();
		botCmdMapArmTime.clear();
		botCmdMapArmSpeed.clear();
		botMapKickCounter.clear();
		botMapLastKickState.clear();
	}


	private void onBallModelIdentificationResult(final IBallModelIdentResult ident)
	{
		if (!isEnabled())
		{
			return;
		}

		var closestKickEvent = kickEvents.stream()
				.min(Comparator.comparingLong(e -> Math.abs(e.getKickTimestamp() - ident.getKickTimestamp())));

		Optional<BotID> kickBotOpt = closestKickEvent.map(FilteredVisionKick::getKickingBot);

		if (kickBotOpt.isEmpty())
		{
			log.debug("Drop ball model identification result: No kick event found");
			return;
		}

		Optional<BotLastKickState> lastKickState = Optional.ofNullable(botMapLastKickState.get(kickBotOpt.get()))
				.flatMap(buffer -> buffer.getClosest(ident.getKickTimestamp()));

		if (lastKickState.isEmpty())
		{
			log.debug("Drop ball model identification result: No kick state found, probably not our robot");
			return;
		}

		database.getBallSamplesByIdentType().get(ident.getType())
				.add(new BallModelSample(
								ident.getType(),
								ident.getKickTimestamp(),
								kickBotOpt.map(BotID::getSaveableString).orElse(""),
								ident.getModelParameters(),
						lastKickState.get()
						)
				);

		BotID kickBot = kickBotOpt.get();

		addKickModelSample(kickBot, lastKickState.get(), ident);
		addRedirectModelSample(kickBot, lastKickState.get(), ident);

		onDatabaseChanged.newEvent(database);
		saveDatabase();
	}


	private void onIncomingBotCommand(BotCommand botCommand)
	{
		if (botCommand.command().getType() != ECommand.CMD_SYSTEM_MATCH_FEEDBACK)
		{
			return;
		}
		var matchFeedback = (TigerSystemMatchFeedback) botCommand.command();

		botMapKickCounter.putIfAbsent(botCommand.botId(), matchFeedback.getKickCounter());
		if (botMapKickCounter.get(botCommand.botId()) != matchFeedback.getKickCounter())
		{
			botMapKickCounter.put(botCommand.botId(), matchFeedback.getKickCounter());

			// Kick counter changed => bot kicked => last kick data is updated
			var lastKick = new BotLastKickState(
					matchFeedback.getLastKickDevice() == EKickerDevice.CHIP,
					matchFeedback.getLastKickDuration(), matchFeedback.getLastKickDribblerSpeed(),
					matchFeedback.getLastKickDribblerForce()
			);

			botMapLastKickState.computeIfAbsent(
					botCommand.botId(),
					id -> new TimeLimitedBuffer<BotLastKickState>().setMaxDuration(TIME_LIMITED_BUFFER_DURATION)
			).add(lastFilteredVisionTimestamp, lastKick);
		}
	}


	private void addKickModelSample(BotID kickBot, BotLastKickState botKickState, IBallModelIdentResult ident)
	{
		if (botCmdMapArmTime.containsKey(kickBot))
		{
			botCmdMapArmTime.get(kickBot).getClosest(ident.getKickTimestamp(), 0.3).ifPresent(kd -> {
				if ((ident.getKickVelocity().z() > 0) == (kd.getDevice() == EKickerDevice.CHIP))
				{
					var sample = KickModelSample.builder()
							.withKickTimestamp(ident.getKickTimestamp())
							.withBotId(kickBot.getSaveableString())
							.withMeasuredKickVel(ident.getKickVelocity().getLength() * 1e-3)
							.withCmdDribbleSpeed(kd.getDribblerSpeed())
							.withCmdDribbleForce(kd.getDribblerForce())
							.withChip(ident.getKickVelocity().z() > 0)
							.withBotStateAtKick(botKickState)
							.build();

					log.debug("Add kick model sample: {}", sample);
					database.getKickSamples().add(sample);
				}
			});

			// ARM_TIME takes priority over normal samples with speed
			return;
		}

		if (botCmdMapArmSpeed.containsKey(kickBot))
		{
			botCmdMapArmSpeed.get(kickBot).getClosest(ident.getKickTimestamp(), 0.3).ifPresent(kd -> {
				if ((ident.getKickVelocity().z() > 0) == (kd.getDevice() == EKickerDevice.CHIP))
				{
					var sample = KickModelSample.builder()
							.withKickTimestamp(ident.getKickTimestamp())
							.withBotId(kickBot.getSaveableString())
							.withMeasuredKickVel(ident.getKickVelocity().getLength() * 1e-3)
							.withCmdKickVel(kd.getKickSpeed())
							.withCmdDribbleSpeed(kd.getDribblerSpeed())
							.withCmdDribbleForce(kd.getDribblerForce())
							.withChip(ident.getKickVelocity().z() > 0)
							.withBotStateAtKick(botKickState)
							.build();

					log.debug("Add kick model sample: {}", sample);
					database.getKickSamples().add(sample);
				}
			});
		}
	}


	private void addRedirectModelSample(BotID kickBot, BotLastKickState botKickState, IBallModelIdentResult ident)
	{
		if (botCmdMapArmSpeed.containsKey(kickBot))
		{
			botCmdMapArmSpeed.get(kickBot).getClosest(ident.getKickTimestamp(), 0.3).ifPresent(kd -> {
				if (ident.getType() == EBallModelIdentType.REDIRECT)
				{
					var redirectIdent = (FlatKickSolverNonLin3Factor.RedirectModelIdentResult) ident;

					RedirectModelSample redirectModelSample = new RedirectModelSample(
							ident.getKickTimestamp(),
							kickBot.getSaveableString(),
							redirectIdent.getInVelocity().multiplyNew(1e-3),
							redirectIdent.getOutVelocity().multiplyNew(1e-3),
							redirectIdent.getSpinFactor(), kd.getKickSpeed(),
							botKickState
					);
					log.debug("Add redirect model sample: {}", redirectModelSample);
					database.getRedirectSamples().add(redirectModelSample);
				}
			});
		}
	}


	private void onOutgoingBotCommand(BotCommand botCommand)
	{
		if (botCommand.command().getType() != ECommand.CMD_SYSTEM_MATCH_CTRL)
		{
			return;
		}
		var matchCtrl = (TigerSystemMatchCtrl) botCommand.command();
		KickerDribblerCommands kd = matchCtrl.getSkill().getKickerDribbler();
		if (kd.getMode().equals(EKickerMode.ARM_TIME))
		{
			botCmdMapArmTime.computeIfAbsent(
					botCommand.botId(),
					b -> new TimeLimitedBuffer<KickerDribblerCommands>().setMaxDuration(TIME_LIMITED_BUFFER_DURATION)
			).add(lastFilteredVisionTimestamp, kd);
		} else if (kd.getMode().equals(EKickerMode.ARM))
		{
			botCmdMapArmSpeed.computeIfAbsent(
					botCommand.botId(),
					b -> new TimeLimitedBuffer<KickerDribblerCommands>().setMaxDuration(TIME_LIMITED_BUFFER_DURATION)
			).add(lastFilteredVisionTimestamp, kd);
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
				.withObjectIndenter(new DefaultIndenter().withLinefeed(System.lineSeparator()))
				.withSeparators(DEFAULT_SEPARATORS.withObjectFieldValueSpacing(Separators.Spacing.AFTER))
		);

		try
		{
			mapper.writeValue(file, database);
		} catch (IOException e)
		{
			log.error("Failed to write kick database", e);
		}
	}


	private void loadDatabase()
	{
		File file = Paths.get(DATABASE_FILE).toFile();
		if (!file.exists())
		{
			log.info("Initializing empty kick database");
			database = new KickDatabase();
			Arrays.stream(EBallModelIdentType.values())
					.forEach(type -> database.getBallSamplesByIdentType().put(type, new ArrayList<>()));
		} else
		{
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

			try
			{
				database = mapper.readValue(file, KickDatabase.class);
			} catch (IOException e)
			{
				log.error("Failed to load kick database", e);
			}
		}
	}
}
