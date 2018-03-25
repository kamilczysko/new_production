package controller.functions;

import java.util.List;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

public class MakePDF{
	
    private Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18,
            Font.BOLD);

    public void makeDocument(Document doc, List<ReportObject> repoObj, String day){
    	try {
    		addMetaData(doc, day);
			addTitlePage(doc, day);
			makeTable(doc, repoObj);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
    	
    }
    
    private void addMetaData(Document document, String day) {
	        document.addTitle(""+ day);
	    }

	  private void addTitlePage(Document document, String day)
	            throws DocumentException {
	        Paragraph preface = new Paragraph();
	        // We add one empty line
	        addEmptyLine(preface, 1);
	        // Lets write a big header
	        preface.add(new Paragraph("Raport maszynowy z dnia "+day, catFont));
	        addEmptyLine(preface, 1);
	        document.add(preface);

	    }
	  
	  private void addEmptyLine(Paragraph paragraph, int number) {
	        for (int i = 0; i < number; i++) {
	            paragraph.add(new Paragraph(" "));
	        }
	    }
	  
	private void makeTable(Document doc, List<ReportObject> repoObj) {
		Paragraph par = new Paragraph();

		for (ReportObject r : repoObj) {
			System.out.println(r.getStation().getName() + " pdf");

			try {
				createTable(doc, par, r);
				addEmptyLine(par, 1);

			} catch (BadElementException e) {
				e.printStackTrace();
			}

		}
		try {
			doc.add(par);
		} catch (DocumentException e) {
			e.printStackTrace();
		}

	}

	  private void createTable(Document doc, Paragraph par, ReportObject repObj)
	            throws BadElementException {
	        PdfPTable table = new PdfPTable(2);

	        PdfPCell c1 = new PdfPCell();
	        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
	        table.addCell(c1);

	        c1 = new PdfPCell();
	        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
	        table.addCell(c1);
	        
	        setTableCells(table, repObj);
	        
	        par.add(table);
	    }
	  
	  public void setTableCells(PdfPTable table, ReportObject rep){
	      table.addCell("Stanowisko");
	        table.addCell(rep.getStation().getName()+"("+rep.getStation().getStationId()+")");
	        table.addCell("Czas rozpoczecia");
	        table.addCell(rep.getStartString());
	        table.addCell("Czas zakonczenia");
	        table.addCell(rep.getEndString());
	        table.addCell("Liczba wykonanych elementow");
	        table.addCell(rep.getAmount()+"");
	        table.addCell("Asortyment");
	        table.addCell("");
	        table.addCell("Operator");
	        table.addCell(rep.getOperator());
	        table.addCell("Problemy");
	        table.addCell(rep.getProblems());
	  }
	 
	
}

