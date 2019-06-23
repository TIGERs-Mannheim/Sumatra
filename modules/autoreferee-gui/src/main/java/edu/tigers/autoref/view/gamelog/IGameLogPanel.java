/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 24, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.view.gamelog;

import java.util.Set;

import edu.tigers.autoref.model.gamelog.GameLogTableModel;
import edu.tigers.autoreferee.engine.log.GameLogEntry.ELogEntryType;
import edu.tigers.sumatra.components.IEnumPanel;


/**
 * @author "Lukas Magel"
 */
public interface IGameLogPanel
{
	/**
	 * @param model
	 */
	void setTableModel(final GameLogTableModel model);
	
	
	/**
	 * @param types
	 */
	void setActiveLogTypes(final Set<ELogEntryType> types);
	
	
	/**
	 * @return
	 */
	IEnumPanel<ELogEntryType> getLogTypePanel();
}
