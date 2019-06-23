/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.athena;

import java.util.List;
import java.util.Map;

import edu.tigers.sumatra.ai.athena.PlayStrategy.Builder;
import edu.tigers.sumatra.ai.athena.roleassigner.RoleMapping;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.plays.others.GuiTestPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;


/**
 * Test mode
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TestModeAthenaAdapter extends AAthenaAdapter
{
	@Override
	public void doProcess(final MetisAiFrame metisAiFrame, final Builder playStrategyBuilder, final AIControl aiControl)
	{
		Map<EPlay, RoleMapping> outputRoleMapping = playStrategyBuilder.getRoleMapping();
		Map<EPlay, RoleMapping> inputGuiRoleMapping = getAiControl().getRoleMapping();
		Map<EPlay, RoleMapping> inputAiRoleMapping = metisAiFrame.getTacticalField().getRoleMapping();
		
		outputRoleMapping.putAll(inputGuiRoleMapping);
		for (EPlay ePlay : inputGuiRoleMapping.keySet())
		{
			Boolean useAi = getAiControl().getUseAiFlags().get(ePlay);
			if ((useAi != null) && useAi && inputAiRoleMapping.containsKey(ePlay))
			{
				outputRoleMapping.put(ePlay, inputAiRoleMapping.get(ePlay));
			}
		}
		
		outputRoleMapping.computeIfAbsent(EPlay.GUI_TEST, e -> new RoleMapping());
		syncTargetPlaySet(outputRoleMapping.keySet(), playStrategyBuilder.getActivePlays());
		
		GuiTestPlay guiPlay = getGuiTestPlay(playStrategyBuilder.getActivePlays());
		
		for (ARole role : aiControl.getAddRoles())
		{
			guiPlay.addRoleToBeAdded(role);
		}
		aiControl.getAddRoles().clear();
		
		for (ARole role : guiPlay.getRoles())
		{
			if (role.isCompleted())
			{
				outputRoleMapping.get(EPlay.GUI_TEST).getDesiredBots().remove(role.getBotID());
			}
		}
	}
	
	
	private GuiTestPlay getGuiTestPlay(final List<APlay> activePlays)
	{
		for (APlay aPlay : activePlays)
		{
			if (aPlay.getType() == EPlay.GUI_TEST)
			{
				return (GuiTestPlay) aPlay;
			}
		}
		throw new IllegalStateException("Gui play must be present");
	}
}
