/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 28, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon;

import java.util.HashMap;
import java.util.Map;


/**
 * Statistic object for knowledgebase
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class KbStatistics
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final Map<KnowledgePlay, Pair>	knowledgePlayNums	= new HashMap<KnowledgePlay, Pair>();
	private final Pair							sum;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param kb
	 */
	public KbStatistics(IKnowledgeBase kb)
	{
		int sSum = 0;
		int fSum = 0;
		for (KnowledgePlay kp : kb.getKnowledgePlays())
		{
			knowledgePlayNums.put(kp, new Pair(kp.getSuccessFields().size(), kp.getFailedFields().size()));
			sSum += kp.getSuccessFields().size();
			fSum += kp.getFailedFields().size();
		}
		sum = new Pair(sSum, fSum);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<KnowledgePlay, Pair> entry : knowledgePlayNums.entrySet())
		{
			sb.append(String.format("%-70s %s%n", entry.getKey().toString(), entry.getValue().toString()));
		}
		sb.append(String.format("%-70s %s%n", "Sum", sum));
		
		return sb.toString();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the knowledgePlayNums
	 */
	public final Map<KnowledgePlay, Pair> getKnowledgePlayNums()
	{
		return knowledgePlayNums;
	}
	
	
	/**
	 * @return the sum
	 */
	public final Pair getSum()
	{
		return sum;
	}
	
	static class Pair
	{
		private final int	successful;
		private final int	failed;
		
		
		/**
		 * @param successful
		 * @param failed
		 */
		public Pair(int successful, int failed)
		{
			this.successful = successful;
			this.failed = failed;
		}
		
		
		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			builder.append("(");
			builder.append(successful);
			builder.append(",");
			builder.append(failed);
			builder.append(")");
			return builder.toString();
		}
		
		
		/**
		 * @return the successful
		 */
		public final int getSuccessful()
		{
			return successful;
		}
		
		
		/**
		 * @return the failed
		 */
		public final int getFailed()
		{
			return failed;
		}
		
	}
}
