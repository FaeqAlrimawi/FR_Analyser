package ie.lero.spare.franalyser.GUI;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.awt.event.ActionEvent;
import org.eclipse.wb.swing.FocusTraversalOnArray;
import ie.lero.spare.franalyser.AssetMap;
import ie.lero.spare.franalyser.BigraphAnalyser;
import ie.lero.spare.franalyser.Mapper;
import ie.lero.spare.franalyser.PredicateGenerator;
import ie.lero.spare.franalyser.PredicateHandler;
import ie.lero.spare.franalyser.utility.FileManipulator;
import java.awt.Component;
import java.awt.Desktop;

import javax.xml.xquery.XQException;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import java.awt.Rectangle;
import java.awt.Font;
import java.awt.SystemColor;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;

public class BasicWindow {

	private Mapper mapper;
	private AssetMap assetMap;
	private PredicateGenerator predicateGenerator;
	private PredicateHandler predicateHandler;
	private BigraphAnalyser bigraphAnalyser;
	private String outputFileName;
	private StringBuilder screenOutput = new StringBuilder();
	private String bigraphFileName = "sb3.big";
	private JFrame frmForensicReadinessAnalysis;
	
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
					BasicWindow window = new BasicWindow();
					window.frmForensicReadinessAnalysis.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public BasicWindow() {
		initialize();
		mapper = new Mapper("match_query.xq");
		assetMap = new AssetMap();
		//predicateGenerator = new PredicateGenerator(assetMap);
		predicateHandler = new PredicateHandler();
	}

	/**
	 * Initialise the contents of the frame.
	 */
	private void initialize() {
		frmForensicReadinessAnalysis = new JFrame();
		frmForensicReadinessAnalysis.getContentPane().setBackground(SystemColor.window);
		frmForensicReadinessAnalysis.setBackground(UIManager.getColor("InternalFrame.inactiveTitleGradient"));
		frmForensicReadinessAnalysis.setFont(new Font("Dialog", Font.PLAIN, 12));
		frmForensicReadinessAnalysis.setTitle("Forensic Readiness Analysis tool");
		frmForensicReadinessAnalysis.setBounds(100, 100, 1200, 908);
		frmForensicReadinessAnalysis.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmForensicReadinessAnalysis.getContentPane().setLayout(null);
		frmForensicReadinessAnalysis.setLocationByPlatform(true);
		
		JPanel panel = new JPanel();
		panel.setBackground(SystemColor.window);
		panel.setBorder(new TitledBorder(null, "Output", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setBounds(331, 28, 811, 710);
		frmForensicReadinessAnalysis.getContentPane().add(panel);
		panel.setLayout(null);
		JScrollPane spane = new JScrollPane();
		spane.setViewportBorder(null);
		spane.setBounds(new Rectangle(26, 41, 759, 641));
		
		spane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		spane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panel.add(spane);
		
		JEditorPane dtrpnTest = new JEditorPane();
		dtrpnTest.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent arg0) {
				if (arg0.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				System.out.println(arg0.getURL().getPath());
				String name = arg0.getURL().getPath().replaceFirst("/", "");
				File myfile = new File(name);
				String path = myfile.getAbsolutePath();
				if(Desktop.isDesktopSupported()) {
				    try {
						Desktop.getDesktop().open(myfile);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				}
			}
		});
		spane.setViewportView(dtrpnTest);
		dtrpnTest.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
		dtrpnTest.setForeground(Color.WHITE);
		dtrpnTest.setBackground(Color.BLACK);
		dtrpnTest.setEditable(false);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBackground(SystemColor.window);
		panel_1.setBorder(new TitledBorder(null, "Steps", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.setBounds(26, 28, 300, 402);
		frmForensicReadinessAnalysis.getContentPane().add(panel_1);
		panel_1.setLayout(null);
		
		JButton button_2 = new JButton("Analyse Bigraph");
		button_2.setFont(new Font("Tahoma", Font.PLAIN, 22));
		button_2.setBounds(50, 309, 224, 41);
		panel_1.add(button_2);
		button_2.setEnabled(false);
		
		JButton button_1 = new JButton("Insert Predicates");
		button_1.setFont(new Font("Tahoma", Font.PLAIN, 22));
		button_1.setBounds(50, 224, 224, 41);
		panel_1.add(button_1);
		button_1.setEnabled(false);
		button_1.setToolTipText("Inserts generated predicates into Bigraph file");
		
		JButton button = new JButton("Generate Predicates");
		button.setFont(new Font("Tahoma", Font.PLAIN, 22));
		button.setBounds(50, 132, 224, 41);
		panel_1.add(button);
		button.setEnabled(false);
		
		JButton btnNewButton = new JButton("Find Matches");
		btnNewButton.setFont(new Font("Tahoma", Font.PLAIN, 22));
		btnNewButton.setBounds(50, 48, 224, 41);
		panel_1.add(btnNewButton);
		
		JLabel label = new JLabel("1.");
		label.setBounds(14, 46, 46, 41);
		panel_1.add(label);
		
		JLabel label_1 = new JLabel("2.");
		label_1.setBounds(14, 132, 46, 41);
		panel_1.add(label_1);
		
		JLabel label_2 = new JLabel("3.");
		label_2.setBounds(14, 226, 46, 41);
		panel_1.add(label_2);
		
		JLabel label_3 = new JLabel("4.");
		label_3.setBounds(14, 309, 46, 41);
		panel_1.add(label_3);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBackground(SystemColor.window);
		panel_2.setBorder(new TitledBorder(null, "Files", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_2.setBounds(26, 432, 300, 306);
		frmForensicReadinessAnalysis.getContentPane().add(panel_2);
		panel_2.setLayout(null);
		
		JButton btnIncidentxml = new JButton("Incident.xml");
		btnIncidentxml.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent arg0) {
				btnIncidentxml.setForeground(SystemColor.textHighlight);
			//	btnIncidentxml.u
			}
			@Override
			public void mouseExited(MouseEvent arg0) {
				btnIncidentxml.setForeground(new Color(0, 0, 255));
			}
		});
		//btnIncidentxml.setBorderPainted(false);
		btnIncidentxml.setContentAreaFilled(false);
		btnIncidentxml.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				FileManipulator.openFile("incident.xml");
			}
		});
		btnIncidentxml.setForeground(new Color(0, 0, 255));
		btnIncidentxml.setBounds(14, 43, 260, 41);
		panel_2.add(btnIncidentxml);
		
		JButton btnSpacexml = new JButton("Space.xml");
		btnSpacexml.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				btnSpacexml.setForeground(SystemColor.textHighlight);
			}
			@Override
			public void mouseExited(MouseEvent e) {
				btnSpacexml.setForeground(new Color(0, 0, 255));
			}
		});
		btnSpacexml.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileManipulator.openFile("space.xml");
			}
		});
		btnSpacexml.setForeground(Color.BLUE);
		btnSpacexml.setContentAreaFilled(false);
		btnSpacexml.setBackground(Color.CYAN);
		btnSpacexml.setBounds(14, 102, 260, 41);
		panel_2.add(btnSpacexml);
		
		JButton btnSbbig = new JButton("sb3.big");
		btnSbbig.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				btnSbbig.setForeground(SystemColor.textHighlight);
			}
			@Override
			public void mouseExited(MouseEvent e) {
				btnSbbig.setForeground(new Color(0, 0, 255));
			}
		});
		btnSbbig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileManipulator.openFile("sb3.big");
			}
		});
		btnSbbig.setForeground(Color.BLUE);
		btnSbbig.setContentAreaFilled(false);
		btnSbbig.setBackground(Color.CYAN);
		btnSbbig.setBounds(14, 224, 260, 41);
		panel_2.add(btnSbbig);
		
		JButton btnMatchqueryxq = new JButton("match_query.xq");
		btnMatchqueryxq.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				btnMatchqueryxq.setForeground(SystemColor.textHighlight);
			}
			@Override
			public void mouseExited(MouseEvent e) {
				btnMatchqueryxq.setForeground(new Color(0,0,255));
			}
		});
		btnMatchqueryxq.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileManipulator.openFile("match_query.xq");
			}
		});
		btnMatchqueryxq.setForeground(Color.BLUE);
		btnMatchqueryxq.setContentAreaFilled(false);
		btnMatchqueryxq.setBackground(Color.CYAN);
		btnMatchqueryxq.setBounds(14, 161, 260, 41);
		panel_2.add(btnMatchqueryxq);
		
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					
					screenOutput.append("<p><font color=\"white\">---------------------------Matching Assets---------------------------<br/>");
					screenOutput.append(">Matching between entities of an incident model in file <a href=\"file:///incident.xml\">incident.xml</a> and entities of a space model in file <a href=\"file:///space.xml\">space.xml</a>.<br/>");
					screenOutput.append("<br/>>Matching is carried out using Xquery language, and it is implemented in <a href=\"file:///match_query.xq\">match_query.xq</a>.<br/>");
					screenOutput.append("<br/>>Result has the following format:<br/></font>");
					screenOutput.append("<font color=\"green\">Incident_Asset_Name</font> <font color=\"white\"> ==> </font> <font color=\"yellow\">matched_space_asset1 | matched_space_asset2 | ...<br/></p>");
					assetMap = mapper.findMatches();
					screenOutput.append("<p><font color=\"white\">>Result</font><br/></p>");
					
					for(String nm : assetMap.getIncidentAssetNames()) {
						screenOutput.append("<font color=\"green\">").append(nm).append("</font> <font color=\"white\"> ==> </font> ");
						String [] sr = assetMap.getSpaceAssetMatched(nm);
						if(sr != null)
						for(String s: sr) {
							screenOutput.append("<font color=\"yellow\">").append(s+"</font> <font color=\"white\"> | </font>");
						}
						screenOutput.append("<br/>");
					}
					screenOutput.append("</p>");
					
					dtrpnTest.setContentType("text/html");
					dtrpnTest.setText(screenOutput.toString());
					
					//enable the predicate generation button
					button.setEnabled(true);
					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (XQException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		frmForensicReadinessAnalysis.getContentPane().setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{btnNewButton, button, button_1, button_2}));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				predicateGenerator = new PredicateGenerator(assetMap);
				predicateHandler = predicateGenerator.generatePredicates();
				
				screenOutput.append("<p><font color=\"white\"><br/><br/>---------------------------Predicates Generation---------------------------<br/>");
				screenOutput.append(">Generating Predicates in Bigrapher format<br/>")
				.append("<br/>>Result below shows information of a predicate in the following format:<br/>")
				.append("{Name:value[ConditionName], Type:value[Preconditon, Postcondition], ActivityName:value, Predicate:value[in Bigraph format]}<br/></font></p>")
				.append("<br/><p><font color=\"white\">>Result<br/>")
				.append(predicateHandler.toString())
				.append("</font></p>");
				
				dtrpnTest.setContentType("text/html");
				dtrpnTest.setText(screenOutput.toString());
				button_1.setEnabled(true);				
			}
		});
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				screenOutput.append("<p><font color=\"white\"><br/><br/>---------------------------Inserting Predicates---------------------------<br/>");
				screenOutput.append(">Inserting predicates into file <a href=\"file:///sb3.big\">sb3.big</a> and generating new Bigrapher file...<br/>");
				screenOutput.append("<br/>>Bigrapher predicates to insert are as follows:<br/></font>")
				.append("<font color=\"yellow\">");
				String [] preds = predicateHandler.convertToBigraphPredicateStatement().split(";");
				for(String s: preds) {
					screenOutput.append(s).append("<br/>");
				}
				screenOutput.append("</font>");
				outputFileName = predicateHandler.insertPredicatesIntoBigraphFile(bigraphFileName);
				
				if(outputFileName != null && !outputFileName.isEmpty()){
					screenOutput.append("<font color=\"white\"><br/>>Successfully completed. Output file name is: <a href=\"file:///").append(outputFileName).append("\">").append(outputFileName).append("</a><br/>");
				}
				else {
					screenOutput.append("<font color=\"white\"><br/>>Operation is</font> <font color=\"red\">NOT</font> <font color=\"white\">complete. Please see logs for more information on the issue.<br/></font></p>");
				}
				
				dtrpnTest.setContentType("text/html");
				dtrpnTest.setText(screenOutput.toString());
				
				button_2.setEnabled(true);
				
			}
		});
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			//	bigraphAnalyser = new BigraphAnalyser(outputFileName);
				
				screenOutput.append("<p><font color=\"white\"><br/><br/>---------------------------Bigraph Analysis---------------------------<br/>");
				screenOutput.append("<p><font color=\"white\">>>Analysis phase using Bigrapher: currently includes validating and executing the generated bigrapher file: <a href=\"file:///").append(outputFileName).append("\">").append(outputFileName).append("</a><br/>");
				screenOutput.append("<br/>>For the moment the original fle [sb3.big] is used for validation and execution since the generated cannot be executed because inserted predicates do not match the defined controls<br/>");
				screenOutput.append("<br/>>Validation command used is: <font color=\"yellow\">bigrapher validate -n ");//.append(bigraphAnalyser.getBigrapherFileName()).append("</font>");
			//	bigraphAnalyser.setBigrapherFileName("sb3.big");	
				/*if(bigraphAnalyser.validateBigraph()) {
					screenOutput.append("<br/><font color=\"white\"><br/>>Bigrapher file ["+ bigraphAnalyser.getBigrapherFileName() +"] is valide. Proceeding to file execution.<br/>");
					screenOutput.append("<br/>>Bigrapher command used is:<br/></font> <font color=\"yellow\">").append(bigraphAnalyser.createDefaultBigrapherExecutionCmd()).append("</font><br/>");
					if (bigraphAnalyser.executeBigraph()){
						screenOutput.append("<font color=\"white\"><br/>>Execution is done. Output folder is <a href=\"file:///").append(bigraphAnalyser.getBigrapherExecutionOutputFolder()).append("\">")
						.append(bigraphAnalyser.getBigrapherExecutionOutputFolder()).append("<br/>");
					} else {
						screenOutput.append("<br/>>Execution is</font> <font color=\"red\"> NOT </font> <font color=\"white\">done. Please see logs for more information of the issue.<br/></font</p>");
					}
				} else {
					screenOutput.append("<br/>>Bigrapher file ["+ outputFileName +"] is <font color=\"red\"> NOT </font> <font color=\"white\">valide. See logs for more information on the issue.<br/></font</p>");
				}*/
				
				dtrpnTest.setContentType("text/html");
				dtrpnTest.setText(screenOutput.toString());
			}
		});
	}
	
}
