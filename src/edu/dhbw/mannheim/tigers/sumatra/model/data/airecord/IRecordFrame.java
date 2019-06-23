/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 23, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.airecord;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.PlayStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMapConst;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;


/**
 * Interface for a record frame
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public interface IRecordFrame
{
	
	/**
	 * @return the worldFrame
	 */
	IRecordWfFrame getRecordWfFrame();
	
	
	/**
	 * @return the refereeMsg
	 */
	Command getRefereeCmd();
	
	
	/**
	 * @return the tacticalInfo
	 */
	TacticalField getTacticalInfo();
	
	
	/**
	 * @return the playStrategy
	 */
	PlayStrategy getPlayStrategy();
	
	
	/**
	 * @return the assigendRolesConst
	 */
	BotIDMapConst<ERole> getAssigendERoles();
	
	
	/**
	 * @param paths
	 */
	void setPaths(List<Path> paths);
	
	
	/**
	 * @return
	 */
	List<Path> getPaths();
	
}