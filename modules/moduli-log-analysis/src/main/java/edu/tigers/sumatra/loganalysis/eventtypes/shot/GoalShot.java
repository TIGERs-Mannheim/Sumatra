/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.eventtypes.shot;

import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.gamelog.proto.LogLabels;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.loganalysis.LogEventProtobufMapper;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;


public class GoalShot implements IShotEventType {
	/** When did the shot start */
	private final long startTimestamp;

	/** When did the shot end */
	private final long endTimestamp;

	/** Did the shot enter the goal? */
	private final boolean successful;

	/** Which robot took the shot? Which team did the shooter belong to */
	private final ITrackedBot shooterBot;


	private IVector2 endOfPass;             //the end of the pass line (is not always the receiverBot kicker pos)


	public GoalShot(ShotBuilder builder)
	{
		this.startTimestamp = builder.getStartFrame();
		this.endTimestamp = builder.getEndFrame();
		this.successful = builder.isSuccessful();
		this.shooterBot = builder.getPasserBot();
		this.endOfPass = builder.getEndOfPass();

	}


	public long getStartTimestamp()
	{
		return startTimestamp;
	}


	public long getEndTimestamp()
	{
		return endTimestamp;
	}


	public boolean isSuccessful()
	{
		return successful;
	}


	public ITrackedBot getShooterBot()
	{
		return shooterBot;
	}

	/**
	 * Creates a list of {@link IDrawableShape } from this goal shot object in order to draw it later
	 * @return list of shapes for goal shot
	 */
	@Override
	public List<IDrawableShape> getDrawableShotShape()
	{
		List<IDrawableShape> shapeList = new ArrayList<>();

		String timestampsString = "Start: " + startTimestamp + "\nEnd: " + endTimestamp;
		ILine passLine = null;
		Color colorPassLine;

		if(successful)
		{
			colorPassLine = Color.CYAN;
		}
		else
		{
			colorPassLine = Color.MAGENTA;
		}

		if(shooterBot != null && endOfPass != null)
		{
			passLine = Line.fromPoints(shooterBot.getBotKickerPos(), endOfPass);
		}

		// source bot that kicks the ball in this pass
		if (shooterBot != null)
		{
			shapeList.add(new DrawableCircle(Circle.createCircle(shooterBot.getPos(),
					Geometry.getBotRadius() + 5), shooterBot.getTeamColor().getColor()));

			shapeList.add(new DrawableAnnotation(shooterBot.getPos(), timestampsString, Color.BLACK));
		}


		if (shooterBot != null && passLine != null)
		{
			shapeList.add(new DrawableLine(passLine, colorPassLine));
		}

		return shapeList;
	}


	@Override
	public void addEventTypeTo(final LogLabels.Labels.Builder labelsBuilder, final int frameId)
	{
		LogEventProtobufMapper mapper = new LogEventProtobufMapper();
		labelsBuilder.addGoalShotLabels(mapper.mapGoalShot(this));
	}
}
