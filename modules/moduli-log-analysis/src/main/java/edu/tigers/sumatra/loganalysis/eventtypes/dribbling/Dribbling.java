/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.eventtypes.dribbling;

import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.gamelog.proto.LogLabels;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.loganalysis.LogEventProtobufMapper;
import edu.tigers.sumatra.loganalysis.eventtypes.IInstantaneousEventType;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class Dribbling implements IInstantaneousEventType
{
	/** Is a robot dibbling in this frame */
	private final boolean isDribbling;

	/** Which robot is dribbling */
	private final ITrackedBot robot;

	/** Which team is dribbling */
	private final ETeamColor robotTeam;


	public Dribbling(final boolean isDribbling, final ITrackedBot robot, final ETeamColor robotTeam)
	{
		this.isDribbling = isDribbling;
		this.robot = robot;
		this.robotTeam = robotTeam;
	}


	public boolean isDribbling()
	{
		return isDribbling;
	}


	public Optional<ITrackedBot> getRobot()
	{
		return Optional.ofNullable(robot);
	}


	public ETeamColor getRobotTeam()
	{
		return robotTeam;
	}


	/**
	 * Creates a list of {@link IDrawableShape } from this dribbling object in order to draw it later
	 *
	 * @return list of shapes for dribbling
	 */
	public List<IDrawableShape> getDrawableShape()
	{
		List<IDrawableShape> shapeList = new ArrayList<>();

		Optional<ITrackedBot> bot = getRobot();

		if (isDribbling() && bot.isPresent())
		{
			shapeList.add(new DrawableArrow(bot.get().getBotKickerPos(),
					Vector2.fromAngle(bot.get().getOrientation()).scaleTo(Geometry.getBallRadius()),
					getRobotTeam().getColor()));
		}

		boolean blueOrYellow = getRobotTeam() == ETeamColor.BLUE ||
				getRobotTeam() == ETeamColor.YELLOW;

		if (blueOrYellow && bot.isPresent())
		{
			IVector2 ballCenter = bot.get().getBotKickerPos()
					.addNew(Vector2.fromAngle(bot.get().getOrientation()).scaleTo(Geometry.getBallRadius()));
			shapeList.add(new DrawableCircle(ballCenter, Geometry.getBallRadius(), bot.get().getTeamColor().getColor()));
		}

		return shapeList;
	}


	@Override
	public void addEventTypeTo(final LogLabels.Labels.Builder labelsBuilder, final int frameId)
	{
		LogEventProtobufMapper mapper = new LogEventProtobufMapper();
		labelsBuilder.addDribblingLabels(mapper.mapDribbling(this));
	}
}
