/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 1, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.BotAiInformation;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.DrawablePath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawablePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.ICircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.ILine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.proto.AugmWrapperProtos;
import edu.dhbw.mannheim.tigers.sumatra.proto.AugmWrapperProtos.AugmWrapper;
import edu.dhbw.mannheim.tigers.sumatra.proto.AugmWrapperProtos.Circle;
import edu.dhbw.mannheim.tigers.sumatra.proto.AugmWrapperProtos.Field;
import edu.dhbw.mannheim.tigers.sumatra.proto.AugmWrapperProtos.Line;
import edu.dhbw.mannheim.tigers.sumatra.proto.AugmWrapperProtos.Point;
import edu.dhbw.mannheim.tigers.sumatra.proto.AugmWrapperProtos.RGB;
import edu.dhbw.mannheim.tigers.sumatra.proto.AugmWrapperProtos.Referee;
import edu.dhbw.mannheim.tigers.sumatra.proto.AugmWrapperProtos.RefereeTeam;
import edu.dhbw.mannheim.tigers.sumatra.proto.AugmWrapperProtos.ShapeCollection;
import edu.dhbw.mannheim.tigers.sumatra.proto.AugmWrapperProtos.Text;
import edu.dhbw.mannheim.tigers.sumatra.proto.AugmWrapperProtos.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.proto.AugmWrapperProtos.Vector;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.EDrawableShapesLayer;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class AugmentedDataTransformer
{
	private long	lastRefMsgCounter	= 0;
	
	
	private enum EShapeCollectionIds
	{
		ROLE_NAME,
		PATHS,
		REFEREE
	}
	
	
	/**
	 * @param frame
	 * @return
	 */
	public AugmWrapper createAugmWrapper(final IRecordFrame frame)
	{
		AugmWrapper.Builder wrapperBuilder = AugmWrapper.newBuilder();
		
		wrapperBuilder.setTimestamp(frame.getWorldFrame().getSystemTime().getTime());
		
		// field
		Field.Builder fieldBuilder = Field.newBuilder();
		fieldBuilder.setAiColor(convColor(frame.getTeamColor()));
		fieldBuilder.setLength((int) AIConfig.getGeometry().getFieldLength());
		fieldBuilder.setWidth((int) AIConfig.getGeometry().getFieldWidth());
		wrapperBuilder.setField(fieldBuilder);
		
		// bots
		for (TrackedTigerBot bot : frame.getWorldFrame().getBots().values())
		{
			TrackedBot.Builder tBotBuilder = TrackedBot.newBuilder();
			Vector.Builder posBuilder = Vector.newBuilder();
			posBuilder.setX(bot.getPos().x());
			posBuilder.setY(bot.getPos().y());
			tBotBuilder.setPos(posBuilder);
			tBotBuilder.setOrient(bot.getAngle());
			tBotBuilder.setColor(convColor(bot.getTeamColor()));
			tBotBuilder.setId(bot.getId().getNumber());
			BotAiInformation aiInfo = frame.getTacticalField().getBotAiInformation().get(bot.getId());
			if (aiInfo != null)
			{
				ShapeCollection.Builder scBuilder = ShapeCollection.newBuilder();
				scBuilder.setIdentifier(EShapeCollectionIds.ROLE_NAME.name());
				scBuilder.addTexts(createTextBuilder(new Vector3(bot.getPos(), 200), aiInfo.getRole(), Color.YELLOW));
				wrapperBuilder.addShapeCollections(scBuilder);
			}
			
			wrapperBuilder.addBots(tBotBuilder);
		}
		
		// all debug shapes (line, circle, point)
		for (Map.Entry<EDrawableShapesLayer, List<IDrawableShape>> entry : frame.getTacticalField().getDrawableShapes()
				.entrySet())
		{
			List<IDrawableShape> shapes = entry.getValue();
			EDrawableShapesLayer layer = entry.getKey();
			ShapeCollection.Builder scBuilder = ShapeCollection.newBuilder();
			scBuilder.setIdentifier(layer.name());
			for (IDrawableShape shape : shapes)
			{
				if (shape.getClass().equals(DrawableLine.class))
				{
					DrawableLine dLine = (DrawableLine) shape;
					scBuilder.addLines(createLineBuilder(dLine, dLine.getColor()));
				} else if (shape.getClass().equals(DrawableCircle.class))
				{
					DrawableCircle dCircle = (DrawableCircle) shape;
					scBuilder.addCircles(createCircleBuilder(dCircle, dCircle.getColor()));
				} else if (shape.getClass().equals(DrawablePoint.class))
				{
					DrawablePoint dPoint = (DrawablePoint) shape;
					scBuilder.addPoints(createPointBuilder(dPoint, dPoint.getColor()));
				}
				// TODO triangle
			}
			wrapperBuilder.addShapeCollections(scBuilder);
		}
		
		// paths
		for (Map.Entry<BotID, DrawablePath> entry : frame.getAresData().getPaths().entrySet())
		{
			TrackedTigerBot bot = frame.getWorldFrame().getBot(entry.getKey());
			if (bot == null)
			{
				continue;
			}
			if (entry.getValue().getPath() == null)
			{
				continue;
			}
			IVector2 pPre = bot.getPos();
			ShapeCollection.Builder scBuilder = ShapeCollection.newBuilder();
			scBuilder.setIdentifier(EShapeCollectionIds.PATHS.name());
			for (IVector2 p : entry.getValue().getPath().getPathPoints())
			{
				DrawablePoint point = new DrawablePoint(p);
				scBuilder.addPoints(createPointBuilder(point, Color.red));
				if (!pPre.equals(p, 4))
				{
					ILine line = edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line.newLine(pPre, p);
					Color color = Color.red;
					scBuilder.addLines(createLineBuilder(line, color));
					pPre = p;
				}
			}
			wrapperBuilder.addShapeCollections(scBuilder);
		}
		
		// referee shapes/marks
		createRefereeShapes(frame, wrapperBuilder);
		
		// referee messages
		createRefereeMessage(frame, wrapperBuilder);
		
		return wrapperBuilder.build();
	}
	
	
	private AugmWrapperProtos.Color convColor(final ETeamColor color)
	{
		return color == ETeamColor.YELLOW ? AugmWrapperProtos.Color.YELLOW
				: AugmWrapperProtos.Color.BLUE;
	}
	
	
	private Line.Builder createLineBuilder(final ILine dLine, final Color color)
	{
		Line.Builder line = Line.newBuilder();
		Vector.Builder vStart = Vector.newBuilder();
		vStart.setX(dLine.supportVector().x());
		vStart.setY(dLine.supportVector().y());
		line.setVStart(vStart);
		
		Vector.Builder vEnd = Vector.newBuilder();
		vEnd.setX(dLine.supportVector().addNew(dLine.directionVector()).x());
		vEnd.setY(dLine.supportVector().addNew(dLine.directionVector()).y());
		line.setVEnd(vEnd);
		
		line.setColor(createRGBBuilder(color));
		
		return line;
	}
	
	
	private Circle.Builder createCircleBuilder(final ICircle dCircle, final Color color)
	{
		Circle.Builder circle = Circle.newBuilder();
		Vector.Builder vCenter = Vector.newBuilder();
		vCenter.setX(dCircle.center().x());
		vCenter.setY(dCircle.center().y());
		circle.setVCenter(vCenter);
		
		circle.setRadius(dCircle.radius());
		circle.setColor(createRGBBuilder(color));
		
		return circle;
	}
	
	
	private Point.Builder createPointBuilder(final DrawablePoint dPoint, final Color color)
	{
		Point.Builder point = Point.newBuilder();
		Vector.Builder vPoint = Vector.newBuilder();
		vPoint.setX(dPoint.x());
		vPoint.setY(dPoint.y());
		point.setVPoint(vPoint);
		point.setColor(createRGBBuilder(color));
		return point;
	}
	
	
	private Text.Builder createTextBuilder(final IVector3 point, final String text, final Color color)
	{
		Text.Builder textBuilder = Text.newBuilder();
		Vector.Builder vPoint = Vector.newBuilder();
		vPoint.setX(point.x());
		vPoint.setY(point.y());
		vPoint.setZ(point.z());
		textBuilder.setVPoint(vPoint);
		textBuilder.setColor(createRGBBuilder(color));
		textBuilder.setText(text);
		return textBuilder;
	}
	
	
	private AugmWrapperProtos.RGB.Builder createRGBBuilder(final Color color)
	{
		AugmWrapperProtos.RGB.Builder rgb = RGB.newBuilder();
		rgb.setR(color.getRed());
		rgb.setG(color.getGreen());
		rgb.setB(color.getBlue());
		return rgb;
	}
	
	
	private void createRefereeShapes(final IRecordFrame frame, final AugmWrapper.Builder wrapperBuilder)
	{
		ShapeCollection.Builder scBuilder = ShapeCollection.newBuilder();
		scBuilder.setIdentifier(EShapeCollectionIds.REFEREE.name());
		
		IVector2 marker = null;
		float radius = 100;
		float fWidth = AIConfig.getGeometry().getFieldWidth();
		
		marker = frame.getTacticalField().getGameState().getRequiredBallPos(frame);
		
		switch (frame.getTacticalField().getGameState())
		{
			case PREPARE_KICKOFF_THEY:
			case PREPARE_KICKOFF_WE:
				radius = AIConfig.getGeometry().getCenterCircleRadius();
				break;
			case STOPPED:
				radius = AIConfig.getGeometry().getBotToBallDistanceStop();
				break;
			case PREPARE_PENALTY_THEY:
			{
				ILine line = edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line.newLine(AIConfig
						.getGeometry().getPenaltyLineOur().addNew(new Vector2(0, fWidth / 2)),
						AIConfig.getGeometry().getPenaltyLineOur().addNew(new Vector2(0, -fWidth / 2)));
				DrawableLine dLine = new DrawableLine(line);
				scBuilder.addLines(createLineBuilder(dLine, Color.red));
			}
				break;
			case PREPARE_PENALTY_WE:
			{
				ILine line = edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line.newLine(AIConfig
						.getGeometry().getPenaltyLineTheir().addNew(new Vector2(0, fWidth / 2)),
						AIConfig.getGeometry().getPenaltyLineTheir().addNew(new Vector2(0, -fWidth / 2)));
				DrawableLine dLine = new DrawableLine(line);
				scBuilder.addLines(createLineBuilder(dLine, Color.red));
			}
				break;
			default:
				break;
		}
		
		if (marker != null)
		{
			DrawableCircle circle = new DrawableCircle(
					new edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle(marker, radius));
			scBuilder.addCircles(createCircleBuilder(circle, Color.red));
			
			DrawablePoint point = new DrawablePoint(marker);
			point.setSize(2);
			scBuilder.addPoints(createPointBuilder(point, Color.red));
		}
		
		if (frame.getTacticalField().getBallLeftFieldPos() != null)
		{
			DrawablePoint point = new DrawablePoint(frame.getTacticalField().getBallLeftFieldPos());
			scBuilder.addPoints(createPointBuilder(point, Color.red));
		}
		
		wrapperBuilder.addShapeCollections(scBuilder);
	}
	
	
	private void createRefereeMessage(final IRecordFrame frame, final AugmWrapper.Builder wrapperBuilder)
	{
		RefereeMsg msg = frame.getLatestRefereeMsg();
		if (msg == null)
		{
			return;
		}
		boolean updated = false;
		if (msg.getCommandCounter() != lastRefMsgCounter)
		{
			lastRefMsgCounter = msg.getCommandCounter();
			updated = true;
		}
		
		Referee.Builder refereeBuilder = Referee.newBuilder();
		refereeBuilder.setCommand(msg.getCommand().name());
		refereeBuilder.setGameState(frame.getTacticalField().getGameState().name());
		refereeBuilder.setUpdated(updated);
		refereeBuilder.setTimeLeft((int) msg.getStageTimeLeft());
		refereeBuilder.setStage(msg.getStage().name());
		
		RefereeTeam.Builder yellowBuilder = RefereeTeam.newBuilder();
		yellowBuilder.setName(msg.getTeamInfoYellow().getName());
		yellowBuilder.setScore(msg.getTeamInfoYellow().getScore());
		yellowBuilder.setYellowCards(msg.getTeamInfoYellow().getYellowCards());
		yellowBuilder.setRedCards(msg.getTeamInfoYellow().getRedCards());
		yellowBuilder.setTimeouts(msg.getTeamInfoYellow().getTimeouts());
		yellowBuilder.setTimeoutTime(msg.getTeamInfoYellow().getTimeoutTime());
		refereeBuilder.setTeamYellow(yellowBuilder);
		
		RefereeTeam.Builder blueBuilder = RefereeTeam.newBuilder();
		blueBuilder.setName(msg.getTeamInfoBlue().getName());
		blueBuilder.setScore(msg.getTeamInfoBlue().getScore());
		blueBuilder.setYellowCards(msg.getTeamInfoBlue().getYellowCards());
		blueBuilder.setRedCards(msg.getTeamInfoBlue().getRedCards());
		blueBuilder.setTimeouts(msg.getTeamInfoBlue().getTimeouts());
		blueBuilder.setTimeoutTime(msg.getTeamInfoBlue().getTimeoutTime());
		refereeBuilder.setTeamBlue(blueBuilder);
		
		wrapperBuilder.setReferee(refereeBuilder);
	}
	
	
}
