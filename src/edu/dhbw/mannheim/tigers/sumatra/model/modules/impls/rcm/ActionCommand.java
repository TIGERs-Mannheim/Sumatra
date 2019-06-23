/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - RCU
 * Date: 06.11.2010
 * Author(s): Lukas
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm;

import java.io.Serializable;


/**
 * This class contains all needed attributes for a command.
 * 
 * @author Lukas
 * 
 */

public class ActionCommand implements Serializable
{
	// --------------------------------------------------------------------------
	// --- instance variables ---------------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= -68111792087214485L;
	
	/** */
	public double					translateX;
	/** */
	public double					translateY;
	/** */
	public double					rotate;
	/** */
	public double					kick;
	/** */
	public double					chipKick;
	/** */
	public double					kickArm;
	/** */
	public double					chipArm;
	/** */
	public double					dribble;
	/** */
	public double					pass;
	/** */
	public double					disarm;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public ActionCommand()
	{
		fillCommandWithNulls();
	}
	
	
	/**
	 * @param translateY
	 * @param translateX
	 * @param rotate
	 * @param kick
	 * @param chipKick
	 * @param kickArm
	 * @param chipArm
	 * @param dribble
	 * @param pass
	 * @param disarm
	 */
	public ActionCommand(double translateY, double translateX, double rotate, double kick, double chipKick,
			double kickArm, double chipArm, double dribble, double pass, double disarm)
	{
		this.translateY = translateY;
		this.translateX = translateX;
		this.rotate = rotate;
		this.kick = kick;
		this.chipKick = chipKick;
		this.kickArm = kickArm;
		this.chipArm = chipArm;
		this.dribble = dribble;
		this.pass = pass;
		this.disarm = disarm;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public final void fillCommandWithNulls()
	{
		translateY = 0.0;
		translateX = 0.0;
		rotate = 0.0;
		kick = 0.0;
		chipKick = 0.0;
		pass = 0.0;
		kickArm = 0.0;
		dribble = 0.0;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param translateX the translateX factor to set
	 */
	public void setTranslateX(double translateX)
	{
		this.translateX = translateX;
	}
	
	
	/**
	 * @param translateY the translateY factor to set
	 */
	public void setTranslateY(double translateY)
	{
		this.translateY = translateY;
	}
	
	
	/**
	 * @param rotate the rotate factor to set
	 */
	public void setRotate(double rotate)
	{
		this.rotate = rotate;
	}
	
	
	/**
	 * @param force the force factor to set
	 */
	public void setKick(double force)
	{
		kick = force;
	}
	
	
	/**
	 * @param chipKick the chipKick factor to set
	 */
	public void setChipKick(double chipKick)
	{
		this.chipKick = chipKick;
	}
	
	
	/**
	 * @param pass the pass factor to set
	 */
	public void setPass(double pass)
	{
		this.pass = pass;
	}
	
	
	/**
	 * @param arm the arm factor to set
	 */
	public void setArm(double arm)
	{
		kickArm = arm;
	}
	
	
	/**
	 * @param dribble the dribble factor to set
	 */
	public void setDribble(double dribble)
	{
		this.dribble = dribble;
	}
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(kickArm);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(chipKick);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(dribble);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(kick);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(pass);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(rotate);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(translateX);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(translateY);
		return (prime * result) + (int) (temp ^ (temp >>> 32));
	}
	
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		ActionCommand other = (ActionCommand) obj;
		if (Double.doubleToLongBits(kickArm) != Double.doubleToLongBits(other.kickArm))
		{
			return false;
		}
		if (Double.doubleToLongBits(chipKick) != Double.doubleToLongBits(other.chipKick))
		{
			return false;
		}
		if (Double.doubleToLongBits(dribble) != Double.doubleToLongBits(other.dribble))
		{
			return false;
		}
		if (Double.doubleToLongBits(kick) != Double.doubleToLongBits(other.kick))
		{
			return false;
		}
		if (Double.doubleToLongBits(pass) != Double.doubleToLongBits(other.pass))
		{
			return false;
		}
		if (Double.doubleToLongBits(rotate) != Double.doubleToLongBits(other.rotate))
		{
			return false;
		}
		if (Double.doubleToLongBits(translateX) != Double.doubleToLongBits(other.translateX))
		{
			return false;
		}
		if (Double.doubleToLongBits(translateY) != Double.doubleToLongBits(other.translateY))
		{
			return false;
		}
		return true;
	}
	
	
	@Override
	public String toString()
	{
		return "ActionCommand [translateX=" + translateX + ", translateY=" + translateY + ", rotate=" + rotate
				+ ", force=" + kick + ", chipKick=" + chipKick + ", pass=" + pass + ", arm=" + kickArm + ", dribble="
				+ dribble + "]";
	}
	
	
	/**
	 * @return the chipArm
	 */
	public final double getChipArm()
	{
		return chipArm;
	}
	
	
	/**
	 * @param chipArm the chipArm to set
	 */
	public final void setChipArm(double chipArm)
	{
		this.chipArm = chipArm;
	}
	
	
	/**
	 * @return the translateX
	 */
	public final double getTranslateX()
	{
		return translateX;
	}
	
	
	/**
	 * @return the translateY
	 */
	public final double getTranslateY()
	{
		return translateY;
	}
	
	
	/**
	 * @return the rotate
	 */
	public final double getRotate()
	{
		return rotate;
	}
	
	
	/**
	 * @return the kick
	 */
	public final double getKick()
	{
		return kick;
	}
	
	
	/**
	 * @return the chipKick
	 */
	public final double getChipKick()
	{
		return chipKick;
	}
	
	
	/**
	 * @return the arm
	 */
	public final double getKickArm()
	{
		return kickArm;
	}
	
	
	/**
	 * @return the dribble
	 */
	public final double getDribble()
	{
		return dribble;
	}
	
	
	/**
	 * @return the pass
	 */
	public final double getPass()
	{
		return pass;
	}
	
	
	/**
	 * @return the disarm
	 */
	public final double getDisarm()
	{
		return disarm;
	}
	
	
	/**
	 * @param disarm the disarm to set
	 */
	public final void setDisarm(double disarm)
	{
		this.disarm = disarm;
	}
}
