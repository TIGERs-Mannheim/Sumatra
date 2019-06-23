/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 16, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.score;

/**
 * Data holder for {@link AScore}s
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ScoreResult
{
	/**
	 */
	public enum EUsefulness
	{
		/**  */
		NEUTRAL(0),
		/**  */
		LIMITED(1),
		/**  */
		BAD(2);
		private final int	level;
		
		
		private EUsefulness(final int level)
		{
			this.level = level;
		}
		
		
		/**
		 * @return the level
		 */
		public int getLevel()
		{
			return level;
		}
	}
	
	private final EUsefulness			usefulness;
	private final int						degree;
	
	private static final ScoreResult	DEFAULT	= new ScoreResult(EUsefulness.NEUTRAL, 0);
	
	
	/**
	 * @param usefulness
	 */
	public ScoreResult(final EUsefulness usefulness)
	{
		super();
		this.usefulness = usefulness;
		degree = 0;
	}
	
	
	/**
	 * @param usefulness
	 * @param degree
	 */
	public ScoreResult(final EUsefulness usefulness, final int degree)
	{
		super();
		this.usefulness = usefulness;
		this.degree = degree;
	}
	
	
	/**
	 * @return the usefulness
	 */
	public EUsefulness getUsefulness()
	{
		return usefulness;
	}
	
	
	/**
	 * The degree of usefulness. 0 is default, the higher, the worse, the lower, the better
	 * 
	 * @return the degree
	 */
	public int getDegree()
	{
		return degree;
	}
	
	
	/**
	 * @return
	 */
	public static ScoreResult defaultResult()
	{
		return DEFAULT;
	}
	
	
	/**
	 * @param result
	 * @return
	 */
	public boolean moreUsefulThan(final ScoreResult result)
	{
		if (getUsefulness().getLevel() < result.getUsefulness().getLevel())
		{
			return true;
		}
		if (getUsefulness().getLevel() > result.getUsefulness().getLevel())
		{
			return false;
		}
		return getDegree() < result.degree;
	}
}
