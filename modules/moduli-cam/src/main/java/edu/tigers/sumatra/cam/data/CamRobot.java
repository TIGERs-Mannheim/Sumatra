/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.cam.data;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


/**
 * Simple data holder; internal data structure for the SSL_DetectionRobot from
 * protobuf-protocol, coming from SSL-Vision.
 */
@Getter
public class CamRobot extends ACamObject
{
	private final BotID botId;
	private final IVector2 pos;
	private final double orientation;
	private final double height;


	/**
	 * Dummy constructor for persistence
	 */
	@SuppressWarnings("unused") // required by @Persistent
	private CamRobot()
	{
		super();
		botId = BotID.noBot();
		pos = Vector2f.ZERO_VECTOR;
		orientation = 0;
		height = 0;
	}


	@SuppressWarnings("squid:S00107") // number of parameters vs. performance
	public CamRobot(
			final double confidence,
			final IVector2 pixel,
			final long tCapture,
			final int camId,
			final long globalFrameId,
			final IVector2 pos,
			final double orientation,
			final double height,
			final BotID botId
	)
	{
		super(confidence, pixel, tCapture, camId, globalFrameId);
		this.pos = pos.copy();
		this.orientation = orientation;
		this.height = height;
		this.botId = botId;
	}


	@Override
	public String toString()
	{
		// noinspection StringBufferReplaceableByString
		StringBuilder builder = new StringBuilder();
		builder.append("SSLRobot [confidence=");
		builder.append(getConfidence());
		builder.append(", height=");
		builder.append(getHeight());
		builder.append(", orientation=");
		builder.append(getOrientation());
		builder.append(", pixelX=");
		builder.append(getPixel().x());
		builder.append(", pixelY=");
		builder.append(getPixel().y());
		builder.append(", robotID=");
		builder.append(getRobotID());
		builder.append(", x=");
		builder.append(getPos().x());
		builder.append(", y=");
		builder.append(getPos().y());
		builder.append("]");
		return builder.toString();
	}


	public int getRobotID()
	{
		return botId.getNumber();
	}


	@Override
	public IVector2 getPos()
	{
		return pos;
	}


	@Override
	public List<Number> getNumberList()
	{
		List<Number> numbers = super.getNumberList();
		numbers.add(getOrientation());
		numbers.add(getRobotID());
		numbers.addAll(botId.getTeamColor().getNumberList());
		numbers.add(getHeight());
		return numbers;
	}


	@Override
	public List<String> getHeaders()
	{
		List<String> headers = new ArrayList<>(super.getHeaders());
		headers.add("orientation");
		headers.add("robotId");
		headers.add("robotColor");
		headers.add("height");
		return headers;
	}
}
