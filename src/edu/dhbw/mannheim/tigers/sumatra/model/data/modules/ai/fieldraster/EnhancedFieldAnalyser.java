/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.04.2013
 * Author(s): Philipp
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.fieldraster;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.rectangle.AIRectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.rectangle.AIRectangleVector;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;


/**
 * Separates the field into quadrants and analyzes the number of the own and opponent bots.<br />
 * <br />
 * <b>Scoring of quadrants:</b>
 * <ul>
 * <li>&lt;0 - more Tigers than bad boys</li>
 * <li>&gt;0 - more bad boys</li>
 * </ul>
 * <br />
 * <b>Numbers of the quadrants:</b><br />
 * 
 * <pre>
 *        TIGERS
 *        _____
 * |-----[     ]-----|
 * |        |        |
 * |        |        |
 * |   3    |    2   |
 * |        |        |
 * |        |        |
 * |--------o--------|
 * |        |        |
 * |        |        |
 * |    4   |    1   |
 * |        |        |
 * |        |        |
 * |-----[_____]-----|
 * 
 *       BAD BOYS
 * </pre>
 * 
 * @author Philipp
 * 
 */
public class EnhancedFieldAnalyser
{
	
	private AIRectangleVector					ratedRectangles;
	private List<ArrayList<AIRectangle>>	maxFoeTakenRectanglesByQuadrant;
	private List<ArrayList<AIRectangle>>	maxTigersTakenRectanglesByQuadrant;
	private FieldRasterGenerator				fieldGenerator;
	private float									totalMaximum;
	private float									quadrantNr1	= 0;
	private float									quadrantNr2	= 0;
	private float									quadrantNr3	= 0;
	private float									quadrantNr4	= 0;
	private static final Logger				log			= Logger.getLogger(EnhancedFieldAnalyser.class.getName());
	
	/**
	 * Enum to choose the best algorithm for the analyzing
	 * 
	 * @author PhilippP
	 * 
	 */
	public enum ESearchAlgorithm
	{
		/** HILLCLIMBING */
		HILLCLIMBING
	}
	
	/**
	 * Describes for witch situation do you want to search
	 * 
	 * @author PhilippP
	 * 
	 */
	public enum ESituation
	{
		/** Search for the area with highest enemy density */
		BLOCKED,
		/** Search for the freest area */
		FREE
	}
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param ratedRectangles
	 */
	public EnhancedFieldAnalyser(AIRectangleVector ratedRectangles)
	{
		this.ratedRectangles = ratedRectangles;
		fieldGenerator = ratedRectangles.getFieldGenerator();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Search a local Maximum with help of specified algorithm
	 * 
	 * @param situation - Free or Blocked
	 * @param algorithm - Witch algorithm should be used
	 * @param position - startPosition
	 * @return
	 */
	public IVector2 getBestPositionInNearOfPoint(ESituation situation, ESearchAlgorithm algorithm, IVector2 position)
	{
		switch (algorithm)
		{
			case HILLCLIMBING:
			{
				int id = fieldGenerator.getAnalysingIDFromPosition(position);
				AIRectangle bestRectangle = fieldGenerator.getAnalysisFieldRectangle(id);
				position = searchWithHillClimbing(situation, bestRectangle).getMidPoint();
			}
				break;
			default:
			{
				int id = fieldGenerator.getAnalysingIDFromPosition(position);
				AIRectangle bestRectangle = fieldGenerator.getAnalysisFieldRectangle(id);
				position = searchWithHillClimbing(situation, bestRectangle).getMidPoint();
			}
		}
		
		return position;
	}
	
	
	// --------------------------------------------------------------------------
	
	/**
	 * Use the hill ClimbinAlgorithm to search a best point in the near of given position
	 * "Finde den Gipfel des Mt. Everest mit Gedächtnisverlust in dichtem
	 * Nebel“
	 * @param situation
	 * @param bestRectangle
	 * @return
	 */
	private AIRectangle searchWithHillClimbing(ESituation situation, AIRectangle bestRectangle)
	{
		List<AIRectangle> neighbours;
		AIRectangle old, current = bestRectangle;
		
		try
		{
			if (situation == ESituation.FREE)
			{
				do
				{
					neighbours = current.getNeighbours();
					old = new AIRectangle(current);
					for (AIRectangle neighbour : neighbours)
					{
						if (neighbour.getValue() > current.getValue())
						{
							current = new AIRectangle(neighbour);
						}
					}
				} while (old.getValue() < current.getValue());
			} else
			{
				do
				{
					neighbours = current.getNeighbours();
					old = new AIRectangle(current);
					for (AIRectangle neighbour : neighbours)
					{
						if (neighbour.getValue() < current.getValue())
						{
							current = new AIRectangle(neighbour);
						}
					}
				} while (old.getValue() > current.getValue());
			}
		} catch (NullPointerException e)
		{
		}
		
		return current;
		/*
		 * for (AIRectangle aiRectangle : neighbours)
		 * {
		 * if (ESituation.FREE == situation)
		 * {
		 * if (aiRectangle.getValue() > nextRectangle.getValue())
		 * {
		 * nextRectangle = bestRectangle;
		 * }
		 * 
		 * } else
		 * {
		 * if (aiRectangle.getValue() < nextRectangle.getValue())
		 * {
		 * nextRectangle = bestRectangle;
		 * }
		 * }
		 * }
		 * if (nextRectangle.equals(bestRectangle))
		 * {
		 * return bestRectangle;
		 * }
		 * return searchWithHillClimbing(situation, nextRectangle);
		 */
	}
	
	
	/**
	 * @return
	 */
	public AIRectangleVector getAnalysingRectangleVector()
	{
		return ratedRectangles;
	}
	
	
	/**
	 * Set a ID-List of the Rectangles with the highest value
	 * 
	 * @param maxFoeTakenRectangleIDs
	 */
	public void setMaxFoeTakenRectangleID(List<ArrayList<AIRectangle>> maxFoeTakenRectangleIDs)
	{
		maxFoeTakenRectanglesByQuadrant = maxFoeTakenRectangleIDs;
		
	}
	
	
	/**
	 * Set a ID-List of the Rectangles with the lowest value
	 * 
	 * @param maxTigersTakenRectangleIDs
	 */
	public void setMaxTigersTakenRectangleID(List<ArrayList<AIRectangle>> maxTigersTakenRectangleIDs)
	{
		maxTigersTakenRectanglesByQuadrant = maxTigersTakenRectangleIDs;
		
	}
	
	
	/**
	 * Return a list with the most taken rectanglesIDs from Foe in a specified Quadrant
	 * 
	 * @param numberOfQuadrant
	 * @return the maxFoeTakenRectangleIDsByQuadrant
	 */
	public ArrayList<AIRectangle> getMaxFoeTakenRectangleIDsInQuadrant(int numberOfQuadrant)
	{
		return maxFoeTakenRectanglesByQuadrant.get(numberOfQuadrant);
	}
	
	
	/**
	 * Return a list with the most taken rectanglesIDs from Tigers in a specified Quadrant
	 * 
	 * @param numberOfQuadrant
	 * @return the maxTigersTakenRectangleIDsByQuadrant
	 */
	public ArrayList<AIRectangle> getMaxTigersTakenRectangleIDsInQuadrant(int numberOfQuadrant)
	{
		return maxTigersTakenRectanglesByQuadrant.get(numberOfQuadrant);
	}
	
	
	/**
	 * Return a list with the most taken {@link AIRectangle} from Tigers
	 * @return the maxTigersTakenRectangles
	 */
	public List<AIRectangle> getMaxTigersTakenRectangles()
	{
		List<AIRectangle> temp = new ArrayList<AIRectangle>();
		for (ArrayList<AIRectangle> quadrant : maxTigersTakenRectanglesByQuadrant)
		{
			for (AIRectangle rec : quadrant)
			{
				temp.add(rec);
			}
		}
		return temp;
	}
	
	
	/**
	 * Return a list with the most taken {@link AIRectangle} from Foe
	 * @return the maxFoeTakenRectangles;
	 */
	public List<AIRectangle> getMaxFoeTakenRectangles()
	{
		List<AIRectangle> temp = new ArrayList<AIRectangle>();
		for (ArrayList<AIRectangle> quadrant : maxFoeTakenRectanglesByQuadrant)
		{
			for (AIRectangle rec : quadrant)
			{
				temp.add(rec);
			}
			
		}
		return temp;
	}
	
	
	/**
	 * Return the highest rectangle Value
	 * 
	 * @return
	 */
	public float getTotalMaximum()
	{
		return totalMaximum;
	}
	
	
	/**
	 * Return true or false if in the quadrant or in the near ist no tiger bot!
	 * Note: <strong> Quadran 1,2 is Foe and 3,4 is Tigers
	 * 
	 * @param numberOfQuadrant {1,2,3,4}
	 * @return
	 */
	public boolean isQuadrantEmptyTigers(int numberOfQuadrant)
	{
		numberOfQuadrant -= 1;
		if ((numberOfQuadrant < 4) || (numberOfQuadrant >= 0))
		{
			float value = maxTigersTakenRectanglesByQuadrant.get(numberOfQuadrant).get(0).getValue();
			if (value >= 0)
			{
				return true;
			}
			return false;
		}
		log.error("Used QuadrantEmptyTigers with wrong pramaeters: 1<=numberOfquadrants<=4) numberOfQuadrants="
				+ numberOfQuadrant);
		return false;
	}
	
	
	/**
	 * Return true or false if in the quadrant or in the near ist no foe bot!
	 * 
	 * @param numberOfQuadrant {1,2,3,4}
	 * @return
	 */
	public boolean isQuadrantEmptyFoe(int numberOfQuadrant)
	{
		numberOfQuadrant -= 1;
		if ((numberOfQuadrant < 4) || (numberOfQuadrant >= 0))
		{
			float value = maxFoeTakenRectanglesByQuadrant.get(numberOfQuadrant).get(0).getValue();
			if (value >= 0)
			{
				return true;
			}
			return false;
		}
		log.error("Used QuadrantEmptyTigers with wrong pramaeters: 1<=numberOfquadrants<=4) numberOfQuadrants="
				+ numberOfQuadrant);
		return false;
	}
	
	
	/**
	 * Return true or false if in the quadrant or in the near ist no foe bot!
	 * 
	 * @return list wit the empty quadrantsNumber
	 */
	public List<Integer> getEmptyTigersQuadrants()
	{
		List<Integer> list = new ArrayList<Integer>();
		for (ArrayList<AIRectangle> rec : maxTigersTakenRectanglesByQuadrant)
		{
			if (rec.get(0).getValue() >= 0)
			{
				list.add(GeoMath.checkQuadrant(rec.get(0).getMidPoint()));
			}
		}
		return list;
	}
	
	
	/**
	 * Return true or false if in the quadrant or in the near ist no foe bot!
	 * 
	 * @return list wit the empty quadrantsNumber
	 */
	public List<Integer> getEmptyFoesQuadrants()
	{
		List<Integer> list = new ArrayList<Integer>();
		int quadrant = 0;
		for (ArrayList<AIRectangle> rec : maxFoeTakenRectanglesByQuadrant)
		{
			quadrant++;
			if (rec.get(0).getValue() >= 0)
			{
				list.add(quadrant);
			}
			
		}
		
		
		return list;
	}
	
	
	/**
	 * Set the highest rectangle value
	 * 
	 * @param totalMaximum
	 */
	public void setTotalMaximum(float totalMaximum)
	{
		this.totalMaximum = Math.abs(totalMaximum);
	}
	
	
	/**
	 * Set the scoring for all Quadrants
	 * 
	 * @param quadrantNr1 - Quadrant 1
	 * @param quadrantNr2 - Quadrant 2
	 * @param quadrantNr3 - Quadrant 3
	 * @param quadrantNr4 - Quadrant 4
	 */
	public void setScoringOfQuadrants(float quadrantNr1, float quadrantNr2, float quadrantNr3, float quadrantNr4)
	{
		this.quadrantNr1 = quadrantNr1;
		this.quadrantNr2 = quadrantNr2;
		this.quadrantNr3 = quadrantNr3;
		this.quadrantNr4 = quadrantNr4;
		
	}
	
	
	/**
	 * @return scoring of quadrantNr1
	 */
	public float getScoringQuadrantNr1()
	{
		return quadrantNr1;
	}
	
	
	/**
	 * @return scoring of quadrantNr2
	 */
	public float getScoringQuadrantNr2()
	{
		return quadrantNr2;
	}
	
	
	/**
	 * @return scoring of quadrantNr3
	 */
	public float getScoringQuadrantNr3()
	{
		return quadrantNr3;
	}
	
	
	/**
	 * @return scoring of quadrantNr4
	 */
	public float getScoringQuadrantNr4()
	{
		return quadrantNr4;
	}
	
	
}
