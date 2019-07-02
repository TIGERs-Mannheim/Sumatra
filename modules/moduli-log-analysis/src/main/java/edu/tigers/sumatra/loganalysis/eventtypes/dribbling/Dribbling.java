/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.eventtypes.dribbling;

import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.loganalysis.LogEventProtobufMapper;
import edu.tigers.sumatra.loganalysis.eventtypes.IInstantaneousEventType;
import edu.tigers.sumatra.labeler.LogLabels;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.ArrayList;
import java.util.List;


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
	
	
	public ITrackedBot getRobot()
	{
		return robot;
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
		
		if (isDribbling())
		{
			shapeList.add(new DrawableArrow(getRobot().getBotKickerPos(),
					Vector2.fromAngle(getRobot().getOrientation()).scaleTo(Geometry.getBallRadius()),
					getRobotTeam().getColor()));
		}
		
		boolean blueOrYellow = getRobotTeam() == ETeamColor.BLUE ||
				getRobotTeam() == ETeamColor.YELLOW;
		
		ITrackedBot bot = getRobot();
		
		if (blueOrYellow)
		{
			IVector2 ballCenter = bot.getBotKickerPos()
					.addNew(Vector2.fromAngle(bot.getOrientation()).scaleTo(Geometry.getBallRadius()));
			shapeList.add(new DrawableCircle(ballCenter, Geometry.getBallRadius(), bot.getTeamColor().getColor()));
		}
		
		return shapeList;
	}
	
	
	@Override
	public void addEventTypeTo(final LogLabels.Labels.Builder labelsBuilder)
	{
		LogEventProtobufMapper mapper = new LogEventProtobufMapper();
		labelsBuilder.addDribblingLabels(mapper.mapDribbling(this));
	}
}