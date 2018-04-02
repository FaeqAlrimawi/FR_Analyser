package ie.lero.spare.franalyser.GUI;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.basic.ComboPopup;
import javax.xml.xquery.XQException;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.UpdateManagerAdapter;
import org.apache.batik.bridge.UpdateManagerEvent;
import org.apache.batik.bridge.UpdateManagerListener;
import org.apache.batik.ext.awt.image.codec.imageio.ImageIODebugUtil;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.svg.SVGDocumentLoaderAdapter;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;

import i.e.lero.spare.pattern_instantiation.AssetMap;
import i.e.lero.spare.pattern_instantiation.BigraphAnalyser;
import i.e.lero.spare.pattern_instantiation.GraphPath;
import i.e.lero.spare.pattern_instantiation.Mapper;
import i.e.lero.spare.pattern_instantiation.Predicate;
import i.e.lero.spare.pattern_instantiation.PredicateGenerator;
import i.e.lero.spare.pattern_instantiation.PredicateHandler;
import ie.lero.spare.franalyser.utility.PredicateType;

import org.apache.batik.swing.svg.GVTTreeBuilderAdapter;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;

public class SVGViewer {

    public static void main(String[] args) {
        
    	try {
	        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    } catch(Exception e) {
	        System.out.println("Error setting native LAF: " + e);
	    }

        SVGViewer app = new SVGViewer();
    }

    JComboBox<String> comboBoxActivities = new JComboBox<String>();
    JComboBox<String> comboBoxPredicatesPre = new JComboBox<String>();
    private final JComboBox<String> comboBoxPost = new JComboBox<String>();
    // The frame.
    protected JFrame frame;

    // The status label.
    protected JLabel label = new JLabel();
    
    String svgFile;

    // The SVG canvas.
    protected JSVGCanvas svgCanvas = new JSVGCanvas();

    private BigraphAnalyser analyser; 
    private PredicateHandler predicateHandler;
    private Document doc;
    private  URI uri;
    private String svgFilePath = "sb3_Execution_Output/output/transitions.svg"; //make default current folder having transitions file
    private final JButton button_1 = new JButton("<");
    private final JLabel label_1 = new JLabel("0/0");
    private final JButton button_2 = new JButton(">");
    private LinkedList<GraphPath> paths;
    private Predicate precond;
    private Predicate postcond;
    private int counter = 0;
    
    public SVGViewer(JFrame f) {
        frame = f;
        initialize();
    }
    
    public SVGViewer() {
    frame = new JFrame();
      initialize();
  }
    
    public SVGViewer(String svgFilePath) {
    frame = new JFrame();
    this.svgFilePath = svgFilePath;
    initialize();
  }
  
    
    public SVGViewer(JFrame f, BigraphAnalyser analyser) {
      this(f);
      this.analyser = analyser;   
    }


    private void initialize() {
    	Mapper m = new Mapper("match_query.xq");
		AssetMap am;
		
			am = m.findMatches();
			PredicateGenerator pred = new PredicateGenerator(am);
			predicateHandler = pred.generatePredicates();
			/*analyser = new BigraphAnalyser(predicateHandler, "sb3.big");
			analyser.setBigrapherExecutionOutputFolder("sb3_Execution_Output");*/
			//analyser.analyse(false);
		
		//JFrame frame = new JFrame();
    	frame.getContentPane().setBackground(SystemColor.window);
		frame.setBackground(UIManager.getColor("InternalFrame.inactiveTitleGradient"));
		frame.setFont(new Font("Dialog", Font.PLAIN, 12));
		frame.setTitle("SVG Viewer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		gd.getDefaultConfiguration().getBounds().getWidth();
		frame.getContentPane().add(createComponents());
		frame.setSize((int)(gd.getDefaultConfiguration().getBounds().getWidth()*0.5), 
				(int)(gd.getDefaultConfiguration().getBounds().getHeight()*0.5));
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
		
    }
    public JComponent createComponents() {
        // Create a panel and add the button, status label and the SVG canvas.
    	
        final JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SystemColor.window);
       loadDocument(svgFilePath);
       svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
       svgCanvas.setEnablePanInteractor(true);
       svgCanvas.setEnableImageZoomInteractor(true);
       svgCanvas.setEnableRotateInteractor(true);
       
       svgCanvas.addUpdateManagerListener(new UpdateManagerListener(){

		@Override
		public void managerResumed(UpdateManagerEvent arg0) {
			// TODO Auto-generated method stub
			System.out.println("manager resumed");
		}

		@Override
		public void managerStarted(UpdateManagerEvent arg0) {
			// TODO Auto-generated method stub
			System.out.println("manager started");
		}

		@Override
		public void managerStopped(UpdateManagerEvent arg0) {
			// TODO Auto-generated method stub
			System.out.println("manager stopped");
		}

		@Override
		public void managerSuspended(UpdateManagerEvent arg0) {
			// TODO Auto-generated method stub
			System.out.println("manager suspened");
		}

		@Override
		public void updateCompleted(UpdateManagerEvent arg0) {
			// TODO Auto-generated method stub
			System.out.println("update completed");
		}

		@Override
		public void updateFailed(UpdateManagerEvent arg0) {
			// TODO Auto-generated method stub
			System.out.println("update failed");
		}

		@Override
		public void updateStarted(UpdateManagerEvent arg0) {
			// TODO Auto-generated method stub
			System.out.println("update started");
		}
    	   
       });
       
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setBackground(SystemColor.window);
        JPanel legend = setupLegend();
        
        ArrayList<String> names = predicateHandler.getActivitNames();
        
        //Initialise comboboxes
        comboBoxActivities.addItem("--Activities--");
        comboBoxPredicatesPre.addItem("--Preconditions--");
        comboBoxPost.addItem("--Postconditions--");
        
        for(String name : names) {
        	comboBoxActivities.addItem(name);
        }
        
        comboBoxActivities.addItemListener(new ItemListener() {
        	public void itemStateChanged(ItemEvent arg0) {
        		if (arg0.getStateChange() == ItemEvent.SELECTED) {
        			comboBoxPredicatesPre.removeAllItems();
    				comboBoxPost.removeAllItems();
        			if(arg0.getItem().toString().contentEquals("--Activities--")) {
        				comboBoxPredicatesPre.addItem("--Preconditions--");
        				comboBoxPost.addItem("--Postconditions--");
        				return;
        			}
    				
        			for(Predicate p : predicateHandler.getPredicates(arg0.getItem().toString(), PredicateType.Precondition)) {
        				comboBoxPredicatesPre.addItem(p.getName());	
        			}
        			for(Predicate p : predicateHandler.getPredicates(arg0.getItem().toString(), PredicateType.Postcondition)) {
        				comboBoxPost.addItem(p.getName());	
        			}
        			
        		}
        	}
        });
                
        JButton btn = new JButton("find paths");
        btn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				ArrayList<Predicate> preds = new ArrayList<Predicate>();
				
				String activity = (String)(comboBoxActivities.getSelectedItem());
    			for (Predicate p1 : predicateHandler.getPredicates(activity, PredicateType.Precondition)) {
    				if (p1.getName().contentEquals((String)(comboBoxPredicatesPre.getSelectedItem()))) {
    					precond = p1;
    					preds.add(p1);
    					break;
    				}
    			}
    			
    			for (Predicate p2 : predicateHandler.getPredicates(activity, PredicateType.Postcondition)) {
    				if (p2.getName().contentEquals((String)(comboBoxPost.getSelectedItem()))) {
    					postcond = p2;
    					preds.add(p2);
    					break;
    				}
    			}
    			paths = precond.getPathsTo(postcond);
    			counter=0;
    			label_1.setText(counter+"/"+paths.size());
    			colorPredicateStates(preds);
			}
		});
        p.add(comboBoxActivities);
        p.add(comboBoxPredicatesPre);        
        p.add(comboBoxPost);
        p.add(btn);
        button_1.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		
        		if(counter > 0) {
        			counter--;
        			 if (counter == 0){
             			ArrayList<Predicate> ls = new ArrayList<Predicate>();
             			ls.add(precond);
             			ls.add(postcond);
             			colorPredicateStates(ls);
             		} else {
             			colorPredicateStates(paths.get(counter).getStateTransitions());
             		}
            		
            		label_1.setText(counter+"/"+paths.size());  
        		}
        	}
        });
        
        p.add(button_1);
        
        p.add(label_1);
        button_2.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		if(counter < paths.size()) {
        		colorPredicateStates(paths.get(counter).getStateTransitions());
        		counter++;
        		label_1.setText(counter+"/"+paths.size());
        		}
        	}
        });
        
        p.add(button_2);
        p.add(label);
        
        panel.add("North", p);
        panel.add("Center", svgCanvas);
        panel.add("South", legend);

        // Set the JSVGCanvas listeners.
        svgCanvas.addSVGDocumentLoaderListener(new SVGDocumentLoaderAdapter() {
            public void documentLoadingStarted(SVGDocumentLoaderEvent e) {
                label.setText("Document Loading...");
            }
            public void documentLoadingCompleted(SVGDocumentLoaderEvent e) {
                label.setText("Document Loaded.");
            }
        });

        svgCanvas.addGVTTreeBuilderListener(new GVTTreeBuilderAdapter() {
            public void gvtBuildStarted(GVTTreeBuilderEvent e) {
                label.setText("Build Started...");
            }
            public void gvtBuildCompleted(GVTTreeBuilderEvent e) {
                label.setText("Build Done.");
                //frame.pack();
            }
        });

        svgCanvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
            public void gvtRenderingPrepare(GVTTreeRendererEvent e) {
                label.setText("Rendering Started...");
            }
            public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
                label.setText("");
                doc = svgCanvas.getSVGDocument();
            }
        });

        return panel;
    }
    
    private void colorPredicateStates(Predicate pred, boolean reload) {
    	
    	String color = "";
    	
     	if(reload)
    	loadDocument(svgFilePath);
		
		if(pred.getPredicateType() == PredicateType.Precondition) {
			color = "green";
		} else if(pred.getPredicateType() == PredicateType.Postcondition) {
			color = "red";
		}
    	for(Integer st : pred.getBigraphStates()) {
    		
			doc.getElementById("s"+st)
			.getElementsByTagName("ellipse").item(0).getAttributes().
				getNamedItem("fill").setTextContent(color);
    	
    	}
    	
    	svgCanvas.setDocument(doc);
    }
    
    private void colorPredicateStates(ArrayList<Predicate> preds) {
   
    	loadDocument(svgFilePath);
		String color = "";
		
		for(Predicate pred : preds ) {
		if(pred.getPredicateType() == PredicateType.Precondition) {
			color = "green";
		} else if(pred.getPredicateType() == PredicateType.Postcondition) {
			color = "red";
		}
    	for(Integer st : pred.getBigraphStates()) {
    		
			doc.getElementById("s"+st)
			.getElementsByTagName("ellipse").item(0).getAttributes().
				getNamedItem("fill").setTextContent(color);
    	
    	}
		}
    	svgCanvas.setDocument(doc);
    }

    private void colorPredicateStates(LinkedList<Integer> states) {
    	
    	   
    	loadDocument(svgFilePath);
		String color = "yellow";
	
    	for(Integer st : states) {
    		
			doc.getElementById("s"+st)
			.getElementsByTagName("ellipse").item(0).getAttributes().
				getNamedItem("fill").setTextContent(color);
		}
    	
    	//initial state as precondition
    	doc.getElementById("s"+states.getFirst())
		.getElementsByTagName("ellipse").item(0).getAttributes().
			getNamedItem("fill").setTextContent("green");
    	
    	//final state as postcondition
    	doc.getElementById("s"+states.getLast())
		.getElementsByTagName("ellipse").item(0).getAttributes().
			getNamedItem("fill").setTextContent("red");
    	
    	svgCanvas.setDocument(doc);
    }
    
    private void loadDocument(String filePath) {
    	 String parser = XMLResourceDescriptor.getXMLParserClassName();
         SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
 		try {
 			uri = new URI(filePath);
 			 doc = f.createDocument(uri.toString());
 			 svgCanvas.setDocument(doc);
 		} catch (MalformedURLException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (URISyntaxException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
    }
    
    
    private JPanel setupLegend() {
    	JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT));
    	BufferedImage img;
    	
    	try {
				img = ImageIO.read(new File("images/imgPre.png"));
			 	ImageIcon icnPre = new ImageIcon(img);
			 	img = ImageIO.read(new File("images/imgPost.png"));
			 	ImageIcon icnPost = new ImageIcon(img);
			 	img = ImageIO.read(new File("images/imgMid.png"));
			 	ImageIcon icnMid = new ImageIcon(img);
		        
		        
		        JLabel lblPre = new JLabel();
		        lblPre.setText("Precondition");
		        lblPre.setIcon(icnPre);
		       
		        JLabel lblPost = new JLabel();
		        lblPost.setText("Postcondition");
		        lblPost.setIcon(icnPost);
		       
		        JLabel lblMid = new JLabel();
		        lblMid.setText("Intermidate");
		        lblMid.setIcon(icnMid);
		        
		        legend.add(lblPre);
		        legend.add(lblMid);
		        legend.add(lblPost);
		        
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
        return legend;
    }
}