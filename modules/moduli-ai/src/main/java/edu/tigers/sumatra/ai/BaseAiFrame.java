/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai;

import edu.tigers.sumatra.botmanager.data.MultimediaControl;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.IShapeLayerIdentifier;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The base frame contains only the basic frame data without any AI results
 */
@Value
public class BaseAiFrame
{
	WorldFrameWrapper worldFrameWrapper;
	boolean newRefereeMsg;
	EAiTeam aiTeam;

	WorldFrame worldFrame;
	RefereeMsg refereeMsg;
	GameState gameState;

	// mutable fields
	ShapeMap shapeMap = new ShapeMap();
	Map<BotID, MultimediaControl> multimediaControl = new HashMap<>();

	/**
	 * previous frame
	 */
	@NonFinal
	AIInfoFrame prevFrame;


	/**
	 * @param worldFrameWrapper
	 * @param newRefereeMsg
	 * @param prevFrame
	 * @param aiTeam
	 */
	public BaseAiFrame(final WorldFrameWrapper worldFrameWrapper, final boolean newRefereeMsg,
			final AIInfoFrame prevFrame, final EAiTeam aiTeam)
	{
		this.worldFrameWrapper = worldFrameWrapper;
		this.prevFrame = prevFrame;
		this.aiTeam = aiTeam;
		this.newRefereeMsg = newRefereeMsg;

		worldFrame = worldFrameWrapper.getWorldFrame(aiTeam);
		refereeMsg = worldFrameWrapper.getRefereeMsg();
		gameState = worldFrameWrapper.getGameState().toBuilder()
				.withOurTeam(aiTeam.getTeamColor())
				.build();
	}


	/**
	 * @param original
	 */
	public BaseAiFrame(final BaseAiFrame original)
	{
		worldFrameWrapper = original.worldFrameWrapper;
		prevFrame = original.prevFrame;
		aiTeam = original.aiTeam;
		newRefereeMsg = original.newRefereeMsg;

		worldFrame = worldFrameWrapper.getWorldFrame(aiTeam);
		refereeMsg = original.refereeMsg;
		gameState = original.gameState;
		shapeMap.addAll(original.shapeMap);
	}


	/**
	 * clear prevFrame to avoid memory leak
	 */
	public void cleanUp()
	{
		prevFrame = null;
	}


	/**
	 * @return
	 */
	public final BotID getKeeperId()
	{
		return BotID.createBotId(refereeMsg.getTeamInfo(aiTeam.getTeamColor()).getGoalie(), aiTeam.getTeamColor());
	}


	/**
	 * @return
	 */
	public final BotID getKeeperOpponentId()
	{
		return BotID.createBotId(refereeMsg.getTeamInfo(aiTeam.getTeamColor().opposite()).getGoalie(),
				aiTeam.getTeamColor().opposite());
	}


	/**
	 * @return the teamColor
	 */
	public final ETeamColor getTeamColor()
	{
		return aiTeam.getTeamColor();
	}


	/**
	 * @return the simpleWorldFrame
	 */
	public final SimpleWorldFrame getSimpleWorldFrame()
	{
		return worldFrameWrapper.getSimpleWorldFrame();
	}


	/**
	 * Get a modifiable shape list
	 *
	 * @param shapeLayer the shape layer
	 * @return the shape list for the given layer
	 */
	public final List<IDrawableShape> getShapes(IShapeLayerIdentifier shapeLayer)
	{
		return shapeMap.get(shapeLayer);
	}
}
