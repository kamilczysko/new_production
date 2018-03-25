package controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;

import controller.functions.MakePDF;
import controller.functions.MyDate;
import controller.functions.ReportObject;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Station;
import model.Timing;
import service.TimingService;

@Controller
public class ReportController {
	
	@FXML
	private DatePicker chooseDay;
	@FXML
	private Pane fileChooserPane;
	@FXML
	private Button saveButton;
	@FXML
	private TextField dateField;
	@FXML
	private TextField startField;
	@FXML
	private TextField endField;
	@FXML
	private TextField operationField;
	@FXML
	private TextField amountField;
	@FXML
	private TextField problemsField;
	@FXML
	private TreeView<ReportObject> treePane;
	
	@Autowired
	private TimingService timingService;
	
	private List<ReportObject> repoList;
	private FileChooser fileChooser = new FileChooser();
	private TreeItem<ReportObject>root = null;
	private boolean isGeneratedReport = false;
	
	@FXML
	private void initialize(){
		File f = new File("C://");
		fileChooser.setInitialDirectory(f);
		fileChooser.setInitialFileName("test");
	}
	
	// Event Listener on Button.onAction
	@FXML
	public void generateReport(ActionEvent event) throws SQLException {
		clearFields();
		repoList = new ArrayList<ReportObject>();
		isGeneratedReport = false;
		if(chooseDay.getValue() != null){
			isGeneratedReport = true;
			Date[] timeRange = getTimeRange();
				List<Timing> timings = timingService.getByDateRange(timeRange[0], timeRange[1]);
				for(Timing t : timings){
					Station actualStation = t.getProduction().getStationObject();
					ReportObject repoTest = getRepoIfExistOrNull(actualStation);
					if(repoTest == null){//jesli jest
						repoList.add(makeReportObj(t));
					}else{
						if(repoTest.getEnd().before(t.getEnd()))
							repoTest.setEnd(t.getEnd());
						repoTest.setAmount(repoTest.getAmount() + t.getAmount());
						if(t.getOpis() != "")
							repoTest.setProblems("\n -"+t.getOpis());
						if(!hasWorker(repoTest, t.getWorker()))
							repoTest.setOperator(repoTest.getOperator()+", "+t.getWorker());
					}
				}
				if(!repoList.isEmpty())
					initTree();	
		}	
	}
	
	private boolean hasWorker(ReportObject obj, String worker){
		if(obj.getOperator().matches(".*["+worker+"].*")){
			return true;
		}
		return false;
	}

	private void initTree(){
		
		if(root != null && root.getChildren() != null)
			root.getChildren().clear();
		root = new TreeItem<ReportObject>();
		for(ReportObject r : repoList){
			root.getChildren().add(newTreeItem(r));
		}
		root.setExpanded(true);
		treePane.setRoot(root);
		
		treePane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<ReportObject>>() {

			@Override
			public void changed(ObservableValue<? extends TreeItem<ReportObject>> arg0, TreeItem<ReportObject> arg1,
					TreeItem<ReportObject> arg2) {
				if(arg0.getValue() != null){
					ReportObject value = arg0.getValue().getValue();
					if(value != null){
						dateField.setText(value.getDate());
						endField.setText(value.getEndString());
						startField.setText(value.getStartString());
						amountField.setText(value.getAmount() +"");
						operationField.setText(value.getOperator());
					}
				}
				
			}
		});
	}

	private void clearFields() {
		dateField.clear();
		endField.clear();
		startField.clear();
		amountField.clear();
		problemsField.clear();
		operationField.clear();
	}
	
	private TreeItem<ReportObject> newTreeItem(ReportObject r){
		TreeItem<ReportObject> item = new TreeItem<ReportObject>();
		item.setValue(r);
		return item;
	}
	
	private ReportObject getRepoIfExistOrNull(Station s){
		for(ReportObject r : repoList){
			if(r.getStation().getStationId() ==  s.getStationId())
				return r;
		}
		return null;
	}
	
	private ReportObject makeReportObj(Timing t){
		ReportObject obj = new ReportObject();
		obj.setAmount(t.getAmount());
		obj.setProblems(t.getOpis());
		if(t.getLacks() != 0)
			obj.setProblems(t.getOpis() + "\n Braki: "+t.getLacks());
		obj.setEnd(t.getEnd());
		obj.setStart(t.getStart());
		obj.setOperation(t.getProduction().getOperationObject());
		obj.setStation(t.getProduction().getStationObject());
		if(obj.getOperator() == null)
			obj.setOperator(t.getWorker());
		else
			if(!obj.getOperator().equals(t.getWorker()))
				obj.setOperator(t.getWorker()+", "+t.getWorker());
		
		return obj;
	}
	
	private SimpleDateFormat dayFormat = new SimpleDateFormat("dd.MM.yyyy");
	private ZoneId defaultZoneId = ZoneId.systemDefault();
	
	@FXML
	public void saveReport(ActionEvent event) {
		
		if(isGeneratedReport){
			String day = dayFormat.format(Date.from(chooseDay.getValue().atStartOfDay(defaultZoneId).toInstant()));
			fileChooser.setInitialFileName("raport_"+day+".pdf");
			File file = fileChooser.showSaveDialog(new Stage());
			MakePDF pdfCreator = new MakePDF();
			try {
				Document document = new Document();
				PdfWriter.getInstance(document, new FileOutputStream(file));
				document.open();
					pdfCreator.makeDocument(document, repoList, day);
				document.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (DocumentException e) {
				e.printStackTrace();
			}

		}
	}
	
	private Date[] getTimeRange(){
		LocalDate value = chooseDay.getValue();
		System.out.println(value);
		Date d1 = Date.from(value.atStartOfDay(defaultZoneId).toInstant());
		MyDate md1 = new MyDate();
		md1.setTime(d1.getTime());
		md1.setHourAndMinute(0, 0);
		md1.setSeconds(1);
		Date d2 = Date.from(value.atStartOfDay(defaultZoneId).toInstant());
		MyDate md2 = new MyDate();
		md2.setTime(d2.getTime());
		md2.setHourAndMinute(23, 59);
		md2.setSeconds(59);
		Date [] dat = {md1,md2};
		return dat;
	}
}
