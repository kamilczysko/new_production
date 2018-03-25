package controller.functions;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import model.Batch;
import model.Operation;
import model.Production;
import model.Station;
import service.BatchService;
import service.ProductionService;
import service.StationService;

@Service
public class SaveProduction {

	// private List<Operation> firstOperations;

	@Autowired
	private StationService stationService;
	@Autowired
	private ProductionService productionService;
	@Autowired
	private TimingTool timingTool;
	@Autowired
	private BatchService batchService;

	private MultiValueMap<Operation, Production> prodMap = new LinkedMultiValueMap<Operation, Production>();

	private SimpleDateFormat format = new SimpleDateFormat();

//	private List<Production> prodListToSave = new ArrayList<Production>();
	private List<Production> lastProduction = new ArrayList<Production>();
	private Map<Long, Station> allStationsFromBase;
	
	public void initStationsMap(){
		allStationsFromBase = new TreeMap<Long, Station>();
		List<Station> all = stationService.getAll();
		for(Station s : all)
			allStationsFromBase.put(s.getStationId(), s);
	}

	public void saveBatches(List<Batch> list) throws SQLException{
		batchService.save(list);
	}
	
	int base = 1, up = 0;
	
	public void saveProduction(List<Operation> fistOperations, Date start) throws SQLException {
		up = 0;
		base = 0;
		lastProduction = new ArrayList<Production>();
		Map<String, Operation> opMap = makeSortedMap(fistOperations);
		List<Production> savedProdList = null;
		char group = 'a';

		timingTool.initDateMap();
		Date timeForOperation = start;
		Date timeForLastOperation = start;

		while (opMap.containsKey(group + "")) {
			timeForOperation = start;
			Operation operation = opMap.get(group + "");
			MultiValueMap<Operation, Production> makeProductionList = makeProductionList(operation);
			Set<Operation> keySet = makeProductionList.keySet();
			for (Operation k : keySet) {
				List<Production> saveProdInBase = saveProdInBase(makeProductionList.get(k));
				timeForOperation = timingTool.saveTiming(saveProdInBase, timeForOperation);
				if (timeForLastOperation.before(timeForOperation))
					timeForLastOperation = timeForOperation;
			}

			group += 1;
		}

		timingTool.saveTiming(saveProdInBase(lastProduction), timeForLastOperation);

		System.out.println(savedProdList);
	}

	// sortowanie partii zrobic

	private List<Production> saveProdInBase(List<Production> production) throws SQLException {
		// zapis produkcji i timingu jednoczesnie
		List<Production> prodList = new ArrayList<Production>();
		for (Production prod : production)
			prodList.add(productionService.save(prod));

		return prodList;
	}

	private Map<String, Operation> makeSortedMap(List<Operation> firstOperations) {
		Map<String, Operation> opMap = new TreeMap<String, Operation>();
		for (Operation first : firstOperations)
			
			switch (first.getGroup()) {// do 4 grup obsluga
			case "a":
				opMap.put("a", first);
				break;
			case "b":
				opMap.put("b", first);
				break;
			case "c":
				opMap.put("c", first);
				break;
			case "d":
				opMap.put("d", first);
				break;
			}
		return opMap;
	}

	private MultiValueMap<Operation, Production> makeProductionList(Operation o) {
		Operation tmpOp = o;
		prodMap = new LinkedMultiValueMap<Operation, Production>();
		
		while (tmpOp.getNextOperationObject() != null) 
			tmpOp = getDataToGenerateProduction(tmpOp);

		if (tmpOp.getNextOperationObject() == null) {
			boolean test = false;
			for (Production p : lastProduction){
				if (tmpOp == p.getOperationObject())
					test = true;
			}
			if (!test)
				getDataToGenerateProduction(tmpOp, lastProduction);
		}
		return prodMap;
	}

	private Operation getDataToGenerateProduction(Operation op) {
		MultiValueMap<Long, Batch> map = op.getMap();
		Set<Long> keySet = map.keySet();
		
		for (long key : keySet) {

			Station station = allStationsFromBase.get(key);

			List<Batch> list = map.get(key);
			sortList(list);

			for (Batch b : list)
				prodMap.add(op, makeProduction(b, station, op));

		}
		return op.getNextOperationObject();
	}

	private Operation getDataToGenerateProduction(Operation op, List<Production> prodListToSave) {
		MultiValueMap<Long, Batch> map = op.getMap();
		Set<Long> keySet = map.keySet();
		for (long key : keySet) {
			Station station = allStationsFromBase.get(key);

			List<Batch> list = map.get(key);
			sortList(list);

			for (Batch b : list)
				prodListToSave.add(makeProduction(b, station, op));

		}
		return op.getNextOperationObject();
	}

	private Production makeProduction(Batch b, Station station, Operation op) {
		Production prod = new Production();
//			batchService.save(b);
			prod.setBatchObject(b);
			prod.setDescription("");
			prod.setOperationObject(op);
			prod.setStationObject(station);
			prod.setPostTime(format.format(op.getPostTime()));
			prod.setPreTime(format.format(op.getPreTime()));
	
		return prod;
	}

	private void sortList(List<Batch> list) {
		Collections.sort(list, new Comparator<Batch>() {

			@Override
			public int compare(Batch o1, Batch o2) {
				return (o1.getNrPartii() > o2.getNrPartii()) ? o1.getNrPartii() : o2.getNrPartii();
			}

		});
	}

}
