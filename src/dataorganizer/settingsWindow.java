package dataorganizer;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.Color;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

public class settingsWindow extends JFrame {

	private JPanel contentPane;
	private JTextField defaultSaveDirectoryField;
	private JButton setDefaultsBtn;
	private JLabel lblMode;
	private JLabel lblTemplateDirectory;
	private JTextField templateDirectoryField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					settingsWindow frame = new settingsWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public settingsWindow() {
		LoadSettings settings = new LoadSettings();
		String saveDirectoryString = null;
		try {
			settings.loadConfigFile();
			saveDirectoryString = settings.getKeyVal("CSVSaveLocation");
		}catch(Exception e) {
			e.printStackTrace();
			settings.loadDefaultConfig();
			settings.saveConfig();
		}
		
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblDirectoryDefaults = new JLabel("Save Directory:");
		lblDirectoryDefaults.setBounds(10, 11, 95, 14);
		contentPane.add(lblDirectoryDefaults);
		
		defaultSaveDirectoryField = new JTextField();
		defaultSaveDirectoryField.setBounds(98, 8, 326, 20);
		contentPane.add(defaultSaveDirectoryField);
		defaultSaveDirectoryField.setColumns(10);
		
		defaultSaveDirectoryField.setText(saveDirectoryString);
		
		JButton btnSave = new JButton("Save");
		btnSave.setBackground(new Color(0, 128, 0));
		btnSave.setBounds(309, 227, 89, 23);
		contentPane.add(btnSave);
		
		setDefaultsBtn = new JButton("Set Defaults");
		setDefaultsBtn.setBounds(26, 227, 105, 23);
		contentPane.add(setDefaultsBtn);
		
		lblMode = new JLabel("Selected Profile: ");
		lblMode.setBounds(10, 36, 81, 14);
		contentPane.add(lblMode);
		
		JComboBox comboBox = new JComboBox();
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"Adventurer", "Educator", "Professional"}));
		comboBox.setBounds(141, 33, 148, 20);
		contentPane.add(comboBox);
		
		ArrayList<String> profileList = new ArrayList<String>();
		profileList.add("Adventurer");
		profileList.add("Educator");
		profileList.add("Professional");
		
		comboBox.setSelectedIndex(profileList.indexOf(settings.getKeyVal("DefaultProfile")));
		
		lblTemplateDirectory = new JLabel("Template Directory: ");
		lblTemplateDirectory.setBounds(10, 61, 121, 14);
		contentPane.add(lblTemplateDirectory);
		
		templateDirectoryField = new JTextField();
		templateDirectoryField.setBounds(141, 58, 283, 20);
		contentPane.add(templateDirectoryField);
		templateDirectoryField.setColumns(10);
		
		templateDirectoryField.setText(settings.getKeyVal("TemplateDirectory"));
		
		btnSave.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				settings.setProp("CSVSaveLocation", defaultSaveDirectoryField.getText());
				settings.setProp("DefaultProfile", (String) comboBox.getSelectedItem());
				settings.setProp("TemplateDirectory", templateDirectoryField.getText());
				settings.saveConfig();
				dispose();
			}
		});
		
		setDefaultsBtn.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				settings.loadDefaultConfig();
				defaultSaveDirectoryField.setText(settings.getKeyVal("CSVSaveLocation"));
				templateDirectoryField.setText(settings.getKeyVal("TemplateDirectory"));
			}
		});
		
	}
}
