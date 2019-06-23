/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 1, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.messaging.topics;

import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.Message;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee;
import edu.dhbw.mannheim.tigers.sumatra.proto.model.modules.impls.ai.AIGeneralProtos.AIException;
import edu.dhbw.mannheim.tigers.sumatra.proto.model.modules.impls.ai.AIGeneralProtos.AIMode;
import edu.dhbw.mannheim.tigers.sumatra.proto.model.modules.impls.ai.AIGeneralProtos.MatchBehaviour;
import edu.dhbw.mannheim.tigers.sumatra.proto.model.modules.impls.ai.AIModulesProtos.PlaysOverview;
import edu.dhbw.mannheim.tigers.sumatra.proto.model.modules.impls.ai.AIModulesProtos.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.proto.model.modules.impls.ai.ApollonProtos.DatabaseOptions;
import edu.dhbw.mannheim.tigers.sumatra.proto.model.modules.impls.ai.ApollonProtos.MatchingOptions;
import edu.dhbw.mannheim.tigers.sumatra.proto.model.modules.impls.ai.AthenaProtos.AvailablePlays;
import edu.dhbw.mannheim.tigers.sumatra.proto.model.modules.impls.ai.AthenaProtos.ForceNewDecision;
import edu.dhbw.mannheim.tigers.sumatra.proto.model.modules.impls.ai.AthenaProtos.PlayCommand;
import edu.dhbw.mannheim.tigers.sumatra.proto.model.modules.impls.ai.AthenaProtos.PlaysToRemove;
import edu.dhbw.mannheim.tigers.sumatra.proto.model.modules.impls.ai.BotOverviewProtos.BotOverview;
import edu.dhbw.mannheim.tigers.sumatra.proto.model.modules.impls.ai.LachesisProtos.AvailableRoles;
import edu.dhbw.mannheim.tigers.sumatra.proto.model.modules.impls.ai.LachesisProtos.ClearRoleCommand;
import edu.dhbw.mannheim.tigers.sumatra.proto.model.modules.impls.ai.LachesisProtos.RoleCommand;
import edu.dhbw.mannheim.tigers.sumatra.proto.model.modules.impls.rcm.ActionCommandProtos.BotActionCommand;
import edu.dhbw.mannheim.tigers.sumatra.proto.model.modules.impls.rcm.BotConnProtos.BotConn;
import edu.dhbw.mannheim.tigers.sumatra.proto.model.modules.impls.rcm.LifeSignProtos.LifeSign;
import edu.dhbw.mannheim.tigers.sumatra.proto.presenter.MainPresenterProtos.Emergency;
import edu.dhbw.mannheim.tigers.sumatra.proto.presenter.MainPresenterProtos.FilenameArray;
import edu.dhbw.mannheim.tigers.sumatra.proto.presenter.MainPresenterProtos.Fps;
import edu.dhbw.mannheim.tigers.sumatra.proto.presenter.MainPresenterProtos.ModulesState;
import edu.dhbw.mannheim.tigers.sumatra.proto.presenter.MainPresenterProtos.ModuliConfigFile;
import edu.dhbw.mannheim.tigers.sumatra.proto.presenter.TimerProtos.TimerInfo;
import edu.dhbw.mannheim.tigers.sumatra.util.log.LogProtos.FileTree;
import edu.dhbw.mannheim.tigers.sumatra.util.log.LogProtos.LogLevel;
import edu.dhbw.mannheim.tigers.sumatra.util.log.LogProtos.LoggingEventProto;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.MessageType;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.RegistryProtos;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.test.TestProtos;


/**
 * This enum contains all used topics and their matching protobuf type
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public enum ETopics implements ITopics
{
	/** REGISTRY topic is used for lost connection. CLients send there last will here and all registries are cleared */
	REGISTRY("registry", MessageType.CONTROL, RegistryProtos.Client.getDefaultInstance()),
	/** Registry for AI module. */
	AI_REGISTRY("registry/ai", MessageType.CONTROL, RegistryProtos.Client.getDefaultInstance()),
	/** Registry for Moduli module. */
	MODULI_REGISTRY("registry/moduli", MessageType.CONTROL, RegistryProtos.Client.getDefaultInstance()),
	/** Registry for Apollon module. */
	APOLLON_REGISTRY("registry/ai/apollon", MessageType.CONTROL, RegistryProtos.Client.getDefaultInstance()),
	/** Registry for Lachesis module. */
	LACHESIS_REGISTRY("registry/ai/lachesis", MessageType.CONTROL, RegistryProtos.Client.getDefaultInstance()),
	/** Registry for Athena module. */
	ATHENA_REGISTRY("registry/ai/athena", MessageType.CONTROL, RegistryProtos.Client.getDefaultInstance()),
	/** Registry for Log module. */
	LOG_REGISTRY("registry/log", MessageType.CONTROL, RegistryProtos.Client.getDefaultInstance()),
	/** Registry for Timer module. */
	TIMER_REGISTRY("registry/timer", MessageType.CONTROL, RegistryProtos.Client.getDefaultInstance()),
	/** Registry for Emergency module. */
	EMERGENCY_REGISTRY("registry/emergency", MessageType.CONTROL, RegistryProtos.Client.getDefaultInstance()),
	/** Registry for Referee module. */
	REFEREE_REGISTRY("registry/referee", MessageType.CONTROL, RegistryProtos.Client.getDefaultInstance()),
	/** Registry for RCM module. */
	RCM_REGISTRY("registry/rcm", MessageType.CONTROL, RegistryProtos.Client.getDefaultInstance()),
	/** Registry for WF module. */
	WF_REGISTRY("registry/wf", MessageType.CONTROL, RegistryProtos.Client.getDefaultInstance()),
	/** Registry for TEST module. */
	TEST_REGISTRY("registry/test", MessageType.CONTROL, RegistryProtos.Client.getDefaultInstance()),
	
	/** Moduli State topic request (send from views) */
	MODULI_STATE_REQUEST("moduli/state/request", MessageType.CONTROL, ModulesState.getDefaultInstance()),
	/** Moduli State topic current (send from sumatra) */
	MODULI_STATE_SUMATRA("moduli/state", MessageType.CONTROL, ModulesState.getDefaultInstance()),
	/** List of all available moduli config files. */
	MODULI_CONFIG("moduli/config", MessageType.CONTROL, FilenameArray.getDefaultInstance()),
	/** */
	MODULI_CONFIG_CHANGE("moduli/config/setter", MessageType.CONTROL, ModuliConfigFile.getDefaultInstance()),
	/** */
	MODULI_LOAD_MODULES("moduli/config/load", MessageType.CONTROL, ModuliConfigFile.getDefaultInstance()),
	
	/** Extra topic to send AI Exceptions message, detailed log message should appear in log */
	AI_EXCEPTION("ai/exception", MessageType.DURABLE_INFO, AIException.getDefaultInstance()),
	/** Topic for Match Behaviour */
	AI_MATCH_BEHAVIOUR("ai/matchbehaviour", MessageType.DURABLE_INFO, MatchBehaviour.getDefaultInstance()),
	/** State of AI: Match, Play Test, Role Test, Emergency */
	AI_MODE("ai/mode/request", MessageType.CONTROL, AIMode.getDefaultInstance()),
	/**
	 * Message with general information about current plays (at the moment number of bots without plays and active plays
	 * list)
	 */
	AI_PLAY_OVERVIEW("ai/plays/overview", MessageType.DURABLE_INFO, PlaysOverview.getDefaultInstance()),
	/** Tactical Field Topic */
	AI_TACTICAL_FIELD("ai/tacticalfield", MessageType.DURABLE_INFO, TacticalField.getDefaultInstance()),
	/** Overview of Bots and their AI state */
	AI_BOTS_OVERVIEW("ai/bots/overview", MessageType.DURABLE_INFO, BotOverview.getDefaultInstance()),
	/** List of bots which are available */
	AI_BOTS_AVAILABLE("ai/bots/available", MessageType.CONTROL, BotOverview.getDefaultInstance()),
	
	/** Settings for Database of Apollon module */
	APOLLON_DATABASE("ai/apollon/db/request", MessageType.CONTROL, DatabaseOptions.getDefaultInstance()),
	/** Settings of matching options for Apollon module */
	APOLLON_MATCHING("ai/apollon/matching/request", MessageType.CONTROL, MatchingOptions.getDefaultInstance()),
	
	/** Topic that broadcasts all available roles */
	LACHESIS_ROLES("ai/lachesis/roles", MessageType.CONTROL, AvailableRoles.getDefaultInstance()),
	/** Topic for adding roles */
	LACHESIS_ADD_ROLE("ai/lachesis/role/add/request", MessageType.DURABLE_INFO, RoleCommand.getDefaultInstance()),
	/** Topic for removing roles */
	LACHESIS_RM_ROLE("ai/lachesis/role/remove/request", MessageType.DURABLE_INFO, RoleCommand.getDefaultInstance()),
	/** Topic for sending Clear Roles command */
	LACHESIS_CLEAR_ROLE("ai/lachesis/role/clear/request", MessageType.DURABLE_INFO, ClearRoleCommand
			.getDefaultInstance()),
	
	/** Topic that broadcasts all available roles */
	ATHENA_PLAYS("ai/athena/plays", MessageType.CONTROL, AvailablePlays.getDefaultInstance()),
	/** Topic for adding plays */
	ATHENA_ADD_PLAY("ai/athena/play/add/request", MessageType.DURABLE_INFO, PlayCommand.getDefaultInstance()),
	/** Topic for adding plays */
	ATHENA_RM_PLAY("ai/athena/play/remove/request", MessageType.DURABLE_INFO, PlaysToRemove.getDefaultInstance()),
	/** Force Play Decision */
	ATHENA_FORCE_DECISION("ai/athena/forcedecision/request", MessageType.DURABLE_INFO, ForceNewDecision
			.getDefaultInstance()),
	
	/** Log record for fatal logs */
	LOG_FATAL("log/fatal", MessageType.NONDURABLE_INFO, LoggingEventProto.getDefaultInstance()),
	/** Log record for error logs */
	LOG_ERROR("log/error", MessageType.NONDURABLE_INFO, LoggingEventProto.getDefaultInstance()),
	/** Log record for warn logs */
	LOG_WARN("log/warn", MessageType.NONDURABLE_INFO, LoggingEventProto.getDefaultInstance()),
	/** Log record for info logs */
	LOG_INFO("log/info", MessageType.NONDURABLE_INFO, LoggingEventProto.getDefaultInstance()),
	/** Log record for debug logs */
	LOG_DEBUG("log/debug", MessageType.NONDURABLE_INFO, LoggingEventProto.getDefaultInstance()),
	/** Log record for trace logs */
	LOG_TRACE("log/trace", MessageType.NONDURABLE_INFO, LoggingEventProto.getDefaultInstance()),
	/** Log level of Sumatra */
	LOG_LEVEL("log/level/request", MessageType.CONTROL, LogLevel.getDefaultInstance()),
	/** File Tree for selecting filters in Log Panel */
	LOG_FILETREE("log/filetree", MessageType.CONTROL, FileTree.getDefaultInstance()),
	
	/** Referee commands: refMsg.proto */
	REFEREE_MSG("ref", MessageType.DURABLE_INFO, SSL_Referee.getDefaultInstance()),
	/** Referee commands that a user creates for its own: refMsg.proto */
	REFEREE_OWN("ref/own/request", MessageType.DURABLE_INFO, SSL_Referee.getDefaultInstance()),
	
	/** Commands for controlling bots manually */
	BOT_ACTION_COMMAND("rcm/aCmd", MessageType.NONDURABLE_INFO, BotActionCommand.getDefaultInstance()),
	/** info about connection status of a bot */
	BOT_CONNECTION("rcm/botConn", MessageType.CONTROL, BotConn.getDefaultInstance()),
	/** info about connection status of a bot */
	LIFE_SIGN("rcm/lifeSign", MessageType.NONDURABLE_INFO, LifeSign.getDefaultInstance()),
	
	/** Sends timer info timer.proto */
	TIMER_INFO("timer", MessageType.NONDURABLE_INFO, TimerInfo.getDefaultInstance()),
	
	/** Sends emergency info */
	EMERGENCY("emergency/request", MessageType.CONTROL, Emergency.getDefaultInstance()),
	
	/** Sends fps of aiinfoframe */
	AI_FPS("ai/fps", MessageType.NONDURABLE_INFO, Fps.getDefaultInstance()),
	/** Sends fps of worldframe */
	WF_FPS("wf/fps", MessageType.NONDURABLE_INFO, Fps.getDefaultInstance()),
	
	/** Test topic for local latency test */
	TEST_LATENCY_LOCAL("test/latency/local", MessageType.NONDURABLE_INFO, TestProtos.TimerInfo.getDefaultInstance()),
	/** Test topic for remote latency test - send (first step) */
	TEST_LATENCY_REMOTE_SND("test/latency/remote/send", MessageType.NONDURABLE_INFO, TestProtos.TimerInfo
			.getDefaultInstance()),
	/** Test topic for remote latency test - send (first step) */
	TEST_LATENCY_REMOTE_RCV("test/latency/remote/receive", MessageType.NONDURABLE_INFO, TestProtos.TimerInfo
			.getDefaultInstance()),
	/** Test topic for reconnect Test */
	TEST_RECONNECT_PUBLISH("test/connection/reconnect/publish", MessageType.NONDURABLE_INFO, TestProtos.ConnectionInfo
			.getDefaultInstance()),
	
	;
	
	
	/** Topic name of the MQTT Topic */
	private final String						mqttTopic;
	/** Control or Info type of expected message */
	private final MessageType				type;
	/** Wrapping type of expected message */
	private final Message					protoType;
	
	private static Map<String, ETopics>	nameValueMap	= new HashMap<String, ETopics>();
	
	static
	{
		for (ETopics topic : ETopics.values())
		{
			nameValueMap.put(topic.getName(), topic);
		}
	}
	
	
	private ETopics(String mqttTopic, MessageType type, Message protoType)
	{
		this.mqttTopic = mqttTopic;
		this.type = type;
		this.protoType = protoType;
	}
	
	
	/**
	 * Used to get the topic name
	 * @return the mqttTopic
	 */
	@Override
	public String getName()
	{
		return mqttTopic;
	}
	
	
	/**
	 * @return the type
	 */
	@Override
	public MessageType getType()
	{
		return type;
	}
	
	
	/**
	 * @return the protoType
	 */
	@Override
	public Message.Builder getProtoType()
	{
		return protoType.newBuilderForType();
	}
	
	
	/**
	 * @param name
	 * @return
	 */
	public static ETopics getTopicByName(String name)
	{
		return nameValueMap.get(name);
	}
}
