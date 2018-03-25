package controller.functions.print;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
/*
 * Created on 2007-08-04
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author krzysztof
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DialogInternalFrame extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6603160759794692290L;
	private JPanel jButtonPanel = null;
	private JPanel jMainPanel = null;
	private JButton jBtnKoncz = null;
	private JButton jBtnNowe = null;

	private javax.swing.JPanel jContentPane = null;
	/**
	 * This is the default constructor
	 */
	public DialogInternalFrame() {
		super();
		initialize();
//		System.out.println("this.getWidth() = " + this.getWidth());
//		((FlowLayout)jButtonPanel.getLayout()).setHgap(this.getWidth() - 170);
	}
	
	public DialogInternalFrame(int xPos, int yPos) {
		this();
		this.setLocation(xPos, yPos);
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setResizable(false);
		this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		this.setPreferredSize(new java.awt.Dimension(360,240));
		this.setSize(360, 240);
		this.setContentPane(getJContentPane());
		this.setVisible(true);
	}
	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	protected javax.swing.JPanel getJContentPane() {
		if(jContentPane == null) {
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(new java.awt.BorderLayout());
			jContentPane.add(getButtonPanel(), java.awt.BorderLayout.SOUTH);
			jContentPane.add(getMainPanel(), java.awt.BorderLayout.CENTER);
		}
		return jContentPane;
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	protected JPanel getButtonPanel() {
		if (jButtonPanel == null) {
			FlowLayout flowLayout1 = new FlowLayout();
			jButtonPanel = new JPanel();
			jButtonPanel.setLayout(flowLayout1);
//			jButtonPanel.setPreferredSize(new java.awt.Dimension(jButtonPanel.getParent().getWidth(),45));
//			Dimension d = flowLayout1.preferredLayoutSize(jButtonPanel);
//			System.out.println("jButtonPanel.getPreferredSize().width=" + jButtonPanel.getPreferredSize().width);
			flowLayout1.setHgap(30);
			flowLayout1.setVgap(12);
			jButtonPanel.add(getKonczBtn());
			jButtonPanel.add(getNoweBtn());
		}
		return jButtonPanel;
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	protected JPanel getMainPanel() {
		if (jMainPanel == null) {
			jMainPanel = new JPanel();
			SpringLayout springLayout = new SpringLayout();
			jMainPanel.setLayout(springLayout);
		}
		return jMainPanel;
	}
	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	protected JButton getKonczBtn() {
		if (jBtnKoncz == null) {
			jBtnKoncz = new JButton();
			jBtnKoncz.setPreferredSize(new Dimension(85,20));
			jBtnKoncz.setText("Przerwij");
			jBtnKoncz.setActionCommand("Przerwij");
			jBtnKoncz.addActionListener(this);
		}
		return jBtnKoncz;
	}
	/**
	 * This method initializes jButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	protected JButton getNoweBtn() {
		if (jBtnNowe == null) {
			jBtnNowe = new JButton();
			jBtnNowe.setPreferredSize(new java.awt.Dimension(85,20));
			jBtnNowe.setText("Wykonaj");
			jBtnNowe.setActionCommand("Wykonaj");
			jBtnNowe.addActionListener(this);
		}
		return jBtnNowe;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		
		String ac = arg0.getActionCommand();
		if (ac.equals("Przerwij"))
			dispose();
	}
}
