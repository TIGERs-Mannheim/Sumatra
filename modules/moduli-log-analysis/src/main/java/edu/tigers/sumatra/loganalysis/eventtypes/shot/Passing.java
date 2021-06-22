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
import java.util.Optional;


public class Passing implements IShotEventType
{

	/** When did the pass start */
	private long startTimestamp;

	/** When did the pass end */
	private long endTimestamp;

	/** Did the pass reach a receiver of the same team */
	private boolean successful;

	/** bot that kicks the ball in this pass/Which team is shot */
	private ITrackedBot passerBot;

	/** real receiver of the pass */
	private ITrackedBot receiverBot;

	/** chip kick */
	private boolean isChipKick;

	/** fake receiver (receiver with is in pass line during the kick) */
	private ITrackedBot receiverBotAtKick;

	/** the end of the pass line (is not always the receiverBot kicker pos) */
	private IVector2 endOfPass;


	public Passing(ShotBuilder builder)
	{
		this.startTimestamp = builder.getStartFrame();
		this.endTimestamp = builder.getEndFrame();
		this.isChipKick = builder.isChipKick();
		this.successful = builder.isSuccessful();
		this.passerBot = builder.getPasserBot();
		this.endOfPass = builder.getEndOfPass();
		this.receiverBot = builder.getReceiverBot();
		this.receiverBotAtKick = builder.getReceiverBotAtKick();
	}


	public ITrackedBot getPasserBot()
	{
		return passerBot;
	}


	public Optional<ITrackedBot> getReceiverBot()
	{
		return Optional.ofNullable(receiverBot);
	}


	public Optional<ITrackedBot> getReceiverBotAtKick()
	{
		return Optional.ofNullable(receiverBotAtKick);
	}


	public long getStartTimestamp()
	{
		return startTimestamp;
	}


	public long getEndTimestamp()
	{
		return endTimestamp;
	}


	public boolean isChipKick()
	{
		return isChipKick;
	}


	public boolean isSuccessful()
	{
		return successful;
	}


	/**
	 * Creates a list of {@link IDrawableShape } from this Passing object in order to draw it later
	 *
	 * @return list of shapes for pass
	 */
	@Override
	public List<IDrawableShape> getDrawableShotShape()
	{
		List<IDrawableShape> shapeList = new ArrayList<>();

		String timestampsString = "Start: " + startTimestamp + "\nEnd: " + endTimestamp;
		ILine passLine = null;
		Color colorPassLine;

		if (successful)
		{
			colorPassLine = (isChipKick) ? Color.GREEN : Color.ORANGE;
		} else
		{
			colorPassLine = (isChipKick) ? Color.RED : Color.BLUE;
		}

		// source bot that kicks the ball in this pass
		if (passerBot != null)
		{
			shapeList.add(new DrawableCircle(Circle.createCircle(passerBot.getPos(),
					Geometry.getBotRadius() + 5), passerBot.getTeamColor().getColor()));

			shapeList.add(new DrawableAnnotation(passerBot.getPos(), timestampsString, Color.BLACK));
		}


		if (passerBot != null && endOfPass != null)
		{
			passLine = Line.fromPoints(passerBot.getBotKickerPos(), endOfPass);
			shapeList.add(new DrawableLine(passLine, colorPassLine));
		}

		// the real receiver of the pass
		if (receiverBot != null)
		{
			shapeList.add(new DrawableCircle(Circle.createCircle(receiverBot.getPos(),
					Geometry.getBotRadius() + 5), Color.RED));
		}

		// the fake receiver (receiver with is in pass line during the kick)
		if (receiverBotAtKick != null)
		{
			shapeList.add(new DrawableCircle(Circle.createCircle(receiverBotAtKick.getPos(),
					Geometry.getBotRadius() + 5), Color.GREEN));
		}

		return shapeList;
	}


	@Override
	public void addEventTypeTo(final LogLabels.Labels.Builder labelsBuilder, final int frameId)
	{
		LogEventProtobufMapper mapper = new LogEventProtobufMapper();
		if (getPasserBot() == null)
		{
			return;
		}
		labelsBuilder.addPassingLabels(mapper.mapPassing(this));
	}
}
