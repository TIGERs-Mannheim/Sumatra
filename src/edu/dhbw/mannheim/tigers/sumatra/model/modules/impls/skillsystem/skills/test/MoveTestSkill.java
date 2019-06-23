/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 14, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * Drive some patterns to test movement of bot
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MoveTestSkill extends PositionSkill
{
	@SuppressWarnings("unused")
	private static final Logger	log						= Logger.getLogger(MoveTestSkill.class.getName());
	private final List<IVector3>	intermediatePoints	= new ArrayList<>();
	private int							id							= 0;
	
	private boolean					loop						= true;
	
	private EMode						mode						= EMode.FWD;
	
	private final List<Double>		errors					= new ArrayList<>();
	private IVector3					lastPoint				= null;
	
	/**
	 */
	public enum EMode
	{
		/**  */
		CUSTOM,
		/**  */
		FWD,
		/**  */
		RWD,
		/**  */
		LEFT,
		/**  */
		RIGHT,
		/**  */
		BALL
	}
	
	
	/**
	 * @param mode
	 * @param loop
	 */
	public MoveTestSkill(final EMode mode, final boolean loop)
	{
		super(ESkillName.MOVE_TEST);
		this.mode = mode;
		this.loop = loop;
		List<IVector3> intermediatePoints = new ArrayList<>();
		intermediatePoints.add(new Vector3(+1500, +1500, AngleMath.PI_HALF));
		intermediatePoints.add(new Vector3(-1500, +1500, AngleMath.PI_HALF));
		intermediatePoints.add(new Vector3(-1500, -1500, AngleMath.PI_HALF));
		intermediatePoints.add(new Vector3(+1500, -1500, AngleMath.PI_HALF));
		
		if ((mode == EMode.CUSTOM) || (mode == EMode.BALL))
		{
			this.intermediatePoints.addAll(intermediatePoints);
		} else
		{
			float orientOffset = 0;
			switch (mode)
			{
				case FWD:
					break;
				case LEFT:
					orientOffset = -AngleMath.PI_HALF;
					break;
				case RIGHT:
					orientOffset = AngleMath.PI_HALF;
					break;
				case RWD:
					orientOffset = AngleMath.PI;
					break;
				default:
					break;
			}
			for (int i = 0; i < intermediatePoints.size(); i++)
			{
				int ni = (i + 1) % intermediatePoints.size();
				float c2n = intermediatePoints.get(ni).getXYVector()
						.subtract(intermediatePoints.get(i).getXYVector()).getAngle();
				IVector3 cv = new Vector3(intermediatePoints.get(i).getXYVector(), c2n + orientOffset);
				IVector3 nv = new Vector3(intermediatePoints.get(ni).getXYVector(), c2n + orientOffset);
				this.intermediatePoints.add(cv);
				this.intermediatePoints.add(nv);
			}
		}
	}
	
	
	@Override
	protected void update(final List<ACommand> cmds)
	{
		if (!loop && (id >= intermediatePoints.size()))
		{
			double rmse = errors.stream().mapToDouble(a -> a).sum() / errors.size();
			log.info("RMSE: " + rmse);
			complete();
			return;
		}
		
		
		setDestination(intermediatePoints.get(getId()).getXYVector());
		
		if (mode == EMode.BALL)
		{
			setOrientation(getWorldFrame().getBall().getPos().subtractNew(getPos()).getAngle());
		} else
		{
			setOrientation(intermediatePoints.get(getId()).z());
		}
		
		float dist = lastPoint == null ? 0 : GeoMath.distancePP(getPos(), intermediatePoints.get(getId()).getXYVector());
		float diff = lastPoint == null ? 0 : Math.abs(AngleMath.getShortestRotation(getAngle(),
				intermediatePoints.get(getId()).z()));
		if ((dist < 5) && (diff < 0.05))
		{
			lastPoint = intermediatePoints.get(getId());
			id++;
		}
		
		if (lastPoint != null)
		{
			double error = GeoMath.distancePL(getPos(), lastPoint.getXYVector(), intermediatePoints.get(getId())
					.getXYVector());
			errors.add(error);
		}
	}
	
	
	private int getId()
	{
		return id % intermediatePoints.size();
	}
}
