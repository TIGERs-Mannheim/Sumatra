/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 1, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.augm;

import java.awt.Color;

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
import edu.dhbw.mannheim.tigers.sumatra.proto.AugmWrapperProtos.Vector;
import edu.tigers.sumatra.ai.data.BotAiInformation;
import edu.tigers.sumatra.ai.data.frames.VisualizationFrame;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.referee.RefereeMsg;
import edu.tigers.sumatra.shapes.circle.ICircle;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class AugmentedDataTransformer
{
	private long lastRefMsgCounter = 0;
	
	
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
	public AugmWrapper createAugmWrapper(final VisualizationFrame frame)
	{
		AugmWrapper.Builder wrapperBuilder = AugmWrapper.newBuilder();
		
		wrapperBuilder.setTimestamp(frame.getWorldFrame().getTimestamp());
		
		// field
		Field.Builder fieldBuilder = Field.newBuilder();
		fieldBuilder.setAiColor(convColor(frame.getTeamColor()));
		fieldBuilder.setLength((int) Geometry.getFieldLength());
		fieldBuilder.setWidth((int) Geometry.getFieldWidth());
		wrapperBuilder.setField(fieldBuilder);
		
		// bots
		for (ITrackedBot bot : frame.getWorldFrame().getBots().values())
		{
			edu.dhbw.mannheim.tigers.sumatra.proto.AugmWrapperProtos.TrackedBot.Builder tBotBuilder = edu.dhbw.mannheim.tigers.sumatra.proto.AugmWrapperProtos.TrackedBot
					.newBuilder();
			Vector.Builder posBuilder = Vector.newBuilder();
			posBuilder.setX((float) bot.getPos().x());
			posBuilder.setY((float) bot.getPos().y());
			tBotBuilder.setPos(posBuilder);
			tBotBuilder.setOrient((float) bot.getAngle());
			tBotBuilder.setColor(convColor(bot.getBot().getColor()));
			tBotBuilder.setId(bot.getBotId().getNumber());
			BotAiInformation aiInfo = frame.getAiInfos().get(bot.getBotId());
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
		// for (List<IDrawableShape> entry : frame.getShapes().get(EShapesLayer.BALL_POSSESSION))
		// {
		// List<IDrawableShape> shapes = entry.getValue();
		// EShapesLayer layer = entry.getKey();
		// ShapeCollection.Builder scBuilder = ShapeCollection.newBuilder();
		// scBuilder.setIdentifier(layer.name());
		// for (IDrawableShape shape : shapes)
		// {
		// if (shape.getClass().equals(DrawableLine.class))
		// {
		// DrawableLine dLine = (DrawableLine) shape;
		// scBuilder.addLines(createLineBuilder(dLine, dLine.getColor()));
		// } else if (shape.getClass().equals(DrawableCircle.class))
		// {
		// DrawableCircle dCircle = (DrawableCircle) shape;
		// scBuilder.addCircles(createCircleBuilder(dCircle, dCircle.getColor()));
		// } else if (shape.getClass().equals(DrawablePoint.class))
		// {
		// DrawablePoint dPoint = (DrawablePoint) shape;
		// scBuilder.addPoints(createPointBuilder(dPoint, dPoint.getColor()));
		// }
		// // TODO triangle
		// }
		// wrapperBuilder.addShapeCollections(scBuilder);
		// }
		
		// referee messages
		createRefereeMessage(frame, wrapperBuilder);
		
		return wrapperBuilder.build();
	}
	
	
	private AugmWrapperProtos.Color convColor(final ETeamColor color)
	{
		return color == ETeamColor.YELLOW ? AugmWrapperProtos.Color.YELLOW
				: AugmWrapperProtos.Color.BLUE;
	}
	
	
	@SuppressWarnings("unused")
	private Line.Builder createLineBuilder(final ILine dLine, final Color color)
	{
		Line.Builder line = Line.newBuilder();
		Vector.Builder vStart = Vector.newBuilder();
		vStart.setX((float) dLine.supportVector().x());
		vStart.setY((float) dLine.supportVector().y());
		line.setVStart(vStart);
		
		Vector.Builder vEnd = Vector.newBuilder();
		vEnd.setX((float) dLine.supportVector().addNew(dLine.directionVector()).x());
		vEnd.setY((float) dLine.supportVector().addNew(dLine.directionVector()).y());
		line.setVEnd(vEnd);
		
		line.setColor(createRGBBuilder(color));
		
		return line;
	}
	
	
	@SuppressWarnings("unused")
	private Circle.Builder createCircleBuilder(final ICircle dCircle, final Color color)
	{
		Circle.Builder circle = Circle.newBuilder();
		Vector.Builder vCenter = Vector.newBuilder();
		vCenter.setX((float) dCircle.center().x());
		vCenter.setY((float) dCircle.center().y());
		circle.setVCenter(vCenter);
		
		circle.setRadius((float) dCircle.radius());
		circle.setColor(createRGBBuilder(color));
		
		return circle;
	}
	
	
	@SuppressWarnings("unused")
	private Point.Builder createPointBuilder(final DrawablePoint dPoint, final Color color)
	{
		Point.Builder point = Point.newBuilder();
		Vector.Builder vPoint = Vector.newBuilder();
		vPoint.setX((float) dPoint.x());
		vPoint.setY((float) dPoint.y());
		point.setVPoint(vPoint);
		point.setColor(createRGBBuilder(color));
		return point;
	}
	
	
	private Text.Builder createTextBuilder(final IVector3 point, final String text, final Color color)
	{
		Text.Builder textBuilder = Text.newBuilder();
		Vector.Builder vPoint = Vector.newBuilder();
		vPoint.setX((float) point.x());
		vPoint.setY((float) point.y());
		vPoint.setZ((float) point.z());
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
	
	
	private void createRefereeMessage(final VisualizationFrame frame, final AugmWrapper.Builder wrapperBuilder)
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
		refereeBuilder.setGameState("");
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
