package edu.tigers.sumatra.aicenter.view;

import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.aicenter.view.statepanel.RoleStatemachinePanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultButtonModel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import java.util.EnumMap;
import java.util.Map;


public class TeamPanel extends JPanel
{
	private final Map<EAIControlState, JRadioButton> modeButtons = new EnumMap<>(EAIControlState.class);
	private final AthenaControlPanel athenaPanel;
	private final RoleControlPanel rolePanel = new RoleControlPanel();
	private final MetisPanel metisPanel = new MetisPanel();
	private final JTabbedPane tabbedPane = new JTabbedPane();

	private final RoleStatemachinePanel statemachinePanel = new RoleStatemachinePanel();


	public TeamPanel()
	{
		super(new MigLayout("wrap 1"));

		athenaPanel = new AthenaControlPanel();

		final JPanel modePanel = new JPanel(new MigLayout("insets 0 0 0 0", "", ""));
		final ButtonGroup modeGroup = new ButtonGroup();
		for (EAIControlState state : EAIControlState.values())
		{
			JRadioButton btn = new JRadioButton(state.toString());
			modePanel.add(btn);
			modeGroup.add(btn);
			modeButtons.put(state, btn);
		}

		final ButtonModel btnModelMode = new DefaultButtonModel();
		btnModelMode.setGroup(modeGroup);

		modeGroup.clearSelection();

		modeButtons.get(EAIControlState.EMERGENCY_MODE).setSelected(true);

		this.add(modePanel);

		this.add(rolePanel);
		this.add(metisPanel);
		this.add(athenaPanel);

		tabbedPane.addTab("Athena", athenaPanel);
		tabbedPane.addTab("Roles", rolePanel);
		tabbedPane.addTab("Metis Calcs", metisPanel);
		tabbedPane.addTab("State Info", statemachinePanel);

		this.add(tabbedPane, "push, grow");
	}


	public RoleControlPanel getRolePanel()
	{
		return rolePanel;
	}


	public MetisPanel getMetisPanel()
	{
		return metisPanel;
	}


	public AthenaControlPanel getAthenaPanel()
	{
		return athenaPanel;
	}


	public JTabbedPane getTabbedPane()
	{
		return tabbedPane;
	}


	public Map<EAIControlState, JRadioButton> getModeButtons()
	{
		return modeButtons;
	}


	public RoleStatemachinePanel getStatemachinePanel()
	{
		return statemachinePanel;
	}

}
