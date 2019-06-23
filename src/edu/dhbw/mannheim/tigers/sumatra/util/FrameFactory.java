/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 24, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Stage;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.TeamInfo;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.AresData;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.PlayStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawablePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.ellipse.DrawableEllipse;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.ellipse.Ellipse;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.ares.Ares;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.Athena;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamProps;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.Metis;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.DefensePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.KeeperPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.OffensivePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.SupportPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.IPathFinder;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.PathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.ERRTPlanner_WPC;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.GenericSkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.WorldFrameFactory;


/**
 * Factory for creating complex frames ({@link AthenaAiFrame})
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class FrameFactory
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final WorldFrameFactory	wfFactory	= new WorldFrameFactory();
	private final Metis					metis			= new Metis();
	private final Athena					athena		= new Athena();
	private final IPathFinder			pathFinder	= new ERRTPlanner_WPC();
	private final Ares					ares			= new Ares(new GenericSkillSystem());
	
	private final Random					rnd			= new Random(System.currentTimeMillis());
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Make a best attempt to create a full {@link AthenaAiFrame} with as much
	 * data as possible for optimal JUnit testing
	 * 
	 * @return
	 */
	public IRecordFrame createFullAiInfoFrame()
	{
		final WorldFrame wf = wfFactory.createWorldFrame(0);
		TeamProps teamProps = TeamConfig.getInstance().getTeamProps();
		if (teamProps == null)
		{
			teamProps = new TeamProps();
		}
		final SSL_Referee sslRefereeMsg = createRefereeMsg(0, Command.DIRECT_FREE_BLUE, 1, 3, (short) 5, teamProps);
		final RefereeMsg refereeMsg = new RefereeMsg(sslRefereeMsg, ETeamColor.YELLOW);
		
		final AIInfoFrame preFrame = createMinimalAiInfoFrame(wf);
		final BaseAiFrame bFrame = new BaseAiFrame(wf, refereeMsg, refereeMsg, preFrame,
				ETeamColor.YELLOW);
		
		PlayStrategy.Builder psb = new PlayStrategy.Builder();
		psb.getActivePlays().add(new OffensivePlay());
		psb.getActivePlays().add(new DefensePlay());
		psb.getActivePlays().add(new SupportPlay());
		psb.getActivePlays().add(new KeeperPlay());
		
		final MetisAiFrame mFrame = metis.process(bFrame);
		final AthenaAiFrame aFrame = athena.process(mFrame, psb);
		
		List<Path> paths = new ArrayList<Path>(6);
		for (int i = 0; i < 6; i++)
		{
			BotID botId = BotID.createBotId(i, ETeamColor.YELLOW);
			MovementCon moveCon = new MovementCon();
			moveCon.updateDestination(new Vector2(rndInField(), rndInField()));
			moveCon.updateTargetAngle(rnd.nextFloat() * AngleMath.PI);
			Map<BotID, Path> existingPathes = new HashMap<BotID, Path>();
			PathFinderInput input = new PathFinderInput(botId, existingPathes, 0, moveCon);
			input.update(wf);
			Path path = pathFinder.calcPath(input);
			paths.add(path);
		}
		
		aFrame.addDebugShape(new DrawableCircle(new Circle(new Vector2(rndInField(), rndInField()), rndPositive())));
		aFrame.addDebugShape(new DrawableEllipse(new Ellipse(new Vector2(rndInField(), rndInField()), rndPositive(),
				rndPositive())));
		aFrame.addDebugShape(new DrawableLine(new Line(new Vector2(rndInField(), rndInField()), new Vector2(rndInField(),
				rndInField()))));
		aFrame.addDebugShape(new DrawablePoint(new Vector2(rndInField(), rndInField())));
		
		
		AresData aresData = ares.process(aFrame);
		
		return new AIInfoFrame(aFrame, aresData, 60);
	}
	
	
	/**
	 * @param wf
	 * @return A instance of {@link WorldFrame} which is filled with everything to at least initialize fake-plays
	 */
	public AIInfoFrame createMinimalAiInfoFrame(final WorldFrame wf)
	{
		BaseAiFrame bFrame = new BaseAiFrame(wf, null, null, null, ETeamColor.YELLOW);
		TacticalField tacticalField = new TacticalField(wf);
		
		MetisAiFrame mFrame = new MetisAiFrame(bFrame, tacticalField);
		AthenaAiFrame aFrame = new AthenaAiFrame(mFrame, new PlayStrategy(new PlayStrategy.Builder()));
		return new AIInfoFrame(aFrame, new AresData(), 60);
	}
	
	
	private int rndInField()
	{
		return rnd.nextInt(4000) - 2000;
	}
	
	
	private int rndPositive()
	{
		return rnd.nextInt(1000) + 1;
	}
	
	
	/**
	 * @param id
	 * @param cmd
	 * @param goalsBlue
	 * @param goalsYellow
	 * @param timeLeft
	 * @param newTeamProps
	 * @return
	 */
	public SSL_Referee createRefereeMsg(final int id, final Command cmd, final int goalsBlue, final int goalsYellow,
			final int timeLeft,
			final TeamProps newTeamProps)
	{
		TeamInfo.Builder teamBlueBuilder = TeamInfo.newBuilder();
		teamBlueBuilder.setGoalie(newTeamProps.getKeeperIdBlue());
		teamBlueBuilder.setName("Blue");
		teamBlueBuilder.setRedCards(1);
		teamBlueBuilder.setScore(goalsBlue);
		teamBlueBuilder.setTimeouts(4);
		teamBlueBuilder.setTimeoutTime(360);
		teamBlueBuilder.setYellowCards(3);
		
		TeamInfo.Builder teamYellowBuilder = TeamInfo.newBuilder();
		teamYellowBuilder.setGoalie(newTeamProps.getKeeperIdYellow());
		teamYellowBuilder.setName("Yellow");
		teamYellowBuilder.setRedCards(0);
		teamYellowBuilder.setScore(goalsYellow);
		teamYellowBuilder.setTimeouts(2);
		teamYellowBuilder.setTimeoutTime(65);
		teamYellowBuilder.setYellowCards(1);
		
		SSL_Referee.Builder builder = SSL_Referee.newBuilder();
		builder.setPacketTimestamp(System.currentTimeMillis());
		builder.setBlue(teamBlueBuilder.build());
		builder.setYellow(teamYellowBuilder.build());
		builder.setCommand(cmd);
		builder.setCommandCounter(id);
		builder.setCommandTimestamp(System.currentTimeMillis());
		builder.setStageTimeLeft(timeLeft);
		builder.setStage(Stage.NORMAL_FIRST_HALF);
		
		return builder.build();
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
