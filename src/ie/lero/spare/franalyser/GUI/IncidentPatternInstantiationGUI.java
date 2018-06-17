package ie.lero.spare.franalyser.GUI;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.management.ThreadInfo;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
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
import java.awt.Toolkit;

import javax.swing.JLabel;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
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
import javax.swing.ListSelectionModel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JList;
import javax.swing.JScrollBar;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

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
	private int windowWidth = 2500;//(int)(Toolkit.getDefaultToolkit().getScreenSize().getWidth()*0.75);
	private int windowHeight = 2000;//(int)(Toolkit.getDefaultToolkit().getScreenSize().getHeight()*0.9);
	private int threadPoolSize = 1;
	private JProgressBar progressBar;
	private static IncidentPatternInstantiationGUI window;
	private Thread instanceThread;
	private JEditorPane logger;
	private JLabel labelProgressBar;
	private JTextPane textPane;
	private int margin = 100;
	private int margin_medium = 50;
	private JTextField txtIncidentPattern;
	private JTextField txtSystemModel;
	private String incidentPatternFilePath;
	private String systemModelFilePath;
	private JScrollPane assetSetScroll_2;
	private JPanel assetSetPanel;
	private JCheckBox [] checkBoxsAssetSets;
	private IncidentPatternInstantiator incidentInstantiator;
	private JButton btnAnalyseSelectedSets;
	
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
		/*mapper = new Mapper("match_query.xq");
		assetMap = new AssetMap();
		//predicateGenerator = new PredicateGenerator(assetMap);
		predicateHandler = new PredicateHandler();*/
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
		panel.setBounds(45, 1306, 2400, 594);
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
		
		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		separator.setBounds(587, 79, 48, 1179);
		frmForensicReadinessAnalysis.getContentPane().add(separator);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(45, 1286, windowWidth-margin, 22);
		
		frmForensicReadinessAnalysis.getContentPane().add(separator_1);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(612, 84, 1838, 1174);
		JPanel summaryPanel = new JPanel();
		summaryPanel.setForeground(SystemColor.window);
		summaryPanel.setBorder(tabbedPane.getBorder());
		JPanel instancesListPanel = new JPanel();
		JPanel instanceViewerPanel = new JPanel();
		
		//tabbedPane.add(summaryPanel);
		tabbedPane.add("Summary", summaryPanel);
		summaryPanel.setLayout(null);
		
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setBounds(summaryPanel.getX()+margin_medium, summaryPanel.getY()+margin_medium+10, tabbedPane.getWidth()-margin, 34);
		
		summaryPanel.add(progressBar);
		
		labelProgressBar = new JLabel("");
		labelProgressBar.setBounds(50, 24, 467, 33);
		summaryPanel.add(labelProgressBar);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new TitledBorder(null, "Asset Map Info", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		scrollPane.setBounds(36, 134, 632, 418);
		summaryPanel.add(scrollPane);
		
		textPane = new JTextPane();
		textPane.setToolTipText("Shows information about the asset map");
		scrollPane.setViewportView(textPane);
		textPane.setEditable(false);
		
		JSeparator separator_2 = new JSeparator();
		separator_2.setOrientation(SwingConstants.VERTICAL);
		separator_2.setBounds(694, 134, 23, 418);
		summaryPanel.add(separator_2);
		
		assetSetScroll_2 = new JScrollPane();
		assetSetScroll_2.setViewportBorder(new TitledBorder(null, "Asset Sets Info", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		assetSetScroll_2.setBounds(719, 134, 1069, 418);
		summaryPanel.add(assetSetScroll_2);
		
		assetSetPanel = new JPanel();
		assetSetScroll_2.setViewportView(assetSetPanel);
		assetSetPanel.setLayout(new BoxLayout(assetSetPanel, BoxLayout.Y_AXIS));
		
		btnAnalyseSelectedSets = new JButton("Analyse selected sets");
		btnAnalyseSelectedSets.setEnabled(false);
		btnAnalyseSelectedSets.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				analyseSelectedAssetsets();
			}
		});
		btnAnalyseSelectedSets.setBounds(1495, 556, 295, 51);
		summaryPanel.add(btnAnalyseSelectedSets);
		tabbedPane.setBackgroundAt(0, SystemColor.desktop);
		tabbedPane.add("Instances List", instancesListPanel);
		tabbedPane.add("Instance Details", instanceViewerPanel);
		
		frmForensicReadinessAnalysis.getContentPane().add(tabbedPane);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBackground(SystemColor.window);
		panel_2.setBounds(12, 79, 549, 1179);
		frmForensicReadinessAnalysis.getContentPane().add(panel_2);
		
		txtIncidentPattern = new JTextField();
		txtIncidentPattern.setToolTipText("incident pattern file");
		txtIncidentPattern.setBounds(26, 50, 364, 63);
		txtIncidentPattern.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				if(txtIncidentPattern.getText().isEmpty()) {
					txtIncidentPattern.setForeground(Color.lightGray);
					txtIncidentPattern.setText("incident pattern...");
				}
			}
		});
		txtIncidentPattern.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				
			}
			@Override
			public void mousePressed(MouseEvent e) {
				if(txtIncidentPattern.getForeground().equals(Color.LIGHT_GRAY)){
					txtIncidentPattern.setForeground(Color.black);
					txtIncidentPattern.setText("");
				}
				
			}
			@Override
			public void mouseExited(MouseEvent e) {
				
			}
		});
		txtIncidentPattern.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			
			}
		});
		panel_2.setLayout(null);
		txtIncidentPattern.setForeground(Color.LIGHT_GRAY);
		txtIncidentPattern.setText("Incident pattern...");
		txtIncidentPattern.setColumns(10);
		panel_2.add(txtIncidentPattern);
		
		JButton btnBrowse = new JButton("Browse");
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				int returnVal = fc.showOpenDialog(frmForensicReadinessAnalysis);

		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fc.getSelectedFile();
		            incidentPatternFilePath = file.toURI().toString();
		            
		            txtIncidentPattern.setText(file.getName());
		            txtIncidentPattern.setForeground(Color.black);
		        } else {
		            incidentPatternFilePath = "";
		        }
			}
		});
		btnBrowse.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
			}
		});
		btnBrowse.setBounds(404, 50, 119, 63);
		panel_2.add(btnBrowse);
		
		txtSystemModel = new JTextField();
		txtSystemModel.setToolTipText("system model file");
		txtSystemModel.setBounds(26, 162, 364, 63);
		txtSystemModel.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if(txtSystemModel.getText().isEmpty()) {
					txtSystemModel.setForeground(Color.LIGHT_GRAY);
					txtSystemModel.setText("system model...");
				}
			}
		});
		txtSystemModel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
			}
			@Override
			public void mousePressed(MouseEvent e) {
				if(txtSystemModel.getForeground().equals(Color.LIGHT_GRAY)) {
					txtSystemModel.setText("");
					txtSystemModel.setForeground(Color.black);	
				}
				
			}
		});
		txtSystemModel.setText("System model...");
		txtSystemModel.setForeground(Color.LIGHT_GRAY);
		txtSystemModel.setColumns(10);
		panel_2.add(txtSystemModel);
		
		JButton button = new JButton("Browse");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				int returnVal = fc.showOpenDialog(frmForensicReadinessAnalysis);

		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fc.getSelectedFile();
		            systemModelFilePath = file.toURI().toString();
		            txtSystemModel.setText(file.getName());
		            txtSystemModel.setForeground(Color.black);
		        } else {
		            systemModelFilePath = "";
		        }
			}
		});
		button.setBounds(404, 162, 119, 63);
		panel_2.add(button);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBounds(36, 415, 487, 236);
		panel_2.add(panel_1);
		panel_1.setBackground(SystemColor.window);
		panel_1.setForeground(Color.BLACK);
		panel_1.setBorder(new TitledBorder(null, "Options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.setLayout(null);
		
		JLabel lblThreadPoolSize = new JLabel("Thread pool size");
		lblThreadPoolSize.setToolTipText("determines the number of threads that can be running in parallel. Max. is 10");
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
		spinner.setModel(new SpinnerNumberModel(1, 1, 10, 1));
		spinner.setBounds(212, 64, 54, 40);
		panel_1.add(spinner);
		
		JButton btnNewButton = new JButton("generate incident instances");
		btnNewButton.setBounds(36, 738, 487, 62);
		panel_2.add(btnNewButton);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					window.createInstance();
			}
		});
		panel_2.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{txtIncidentPattern}));
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
				incidentInstantiator = new IncidentPatternInstantiator();
				if(incidentPatternFilePath == null || incidentPatternFilePath.isEmpty()) {
					//execute with default file names
					incidentInstantiator.execute(threadPoolSize, window);	
				} else {
					incidentInstantiator.execute(incidentPatternFilePath, systemModelFilePath, threadPoolSize, window);
				}
				
			}
			
		};
		
		instanceThread = new Thread(instance);
		instanceThread.start();
		logger.setText("");
		progressBar.setValue(0);
		labelProgressBar.setText("Running...");
		
	}

	@Override
	public void updateAssetMapInfo(String msg) {
		// TODO Auto-generated method stub
		textPane.setText(textPane.getText()+"\n"+msg);
	}

	@Override
	public void updateAssetSetInfo(LinkedList<String[]> assetSets) {
		// TODO Auto-generated method stub
		StringBuilder str = new StringBuilder();
		
		//JLabel [] labels = new JLabel[assetSets.size()];
		String [] labels = new String[assetSets.size()];
		
		checkBoxsAssetSets = new JCheckBox[assetSets.size()+2];
		
		checkBoxsAssetSets[0] = new JCheckBox("All ("+assetSets.size()+")");
		checkBoxsAssetSets[1] = new JCheckBox("None");
		
		checkBoxsAssetSets[0].addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if(checkBoxsAssetSets[0].isSelected()) {
					checkBoxsAssetSets[1].setSelected(false);
					for(int i=2; i<checkBoxsAssetSets.length;i++) {
						checkBoxsAssetSets[i].setSelected(true);
					}
				}
			}
		});
		checkBoxsAssetSets[0].setSelected(true);
		checkBoxsAssetSets[1].addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if(checkBoxsAssetSets[1].isSelected()) {
					checkBoxsAssetSets[0].setSelected(false);
					for(int i=2; i<checkBoxsAssetSets.length;i++) {
						checkBoxsAssetSets[i].setSelected(false);
					}
				}
			}
		});
		assetSetPanel.add(checkBoxsAssetSets[0]);
		assetSetPanel.add(checkBoxsAssetSets[1]);
		
		for(int i=0;i<assetSets.size();i++) {
			//listAssetSets.add(new JLabel("set ["+i+"]"));
			str.delete(0, str.length());
			str.append("set [").append(i).append("]: ")
			.append(Arrays.toString(assetSets.get(i)));
			labels[i] = str.toString();
			checkBoxsAssetSets[i+2] = new JCheckBox(labels[i]);
			
			//listAssetSets.add(checks[i]);
			//checks[i].setBounds(assetSetScroll_2.getX()+10, assetSetScroll_2.getY()+10, assetSetScroll_2.getWidth()-20, 50);
			checkBoxsAssetSets[i+2].setSelected(true);
			assetSetPanel.add(checkBoxsAssetSets[i+2]);			
		}
		
		
		assetSetPanel.validate();
		btnAnalyseSelectedSets.setEnabled(true);
		labelProgressBar.setText("Select asset sets to analyse.");
		
		//listAssetSets.setListData(checks);
	}
	
	private void analyseSelectedAssetsets() {
		LinkedList<Integer> selectedSets = new LinkedList<Integer>();
		
		
		if(checkBoxsAssetSets[0].isSelected()) {
			for(int i=2;i<checkBoxsAssetSets.length;i++) {
				selectedSets.add(i-2);
			}
		} else {
			for(int i=2;i<checkBoxsAssetSets.length;i++) {
				if(checkBoxsAssetSets[i].isSelected()) {
					selectedSets.add(i-2);
				}
			}
		}
		
		if(selectedSets.size()>0) {
			labelProgressBar.setText("Analysing...");
			incidentInstantiator.setAssetSetsSelected(selectedSets);
			incidentInstantiator.setSetsSelected(true);
		} else {
			labelProgressBar.setText("No asset sets are selected.");
		}
		
	}
	
	private void cancelExecution(){
		labelProgressBar.setText("Execution cancelled.");
		progressBar.setValue(0);
	}
}
