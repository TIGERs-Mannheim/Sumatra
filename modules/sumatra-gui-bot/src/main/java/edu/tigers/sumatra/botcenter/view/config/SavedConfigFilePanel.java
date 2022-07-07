/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botcenter.view.config;

import edu.tigers.sumatra.botmanager.configs.ConfigFile;
import lombok.Getter;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;


public class SavedConfigFilePanel extends JPanel
{
	@Serial
	private static final long serialVersionUID = 3052423100929562899L;
	@Getter
	private final JComboBox<Integer> configVersion = new JComboBox<>();
	@Getter
	private JCheckBox autoUpdate = new JCheckBox("Auto-Update");
	private final List<JTextField> fields = new ArrayList<>();
	@Getter
	private final JButton delete = new JButton("Delete Config");
	@Getter
	private final JButton save = new JButton("Save Changes");
	@Getter
	private transient ConfigFile file;

	private JPanel fieldsPanel = new JPanel();


	public SavedConfigFilePanel(List<Integer> versions)
	{
		setLayout(new MigLayout("wrap 2", "[250][250,fill]"));

		configVersion.setPreferredSize(new Dimension(80, 25));
		for (int version : versions)
		{
			configVersion.addItem(version);
		}

		add(new JLabel("Version"), "span 2, split 5");
		add(configVersion);
		add(delete);
		add(save);
		add(autoUpdate);
	}


	public void setFields(ConfigFile file)
	{
		this.file = file;
		remove(fieldsPanel);
		fields.clear();
		fieldsPanel = new JPanel();
		fieldsPanel.setLayout(new MigLayout("wrap 2", "[150][100, fill]"));
		for (int i = 0; i < file.getNames().size(); i++)
		{
			JLabel label = new JLabel(file.getNames().get(i));
			JTextField field = new JTextField(file.getValues().get(i));

			fieldsPanel.add(label);
			fieldsPanel.add(field);

			fields.add(field);
		}
		add(fieldsPanel);
	}


	public void setAutoUpdate(boolean update)
	{
		autoUpdate.setSelected(update);
	}


	public void parseValues()
	{
		for (int i = 0; i < fields.size(); i++)
		{
			file.getValues().set(i, fields.get(i).getText());
		}
	}
}