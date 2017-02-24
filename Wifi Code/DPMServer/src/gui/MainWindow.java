package gui;
import java.awt.*;
import java.awt.event.*;

import guigeneration.*;

public class MainWindow extends Frame implements ComponentListener {
	private static final long serialVersionUID = 3783819071726964932L;
	private static final int WIDTH = 1000, HEIGHT = 600;
	
	//private EV3InformationPanel ev3InformationPanel;
	private EV3GeneratedPanel ev3GeneratedPanel;
	private TimerPanel timerPanel;
	private WifiOutput wifiOut;
	private String xmlLayoutFile;
	private int portNumber;
	
	public MainWindow(String xmlLayout, int port) {
		super("ECSE 211 - Final Competition");
		this.xmlLayoutFile = xmlLayout;
		this.portNumber = port;
		this.createWindow();
		this.setVisible(true);
	}
	
	private void createWindow() {
		this.setSize(WIDTH, HEIGHT);
		this.setLocationRelativeTo(null);
		
		// setup menu bar
		MenuBar mb = new MenuBar();
		mb.add(new MainWindowMenu(this));
		this.setMenuBar(mb);
		
		// layout subpanels via new gridbag
		GridBagConstraints gridConstraints = new GridBagConstraints();
		this.setLayout(new GridBagLayout());
		
		gridConstraints.gridx = 0;
		gridConstraints.gridy = 1;
		gridConstraints.gridheight = 10;
		gridConstraints.gridwidth = 1;
		
		// add main information panel
		ev3GeneratedPanel = XMLReader.createEV3Panel(this.xmlLayoutFile, this.portNumber, this);
		this.add(ev3GeneratedPanel, gridConstraints);
		
		gridConstraints.gridx = 1;
		gridConstraints.gridy = 0;
		gridConstraints.gridheight = 10;
		gridConstraints.gridwidth = 2;
		Label lbl = new Label("         ");
		this.add(lbl, gridConstraints);
		
		gridConstraints.gridx = 3;
		gridConstraints.gridy = 0;
		gridConstraints.gridheight = 3;
		gridConstraints.gridwidth = 1;
		// add timer shat here
		timerPanel = new TimerPanel(this);
		this.add(timerPanel, gridConstraints);
		//this.timerPanel.start();
		
		gridConstraints.gridx = 3;
		gridConstraints.gridy = 3;
		gridConstraints.gridheight = 7;
		gridConstraints.gridwidth = 1;
		// add system output
		wifiOut = new WifiOutput(this);
		this.add(wifiOut, gridConstraints);
		
		
		// set close listener
		this.setCloseListener();
		// set window resize listener
		this.setWindowSizeListener();
	}
	
	public void exit() {
		this.setVisible(false);
		this.dispose();
		System.exit(0);
	}
	
	private void setCloseListener() {
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
	}
	
	private void setWindowSizeListener() {
		this.addComponentListener(this);
	}
	
	public void displayOutput(String out, boolean secondNewline) {
		wifiOut.append(out, secondNewline);
	}
	
	public void startTimer() {
		this.timerPanel.start();
	}
	
	public void clearTimer() {
		this.timerPanel.clear();
	}
	
	public void clearWifiPanel() {
		this.wifiOut.clear();
	}
	
	
	@Override
	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentResized(ComponentEvent arg0) {
		//this.resize(getHeight() / 20, getWidth() / 20);
	}

	@Override
	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
}
