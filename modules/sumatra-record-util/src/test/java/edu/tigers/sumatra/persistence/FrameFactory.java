/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import edu.tigers.sumatra.ai.AIInfoFrame;
import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.ares.Ares;
import edu.tigers.sumatra.ai.ares.AresData;
import edu.tigers.sumatra.ai.athena.Athena;
import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.Metis;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.DrawableBot;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.skillsystem.GenericSkillSystem;
import edu.tigers.sumatra.wp.WorldFrameFactory;
import edu.tigers.sumatra.wp.data.WorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;

import java.awt.Color;
import java.util.Random;


/**
 * Factory for creating complex frames ({@link AthenaAiFrame})
 */
public class FrameFactory
{
	private final Metis metis = new Metis();
	private final Athena athena = new Athena();
	private final Ares ares = new Ares(GenericSkillSystem.forAnalysis());

	private final Random rnd = new Random(0);


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
		final AIInfoFrame preFrame = createMinimalAiInfoFrame(wfw, teamColor);
		final BaseAiFrame bFrame = new BaseAiFrame(wfw, true, preFrame,
				EAiTeam.primary(teamColor));

		final MetisAiFrame mFrame = metis.process(bFrame);
		final AthenaAiFrame aFrame = athena.process(mFrame);

		ares.process(aFrame, new AresData());

		aFrame.getShapeMap().get(EAiShapesLayer.AI_ROLE_COLOR)
				.add(new DrawableCircle(Circle.createCircle(Vector2.fromXY(rndInField(), rndInField()), rndPositive())));
		aFrame.getShapeMap().get(EAiShapesLayer.AI_ROLE_COLOR)
				.add(new DrawableLine(
						Lines.segmentFromOffset(Vector2.fromXY(rndInField(), rndInField()), Vector2.fromXY(rndInField(),
								rndInField()))));
		aFrame.getShapeMap().get(EAiShapesLayer.AI_ROLE_COLOR)
				.add(new DrawablePoint(Vector2.fromXY(rndInField(), rndInField())));
		aFrame.getShapeMap().get(EAiShapesLayer.AI_ROLE_COLOR)
				.add(new DrawableRectangle(Rectangle.fromPoints(Vector2.fromXY(500, 200), Vector2f.ZERO_VECTOR),
						Color.magenta));
		aFrame.getShapeMap().get(EAiShapesLayer.AI_ROLE_COLOR)
				.add(new DrawableTriangle(Vector2.fromXY(100, 0), Vector2.fromXY(100, 200), Vector2.fromXY(300, 600)));
		aFrame.getShapeMap().get(EAiShapesLayer.AI_ROLE_COLOR)
				.add(new DrawableArc(Arc.createArc(Vector2.fromXY(0, 0), 100, 0, 2), Color.red));
		aFrame.getShapeMap().get(EAiShapesLayer.AI_ROLE_COLOR)
				.add(new DrawableBot(Vector2.fromXY(0, 0), 0, Color.red, 90, 75));
		aFrame.getShapeMap().get(EAiShapesLayer.AI_ROLE_COLOR)
				.add(new DrawableAnnotation(Vector2.fromXY(0, 0), "blubb", Color.red));

		return AIInfoFrame.builder()
				.baseAiFrame(bFrame)
				.athenaAiFrame(aFrame)
				.aresData(new AresData())
				.build();
	}


	/**
	 * @param wfw
	 * @param teamColor
	 * @return A instance of {@link WorldFrame} which is filled with everything to at least initialize fake-plays
	 */
	private AIInfoFrame createMinimalAiInfoFrame(final WorldFrameWrapper wfw, final ETeamColor teamColor)
	{
		BaseAiFrame bFrame = new BaseAiFrame(wfw, false, null, EAiTeam.primary(teamColor));
		return AIInfoFrame.fromBaseAiFrame(bFrame);
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
