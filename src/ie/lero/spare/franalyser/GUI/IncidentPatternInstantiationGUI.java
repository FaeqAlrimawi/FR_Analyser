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

import i.e.lero.spare.pattern_instantiation.AssetMap;
import i.e.lero.spare.pattern_instantiation.BigraphAnalyser;
import i.e.lero.spare.pattern_instantiation.IncidentPatternInstantiator;
import i.e.lero.spare.pattern_instantiation.Mapper;
import i.e.lero.spare.pattern_instantiation.PredicateGenerator;
import i.e.lero.spare.pattern_instantiation.PredicateHandler;
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
import javax.swing.JTextField;
import javax.swing.BoxLayout;
import javax.swing.SwingConstants;

public class IncidentPatternInstantiationGUI {

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
					IncidentPatternInstantiationGUI window = new IncidentPatternInstantiationGUI();
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
	public IncidentPatternInstantiationGUI() {
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
		frmForensicReadinessAnalysis.setTitle("Incident Pattern Instantiation");
		frmForensicReadinessAnalysis.setBounds(100, 100, 1200, 1091);
		frmForensicReadinessAnalysis.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmForensicReadinessAnalysis.getContentPane().setLayout(null);
		frmForensicReadinessAnalysis.setLocationByPlatform(true);
		
		JPanel panel = new JPanel();
		panel.setBackground(SystemColor.window);
		panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Log", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel.setBounds(26, 239, 1116, 710);
		frmForensicReadinessAnalysis.getContentPane().add(panel);
		panel.setLayout(null);
		JScrollPane spane = new JScrollPane();
		spane.setViewportBorder(null);
		spane.setBounds(new Rectangle(26, 41, 1060, 641));
		
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
		
		JPanel panel_4 = new JPanel();
		panel_4.setBorder(new TitledBorder(null, "Incident Pattern", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_4.setBackground(SystemColor.window);
		panel_4.setBounds(604, 28, 436, 120);
		frmForensicReadinessAnalysis.getContentPane().add(panel_4);
		panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));
		
		JButton btnIncidentxml = new JButton("Incident Pattern");
		btnIncidentxml.setHorizontalAlignment(SwingConstants.LEFT);
		panel_4.add(btnIncidentxml);
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
		
		JPanel panel_3 = new JPanel();
		panel_3.setBounds(100, 28, 478, 120);
		frmForensicReadinessAnalysis.getContentPane().add(panel_3);
		panel_3.setBackground(SystemColor.window);
		panel_3.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Research Center Representation", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));
		
		JButton btnSpacexml = new JButton("Research center model");
		btnSpacexml.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileManipulator.openFile("etc/research_centre_model.environment");
			}
		});
		panel_3.add(btnSpacexml);
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
		btnSpacexml.setForeground(Color.BLUE);
		btnSpacexml.setContentAreaFilled(false);
		btnSpacexml.setBackground(Color.CYAN);
		
		JButton btnSbbig = new JButton("BRS model");
		panel_3.add(btnSbbig);
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
				FileManipulator.openFile("etc/research_centre_system.big");
			}
		});
		btnSbbig.setForeground(Color.BLUE);
		btnSbbig.setContentAreaFilled(false);
		btnSbbig.setBackground(Color.CYAN);
		
		JButton btnNewButton = new JButton("Start Analysis");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				IncidentPatternInstantiator inc = new IncidentPatternInstantiator();
				//inc.execute();
			}
		});
		btnNewButton.setBounds(350, 166, 478, 62);
		frmForensicReadinessAnalysis.getContentPane().add(btnNewButton);
	}
}
