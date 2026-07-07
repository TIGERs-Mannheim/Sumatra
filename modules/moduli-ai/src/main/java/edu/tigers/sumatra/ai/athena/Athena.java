package edu.tigers.sumatra.ai.athena;

import com.github.g3force.configurable.ConfigRegistration;
import edu.tigers.sumatra.ai.data.BotAiInformation;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.botmanager.data.MultimediaControl;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.PathFinderPrioMap;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.skillsystem.SkillExecutor;
import edu.tigers.sumatra.skillsystem.skills.ISkill;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Athena handles plays and roles and executes skills from roles.
 */
@Log4j2
public class Athena
{
	private static final String CONFIG_PLAYS = "plays";
	private static final String CONFIG_ROLES = "roles";

	static
	{
		for (EPlay ec : EPlay.values())
		{
			ConfigRegistration.registerClass(CONFIG_PLAYS, ec.getInstanceableClass().getImpl());
		}
		for (ERole ec : ERole.values())
		{
			ConfigRegistration.registerClass(CONFIG_ROLES, ec.getInstanceableClass().getImpl());
		}
	}

	@Getter
	private AthenaGuiInput athenaGuiInput = new AthenaGuiInput();
	private IAthenaAdapter athenaAdapter = new MatchModeAthenaAdapter();
	@Getter
	private EAIControlState controlState = EAIControlState.MATCH_MODE;
	private boolean newControlState = false;

	private final ASkillSystem skillSystem;
	private final Map<BotID, Boolean> botIsStopped = new HashMap<>();


	public Athena(ASkillSystem skillSystem)
	{
		this.skillSystem = skillSystem;
	}


	public void changeMode(final EAIControlState mode)
	{
		controlState = mode;
		newControlState = true;
	}


	private void updateAdapter()
	{
		if (!newControlState)
		{
			return;
		}
		newControlState = false;
		athenaAdapter.stop(athenaGuiInput);
		switch (controlState)
		{
			case EMERGENCY_MODE -> athenaAdapter = new EmergencyModeAthenaAdapter();
			case MATCH_MODE -> athenaAdapter = new MatchModeAthenaAdapter();
			case TEST_MODE -> athenaAdapter = new TestModeAthenaAdapter();
			default -> throw new IllegalStateException();
		}
	}


	/**
	 * perform play selection, call update on every play, execute skills, and collect bot AI info
	 *
	 * @param metisAiFrame
	 * @return
	 */
	public AthenaAiFrame process(final MetisAiFrame metisAiFrame)
	{
		updateAdapter();
		var input = athenaGuiInput.copy();
		IPlayStrategy playStrategy = athenaAdapter.process(metisAiFrame, input);
		athenaGuiInput = input;

		Map<BotID, BotAiInformation> aiInfos = getBotAiInfos(
				metisAiFrame.getWorldFrame(),
				playStrategy,
				metisAiFrame.getTeamColor()
		);

		AthenaAiFrame athenaAiFrame = AthenaAiFrame.builder()
				.baseAiFrame(metisAiFrame.getBaseAiFrame())
				.metisAiFrame(metisAiFrame)
				.playStrategy(playStrategy)
				.botAiInformation(aiInfos)
				.build();

		executePlaysAndRoles(athenaAiFrame);
		executeSkills(athenaAiFrame);
		drawRoleNames(athenaAiFrame);

		return athenaAiFrame;
	}


	private void executeSkills(final AthenaAiFrame frame)
	{
		PathFinderPrioMap prioMap = frame.getTacticalField().getPathFinderPrioMap();
		List<ARole> assignedRoles = getAssignedRoles(frame);

		for (var botId : frame.getWorldFrame().getTigerBotsAvailable().keySet())
		{
			SkillExecutor executor = skillSystem.getExecutor(botId);
			executor.setPrioMap(prioMap);

			MultimediaControl control = frame.getBaseAiFrame().getMultimediaControl()
					.getOrDefault(botId, MultimediaControl.DEFAULT);
			executor.setMultimediaControl(new MultimediaControl(control));
		}

		for (ARole role : assignedRoles)
		{
			ISkill skill = role.getNewSkill();
			if (skill != null)
			{
				BotID botId = role.getBotID();
				skillSystem.execute(botId, skill);
				botIsStopped.put(botId, Boolean.FALSE);
			}
		}

		Set<BotID> botsAssigned = assignedRoles.stream().map(ARole::getBotID).collect(Collectors.toSet());
		stopBotsNotAssigned(frame, botsAssigned);
	}


	private List<ARole> getAssignedRoles(AthenaAiFrame frame)
	{
		Set<BotID> botsAssigned = new HashSet<>();
		List<ARole> assignedRoles = new ArrayList<>();

		for (APlay play : frame.getPlayStrategy().getActivePlays())
		{
			for (ARole role : play.getRoles())
			{
				BotID botId = role.getBotID();
				if (!botId.isBot())
				{
					log.error("Role {} has no assigned bot id!", role);
				} else if (botsAssigned.contains(botId))
				{
					log.error("Bot with id {} already has another role assigned. Can not assign {}.", botId, role);
					logCurrentRoleAssignment(frame);
				} else
				{
					botsAssigned.add(botId);
					assignedRoles.add(role);
				}
			}
		}
		return assignedRoles;
	}


	private void logCurrentRoleAssignment(final AthenaAiFrame frame)
	{
		for (APlay play : frame.getPlayStrategy().getActivePlays())
		{
			for (ARole role : play.getRoles())
			{
				log.warn("{}: {} -> {}", play, role, role.getBotID());
			}
		}
	}


	private void stopBotsNotAssigned(final AthenaAiFrame frame, final Set<BotID> botsAssigned)
	{
		Set<BotID> botsLeft = new HashSet<>(frame.getWorldFrame().getTigerBotsAvailable().keySet());
		botsLeft.removeAll(botsAssigned);

		for (BotID botId : botsLeft)
		{
			// No role for this bot: Stop him (if not yet done)
			final boolean stopped = botIsStopped.getOrDefault(botId, true);
			if (!stopped)
			{
				skillSystem.execute(botId, new IdleSkill());
				botIsStopped.put(botId, Boolean.TRUE);
			}
		}
	}


	private Map<BotID, BotAiInformation> getBotAiInfos(
			WorldFrame wFrame,
			IPlayStrategy playStrategy,
			ETeamColor teamColor
	)
	{
		Map<BotID, BotAiInformation> aiInfos = new HashMap<>();
		List<ISkill> skills = skillSystem.getCurrentSkills(teamColor);
		for (ISkill skill : skills)
		{
			var aiInfo = getBotAiInfo(wFrame, playStrategy, skill);
			aiInfos.put(skill.getBotId(), aiInfo);
		}
		return aiInfos;
	}


	private BotAiInformation getBotAiInfo(
			WorldFrame wFrame,
			IPlayStrategy playStrategy,
			ISkill skill
	)
	{
		BotID botId = skill.getBotId();
		BotAiInformation aiInfo = skill.getBotAiInfo();
		ITrackedBot bot = wFrame.getBot(botId);

		if (bot != null)
		{
			addAiInfoForRobotInfo(aiInfo, bot);
		}

		for (APlay play : playStrategy.getActivePlays())
		{
			addAiInfoForPlay(aiInfo, play, botId);
		}

		return aiInfo;
	}


	private void addAiInfoForRobotInfo(
			BotAiInformation aiInfo,
			ITrackedBot bot
	)
	{
		var robotInfo = bot.getRobotInfo();

		aiInfo.setDribblerSpeedFeedback(robotInfo.getDribbleSpeedFeedback());
		aiInfo.setDribblerSpeed(robotInfo.getDribbleSpeedSet());
		aiInfo.setDribbleTraction(robotInfo.getDribbleTraction());

		String ballContact = "";
		if (robotInfo.isBarrierInterrupted())
		{
			ballContact += "BARRIER";
		}

		if (bot.getBallContact().hasContactFromVision())
		{
			ballContact += " VISION";
		}

		aiInfo.setBallContact(ballContact);

		double curVel = bot.getVel().getLength2();
		aiInfo.setVelocityCurrent(curVel);
		aiInfo.setAngularVelocity(bot.getAngularVel());
	}


	private void addAiInfoForPlay(BotAiInformation aiInfo, APlay play, BotID botId)
	{
		for (ARole role : play.getRoles())
		{
			if (!role.getBotID().equals(botId))
			{
				continue;
			}
			aiInfo.setPlay(play.toString());
			aiInfo.setRole(role.toString());
			aiInfo.setRoleState(role.getCurrentState());
			return;
		}
	}


	private void drawRoleNames(final AthenaAiFrame aiFrame)
	{
		List<IDrawableShape> shapes = aiFrame.getShapeMap().get(EAiShapesLayer.AI_ROLE_NAMES);
		for (ARole role : aiFrame.getPlayStrategy().getActiveRoles().values())
		{
			if (role.getBot() == null)
			{
				continue;
			}

			IVector2 pos = role.getPos();
			IState state = role.getCurrentState();
			String text = role.toString();
			if (state != null)
			{
				text += "\n" + state;
			}
			text += "\n" + role.getCurrentSkill().getClass().getSimpleName();
			DrawableAnnotation dTxtRole = new DrawableAnnotation(pos, text);
			dTxtRole.setColor(aiFrame.getTeamColor().getColor());
			dTxtRole.withFontHeight(50);
			dTxtRole.withOffset(Vector2.fromX(130));
			shapes.add(dTxtRole);
		}
	}


	/**
	 * @param aiFrame
	 */
	public void onException(final AthenaAiFrame aiFrame)
	{
		for (ARole role : aiFrame.getPlayStrategy().getActiveRoles().values())
		{
			role.setCompleted();
		}
	}


	private void executePlaysAndRoles(final AthenaAiFrame frame)
	{
		Set<APlay> plays = frame.getPlayStrategy().getActivePlays();
		try
		{
			plays.forEach(play -> play.getRoles().forEach(role -> role.updateBefore(frame)));
			plays.forEach(play -> play.updateBeforeRoles(frame));
			plays.forEach(play -> play.getRoles().forEach(role -> role.update(frame)));
			plays.forEach(APlay::updateAfterRoles);
		} catch (Exception err)
		{
			log.error("Exception during athena update!", err);
		}
	}
}
