/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.05.2014
 * Author(s): JulianT
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.support;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.HierarchicalConfiguration;

import edu.dhbw.mannheim.tigers.sumatra.model.data.ValuedField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.OpenClHandler;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IConfigObserver;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Shall refine support positions using hillclimbing based on ValuePoints
 * estimated by RedirectPosGPUCalc
 * 
 * @author JulianT
 */
public class SupportPositionGPUCalc extends ACalculator implements IConfigObserver
{
	// private static final Logger log = Logger.getLogger(SupportPositionCalc.class.getName());
	@Configurable(comment = "Maximum climbing distance per frame in mm")
	private static int	climbingDistance	= 50;
	
	
	/**
	 * 
	 */
	public SupportPositionGPUCalc()
	{
		if (!OpenClHandler.isOpenClSupported())
		{
			setActive(false);
		}
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		Map<BotID, IVector2> positions = new HashMap<BotID, IVector2>();
		
		for (TrackedTigerBot bot : baseAiFrame.getWorldFrame().getTigerBotsAvailable().values())
		{
			if (newTacticalField.getSupportValues().isEmpty())
			{
				positions.put(bot.getId(), newTacticalField.getSupportRedirectPositions().get(bot.getId()));
			} else
			{
				positions.put(bot.getId(), climbHill(bot, newTacticalField.getSupportValues().get(bot.getId())));
			}
		}
		
		newTacticalField.setSupportPositions(positions);
	}
	
	
	private IVector2 climbHill(final TrackedTigerBot bot, final ValuedField valuePoints)
	{
		IVector2 botPos = getXY(bot.getPos(), valuePoints);
		
		IVector2 bestNeighbour = bestNeighbourWithTol(botPos, valuePoints, 0.1f);
		
		if (bestNeighbour != null)
		{
			return getVector(bestNeighbour, valuePoints);
		}
		// fine tuning
		IVector2 fineTunedBestNeighbour = bestNeighbourWithTol(botPos, valuePoints, 0.001f);
		if (fineTunedBestNeighbour != null)
		{
			return getVector(fineTunedBestNeighbour, valuePoints);
		}
		return bot.getPos();
	}
	
	
	private IVector2 bestNeighbourWithTol(final IVector2 botPos, final ValuedField valuePoints, final float tol)
	{
		List<IVector2> neighbours = getNeighbours(botPos, valuePoints, 0);
		IVector2 bestNeighbour = neighbours.get(0);
		
		int searchDistAroundBot = 0;
		do
		{
			neighbours = getNeighbours(botPos, valuePoints, searchDistAroundBot);
			if (neighbours.size() > 0)
			{
				bestNeighbour = botPos;
			}
			for (IVector2 neighbour : neighbours)
			{
				if (valuePoints.getValue((int) neighbour.x(), (int) neighbour.y()) <= (valuePoints.getValue(
						(int) bestNeighbour.x(),
						(int) bestNeighbour.y()) - tol))
				{
					bestNeighbour = neighbour;
					return bestNeighbour;
				}
			}
			searchDistAroundBot++;
		} while (searchDistAroundBot < valuePoints.getNumX());
		return null;
	}
	
	
	/*
	 * private IVector2 getMin(final ValuedField field)
	 * {
	 * int minX = 0, minY = 0;
	 * for (int i = 0; i < field.getNumX(); i++)
	 * {
	 * for (int j = 0; j < field.getNumY(); j++)
	 * {
	 * if (field.getValue(i, j) < field.getValue(minX, minY))
	 * {
	 * minX = i;
	 * minY = j;
	 * }
	 * }
	 * }
	 * return getVector(new Vector2(minX, minY), field);
	 * }
	 */
	
	
	private IVector2 getXY(final IVector2 vector, final ValuedField field)
	{
		double xFactor = AIConfig.getGeometry().getFieldLength() / field.getNumX();
		double yFactor = AIConfig.getGeometry().getFieldWidth() / field.getNumY();
		
		double x = Math.round((vector.x() / xFactor) + (0.5 * field.getNumX()));
		double y = Math.round((vector.y() / yFactor) + (0.5 * field.getNumY()));
		
		if (x < 0)
		{
			x = 0;
		} else if (x >= field.getNumX())
		{
			x = field.getNumX() - 1;
		}
		
		if (y < 0)
		{
			y = 0;
		} else if (y >= field.getNumY())
		{
			y = field.getNumY() - 1;
		}
		
		return new Vector2(x, y);
	}
	
	
	private IVector2 getVector(final IVector2 xy, final ValuedField field)
	{
		double xFactor = AIConfig.getGeometry().getFieldLength() / field.getNumX();
		double yFactor = AIConfig.getGeometry().getFieldWidth() / field.getNumY();
		
		IVector2 vector = new Vector2((xy.x() * xFactor) - (0.5 * AIConfig.getGeometry().getFieldLength()),
				(xy.y() * yFactor) - (0.5 * AIConfig.getGeometry().getFieldWidth()));
		return vector;
	}
	
	
	private List<IVector2> getNeighbours(final IVector2 xy, final ValuedField field, final int dist)
	{
		List<IVector2> neighbours = new LinkedList<IVector2>();
		
		if (xy.x() > dist)
		{
			if (xy.y() > dist)
			{
				neighbours.add(new Vector2(xy.x() - (dist + 1), xy.y() - (dist + 1)));
			}
			
			neighbours.add(new Vector2(xy.x() - (dist + 1), xy.y()));
			
			if (xy.y() < (field.getNumY() - (dist + 1)))
			{
				neighbours.add(new Vector2(xy.x() - (dist + 1), xy.y() + (dist + 1)));
			}
		}
		
		if (xy.y() > dist)
		{
			neighbours.add(new Vector2(xy.x(), xy.y() - (dist + 1)));
		}
		
		if (xy.y() < (field.getNumY() - (dist + 1)))
		{
			neighbours.add(new Vector2(xy.x(), xy.y() + (dist + 1)));
		}
		
		if (xy.x() < (field.getNumX() - (dist + 1)))
		{
			if (xy.y() > dist)
			{
				neighbours.add(new Vector2(xy.x() + (dist + 1), xy.y() - (dist + 1)));
			}
			
			neighbours.add(new Vector2(xy.x() + (dist + 1), xy.y()));
			
			if (xy.y() < (field.getNumY() - (dist + 1)))
			{
				neighbours.add(new Vector2(xy.x() + (dist + 1), xy.y() + (dist + 1)));
			}
		}
		
		return neighbours;
	}
	
	
	@Override
	public void onLoad(final HierarchicalConfiguration newConfig)
	{
		// TODO JulianT: Auto-generated method stub
		
	}
	
	
	@Override
	public void onReload(final HierarchicalConfiguration freshConfig)
	{
		// TODO JulianT: Auto-generated method stub
		
	}
}
