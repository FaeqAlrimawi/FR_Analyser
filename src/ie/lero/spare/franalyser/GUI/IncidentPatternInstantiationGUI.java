package ie.lero.spare.franalyser.GUI;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.management.ThreadInfo;
import java.util.Hashtable;
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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JTabbedPane;
import javax.swing.JProgressBar;
import javax.swing.JTextPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class IncidentPatternInstantiationGUI implements IncidentPatternInstantiationListener{

	private Mapper mapper;
	private AssetMap assetMap;
	private PredicateGenerator predicateGenerator;
	private PredicateHandler predicateHandler;
	private BigraphAnalyser bigraphAnalyser;
	private String outputFileName;
	private StringBuilder screenOutput = new StringBuilder();
	private String bigraphFileName = "sb3.big";
	private JFrame frmForensicReadinessAnalysis;
	private int windowWidth = 2100;
	private int windowHeight = 1900;
	private int threadPoolSize = 4;
	private JProgressBar progressBar;
	private static IncidentPatternInstantiationGUI window;
	private Thread instanceThread;
	private JEditorPane logger;
	private JLabel labelProgressBar;
	private JTextPane textPane;
	
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
					window = new IncidentPatternInstantiationGUI();
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
		frmForensicReadinessAnalysis.setTitle("Potential Incident Generation");
		frmForensicReadinessAnalysis.setBounds(100, 100, windowWidth, windowHeight);
		frmForensicReadinessAnalysis.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmForensicReadinessAnalysis.getContentPane().setLayout(null);
		frmForensicReadinessAnalysis.setLocationByPlatform(true);
		
		JPanel panel = new JPanel();
		
		panel.setBackground(SystemColor.window);
		panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Log", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel.setBounds(45, 1011, 1983, 773);
		frmForensicReadinessAnalysis.getContentPane().add(panel);
		panel.setLayout(null);
		JScrollPane spane = new JScrollPane();
		spane.setViewportBorder(null);
		spane.setBounds(new Rectangle(50, 50, panel.getWidth()-100, panel.getHeight()-100));
		
		spane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		spane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panel.add(spane);
		
		logger = new JEditorPane();
		
		logger.addHyperlinkListener(new HyperlinkListener() {
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
		spane.setViewportView(logger);
		logger.setEditorKit(JEditorPane.createEditorKitForContentType("text"));
		logger.setForeground(Color.WHITE);
		logger.setBackground(Color.BLACK);
		logger.setEditable(false);
		logger.setBounds(spane.getBounds());
		logger.setText("<!DOCTYPE html><html><body><p style=\"color:white;font-size:40;\">starting...</p></body></html>");
		
		JPanel panel_4 = new JPanel();
		panel_4.setBorder(new TitledBorder(null, "Incident Pattern", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_4.setBackground(SystemColor.window);
		panel_4.setBounds(45, 100, 478, 120);
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
				FileManipulator.openFile("etc/scenario1/interruption_incident-pattern.cpi");
			}
		});
		btnIncidentxml.setForeground(new Color(0, 0, 255));
		
		JPanel panel_3 = new JPanel();
		panel_3.setBounds(45, 262, 478, 120);
		frmForensicReadinessAnalysis.getContentPane().add(panel_3);
		panel_3.setBackground(SystemColor.window);
		panel_3.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Research Center Representation", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));
		
		JButton btnSpacexml = new JButton("Research center model");
		btnSpacexml.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileManipulator.openFile("etc/scenario1/research_centre_model.cps");
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
				FileManipulator.openFile("etc/scenario1/research_centre_system.big");
			}
		});
		btnSbbig.setForeground(Color.BLUE);
		btnSbbig.setContentAreaFilled(false);
		btnSbbig.setBackground(Color.CYAN);
		
		JButton btnNewButton = new JButton("generate incident instances");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					window.createInstance();
			}
		});
		btnNewButton.setBounds(45, 851, 478, 62);
		frmForensicReadinessAnalysis.getContentPane().add(btnNewButton);
		
		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		separator.setBounds(587, 79, 48, 859);
		frmForensicReadinessAnalysis.getContentPane().add(separator);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBackground(SystemColor.window);
		panel_1.setForeground(Color.BLACK);
		panel_1.setBorder(new TitledBorder(null, "Options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.setBounds(45, 536, 478, 236);
		frmForensicReadinessAnalysis.getContentPane().add(panel_1);
		panel_1.setLayout(null);
		
		JLabel lblThreadPoolSize = new JLabel("Thread pool size");
		lblThreadPoolSize.setFont(new Font("Tahoma", Font.PLAIN, 24));
		lblThreadPoolSize.setBounds(26, 64, 212, 40);
		panel_1.add(lblThreadPoolSize);
		
		JSpinner spinner = new JSpinner();
		spinner.getModel().setValue(threadPoolSize);
		
		spinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				threadPoolSize = (int)spinner.getModel().getValue();
				
			}
		});
		spinner.setModel(new SpinnerNumberModel(4, 1, 10, 1));
		spinner.setBounds(212, 64, 54, 40);
		panel_1.add(spinner);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(45, 974, 1988, 22);
		frmForensicReadinessAnalysis.getContentPane().add(separator_1);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(612, 84, 1416, 854);
		JPanel summaryPanel = new JPanel();
		summaryPanel.setForeground(SystemColor.window);
		JPanel instancesListPanel = new JPanel();
		JPanel instanceViewerPanel = new JPanel();
		
		//tabbedPane.add(summaryPanel);
		tabbedPane.add("Summary", summaryPanel);
		summaryPanel.setLayout(null);
		
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setBounds(36, 72, 1359, 34);
		summaryPanel.add(progressBar);
		
		labelProgressBar = new JLabel("");
		labelProgressBar.setBounds(36, 28, 201, 33);
		summaryPanel.add(labelProgressBar);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(36, 134, 1359, 392);
		summaryPanel.add(scrollPane);
		
		textPane = new JTextPane();
		scrollPane.setViewportView(textPane);
		textPane.setEditable(false);
		tabbedPane.setBackgroundAt(0, SystemColor.desktop);
		tabbedPane.add("Instances List", instancesListPanel);
		tabbedPane.add("Instance Details", instanceViewerPanel);
		
		frmForensicReadinessAnalysis.getContentPane().add(tabbedPane);
	}

	@Override
	public void updateProgress(int progress) {
		// TODO Auto-generated method stub
		progressBar.setValue(progressBar.getValue()+progress);
	}

	@Override
	public void updateLogger(String msg) {
		// TODO Auto-generated method stub
		logger.setText(logger.getText()+msg);
	}
	
	public void createInstance() {
		
		Runnable instance = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				IncidentPatternInstantiator inc = new IncidentPatternInstantiator();
				inc.execute(threadPoolSize, window);
			}
			
		};
		
		instanceThread = new Thread(instance);
		instanceThread.start();
		logger.setText("");
		progressBar.setValue(0);
		labelProgressBar.setText("Running...");
		
	}

	@Override
	public void updateAssetSetInfo(String msg) {
		// TODO Auto-generated method stub
		textPane.setText(textPane.getText()+"\n"+msg);
	}
}
