package ie.lero.spare.franalyser.GUI;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.JLabel;
import javax.swing.Box;
import javax.swing.JFileChooser;

import java.awt.Component;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;
import org.eclipse.wb.swing.FocusTraversalOnArray;

public class MainGUI extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	private String incidentFileName;
	private String spaceFileName;
	private JTextField textField_1;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		try {
	        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    } catch(Exception e) {
	        System.out.println("Error setting native LAF: " + e);
	    }
	    
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainGUI frame = new MainGUI();
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
	public MainGUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1000, 622);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		Box verticalBox = Box.createVerticalBox();
		verticalBox.setFont(null);
		verticalBox.setBounds(18, 141, 285, 58);
		contentPane.add(verticalBox);
		
		JLabel lblEnvironmentModel = new JLabel("Environment Model");
		verticalBox.add(lblEnvironmentModel);
		lblEnvironmentModel.setFont(new Font("Tahoma", Font.PLAIN, 20));
		
		Box horizontalBox = Box.createHorizontalBox();
		verticalBox.add(horizontalBox);
		
		textField_1 = new JTextField();
		textField_1.setFont(new Font("Tahoma", Font.PLAIN, 20));
		horizontalBox.add(textField_1);
		textField_1.setColumns(10);
		textField_1.setAlignmentX(1.0f);
		
		JButton button = new JButton("...");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				incidentFileName = getFileFromFileChooser(textField_1);
			}
		});
		horizontalBox.add(button);
		button.setFont(new Font("Tahoma", Font.PLAIN, 20));
		verticalBox.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{lblEnvironmentModel, horizontalBox, textField_1, button}));
		
		Box verticalBox_1 = Box.createVerticalBox();
		verticalBox_1.setBounds(18, 66, 288, 58);
		contentPane.add(verticalBox_1);
		
		JLabel lblDomainindependentIncident = new JLabel("Domain-Independent Incident");
		verticalBox_1.add(lblDomainindependentIncident);
		lblDomainindependentIncident.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblDomainindependentIncident.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		Box horizontalBox_1 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_1);
		
		textField = new JTextField();
		textField.setFont(new Font("Tahoma", Font.PLAIN, 20));
		horizontalBox_1.add(textField);
		textField.setAlignmentX(Component.RIGHT_ALIGNMENT);
		textField.setColumns(10);
		
		JButton btnChooseFile = new JButton("...");
		horizontalBox_1.add(btnChooseFile);
		btnChooseFile.setFont(new Font("Tahoma", Font.PLAIN, 20));
		btnChooseFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				incidentFileName = getFileFromFileChooser(textField);
			}
		});
		
		
	}
	
	public String getFileFromFileChooser(Component comp) {
		
		String name = null;
		
		JFileChooser chooser = new JFileChooser();
		
		int returnVal = chooser.showOpenDialog(this);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			 name = chooser.getSelectedFile().getName();
		   System.out.println("You chose to open this file: " + name);
		   ((JTextField)(comp)).setText(name);
		}
		
		return name;
	}
}
