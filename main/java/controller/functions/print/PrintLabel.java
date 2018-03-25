package controller.functions.print;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.print.Doc;
import javax.print.DocFlavor;
//import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.Attribute;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/*
 * Created on 2007-10-02
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
public class PrintLabel extends DialogInternalFrame implements Printable, Doc {

/**
	 * 
	 */
	private static final long serialVersionUID = -9209006069585621477L;

	private PrintRequestAttributeSet printAtributes;
	private PrintService defaultPrinter = null;
	private JScrollPane	jScrollPane = null;
	private JComboBox printersJCB = null;
	
	public PrintLabel(int xLoc, int yLoc) {
		super(xLoc, yLoc);
		initialize();
//		 Przygotowanie drukarki do wydruku
//		DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
		printAtributes = new HashPrintRequestAttributeSet();
		printAtributes.add(OrientationRequested.PORTRAIT);
		printAtributes.add(MediaSizeName.ISO_A5);
		printAtributes.add(new Copies(1));
		printAtributes.add(new JobName("PrnMagLabel", null));
	}
	
	public PrintLabel(int xLoc, int yLoc, Attribute formatka, Attribute orientacja) {
		super(xLoc, yLoc);
		initialize();
//		 Przygotowanie drukarki do wydruku
//		DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
		printAtributes = new HashPrintRequestAttributeSet();
		printAtributes.add(orientacja);
		if (formatka != null)
			printAtributes.add(formatka);
//		printAtributes.add(OrientationRequested.PORTRAIT);
//		printAtributes.add(MediaSizeName.ISO_A5);
		printAtributes.add(new Copies(1));
		printAtributes.add(new JobName("PrnMagLabel", null));
	}
	
	
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setResizable(true);
		this.setName("printLabel");
		this.setTitle("Wydruk etykiety");
		this.setPreferredSize(new Dimension(450, 375));
		this.setSize(450, 375);
		this.getMainPanel().setLayout(new BorderLayout());
//		String[] s = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
//		Font[] f = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
//		System.out.println("xxx");
		defaultPrinter = PrintServiceLookup.lookupDefaultPrintService();
		this.getButtonPanel().add(getPrintersJCB(), 0);
		}
	
/*	public void initDatas(int id) {
		regId = id;
		DataBase mdb = ((Magazyn)getTopLevelAncestor()).getDataBase();
		regRS = mdb.getPositions((short)id);
		DataBase.RegalInfo ri = mdb.getRegalInfo(id);
		regNr = ri.getOznaczenie();
		magName = ri.getMagazynNazwa();
//		this.setTitle("Wydruk kodow paskowych regalu " + regNr);
		this.getMainPanel().add(getJScrollPane());
	}//*/
	
	/* (non-Javadoc)
	 * @see java.awt.print.Printable#print(java.awt.Graphics, java.awt.print.PageFormat, int)
	 */
	public int print(Graphics gr, PageFormat pf, int page)
			throws PrinterException {
		/*
		 * Strona A6 LANDSCAPE ma wymiary 148.5x105[mm], przy rozdzielczosci wydruku 600dpi
		 * odpowiada to 3508x2480 punktow
		 */
		int theRet = Printable.NO_SUCH_PAGE;
/*		try {
			if (regRS.absolute(page * 2 + 1)) {
				Graphics2D g2d= (Graphics2D)gr;
				StringBuffer bkod = new StringBuffer("00000000");			//kod pozycji ma 8 znakow
				String kod = Integer.toHexString(regRS.getInt(1)).toUpperCase();	//hexadecymalnych
				bkod.replace(8 - kod.length(), 8, kod);						//pozycjonowanych na koncu
				g2d.setFont(Font.decode("Free 3 of 9 Extended-PLAIN-100"));
//				g2d.setFont(Font.decode("Verdana-PLAIN-72"));
//				String[] s = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
				g2d.drawString("*" + bkod.toString() + "*",20 ,120);//155,155);
				g2d.drawString("*" + bkod.toString() + "*",20 ,120+75);//155,155);
				g2d.setFont(Font.decode("Verdana-PLAIN-20"));
				g2d.drawString(magName + "/Rega� " + regNr, 20, 40);//105,250);
				g2d.setFont(Font.decode("Verdana-PLAIN-36"));
				g2d.drawString("Pozycja:" + regRS.getShort(4) + 
								"/Poziom:" + regRS.getShort(3), 20, 250);//105,250);
				if (regRS.absolute(page * (2 + 1))) {
					bkod = new StringBuffer("00000000");					//kod pozycji ma 8 znakow
					kod = Integer.toHexString(regRS.getInt(1)).toUpperCase();		//hexadecymalnych
					bkod.replace(8 - kod.length(), 8, kod);					//pozycjonowanych na koncu
					g2d.setFont(Font.decode("Free 3 of 9 Extended-PLAIN-100"));
					g2d.drawString("*" + bkod.toString() + "*",20 ,298+120);//155,155);
					g2d.drawString("*" + bkod.toString() + "*",20 ,298+120+75);//155,155);
					g2d.setFont(Font.decode("Verdana-PLAIN-20"));
					g2d.drawString(magName + "/Reagal " + regNr, 20, 298+40);//105,250);
					g2d.setFont(Font.decode("Verdana-PLAIN-36"));
					g2d.drawString("Pozycja:" + regRS.getShort(4) + 
									"/Poziom:" + regRS.getShort(3), 20, 298+250);//105,250);
				}//*/
/*				theRet = Printable.PAGE_EXISTS;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//*/
		return theRet;
	}

	/* (non-Javadoc)
	 * @see javax.print.Doc#getDocFlavor()
	 */
	public DocFlavor getDocFlavor() {
		// TODO Auto-generated method stub
		return DocFlavor.SERVICE_FORMATTED.PRINTABLE;
	}

	/* (non-Javadoc)
	 * @see javax.print.Doc#getPrintData()
	 */
	public Object getPrintData() throws IOException {
		// TODO Auto-generated method stub
		return this;
	}

	/* (non-Javadoc)
	 * @see javax.print.Doc#getAttributes()
	 */
	public DocAttributeSet getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.print.Doc#getReaderForText()
	 */
	public Reader getReaderForText() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.print.Doc#getStreamForBytes()
	 */
	public InputStream getStreamForBytes() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	private void drukuj() {
		
/*		if (printAtributes.get(OrientationRequested.class) == OrientationRequested.PORTRAIT) {
			if (printAtributes.get(MediaSizeName.class) == MediaSizeName.ISO_A5)
				printAtributes.add(new MediaPrintableArea(0,0,148,210,MediaPrintableArea.MM));
			else
				if (printAtributes.get(MediaSizeName.class) == MediaSizeName.ISO_A4)
					printAtributes.add(new MediaPrintableArea(0,0,210,297,MediaPrintableArea.MM));
				else
					if (printAtributes.get(MediaSizeName.class) == MediaSizeName.ISO_A7)
						printAtributes.add(new MediaPrintableArea(0,0,74,105,MediaPrintableArea.MM));
		}
		else {
			if (printAtributes.get(MediaSizeName.class) == MediaSizeName.ISO_A5)
				printAtributes.add(new MediaPrintableArea(0,0,210,148,MediaPrintableArea.MM));
			else
				if (printAtributes.get(MediaSizeName.class) == MediaSizeName.ISO_A4)
					printAtributes.add(new MediaPrintableArea(0,0,297,210,MediaPrintableArea.MM));
		}//*/
		PrintService usedPrinter = (PrintService) getPrintersJCB().getSelectedItem();
		printAtributes.add(new Copies(1));
		if (printAtributes.get(Media.class) == MediaSizeName.ISO_A5)
			printAtributes.add(new MediaPrintableArea(0,0,148,210,MediaPrintableArea.MM));
		else
			if (printAtributes.get(Media.class) == MediaSizeName.ISO_A4)
				printAtributes.add(new MediaPrintableArea(0,0,210,297,MediaPrintableArea.MM));
			else
				if (printAtributes.get(Media.class) == MediaSizeName.ISO_A6)
					printAtributes.add(new MediaPrintableArea(0,0,105,148,MediaPrintableArea.MM));
				else
					if (printAtributes.get(Media.class) == MediaSizeName.ISO_A7)
						printAtributes.add(new MediaPrintableArea(0,0,74,105,MediaPrintableArea.MM));
					else {
						printAtributes.remove(Media.class);
						if (usedPrinter.getName().contains("DYMO"))
						{
							printAtributes.add(OrientationRequested.LANDSCAPE);
							printAtributes.add(new MediaPrintableArea(0, 0, 50, 70, MediaPrintableArea.MM));
						}
						else
							printAtributes.add(new MediaPrintableArea(0, 0, 80, 50, MediaPrintableArea.MM));
						printAtributes.add(new Copies(getLabelCopies()));
					}
		PrinterJob pj = PrinterJob.getPrinterJob();
		defaultPrinter = PrintServiceLookup.lookupDefaultPrintService();
		if (usedPrinter == null)
			usedPrinter = defaultPrinter;
		try {
			if (usedPrinter != null) {
				pj.setPrintService(usedPrinter);
				pj.setPrintable(this);
//				pj.setCopies(getLabelCopies());
				pj.print(printAtributes);
			}
		} catch (PrinterException e) {
			e.printStackTrace();
		}
//		printAtributes.add(new javax.print.attribute.standard.);
/*		try {
			printJob.print(this, printAtributes);
		} catch (PrintException e) {
			e.printStackTrace();
		}//*/
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel(final int strona) {
		
			JPanel jPanel = new JPanel() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 2005715984963741192L;

				public void paintComponent(Graphics gr) {
					
					super.paintComponent(gr);
					PageFormat panelFormat = new PageFormat();
					Paper arkusz = new Paper();
					arkusz.setImageableArea(0,0,600,800);
					panelFormat.setPaper(arkusz);
					try {
						PrintLabel.this.print(gr, panelFormat, strona);
					} catch (PrinterException e) {
						e.printStackTrace();
					}
				}
			};
			jPanel.setPreferredSize(new Dimension(600,800));
			jPanel.setMaximumSize(new Dimension(600,800));
			jPanel.setBackground(java.awt.Color.white);
		return jPanel;
	}
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	protected JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane(getJPanel(0));
//			jScrollPane.setBounds(20, 20, 280, 200);
			jScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			jScrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		}
		return jScrollPane;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		
		String ac = arg0.getActionCommand();
		if (ac.equals("Przerwij"))
			dispose();
		else
			if (ac.equals("Wykonaj")) {
//				printProcessing = true;
				drukuj();
//				printProcessing = false;
				dispose();
			}
	}

	public int getLabelCopies() {
		return 1;
	}
	
	public void setPageFormat(Media formatka) {
		printAtributes.add(formatka);
	}
	
	public void setPrintableArea(MediaPrintableArea mpa) {
		printAtributes.add(mpa);
	}

	public void setPageOrientation(OrientationRequested or) {printAtributes.add(or);}

	public void setPageOrientation() {
		setPageOrientation(OrientationRequested.PORTRAIT);
	}
	
/*	public void setPageOrientationLandscape() {
		printAtributes.add(OrientationRequested.LANDSCAPE);
	}//*/
	
	private JComboBox getPrintersJCB() {
		
		if (printersJCB == null) {
			printersJCB = new JComboBox(PrinterJob.lookupPrintServices());
			printersJCB.setPreferredSize(new Dimension(350, 25));
			printersJCB.setSelectedItem(defaultPrinter);
		}
		return printersJCB;
	}
	
	public void addPrintersListener(ActionListener myal) {
		printersJCB.addActionListener(myal);
	}
	
	public PrintService getSelectedPrinter() {
		return (PrintService) getPrintersJCB().getSelectedItem();
	}
	
	public String code128(String kod)
	{
		StringBuffer theRet = new StringBuffer(kod.length() + 4);
		if (kod.length() == 0)
			kod = "0";
		int d = 0, z = 0;
		char cs = 0;

//iteracja po znakach kodowanego lancucha		
		for (int i = 0; i < kod.length(); i++)
		{
			//zliczanie kolejnych cyfr
			d = 0;
			for (int j = i; j < kod.length(); j++)
				if (47 < kod.codePointAt(j) && kod.codePointAt(j) < 58)
					d++;
				else
					break;
			if (d > 3 && d % 2 == 0)
			{
//				if (d % 2 != 0)
//					d--;
				if (theRet.length() == 0)		//jezeli jest to poczatek kodowanego lancucha
					theRet.append((char)205);	//dodaj znak inicjowania tabeli C
				else
					theRet.append((char)199);	//dodaj znak zmiany na tabele C
				cs = 'C';
				for (int j = 0; j < d; j += 2)
				{
					z = Integer.parseInt(kod.substring(i+j, i+j+2)) + 32;
					if (31 < z && z < 127)
						theRet.append((char) z);
					else
						theRet.append((char) (z + 68));
				}
				i += d - 1;
			}
			else
			{
				if (theRet.length() == 0)		//jezeli jest to poczatek kodowanego lancucha
					theRet.append((char)204);	//dodaj znak inicjowania tabeli B
				if (i >= kod.length())
					break;
				if (cs == 'C')
				{
					theRet.append((char)200);	//przelacz na tabele B
					cs = 'B';
				}
				theRet.append(kod.charAt(i));	//koduje kolejny znak
			}
		}
		//generowanie znaku kontrolnego
		z = theRet.codePointAt(0) - 100;
		for (int i = 1; i < theRet.length(); i++)
			if (theRet.codePointAt(i) < 127)
				z += (theRet.codePointAt(i) - 32) * i;
			else
				if (theRet.codePointAt(i) > 194)
					z += (theRet.codePointAt(i) - 100) * i;
		z %= 103;
		if (z < 95)
			theRet.append((char) (z + 32));
		else
			theRet.append((char) (z + 100));
		theRet.append((char)206);
		
		return theRet.toString();
	}
}
