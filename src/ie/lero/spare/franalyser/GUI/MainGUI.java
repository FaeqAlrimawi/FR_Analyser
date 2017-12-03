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
import javax.swing.border.TitledBorder;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.FlowLayout;

public class MainGUI extends JFrame {

	private JPanel contentPane;
	private JTextField txtEnterDomainindependentIncident;
	private String incidentFileName;
	private String spaceFileName;
	private JTextField textField_1;
	private JTextField textField;
	private String bigrapherFileName;
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
		setBounds(100, 100, 1000, 914);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Input", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel.setBounds(23, 58, 310, 269);
		contentPane.add(panel);
		panel.setLayout(null);
		
		Box verticalBox_1 = Box.createVerticalBox();
		verticalBox_1.setBounds(13, 40, 284, 58);
		panel.add(verticalBox_1);
		
		JLabel lblDomainindependentIncident = new JLabel("Domain-Independent Incident");
		verticalBox_1.add(lblDomainindependentIncident);
		lblDomainindependentIncident.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblDomainindependentIncident.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		Box horizontalBox_1 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_1);
		
		txtEnterDomainindependentIncident = new JTextField();
		txtEnterDomainindependentIncident.setFont(new Font("Tahoma", Font.PLAIN, 20));
		horizontalBox_1.add(txtEnterDomainindependentIncident);
		txtEnterDomainindependentIncident.setAlignmentX(Component.RIGHT_ALIGNMENT);
		txtEnterDomainindependentIncident.setColumns(10);
		
		JButton btnChooseFile = new JButton("...");
		horizontalBox_1.add(btnChooseFile);
		btnChooseFile.setFont(new Font("Tahoma", Font.PLAIN, 20));
		
		Box verticalBox = Box.createVerticalBox();
		verticalBox.setBounds(13, 111, 284, 58);
		panel.add(verticalBox);
		verticalBox.setFont(null);
		
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
		
		Box verticalBox_2 = Box.createVerticalBox();
		verticalBox_2.setBounds(13, 181, 284, 60);
		panel.add(verticalBox_2);
		
		JLabel lblEnvironmentBigrapherFile = new JLabel("Environment Bigrapher File");
		verticalBox_2.add(lblEnvironmentBigrapherFile);
		lblEnvironmentBigrapherFile.setFont(new Font("Tahoma", Font.PLAIN, 20));
		
		Box horizontalBox_2 = Box.createHorizontalBox();
		verticalBox_2.add(horizontalBox_2);
		
		textField = new JTextField();
		textField.setFont(new Font("Tahoma", Font.PLAIN, 20));
		textField.setColumns(10);
		textField.setAlignmentX(1.0f);
		horizontalBox_2.add(textField);
		
		JButton button_1 = new JButton("...");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bigrapherFileName = getFileFromFileChooser(textField);
			}
		});
		button_1.setFont(new Font("Tahoma", Font.PLAIN, 20));
		horizontalBox_2.add(button_1);
		panel.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{verticalBox_1, lblDomainindependentIncident, horizontalBox_1, txtEnterDomainindependentIncident, btnChooseFile, verticalBox, lblEnvironmentModel, horizontalBox, textField_1, button}));
		btnChooseFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				incidentFileName = getFileFromFileChooser(txtEnterDomainindependentIncident);
			}
		});
		
		
	}
	
	public String getFileFromFileChooser(Component comp) {
		
		String name = null;
		
		JFileChooser chooser = new JFileChooser();
		
		int returnVal = chooser.showOpenDialog(this);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			 name = chooser.getSelectedFile().getName();
		   ((JTextField)(comp)).setText(name);
		}
		
		return name;
	}
}
