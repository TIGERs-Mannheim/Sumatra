/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 24, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Stage;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.TeamInfo;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.BangBangTrajectory2D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.ITrajectory1D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.TrajectoryGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.AresData;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.DrawablePath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.PlayStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawableBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawablePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawableText;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawableTrajectory;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Arc;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableArc;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.ellipse.DrawableEllipse;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.ellipse.Ellipse;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.rectangle.DrawableRectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.rectangle.Rectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.DrawableSpline;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.ISpline;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.triangle.DrawableTriangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.ares.Ares;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.Athena;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.Metis;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.KeeperPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.OffensivePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.SupportPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.DefensePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.PathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.IPath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.ERRTFinder;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree.DrawableTree;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree.Node;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.IPathFinder;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPathNode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles.CatchBallObstacle;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles.FieldBorderObstacle;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles.SimpleTimeAwareBallObstacle;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles.SimpleTimeAwareRobotObstacle;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles.TrajAwareRobotObstacle;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.spline.SplineGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.GenericSkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.WorldFrameFactory;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;


/**
 * Factory for creating complex frames ({@link AthenaAiFrame})
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class FrameFactory
{
	private final WorldFrameFactory	wfFactory	= new WorldFrameFactory();
	private final Metis					metis			= new Metis();
	private final Athena					athena		= new Athena();
	private final IPathFinder			pathFinder	= new ERRTFinder();
	private final SplineGenerator		splineGen	= new SplineGenerator(EBotType.TIGER_V3);
	private final Ares					ares			= new Ares(new GenericSkillSystem());
	
	private final Random					rnd			= new Random(SumatraClock.currentTimeMillis());
	
	
	/**
	 * Make a best attempt to create a full {@link AthenaAiFrame} with as much
	 * data as possible for optimal JUnit testing
	 * 
	 * @return
	 */
	public AIInfoFrame createFullAiInfoFrame()
	{
		final WorldFrame wf = wfFactory.createWorldFrame(0);
		final SSL_Referee sslRefereeMsg = createRefereeMsg(0, Command.DIRECT_FREE_BLUE, 1, 3, (short) 5);
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
		
		AresData aresData = ares.process(aFrame);
		for (int i = 0; i < 6; i++)
		{
			BotID botId = BotID.createBotId(i, ETeamColor.YELLOW);
			MovementCon moveCon = new MovementCon();
			moveCon.updateDestination(new Vector2(rndInField(), rndInField()));
			moveCon.updateTargetAngle(rnd.nextFloat() * AngleMath.PI);
			PathFinderInput input = new PathFinderInput(botId, moveCon);
			input.getFieldInfo().updateWorldFrame(wf);
			IPath path = pathFinder.calcPath(input);
			ISpline spline = splineGen.createSpline(wf.getBot(botId), path.getPathPoints(),
					path.getTargetOrientation(), 0);
			DrawablePath dPath = new DrawablePath();
			dPath.setPath(path);
			dPath.getPathDebugShapes().add(new DrawableSpline(spline, 0));
			dPath.getPathDebugShapes().add(new DrawableTree(new Node(AVector2.ZERO_VECTOR), Color.black));
			aresData.getLatestPaths().put(botId, dPath);
		}
		
		
		List<TrajPathNode> nodes = new ArrayList<>();
		TrackedTigerBot tBot = wf.getBot(BotID.createBotId(0, ETeamColor.YELLOW));
		BangBangTrajectory2D trajXY = TrajectoryGenerator.generatePositionTrajectory(tBot
				, AVector2.ZERO_VECTOR);
		ITrajectory1D trajW = TrajectoryGenerator.generateRotationTrajectory(tBot, 0, trajXY);
		nodes.add(new TrajPathNode(trajXY, trajW, 1, 0));
		TrajPath trajPath = new TrajPath(nodes, 0, System.nanoTime());
		
		aFrame.addDebugShape(new DrawableCircle(new Circle(new Vector2(rndInField(), rndInField()), rndPositive())));
		aFrame.addDebugShape(new DrawableEllipse(new Ellipse(new Vector2(rndInField(), rndInField()), rndPositive(),
				rndPositive())));
		aFrame.addDebugShape(new DrawableLine(new Line(new Vector2(rndInField(), rndInField()), new Vector2(rndInField(),
				rndInField()))));
		aFrame.addDebugShape(new DrawablePoint(new Vector2(rndInField(), rndInField())));
		aFrame.addDebugShape(new DrawableTree(new Node(AVector2.ZERO_VECTOR), Color.black));
		aFrame.addDebugShape(new DrawableRectangle(new Rectangle(new Vector2(500, 200), AVector2.ZERO_VECTOR),
				Color.magenta));
		aFrame.addDebugShape(new DrawableTriangle(new Vector2(100, 0), new Vector2(100, 200), new Vector2(300, 600)));
		aFrame.addDebugShape(new DrawableArc(new Arc(new Vector2(0, 0), 100, 0, 2), Color.red));
		aFrame.addDebugShape(new DrawableBot(new Vector2(0, 0), 0, Color.red));
		aFrame.addDebugShape(new DrawableText(new Vector2(0, 0), "blubb", Color.red));
		aFrame.addDebugShape(new DrawableTrajectory(new BangBangTrajectory2D(AVector2.ZERO_VECTOR, AVector2.ZERO_VECTOR,
				AVector2.ZERO_VECTOR, 2, 2, 2)));
		aFrame.addDebugShape(new CatchBallObstacle(wf.getBall(), 2, new DynamicPosition(AVector2.ZERO_VECTOR), 8));
		aFrame.addDebugShape(new SimpleTimeAwareBallObstacle(wf.getBall(), 100));
		aFrame.addDebugShape(new SimpleTimeAwareRobotObstacle(wf.getWorldFramePrediction().getBot(
				BotID.createBotId(0, ETeamColor.YELLOW)), 100));
		aFrame.addDebugShape(new FieldBorderObstacle(AIConfig.getGeometry().getFieldWBorders()));
		aFrame.addDebugShape(new TrajAwareRobotObstacle(trajPath, 100));
		
		aFrame.getTacticalField().getBallBuffer()
				.add(new TrackedBall(AVector3.ZERO_VECTOR, AVector3.ZERO_VECTOR, AVector3.ZERO_VECTOR, 0, true));
		
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
	
	
	/**
	 * @param wf
	 * @param playStrategy create with new PlayStrategy(new PlayStrategy.Builder())
	 * @return A instance of {@link WorldFrame} which is filled with everything to at least initialize fake-plays
	 */
	public AIInfoFrame createMinimalAiInfoFrame(final WorldFrame wf, final PlayStrategy playStrategy)
	{
		BaseAiFrame bFrame = new BaseAiFrame(wf, null, null, null, ETeamColor.YELLOW);
		TacticalField tacticalField = new TacticalField(wf);
		
		MetisAiFrame mFrame = new MetisAiFrame(bFrame, tacticalField);
		AthenaAiFrame aFrame = new AthenaAiFrame(mFrame, playStrategy);
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
	 * @return
	 */
	public SSL_Referee createRefereeMsg(final int id, final Command cmd, final int goalsBlue, final int goalsYellow,
			final int timeLeft)
	{
		TeamInfo.Builder teamBlueBuilder = TeamInfo.newBuilder();
		teamBlueBuilder.setGoalie(TeamConfig.getKeeperIdBlue());
		teamBlueBuilder.setName("Blue");
		teamBlueBuilder.setRedCards(1);
		teamBlueBuilder.setScore(goalsBlue);
		teamBlueBuilder.setTimeouts(4);
		teamBlueBuilder.setTimeoutTime(360);
		teamBlueBuilder.setYellowCards(3);
		
		TeamInfo.Builder teamYellowBuilder = TeamInfo.newBuilder();
		teamYellowBuilder.setGoalie(TeamConfig.getKeeperIdYellow());
		teamYellowBuilder.setName("Yellow");
		teamYellowBuilder.setRedCards(0);
		teamYellowBuilder.setScore(goalsYellow);
		teamYellowBuilder.setTimeouts(2);
		teamYellowBuilder.setTimeoutTime(65);
		teamYellowBuilder.setYellowCards(1);
		
		SSL_Referee.Builder builder = SSL_Referee.newBuilder();
		builder.setPacketTimestamp(SumatraClock.currentTimeMillis());
		builder.setBlue(teamBlueBuilder.build());
		builder.setYellow(teamYellowBuilder.build());
		builder.setCommand(cmd);
		builder.setCommandCounter(id);
		builder.setCommandTimestamp(SumatraClock.currentTimeMillis());
		builder.setStageTimeLeft(timeLeft);
		builder.setStage(Stage.NORMAL_FIRST_HALF);
		
		return builder.build();
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
