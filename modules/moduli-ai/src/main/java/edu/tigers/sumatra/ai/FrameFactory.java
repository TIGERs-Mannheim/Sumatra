/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 24, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ai.ares.Ares;
import edu.tigers.sumatra.ai.athena.Athena;
import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.PlayStrategy;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.AIInfoFrame;
import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.metis.Metis;
import edu.tigers.sumatra.ai.pandora.plays.KeeperPlay;
import edu.tigers.sumatra.ai.pandora.plays.OffensivePlay;
import edu.tigers.sumatra.ai.pandora.plays.SupportPlay;
import edu.tigers.sumatra.ai.pandora.plays.defense.DefensePlay;
import edu.tigers.sumatra.ai.sisyphus.TrajectoryGenerator;
import edu.tigers.sumatra.ai.sisyphus.errt.tree.DrawableTree;
import edu.tigers.sumatra.ai.sisyphus.errt.tree.Node;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.TrajPathNode;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles.CatchBallObstacle;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles.FieldBorderObstacle;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles.SimpleTimeAwareBallObstacle;
import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.DrawableBot;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableEllipse;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.drawable.DrawableText;
import edu.tigers.sumatra.drawable.DrawableTrajectory2D;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.referee.RefereeHandler;
import edu.tigers.sumatra.referee.RefereeMsg;
import edu.tigers.sumatra.shapes.circle.Arc;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.shapes.ellipse.Ellipse;
import edu.tigers.sumatra.shapes.rectangle.Rectangle;
import edu.tigers.sumatra.skillsystem.GenericSkillSystem;
import edu.tigers.sumatra.skillsystem.MovementCon;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2D;
import edu.tigers.sumatra.wp.WorldFrameFactory;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.ShapeMap;
import edu.tigers.sumatra.wp.data.WorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * Factory for creating complex frames ({@link AthenaAiFrame})
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class FrameFactory
{
	private final Metis	metis		= new Metis();
	private final Athena	athena	= new Athena();
	private final Ares	ares		= new Ares(new GenericSkillSystem());
	
	private final Random	rnd		= new Random(System.currentTimeMillis());
	
	
	/**
	 * Make a best attempt to create a full {@link AthenaAiFrame} with as much
	 * data as possible for optimal JUnit testing
	 * 
	 * @param teamColor
	 * @param frameId
	 * @param timestamp
	 * @return
	 */
	public AIInfoFrame createFullAiInfoFrame(final ETeamColor teamColor, final long frameId, final long timestamp)
	{
		final RefereeMsg refereeMsg = RefereeHandler.createRefereeMsg(0, Command.DIRECT_FREE_BLUE, 1, 3, (short) 5, 0,
				timestamp, null);
		final WorldFrameWrapper wfw = new WorldFrameWrapper(WorldFrameFactory.createSimpleWorldFrame(frameId, timestamp),
				refereeMsg, new ShapeMap());
		return createFullAiInfoFrame(teamColor, wfw);
	}
	
	
	/**
	 * Make a best attempt to create a full {@link AthenaAiFrame} with as much
	 * data as possible for optimal JUnit testing
	 * 
	 * @param teamColor
	 * @param wfw
	 * @return
	 */
	public AIInfoFrame createFullAiInfoFrame(final ETeamColor teamColor, final WorldFrameWrapper wfw)
	{
		final WorldFrame wf = wfw.getWorldFrame(teamColor);
		
		final AIInfoFrame preFrame = createMinimalAiInfoFrame(wfw, teamColor);
		final BaseAiFrame bFrame = new BaseAiFrame(wfw, true, preFrame,
				teamColor);
		
		PlayStrategy.Builder psb = new PlayStrategy.Builder();
		psb.getActivePlays().add(new OffensivePlay());
		psb.getActivePlays().add(new DefensePlay());
		psb.getActivePlays().add(new SupportPlay());
		psb.getActivePlays().add(new KeeperPlay());
		
		final MetisAiFrame mFrame = metis.process(bFrame);
		final AthenaAiFrame aFrame = athena.process(mFrame, psb);
		
		ares.process(aFrame);
		for (int i = 0; i < 6; i++)
		{
			BotID botId = BotID.createBotId(i, teamColor);
			ITrackedBot tBot = wf.getBot(botId);
			assert tBot != null;
			MovementCon moveCon = new MovementCon();
			moveCon.updateDestination(new Vector2(rndInField(), rndInField()));
			moveCon.updateTargetAngle(rnd.nextDouble() * AngleMath.PI);
		}
		
		
		List<TrajPathNode> nodes = new ArrayList<>();
		ITrackedBot tBot = wf.getBot(BotID.createBotId(0, teamColor));
		BangBangTrajectory2D trajXY = new TrajectoryGenerator().generatePositionTrajectory(tBot, AVector2.ZERO_VECTOR);
		nodes.add(new TrajPathNode(trajXY, 1, 0));
		
		aFrame.getTacticalField().getDrawableShapes().get(EShapesLayer.UNSORTED)
				.add(new DrawableCircle(new Circle(new Vector2(rndInField(), rndInField()), rndPositive())));
		aFrame.getTacticalField().getDrawableShapes().get(EShapesLayer.UNSORTED)
				.add(new DrawableEllipse(new Ellipse(new Vector2(rndInField(), rndInField()), rndPositive(),
						rndPositive())));
		aFrame.getTacticalField().getDrawableShapes().get(EShapesLayer.UNSORTED)
				.add(new DrawableLine(new Line(new Vector2(rndInField(), rndInField()), new Vector2(rndInField(),
						rndInField()))));
		aFrame.getTacticalField().getDrawableShapes().get(EShapesLayer.UNSORTED)
				.add(new DrawablePoint(new Vector2(rndInField(), rndInField())));
		aFrame.getTacticalField().getDrawableShapes().get(EShapesLayer.UNSORTED)
				.add(new DrawableTree(new Node(AVector2.ZERO_VECTOR), Color.black));
		aFrame.getTacticalField().getDrawableShapes().get(EShapesLayer.UNSORTED)
				.add(new DrawableRectangle(new Rectangle(new Vector2(500, 200), AVector2.ZERO_VECTOR),
						Color.magenta));
		aFrame.getTacticalField().getDrawableShapes().get(EShapesLayer.UNSORTED)
				.add(new DrawableTriangle(new Vector2(100, 0), new Vector2(100, 200), new Vector2(300, 600)));
		aFrame.getTacticalField().getDrawableShapes().get(EShapesLayer.UNSORTED)
				.add(new DrawableArc(new Arc(new Vector2(0, 0), 100, 0, 2), Color.red));
		aFrame.getTacticalField().getDrawableShapes().get(EShapesLayer.UNSORTED)
				.add(new DrawableBot(new Vector2(0, 0), 0, Color.red, 90, 75));
		aFrame.getTacticalField().getDrawableShapes().get(EShapesLayer.UNSORTED)
				.add(new DrawableText(new Vector2(0, 0), "blubb", Color.red));
		aFrame.getTacticalField().getDrawableShapes().get(EShapesLayer.UNSORTED)
				.add(new DrawableTrajectory2D(new BangBangTrajectory2D(AVector2.ZERO_VECTOR, AVector2.ZERO_VECTOR,
						AVector2.ZERO_VECTOR, 2, 2, 2)));
		aFrame.getTacticalField().getDrawableShapes().get(EShapesLayer.UNSORTED)
				.add(new CatchBallObstacle(wf.getBall(), 2, new DynamicPosition(AVector2.ZERO_VECTOR), 8));
		aFrame.getTacticalField().getDrawableShapes().get(EShapesLayer.UNSORTED)
				.add(new SimpleTimeAwareBallObstacle(wf.getBall(), 100));
		aFrame.getTacticalField().getDrawableShapes().get(EShapesLayer.UNSORTED)
				.add(new FieldBorderObstacle(Geometry.getFieldWBorders()));
		
		return new AIInfoFrame(aFrame);
	}
	
	
	/**
	 * @param wfw
	 * @param teamColor
	 * @return A instance of {@link WorldFrame} which is filled with everything to at least initialize fake-plays
	 */
	public AIInfoFrame createMinimalAiInfoFrame(final WorldFrameWrapper wfw, final ETeamColor teamColor)
	{
		BaseAiFrame bFrame = new BaseAiFrame(wfw, false, null, teamColor);
		TacticalField tacticalField = new TacticalField();
		
		MetisAiFrame mFrame = new MetisAiFrame(bFrame, tacticalField);
		AthenaAiFrame aFrame = new AthenaAiFrame(mFrame, new PlayStrategy(new PlayStrategy.Builder()));
		return new AIInfoFrame(aFrame);
	}
	
	
	/**
	 * @param wfw
	 * @param playStrategy create with new PlayStrategy(new PlayStrategy.Builder())
	 * @param teamColor
	 * @return A instance of {@link WorldFrame} which is filled with everything to at least initialize fake-plays
	 */
	public AIInfoFrame createMinimalAiInfoFrame(final WorldFrameWrapper wfw, final PlayStrategy playStrategy,
			final ETeamColor teamColor)
	{
		BaseAiFrame bFrame = new BaseAiFrame(wfw, false, null, teamColor);
		TacticalField tacticalField = new TacticalField();
		
		MetisAiFrame mFrame = new MetisAiFrame(bFrame, tacticalField);
		AthenaAiFrame aFrame = new AthenaAiFrame(mFrame, playStrategy);
		return new AIInfoFrame(aFrame);
	}
	
	
	/**
	 * @param frameId
	 * @param timestamp
	 * @return
	 */
	public WorldFrameWrapper createWorldFrameWrapper(final long frameId, final long timestamp)
	{
		WorldFrameWrapper wfw = new WorldFrameWrapper(WorldFrameFactory.createSimpleWorldFrame(frameId, timestamp),
				new RefereeMsg(), new ShapeMap());
		return wfw;
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
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public void close()
	{
		metis.stop();
	}
}
