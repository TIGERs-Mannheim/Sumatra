/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.08.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.calibrate.ChipKickCalibratePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.calibrate.StraightKickCalibratePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.KeeperSoloPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.NDefenderWDPPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.BallBreakingPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.BallBreakingV2Play;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.BallConquerPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.BallGettingPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.DirectShotV2Play;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.DirectShotV3Play;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.DoublePassPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.FlyingTigerPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.IndirectShotMultiplePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.IndirectShotV2Play;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.IndirectShotV3Play;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.IndirectShotV4Play;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.IntelligentBallGetterPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.MovingShotPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.PassingPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.RamboPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.mixedteam.IndirectReceiverPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.mixedteam.PassReceiverPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.mixedteam.PasserPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.AroundTheBallPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.CheeringPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.DestChangedTestPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.GuiTestPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.InitPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.MaintenancePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.MoveTestPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.HaltPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.freekick.CornerPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.freekick.FreekickMarkerPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.freekick.FreekickMovePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.freekick.ThrowInUsPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.kickoff.KickOffChipPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.kickoff.KickOffIndirectPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.kickoff.PositioningOnKickOffThem;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.penalty.PenaltyShootoutThemPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.penalty.PenaltyShootoutUsPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.penalty.PenaltyThemPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.penalty.PenaltyUsPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.stop.StopMarkerPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.stop.StopMovePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.support.BreakClearPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.support.BreakClearRedirecterPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.support.ManToManMarkerPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.support.PatternBlockPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.tecchallenges.NavigationChallengePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.tecchallenges.ShootingChallengePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.test.DebugShapesTestPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.test.PathplanningTestPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.test.TecShootChalTestPlay;


/**
 * Enumeration that represents the different {@link APlay}.
 * <p>
 * <b>Important:</b> An entry in the {@link PlayFactory} isn't necessary anymore. A play is reflected by it's class and
 * the constructor.
 * </p>
 * <b>Never</b> delete existing plays here. Mark them as {@link EPlayType#DEPRECATED}. Else, the knowledgebase may fail
 * to load
 * because the enum field is missing...
 * 
 * 
 * @author Gero
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @author DanielAl
 */
public enum EPlay
{
	// ##### OFFENSE
	/**  */
	RAMBO(EPlayType.OFFENSIVE, 1, 1, RamboPlay.class),
	/**  */
	BALL_GETTING(EPlayType.DISABLED, 1, 1, BallGettingPlay.class),
	/**  */
	INTELLIGENT_BALL_GETTING(EPlayType.DISABLED, 1, 1, IntelligentBallGetterPlay.class),
	/**  */
	BALL_BREAKING(EPlayType.DISABLED, 1, 1, BallBreakingPlay.class),
	/**  */
	BALL_BREAKING_V2(EPlayType.OFFENSIVE, 1, 1, BallBreakingV2Play.class),
	/**  */
	BALL_CONQUER(EPlayType.DEFECT, 1, 2, BallConquerPlay.class),
	/**  */
	DIRECT_SHOTV2(EPlayType.DISABLED, 1, 1, DirectShotV2Play.class, EProperty.GOAL_SCORER),
	/**  */
	DIRECT_SHOTV3(EPlayType.OFFENSIVE, 1, 1, DirectShotV3Play.class, EProperty.GOAL_SCORER),
	/**  */
	MOVING_SHOT(EPlayType.DEPRECATED, 1, 1, MovingShotPlay.class, EProperty.GOAL_SCORER),
	/**  */
	INDIRECT_SHOTV2(EPlayType.OFFENSIVE, 2, 2, IndirectShotV2Play.class, EProperty.GOAL_SCORER),
	/**  */
	INDIRECT_SHOTV3(EPlayType.DISABLED, 2, 2, IndirectShotV3Play.class, EProperty.GOAL_SCORER),
	/**  */
	INDIRECT_SHOTV4(EPlayType.OFFENSIVE, 2, 2, IndirectShotV4Play.class, EProperty.GOAL_SCORER),
	/**  */
	INDIRECT_SHOT_MULTIPLE(EPlayType.OFFENSIVE, 2, 3, IndirectShotMultiplePlay.class, EProperty.GOAL_SCORER),
	/**  */
	DOUBLE_PASS(EPlayType.DEFECT, 2, 2, DoublePassPlay.class, EProperty.GOAL_SCORER),
	
	/**  */
	PASSING(EPlayType.DISABLED, 2, 2, PassingPlay.class),
	/** */
	FLYING_TIGER(EPlayType.OFFENSIVE, 2, 2, FlyingTigerPlay.class),
	
	// ##### SUPPORT
	/**  */
	MAN_TO_MAN_MARKER(EPlayType.SUPPORT, 1, 3, ManToManMarkerPlay.class),
	/**  */
	BREAK_CLEAR(EPlayType.SUPPORT, 1, 2, BreakClearPlay.class),
	/**  */
	BREAK_CLEAR_REDIRECT(EPlayType.SUPPORT, 1, 2, BreakClearRedirecterPlay.class),
	
	/**  */
	// BREAK_CLEAR_OWN_SIDE(EPlayType.SUPPORT, 1, 2, BreakClearOwnSidePlay.class),
	/**  */
	PATTERN_BLOCK_PLAY(EPlayType.DEFECT, 1, 1, PatternBlockPlay.class),
	
	// ##### DEFENSE
	/**  */
	KEEPER_SOLO(EPlayType.KEEPER, 1, 1, KeeperSoloPlay.class),
	/** */
	N_DEFENDER_DEFENSPOINTS(EPlayType.DEFENSIVE, 1, 3, NDefenderWDPPlay.class),
	
	// ##### MIXED TEAM
	/**  */
	PASSER_MIXED(EPlayType.OFFENSIVE, 1, 1, PasserPlay.class, EProperty.MIXED_TEAM),
	/**  */
	PASS_RECEIVER_MIXED(EPlayType.DISABLED, 1, 1, PassReceiverPlay.class, EProperty.MIXED_TEAM),
	/**  */
	INDIRECT_RECEIVER_MIXED(EPlayType.OFFENSIVE, 1, 1, IndirectReceiverPlay.class, EProperty.MIXED_TEAM),
	
	// ##### STANDARD
	/**  */
	HALT(EPlayType.STANDARD, EPlay.MAX_BOTS, EPlay.MAX_BOTS, HaltPlay.class),
	/**  */
	INIT(EPlayType.STANDARD, EPlay.MAX_BOTS, EPlay.MAX_BOTS, InitPlay.class),
	/**  */
	MAINTENANCE(EPlayType.STANDARD, EPlay.MAX_BOTS, EPlay.MAX_BOTS, MaintenancePlay.class),
	/** */
	CHEERING(EPlayType.TEST, EPlay.MAX_BOTS, EPlay.MAX_BOTS, CheeringPlay.class),
	
	// free kick
	/**  */
	FREEKICK_MARKER(EPlayType.STANDARD, 2, 2, FreekickMarkerPlay.class),
	/**  */
	FREEKICK_MOVE(EPlayType.STANDARD, 1, 3, FreekickMovePlay.class),
	/**  */
	THROW_IN_US(EPlayType.STANDARD, 2, 2, ThrowInUsPlay.class),
	/** */
	CORNER_US(EPlayType.TEST, 3, 3, CornerPlay.class),
	
	// kick-off
	/**  */
	POSITIONING_ON_KICK_OFF_THEM(EPlayType.STANDARD, 1, 4, PositioningOnKickOffThem.class),
	/**  */
	KICK_OFF_INDIRECT(EPlayType.DEFECT, 4, 4, KickOffIndirectPlay.class),
	/**  */
	KICK_OFF_CHIP(EPlayType.STANDARD, 1, 1, KickOffChipPlay.class),
	
	// stop
	/**  */
	STOP_MOVE(EPlayType.STANDARD, 1, 4, StopMovePlay.class),
	/**  */
	STOP_MARKER(EPlayType.STANDARD, 1, 1, StopMarkerPlay.class),
	
	// penalty kick
	/** Penalty Game */
	PENALTY_THEM(EPlayType.STANDARD, EPlay.MAX_BOTS, EPlay.MAX_BOTS, PenaltyThemPlay.class),
	/** penalty Game */
	PENALTY_US(EPlayType.STANDARD, EPlay.MAX_BOTS, EPlay.MAX_BOTS, PenaltyUsPlay.class),
	/** Penalty Shoutout */
	PENALTY_SHOOTOUT_US(EPlayType.STANDARD, EPlay.MAX_BOTS, EPlay.MAX_BOTS, PenaltyShootoutUsPlay.class),
	/** Penalty Shoutout */
	PENALTY_SHOOTOUT_THEM(EPlayType.STANDARD, EPlay.MAX_BOTS, EPlay.MAX_BOTS, PenaltyShootoutThemPlay.class),
	
	// calibrate
	/**  */
	CHIP_KICK_CALIBRATE(EPlayType.CALIBRATE, 1, 1, ChipKickCalibratePlay.class),
	/**  */
	STRAIGHT_KICK_CALIBRATE(EPlayType.CALIBRATE, 1, 1, StraightKickCalibratePlay.class),
	
	// tecchallenges
	/** */
	SHOOTING_CHALLENGE(EPlayType.CHALLENGE, 1, 1, ShootingChallengePlay.class),
	/**  */
	NAVIGATION_CHALLENGE(EPlayType.CHALLENGE, 3, 3, NavigationChallengePlay.class),
	
	// test
	/**  */
	AROUND_THE_BALL(EPlayType.TEST, EPlay.MAX_BOTS, EPlay.MAX_BOTS, AroundTheBallPlay.class),
	/**  */
	GUI_TEST_PLAY(EPlayType.HELPER, EPlay.MAX_BOTS, EPlay.MAX_BOTS, GuiTestPlay.class),
	/**  */
	PATHPLANNING_TEST_PLAY(EPlayType.TEST, 1, EPlay.MAX_BOTS, PathplanningTestPlay.class),
	/**  */
	DEBUG_SHAPES(EPlayType.TEST, 1, 1, DebugShapesTestPlay.class),
	/**  */
	MOVE_TEST(EPlayType.TEST, 2, 2, MoveTestPlay.class),
	/**  */
	DEST_CHANGED_TEST(EPlayType.TEST, 1, 1, DestChangedTestPlay.class),
	/**  */
	TEC_SHOOT_CHAL_TEST(EPlayType.TEST, 1, 5, TecShootChalTestPlay.class),
	
	
	/**  */
	UNITIALIZED(EPlayType.HELPER, 1, 1, null), ;
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/** constant that indicates that the maximum number of bots is needed */
	public static final int			MAX_BOTS	= 0;
	
	/** @see EPlayType */
	private final EPlayType			type;
	private final int					minRoles;
	private final int					maxRoles;
	private final List<EProperty>	properties;
	private final Class<?>			impl;
	
	
	private enum EProperty
	{
		DUMMY,
		GOAL_SCORER,
		MIXED_TEAM,
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * 
	 * @param type
	 * @param minRoles minimal number of roles that
	 * @param maxRoles
	 * @param impl
	 */
	private EPlay(EPlayType type, int minRoles, int maxRoles, Class<?> impl)
	{
		this(type, minRoles, maxRoles, impl, EProperty.DUMMY);
	}
	
	
	/**
	 * 
	 * @param type
	 * @param minRoles minimal number of roles that
	 * @param maxRoles
	 * @param impl
	 * @param properties
	 */
	private EPlay(EPlayType type, int minRoles, int maxRoles, Class<?> impl, EProperty... properties)
	{
		this.type = type;
		this.minRoles = minRoles;
		this.maxRoles = maxRoles;
		this.impl = impl;
		this.properties = Arrays.asList(properties);
		PlayType.registerPlay(this);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return The {@link EPlayType}
	 */
	public EPlayType getType()
	{
		return type;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public int getMinRoles()
	{
		return minRoles;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public int getMaxRoles()
	{
		return maxRoles;
	}
	
	
	/**
	 * Returns the first public constructor of the Play.
	 * @return
	 * @throws SecurityException
	 */
	public Constructor<?> getConstructor()
	{
		return impl.getConstructors()[0];
	}
	
	
	/**
	 * @return Whether this play is of type
	 */
	public boolean isDeprecatedPlay()
	{
		return type.equals(EPlayType.DEPRECATED);
	}
	
	
	/**
	 * @return Whether this play can/should be used in a game
	 */
	public boolean isOperative()
	{
		return type.equals(EPlayType.OFFENSIVE) || type.equals(EPlayType.DEFENSIVE) || type.equals(EPlayType.SUPPORT);
	}
	
	
	@Override
	public String toString()
	{
		return name();
	}
	
	
	/**
	 * Is this play intended for scoring goals?
	 * 
	 * @return
	 */
	public boolean isGoalScorer()
	{
		return properties.contains(EProperty.GOAL_SCORER);
	}
	
	
	/**
	 * Is this play intended for scoring goals?
	 * 
	 * @return
	 */
	public boolean isMixedTeamPlay()
	{
		return properties.contains(EProperty.MIXED_TEAM);
	}
}
