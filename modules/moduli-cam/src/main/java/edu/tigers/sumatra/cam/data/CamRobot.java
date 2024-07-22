/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.cam.data;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * Simple data holder; internal data structure for the
 * SSL_DetectionRobot from
 * protobuf-protocol, coming from
 * the SSL-Vision.
 *
 * @author Gero
 */
@Persistent
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


	/**
	 * <p>
	 * <i>(Being aware of EJ-SE Item 2; but we prefer performance over readability - at least in this case. Objects are
	 * created at only one point in the system, but needs to be fast.</i>
	 * </p>
	 *
	 * @param confidence
	 * @param pixel
	 * @param tCapture
	 * @param camId
	 * @param frameId
	 * @param pos
	 * @param orientation
	 * @param height
	 * @param botId
	 */
	@SuppressWarnings("squid:S00107") // number of parameters vs. performance
	public CamRobot(final double confidence,
			final IVector2 pixel,
			final long tCapture,
			final Long tCaptureCamera,
			final long tSent,
			final int camId,
			final long frameId,
			final IVector2 pos,
			final double orientation,
			final double height,
			final BotID botId)
	{
		super(confidence, pixel, tCapture, tCaptureCamera, tSent, camId, frameId);
		this.pos = pos.copy();
		this.orientation = orientation;
		this.height = height;
		this.botId = botId;
	}


	/**
	 * New CamRobot with adjusted tCapture timestamp.
	 *
	 * @param orig
	 * @param tCapture
	 */
	public CamRobot(final CamRobot orig, final long tCapture)
	{
		super(orig.getConfidence(), orig.getPixel(), tCapture, orig.getTCaptureCamera(), orig.getTSent(),
				orig.getCameraId(), orig.getFrameId());
		pos = orig.pos.copy();
		orientation = orig.orientation;
		height = orig.height;
		botId = orig.botId;
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


	/**
	 * @return the robotID
	 */
	public int getRobotID()
	{
		return botId.getNumber();
	}


	/**
	 * @return the pos
	 */
	@Override
	public IVector2 getPos()
	{
		return pos;
	}


	/**
	 * @return the orientation
	 */
	public double getOrientation()
	{
		return orientation;
	}


	/**
	 * @return the height
	 */
	public double getHeight()
	{
		return height;
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


	/**
	 * @return the botId
	 */
	public BotID getBotId()
	{
		return botId;
	}
}
