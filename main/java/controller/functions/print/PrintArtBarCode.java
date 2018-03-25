package controller.functions.print;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.print.PageFormat;
//import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.TreeMap;

import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/*
 * Created on 2008-01-05
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author MAGAZYN
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PrintArtBarCode extends PrintLabel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4449476507450160129L;
	JTextField labelsNb;
	JPanel formatJP = null;
	JRadioButton formatA4JRB = null;
	JRadioButton formatT85JRB = null;
	JRadioButton formatT73JRB = null;
	JRadioButton formatA5JRB = null;
	JRadioButton format8A4JRB = null;
	String[][] printData = null;
	
	/**
	 * @param xLoc
	 * @param yLoc
	 */
	public PrintArtBarCode(int xLoc, int yLoc, String [][] printData) {
		
		super(xLoc, yLoc, null, OrientationRequested.PORTRAIT);
		this.printData = printData;
		setSize(800, 500);
		setPreferredSize(new Dimension(800,500));
		JPanel bp = getButtonPanel();
		FlowLayout fl = (FlowLayout) bp.getLayout();
		fl.setHgap(50);
		JLabel lbl = new JLabel("Ilo�� kopii:");
		bp.add(lbl);
		bp.add(getCopyNbJTF());
		lbl.setLabelFor(labelsNb);
		bp.add(getFormatJP());
		bp.setPreferredSize(new Dimension(740, 90));
		getMainPanel().add(getJScrollPane());
		format8A4JRB.setSelected(true);
		this.invalidate();
		this.validate();
		this.repaint();
		
	}	
	public void setLabalsNb(int eNb) {
		labelsNb.setText("" + eNb);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.print.Printable#print(java.awt.Graphics, java.awt.print.PageFormat, int)
	 */
	public int print(Graphics gr, PageFormat pf, int page)
			throws PrinterException {
		/*
		 * Strona A7 LANDSCAPE ma wymiary 105x74.25[mm], przy rozdzielczosci wydruku 600dpi
		 * odpowiada to 2480x1754 punktow
		 */
		String kodp = null;
		//Wymiary etykiety:
		int formatka = 4;	//24 wydruki na A4
		int etW = 195;
		int etH = 105;
		//Marginesy:
		int lm = 0;
		int gm = 0;
		if (formatT85JRB.isSelected()) {	//1 wydruk na etykiecie 80mmx50mm
			formatka = 8;
			etW = 226;//223;
			etH = 141;//115;
			//Marginesy:
			lm = 0;
			gm = 0;
		}
		else
			if (formatA5JRB.isSelected()) {	//2 wydruki na A5
				formatka = 5;
				etW = 405;
				etH = 295;
				lm = -22;//-5
				gm = 0;
			}
			else
				if (format8A4JRB.isSelected()) {	//8 wydrukow na A4
					formatka = 7;
					etW = 295;
					etH = 210;
					lm = 0;//-8;
					gm = 0;
				}
				else
					if (formatT73JRB.isSelected()) {	//1 wydruk na etykiecie 75mmx35mm
						formatka = 6;
						etW = 212;//212;
						etH = 100;//115;
						//Marginesy:
						lm = 0;
						gm = 0;
					}
		if (getSelectedPrinter().getName().contains("DYMO"))
		{
			etW = 198;
			lm = -16;
			setPageOrientation(OrientationRequested.LANDSCAPE);
		}
		int theRet = Printable.NO_SUCH_PAGE;
		kodp = "*" + printData[0][3] + "*";
		Graphics2D g2d= (Graphics2D)gr;
		g2d.translate(pf.getImageableX(), pf.getImageableY());
		g2d.setColor(Color.black);
		int lbNb = 1;
		try {
			lbNb = Integer.parseInt(labelsNb.getText());
		} catch(NumberFormatException nfe) {
			nfe.printStackTrace();
			lbNb = 1;
		}
		if (formatka == 4) {
			int lop = lbNb * printData.length - page * 24;		//lop -> labels on page
			if (lop > 0) {
				theRet = Printable.PAGE_EXISTS;
				int lnb = (int) Math.ceil(lop / 2.0);
				if (lnb > 8)
					lnb = 8;
				for (int i = 0; i < lnb; i++) {
					int rowLbNb = lop - i * 3;
					if (rowLbNb > 3)
						rowLbNb = 3;
					for (int j = 0; j < rowLbNb; j++) {
						int lbi = (int) Math.floor((page * 24.0 + i * 3 + j) / lbNb);
						Font etf = Font.decode("Impact-PLAIN-9");
						g2d.setFont(etf);
						FontRenderContext frc = g2d.getFontRenderContext();
						TextLayout layout = new TextLayout(printData[lbi][0], etf, frc);	//nazwa
						int dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
						g2d.drawString(printData[lbi][0], dm + lm + j * etW, 8 + gm + i * etH);
						etf = Font.decode("Verdana-PLAIN-9");
						g2d.setFont(etf);
						layout = new TextLayout(printData[lbi][1], etf, frc);	//indeks katalogowy
						dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
						g2d.drawString(printData[lbi][1], dm + lm + j * etW, 17 + gm + i * etH);
//						layout = new TextLayout(printData[lbi][2], etf, frc);	//indeks handlowy
//						dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
//						g2d.drawString(printData[lbi][2], dm + lm + j * etW, 26 + gm + i * etH);
						etf = Font.decode("Verdana-PLAIN-7");
						g2d.setFont(etf);
						layout = new TextLayout(printData[lbi][3], etf, frc);	//kod paskowy
						dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
						g2d.drawString(printData[lbi][3], dm + lm + j * etW, 82 + gm + i * etH);
						etf = Font.decode("Code 128-PLAIN-44");
						g2d.setFont(etf);
						kodp = code128(printData[lbi][3]);//TODO id artykulu
						layout = new TextLayout(kodp, etf, frc);
						dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
						g2d.drawString(kodp, dm + lm - 9 + j * etW, 72 + gm + i * etH);
					}
				}
			}
		}
		else 
			if (formatka == 8 && page < printData.length) {	//drukarka Godex
				theRet = Printable.PAGE_EXISTS;
				Font etf = Font.decode("Impact-PLAIN-11");
				g2d.setFont(etf);
				FontRenderContext frc = g2d.getFontRenderContext();
				TextLayout layout = new TextLayout(printData[page][0], etf, frc);	//nazwa
				int dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
				g2d.drawString(printData[page][0], dm + lm, 12 + gm);
				etf = Font.decode("Verdana-PLAIN-16");
				g2d.setFont(etf);
				layout = new TextLayout(printData[page][1], etf, frc);	//indeks katalogowy
				dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
				g2d.drawString(printData[page][1], dm + lm, 35 + gm);
				layout = new TextLayout(printData[page][2], etf, frc);	//indeks handlowy
				dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
				g2d.drawString(printData[page][2], dm + lm, 59 + gm);
				etf = Font.decode("Verdana-PLAIN-10");
				g2d.setFont(etf);
				layout = new TextLayout(printData[page][3], etf, frc);	//kod paskowy
				dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
				g2d.drawString(printData[page][3], dm + lm, 135 + gm);
//				etf = Font.decode("Free 3 of 9 Extended-PLAIN-32");
				etf = Font.decode("Code 128-PLAIN-52");
				g2d.setFont(etf);
				kodp = code128(printData[page][3]);//
//				kodp = "*" + printData[page][3] + "*";
				layout = new TextLayout(kodp, etf, frc);
				dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
//				g2d.drawString(kodp, dm + lm, 86 + gm);
//				g2d.drawString(kodp, dm + lm, 105 + gm);
//				g2d.drawString(kodp, dm + lm, 121 + gm);
//				g2d.drawString(kodp, dm + lm, 105 + gm);
				g2d.drawString(kodp, dm + lm - 10, 118 + gm);
			}
			else
//				if (formatka == 5) {
//					page *= 2;										//dwa wydruki na stronie:
//					if (page < printData.length) {
//						java.net.URL imgURL = ClassLoader.getSystemResource("ikons/logos.gif");
//						ImageIcon logos = new ImageIcon(imgURL);
//						AffineTransform skalowanie = new AffineTransform();
//						skalowanie.setToTranslation(20.0,5.0);
//						g2d.drawImage(logos.getImage(),skalowanie,logos.getImageObserver());
//						Font etf = Font.decode("Impact-PLAIN-18");
//						g2d.setFont(etf);
//						FontRenderContext frc = g2d.getFontRenderContext();
//						TextLayout layout = new TextLayout(printData[page][0], etf, frc);
//						int dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
//						g2d.drawString(printData[page][0], dm, 117);
//						etf = Font.decode("Verdana-PLAIN-18");
//						g2d.setFont(etf);
//						layout = new TextLayout(printData[page][1], etf, frc);
//						dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
//						g2d.drawString(printData[page][1], dm, 135);
//						layout = new TextLayout(printData[page][2], etf, frc);
//						dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
//						g2d.drawString(printData[page][2], dm, 155);
//						etf = Font.decode("Verdana-PLAIN-14");
//						g2d.setFont(etf);
//						layout = new TextLayout(printData[page][3], etf, frc);
//						dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
//						g2d.drawString(printData[page][3], dm, 270);
////						etf = Font.decode("Free 3 of 9 Extended-PLAIN-60");
//						etf = Font.decode("Code 128-PLAIN-92");
//						g2d.setFont(etf);
////						frc = g2d.getFontRenderContext();
//						kodp = code128(printData[page][3]);//
////						kodp = "*" + printData[page][3] + "*";
//						layout = new TextLayout(kodp, etf, frc);
//						dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
////						g2d.drawString(kodp, dm, 195);
////						g2d.drawString(kodp, dm, 225);
////						g2d.drawString(kodp, dm, 210);
//						g2d.drawString(kodp, dm + lm, 250);
//						if (++page < printData.length) {
//							skalowanie.setToTranslation(20.0,295+5.0);
//							g2d.drawImage(logos.getImage(),skalowanie,logos.getImageObserver());
//							etf = Font.decode("Impact-PLAIN-18");
//							g2d.setFont(etf);
//							layout = new TextLayout(printData[page][0], etf, frc);
//							dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
//							g2d.drawString(printData[page][0], dm, etH + 117);
//							etf = Font.decode("Verdana-PLAIN-18");
//							g2d.setFont(etf);
//							layout = new TextLayout(printData[page][1], etf, frc);
//							dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
//							g2d.drawString(printData[page][1], dm, etH + 135);
//							layout = new TextLayout(printData[page][2], etf, frc);
//							dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
//							g2d.drawString(printData[page][2], dm, etH + 155);
//							etf = Font.decode("Verdana-PLAIN-14");
//							g2d.setFont(etf);
//							layout = new TextLayout(printData[page][3], etf, frc);
//							dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
//							g2d.drawString(printData[page][3], dm, etH + 270);
////							etf = Font.decode("Free 3 of 9 Extended-PLAIN-60");
//							etf = Font.decode("Code 128-PLAIN-92");
//							g2d.setFont(etf);
//							kodp = code128(printData[page][3]);//
////							kodp = "*" + printData[page][3] + "*";
//							layout = new TextLayout(kodp, etf, frc);
//							dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
////							g2d.drawString(kodp, dm, etH + 195);
////							g2d.drawString(kodp, dm, etH + 225);
////							g2d.drawString(kodp, dm, etH + 210);
//							g2d.drawString(kodp, dm + lm, etH + 250);
//						}
//						theRet = Printable.PAGE_EXISTS;
//					}
//				}
//				else
					if (formatka == 7) {
//						lbNb = printData.length ;
						int lop = lbNb * printData.length - page * 8;		//lop -> labels left to print
						if (lop > 0) {
							theRet = Printable.PAGE_EXISTS;
							int lnb = (int) Math.ceil(lop / 2.0);		//ilosc wierszy etykiet na stronie
							if (lnb > 4)									//maks. 4 wiersze po 2 etykiety
								lnb = 4;
							for (int i = 0; i < lnb; i++) {				//iteracja rysowania etykiet po wierszach
								int rowLbNb = lop - i * 2;				//ilosc etykiet do wydruku w biezacym wierszu
								if (rowLbNb > 2)						//jednak nie wiecej niz 2 etykiety
									rowLbNb = 2;
								for (int j = 0; j < rowLbNb; j++) {		//iteracja rysowania etykiet po kolumnach 
//									int lbi = page * 8 + i * 2 + j;		//ustalenie indeksu rysowanej etykiety
									int lbi = (int) Math.floor((page * 8.0 + i * 2 + j) / lbNb);
//									System.out.println("page * 8 + i * 2 + j = " + (page * 8 + i * 2 + j));
//									System.out.println("Math.round((page * 8 + i * 2 + j) / lbNb) = " + Math.floor((page * 8.0 + i * 2 + j) / lbNb));
//									System.out.println("lbi = " + lbi);
									Font etf = Font.decode("Impact-PLAIN-14");
									g2d.setFont(etf);
									FontRenderContext frc = g2d.getFontRenderContext();
									TextLayout layout = new TextLayout(printData[0][0], etf, frc);	//nazwa
									int dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
									g2d.drawString(printData[lbi][0], dm + lm + j * etW, 16 + gm + i * etH);
									etf = Font.decode("Verdana-PLAIN-20");
									g2d.setFont(etf);
									layout = new TextLayout(printData[lbi][1], etf, frc);	//indeks katalogowy
									dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
									g2d.drawString(printData[lbi][1], dm + lm + j * etW, 40 + gm + i * etH);
									layout = new TextLayout(printData[lbi][2], etf, frc);	//indeks handlowy
									dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
									g2d.drawString(printData[lbi][2], dm + lm + j * etW, 65 + gm + i * etH);
									etf = Font.decode("Verdana-PLAIN-12");
									g2d.setFont(etf);
									layout = new TextLayout(printData[lbi][3], etf, frc);	//kod paskowy
									dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
									g2d.drawString(printData[lbi][3], dm + lm + j * etW, 165 + gm + i * etH);
//									etf = Font.decode("Free 3 of 9 Extended-PLAIN-38");
									etf = Font.decode("Code 128-PLAIN-68");
									g2d.setFont(etf);
									kodp = code128(printData[lbi][3]);//
//									kodp = "*" + printData[lbi][3] + "*";
									layout = new TextLayout(kodp, etf, frc);
									dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
//									g2d.drawString(kodp, dm + lm + j * etW, 105 + gm + i * etH);
//									g2d.drawString(kodp, dm + lm + j * etW, 126 + gm + i * etH);
//									g2d.drawString(kodp, dm + lm + j * etW, 147 + gm + i * etH);
//									g2d.drawString(kodp, dm + lm + j * etW, 97 + gm + i * etH);
//									g2d.drawString(kodp, dm + lm + j * etW, 125 + gm + i * etH);
									g2d.drawString(kodp, dm + lm - 13 + j * etW, 144 + gm + i * etH);
								}
							}
						}
					}
					else
						if (formatka == 6 && page < printData.length)
							if (getSelectedPrinter().getName().contains("DYMO"))
							{
								theRet = Printable.PAGE_EXISTS;
								Font etf = Font.decode("Impact-PLAIN-10");
								g2d.setFont(etf);
								FontRenderContext frc = g2d.getFontRenderContext();
								TextLayout layout = new TextLayout(printData[page][0], etf, frc);	//nazwa
								int dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
								g2d.drawString(printData[page][0], dm + lm, 15 + gm);
								etf = Font.decode("Verdana-PLAIN-14");
								g2d.setFont(etf);
								layout = new TextLayout(printData[page][1], etf, frc);	//indeks katalogowy
								dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
								g2d.drawString(printData[page][1], dm + lm, 32 + gm);
								layout = new TextLayout(printData[page][2], etf, frc);	//indeks handlowy
								dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
								g2d.drawString(printData[page][2], dm + lm, 49 + gm);
								etf = Font.decode("Verdana-PLAIN-10");
								g2d.setFont(etf);
								layout = new TextLayout(printData[page][3], etf, frc);	//kod paskowy
								dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
								g2d.drawString(printData[page][3], dm + lm, 128 + gm);
//								etf = Font.decode("Free 3 of 9 Extended-PLAIN-24");
								etf = Font.decode("Code 128-PLAIN-36");
								g2d.setFont(etf);
								kodp = code128(printData[page][3]);//"*" + printData[page][3] + "*";
//								kodp = code128("ABCDEF-1234567");
								layout = new TextLayout(kodp, etf, frc);
								dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
//								g2d.drawString(kodp, dm + lm, 52 + gm);
//								g2d.drawString(kodp, dm + lm, 67 + gm);
//								g2d.drawString(kodp, dm + lm, 75 + gm);
//								g2d.drawString(kodp, dm + lm, 93 + gm);
								g2d.drawString(kodp, dm + lm, 88 + gm);
								g2d.drawString(kodp, dm + lm, 114 + gm);
							}
							else
							{
								theRet = Printable.PAGE_EXISTS;
								Font etf = Font.decode("Impact-PLAIN-10");
								g2d.setFont(etf);
								FontRenderContext frc = g2d.getFontRenderContext();
								TextLayout layout = new TextLayout(printData[page][0], etf, frc);	//nazwa
								int dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
								g2d.drawString(printData[page][0], dm + lm, 10 + gm);
								etf = Font.decode("Verdana-PLAIN-14");
								g2d.setFont(etf);
								layout = new TextLayout(printData[page][1], etf, frc);	//indeks katalogowy
								dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
								g2d.drawString(printData[page][1], dm + lm, 22 + gm);
								layout = new TextLayout(printData[page][2], etf, frc);	//indeks handlowy
								dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
								g2d.drawString(printData[page][2], dm + lm, 34 + gm);
								etf = Font.decode("Verdana-PLAIN-10");
								g2d.setFont(etf);
								layout = new TextLayout(printData[page][3], etf, frc);	//kod paskowy
								dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
								g2d.drawString(printData[page][3], dm + lm, 92 + gm);
//								etf = Font.decode("Free 3 of 9 Extended-PLAIN-30");
								etf = Font.decode("Code 128-PLAIN-48");
								g2d.setFont(etf);
								kodp = code128(printData[page][3]);
//								kodp = "*" + printData[page][3] + "*";
								layout = new TextLayout(kodp, etf, frc);
								dm = (int) Math.round((etW - layout.getBounds().getWidth()) / 2.0);
//								g2d.drawString(kodp, dm + lm, 52 + gm);
//								g2d.drawString(kodp, dm + lm, 67 + gm);
//								g2d.drawString(kodp, dm + lm, 60 + gm);
								g2d.drawString(kodp, dm + lm - 10, 80 + gm);
							}
		return theRet;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
/*	private JPanel getJPanel(final int strona) {
		
			JPanel jPanel = new JPanel() {
				public void paintComponent(Graphics gr) {
					
					super.paintComponent(gr);
					PageFormat panelFormat = new PageFormat();
					Paper arkusz = new Paper();
					arkusz.setImageableArea(0,0,600,800);
					panelFormat.setPaper(arkusz);
					try {
						PrintArtBarCode.this.print(gr, panelFormat, strona);
					} catch (PrinterException e) {
						e.printStackTrace();
					}
				}
			};
			jPanel.setPreferredSize(new Dimension(600,800));
			jPanel.setMaximumSize(new Dimension(600,800));
			jPanel.setBackground(java.awt.Color.white);
		return jPanel;
	}//*/
	
	private JPanel getFormatJP () {
		if (formatJP == null) {
			formatJP = new JPanel();
			formatJP.setPreferredSize(new Dimension(310, 40));
			formatJP.setSize(310, 40);
			ButtonGroup bg = new ButtonGroup();
			formatA4JRB = new JRadioButton("24/A4");
			formatA4JRB.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {
					getJScrollPane().repaint();
					setPageFormat(MediaSizeName.ISO_A4);
//					getMainPanel().repaint();
//					getJContentPane().repaint();
//					getJScrollPane().invalidate();
//					getJScrollPane().validate();
				}
			});
			formatT85JRB = new JRadioButton("80x50");
			formatT85JRB.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {
					getJScrollPane().repaint();
					setPageFormat(MediaSizeName.ISO_A3);
//					setPrintableArea(new MediaPrintableArea(0, 0, 80, 50, MediaPrintableArea.MM));
//					getMainPanel().repaint();
//					getJContentPane().repaint();
//					getJScrollPane().invalidate();
//					getJScrollPane().validate();
				}
			});
			formatT73JRB = new JRadioButton("75x35");
			formatT73JRB.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {
					getJScrollPane().repaint();
					setPageFormat(MediaSizeName.ISO_A3);
//					setPrintableArea(new MediaPrintableArea(0, 0, 80, 50, MediaPrintableArea.MM));
//					getMainPanel().repaint();
//					getJContentPane().repaint();
//					getJScrollPane().invalidate();
//					getJScrollPane().validate();
				}
			});
			formatA5JRB = new JRadioButton("2/A5");
			formatA5JRB.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {
					getJScrollPane().repaint();
					setPageFormat(MediaSizeName.ISO_A5);
//					getMainPanel().repaint();
//					getJContentPane().repaint();
//					getJScrollPane().invalidate();
//					getJScrollPane().validate();
				}
			});
			format8A4JRB = new JRadioButton("8/A4");
			format8A4JRB.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {
					getJScrollPane().repaint();
					setPageFormat(MediaSizeName.ISO_A4);
//					getMainPanel().repaint();
//					getJContentPane().repaint();
//					getJScrollPane().invalidate();
//					getJScrollPane().validate();
				}
			});
			bg.add(formatA4JRB);
			bg.add(format8A4JRB);
			bg.add(formatA5JRB);
			bg.add(formatT85JRB);
			bg.add(formatT73JRB);
			formatA4JRB.setSelected(true);
			formatJP.add(formatA4JRB);
			formatJP.add(format8A4JRB);
			formatJP.add(formatA5JRB);
			formatJP.add(formatT85JRB);
			formatJP.add(formatT73JRB);
			addPrintersListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					// TODO Auto-generated method stub
//					System.out.println("Wybrano drukarke:");
//					System.out.println(getSelectedPrinter().getName());
				}
			});
		}
		return formatJP;
	}
	
	private JTextField getCopyNbJTF() {
		if (labelsNb == null) {
			labelsNb = new JTextField();
			labelsNb.setEnabled(true);
			labelsNb.setEditable(true);
			labelsNb.setPreferredSize(new Dimension(38, 25));
			labelsNb.setSize(38, 25);
			labelsNb.setText("1");
			labelsNb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					getJScrollPane().repaint();
				}
			});
		}
		return labelsNb;
	}
	
	public int getLabelCopies() {
		int theRet = 1;
		try {
			theRet = Integer.parseInt(labelsNb.getText());
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}
		return theRet;
	}
	
	private String normalizeArtBCode(String wfmCode) {
		
		String theRet = wfmCode;
		if (wfmCode.length() == 12)
			theRet = wfmCode.substring(0, 2) + wfmCode.substring(7);
		return theRet;
	}
}
