/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.finisher;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.g3force.instanceables.InstanceableClass;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ids.BotID;


/**
 * Handler for finisher moves
 */
public final class FinisherMoves
{
	private final List<IFinisherMove> moves = new ArrayList<>();
	
	
	private FinisherMoves()
	{
	}
	
	
	public static FinisherMoves all()
	{
		FinisherMoves finisherMoves = new FinisherMoves();
		for (EFinisherMove finisherMove : EFinisherMove.values())
		{
			finisherMoves.moves.add(createFinisherMove(finisherMove));
		}
		return finisherMoves;
	}
	
	
	/**
	 * creates a finisher move according to the enum finisherMove
	 * 
	 * @param finisherMove
	 * @return a IFinisherMove instance
	 */
	public static IFinisherMove createFinisherMove(final EFinisherMove finisherMove)
	{
		try
		{
			return (IFinisherMove) finisherMove.getInstanceableClass().newDefaultInstance();
		} catch (InstanceableClass.NotCreateableException e)
		{
			throw new IllegalStateException("Could not create finisher move " + finisherMove, e);
		}
	}
	
	
	/**
	 * returns all finisher moves with trajectories that fit the botID
	 * 
	 * @param aiFrame
	 * @param botID
	 * @return a list of all finisher moves
	 */
	public List<IFinisherMove> getFinisherMoves(final BaseAiFrame aiFrame, final BotID botID)
	{
		for (IFinisherMove move : moves)
		{
			move.generateTrajectory(aiFrame, botID);
		}
		return moves;
	}
	
	
	/**
	 * Collect the applicable finisher moves
	 * 
	 * @param aiFrame the current frame
	 * @param botId
	 * @return a list of applicable finisher moves
	 */
	public List<IFinisherMove> getApplicableFinisherMoves(final BaseAiFrame aiFrame, final BotID botId)
	{
		return moves.stream().filter(f -> f.isApplicable(aiFrame, botId)).collect(Collectors.toList());
	}
}
