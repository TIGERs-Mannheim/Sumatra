/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai;

import java.awt.Color;
import java.util.Random;

import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ai.ares.Ares;
import edu.tigers.sumatra.ai.athena.Athena;
import edu.tigers.sumatra.ai.data.AresData;
import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.MultiTeamMessage;
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
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.DrawableBot;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableEllipse;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.drawable.DrawableTrajectory2D;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.ellipse.Ellipse;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.MovementCon;
import edu.tigers.sumatra.pathfinder.obstacles.FieldBorderObstacle;
import edu.tigers.sumatra.pathfinder.obstacles.SimpleTimeAwareBallObstacle;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.referee.data.RefereeMsgBuilder;
import edu.tigers.sumatra.skillsystem.GenericSkillSystem;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2D;
import edu.tigers.sumatra.wp.WorldFrameFactory;
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
	
	private final Random rnd = new Random(0);
	
	
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
		Referee.SSL_Referee sslRefereeMsg = RefereeMsgBuilder.aRefereeMsg()
				.withCommand(Command.DIRECT_FREE_BLUE)
				.withCommandCounter(0)
				.build();
		final RefereeMsg refereeMsg = new RefereeMsg(timestamp, sslRefereeMsg);
		final WorldFrameWrapper wfw = new WorldFrameWrapper(WorldFrameFactory.createSimpleWorldFrame(frameId, timestamp),
				refereeMsg, GameState.RUNNING);
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
		final WorldFrame wf = wfw.getWorldFrame(EAiTeam.primary(teamColor));
		
		final AIInfoFrame preFrame = createMinimalAiInfoFrame(wfw, teamColor);
		final BaseAiFrame bFrame = new BaseAiFrame(wfw, true, preFrame,
				EAiTeam.primary(teamColor), MultiTeamMessage.DEFAULT);
		
		PlayStrategy.Builder psb = new PlayStrategy.Builder();
		psb.getActivePlays().add(new OffensivePlay());
		psb.getActivePlays().add(new DefensePlay());
		psb.getActivePlays().add(new SupportPlay());
		psb.getActivePlays().add(new KeeperPlay());
		
		final MetisAiFrame mFrame = metis.process(bFrame);
		final AthenaAiFrame aFrame = athena.process(mFrame, psb);
		
		ares.process(aFrame, new AresData());
		for (int i = 0; i < 6; i++)
		{
			MovementCon moveCon = new MovementCon();
			moveCon.updateDestination(Vector2.fromXY(rndInField(), rndInField()));
			moveCon.updateTargetAngle(rnd.nextDouble() * AngleMath.PI);
		}
		
		aFrame.getTacticalField().getDrawableShapes().get(EAiShapesLayer.UNSORTED)
				.add(new DrawableCircle(Circle.createCircle(Vector2.fromXY(rndInField(), rndInField()), rndPositive())));
		aFrame.getTacticalField().getDrawableShapes().get(EAiShapesLayer.UNSORTED)
				.add(new DrawableEllipse(Ellipse.createEllipse(Vector2.fromXY(rndInField(), rndInField()), rndPositive(),
						rndPositive())));
		aFrame.getTacticalField().getDrawableShapes().get(EAiShapesLayer.UNSORTED)
				.add(new DrawableLine(
						Line.fromDirection(Vector2.fromXY(rndInField(), rndInField()), Vector2.fromXY(rndInField(),
								rndInField()))));
		aFrame.getTacticalField().getDrawableShapes().get(EAiShapesLayer.UNSORTED)
				.add(new DrawablePoint(Vector2.fromXY(rndInField(), rndInField())));
		aFrame.getTacticalField().getDrawableShapes().get(EAiShapesLayer.UNSORTED)
				.add(new DrawableRectangle(Rectangle.fromPoints(Vector2.fromXY(500, 200), AVector2.ZERO_VECTOR),
						Color.magenta));
		aFrame.getTacticalField().getDrawableShapes().get(EAiShapesLayer.UNSORTED)
				.add(new DrawableTriangle(Vector2.fromXY(100, 0), Vector2.fromXY(100, 200), Vector2.fromXY(300, 600)));
		aFrame.getTacticalField().getDrawableShapes().get(EAiShapesLayer.UNSORTED)
				.add(new DrawableArc(Arc.createArc(Vector2.fromXY(0, 0), 100, 0, 2), Color.red));
		aFrame.getTacticalField().getDrawableShapes().get(EAiShapesLayer.UNSORTED)
				.add(new DrawableBot(Vector2.fromXY(0, 0), 0, Color.red, 90, 75));
		aFrame.getTacticalField().getDrawableShapes().get(EAiShapesLayer.UNSORTED)
				.add(new DrawableAnnotation(Vector2.fromXY(0, 0), "blubb", Color.red));
		aFrame.getTacticalField().getDrawableShapes().get(EAiShapesLayer.UNSORTED)
				.add(new DrawableTrajectory2D(new BangBangTrajectory2D(AVector2.ZERO_VECTOR, AVector2.ZERO_VECTOR,
						AVector2.ZERO_VECTOR, 2, 2)));
		aFrame.getTacticalField().getDrawableShapes().get(EAiShapesLayer.UNSORTED)
				.add(new SimpleTimeAwareBallObstacle(wf.getBall(), 100));
		aFrame.getTacticalField().getDrawableShapes().get(EAiShapesLayer.UNSORTED)
				.add(new FieldBorderObstacle(Geometry.getFieldWBorders()));
		
		return new AIInfoFrame(aFrame, new AresData());
	}
	
	
	/**
	 * @param wfw
	 * @param teamColor
	 * @return A instance of {@link WorldFrame} which is filled with everything to at least initialize fake-plays
	 */
	private AIInfoFrame createMinimalAiInfoFrame(final WorldFrameWrapper wfw, final ETeamColor teamColor)
	{
		BaseAiFrame bFrame = new BaseAiFrame(wfw, false, null, EAiTeam.primary(teamColor), MultiTeamMessage.DEFAULT);
		TacticalField tacticalField = new TacticalField();
		
		MetisAiFrame mFrame = new MetisAiFrame(bFrame, tacticalField);
		AthenaAiFrame aFrame = new AthenaAiFrame(mFrame, new PlayStrategy(new PlayStrategy.Builder()));
		return new AIInfoFrame(aFrame, new AresData());
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
		BaseAiFrame bFrame = new BaseAiFrame(wfw, false, null, EAiTeam.primary(teamColor), MultiTeamMessage.DEFAULT);
		TacticalField tacticalField = new TacticalField();
		
		MetisAiFrame mFrame = new MetisAiFrame(bFrame, tacticalField);
		AthenaAiFrame aFrame = new AthenaAiFrame(mFrame, playStrategy);
		return new AIInfoFrame(aFrame, new AresData());
	}
	
	
	/**
	 * @param frameId
	 * @param timestamp
	 * @return
	 */
	public WorldFrameWrapper createWorldFrameWrapper(final long frameId, final long timestamp)
	{
		return new WorldFrameWrapper(WorldFrameFactory.createSimpleWorldFrame(frameId, timestamp),
				new RefereeMsg(), GameState.RUNNING);
	}
	
	
	private int rndInField()
	{
		return rnd.nextInt(4000) - 2000;
	}
	
	
	private int rndPositive()
	{
		return rnd.nextInt(1000) + 1;
	}
	
}
