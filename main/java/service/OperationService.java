package service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import application.QueryCreator;
import model.Operation;
import model.Process;
import model.StationType;

@Service
public class OperationService {

	@Autowired
	private ProcessService processService;
	@Autowired
	private StationTypeService stationTypeService;
	@Autowired
	private QueryCreator query;
	
	private final int INDEX_OF_LAST_OPERATION = 0;
	
	public List<Operation> getAll(){
		return null;
	}
	
	public Operation getById(long id){
		String sql = "select * from operacja where IDOperacja = "+id ;
		Operation result = null;
		
		try {
			result = makeSingleOperation(query.makeSimpleQuery(sql));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public List<Operation> getOperationsForProcess(Process p){
		String sql = "select * from operacja where Proces = "+p.getIDProces()+" order by IDOperacja DESC";
		List<Operation> list = new ArrayList<Operation>();
		
		Map<Long, Operation> map = getPlainOperationList(query.makeSimpleQuery(sql));
		list = makeListOfCompletedOperationsForSelectedProcess(map);
		
		return list;
	}
	
	public List<Operation> save(List<Operation>operations) throws SQLException{
		for(Operation op : operations)
			saveOperationInBase(op);
		return operations;
	}
	
	public Operation save(Operation operation) throws SQLException{
		saveOperationInBase(operation);
		return operation;
	}
	
	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	
	private void saveOperationInBase(Operation op) throws SQLException{
		String sql = "insert into operacja (Nazwa, Proces, Kolejna, StationType, PreTime, PostTime, Duration, Opis)"
				+ " values (?, ?, ?, ?, ?, ?, ?, ?)";
		
		PreparedStatement statement = query.makeQueryWithArgumentsAndReturnGeneratedKeys(sql);
		statement.setString(1, op.getName());
		statement.setLong(2, op.getProces().getIDProces());
		if(op.getNextOperationObject() != null)
			statement.setLong(3, op.getNextOperationsId());
		else
			statement.setNull(3, Types.NULL);
		statement.setLong(4, op.getStationType().getIDTypu());
		statement.setString(5, timeFormat.format(op.getPreTime()));
		statement.setString(6, timeFormat.format(op.getPostTime()));
		statement.setString(7, timeFormat.format(op.getDuration()));
		statement.setString(8, op.getDescription());
		
		int executeUpdate = statement.executeUpdate();
		if(executeUpdate > 0){
			ResultSet generatedKeys = statement.getGeneratedKeys();
			if(generatedKeys.next())
				op.setId(generatedKeys.getLong(1));
		}
	}
	
	private List<Operation> makeListOfCompletedOperationsForSelectedProcess(Map<Long, Operation> map){
		List<Operation> list = new ArrayList<Operation>();
		
		setNextOperationsInList(map);
		Set<Long> keySet = map.keySet();
		for(long key : keySet){
			Operation op = map.get(key);
			findStationTypeForOperation(op);
			list.add(op);
		}
		
		return list;
	}
	
	private void findStationTypeForOperation(Operation op){
		Long typeId = op.getStationTypeId();
		StationType type = stationTypeService.getById(typeId);
		op.setStationType(type);
	}
	
	private Map<Long, Operation> getPlainOperationList(ResultSet result){
		Map<Long, Operation> operationsForSelectedProcess = new TreeMap<Long, Operation>();
		
		try {
			while(result.next()){
				Operation singleOperation = makeSingleOperation(result);
				operationsForSelectedProcess.put(singleOperation.getId(), singleOperation);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return operationsForSelectedProcess;
	}
	
	protected Operation makeSingleOperation(ResultSet result) throws SQLException{
		Operation operation = new Operation();
		
		if(result != null){
			operation.setId(result.getLong("IDOperacja"));
			operation.setName(result.getString("Nazwa"));
			operation.setNextOperationsId(result.getLong("Kolejna"));
			operation.setProces(processService.getById(result.getLong("Proces")));
			operation.setPreTime(result.getTime("PreTime"));
			operation.setPostTime(result.getTime("PostTime"));
			operation.setDuration(result.getTime("Duration"));
			operation.setDescription(result.getString("Opis"));
			operation.setStationTypeId(result.getLong("StationType"));
		}
		return operation;
	}


	
	private void setNextOperationsInList(Map<Long, Operation> map) {
		Set<Long> keySet = map.keySet();

		for (long key : keySet) {
			Operation currentOperation = map.get(key);
			Long nextOperationId = currentOperation.getNextOperationsId();
			if (nextOperationId != INDEX_OF_LAST_OPERATION) {
				Operation nextOperationObjectFromMap = map.get(nextOperationId);
				System.out.println("operacja: "+currentOperation+", "+currentOperation.getNextOperationsId()+" --- "+nextOperationObjectFromMap.getId());
				currentOperation.setNextOperationObject(nextOperationObjectFromMap);
			} else
				currentOperation.setNextOperationObject(null);
		}
	}
	
}
