package service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import application.QueryCreator;
import controller.StationDefinitionController;
import model.Batch;
import model.Operation;
import model.Production;
import model.Station;
import model.Timing;

@Service
public class ProductionService {

	@Autowired
	private TimingService timingService;
	@Autowired
	private QueryCreator query;

	private Map<Long, Production> productionMap = new TreeMap<Long, Production>();
	
	public void initMaps(){
		productionMap.clear();
	}

	public List<Production> getAll() throws SQLException {
		String sql = "select produkcja.IDProdukcja, produkcja.Partia, produkcja.Operacja, produkcja.Stanowisko, produkcja.PreTime, produkcja.PostTime, produkcja.Opis, "
				+ "station.IDStanowisko, station.Nazwa as stationName, station.Opis as stationDesc, station.PostTime as stationPostTime, station.PreTime as stationPreTime, "
				+ "operacja.IDOperacja, operacja.Nazwa, operacja.Kolejna, operacja.PreTime as operacjaPreTime, operacja.PostTime as operacjaPostTime, operacja.Duration, operacja.Opis as operacjaOpis, operacja.StationType, "
				+ "partia.IDPartia, partia.Ilosc, partia.NrPartii, partia.Grupa, partia.Opis as batchDesc, partia.Zlecenie "
				+ "from produkcja "
				+ "inner join partia on Partia = IDPartia "
				+ "inner join operacja on Operacja = IDOperacja "
				+ "inner join station on Stanowisko = IDStanowisko";

		ResultSet rs = query.makeSimpleQuery(sql);
		List<Production> list = makeListOfProductions(rs);

		return list;
	}

	public Production getById(long id) throws SQLException {
		if(productionMap.containsKey(id))
			return productionMap.get(id);
		
		String sql = "select produkcja.IDProdukcja, produkcja.Partia, produkcja.Operacja, produkcja.Stanowisko, produkcja.PreTime, produkcja.PostTime, produkcja.Opis, "
				+ "station.IDStanowisko, station.Nazwa as stationName, station.Opis as stationDesc, station.PostTime as stationPostTime, station.PreTime as stationPreTime, "
				+ "operacja.IDOperacja, operacja.Nazwa, operacja.Kolejna, operacja.PreTime as operacjaPreTime, operacja.PostTime as operacjaPostTime, operacja.Duration, operacja.Opis as operacjaOpis, operacja.StationType, "
				+ "partia.IDPartia, partia.Ilosc, partia.NrPartii, partia.Grupa, partia.Opis as batchDesc, partia.Zlecenie "
				+ "from produkcja "
				+ "inner join partia on Partia = IDPartia "
				+ "inner join operacja on Operacja = IDOperacja "
				+ "inner join station on Stanowisko = IDStanowisko "
				+"where IDProdukcja = " + id;
		List<Production> list = new ArrayList<Production>();

		ResultSet rs = query.makeSimpleQuery(sql);
		while(rs.next()) {
			Production prod = makeSingleProduction(rs);
			prod.setBatchObject(makeSingleBatch(rs));
			prod.setOperationObject(makeSingleOperation(rs));
			prod.setStationObject(makeSingleStationObject(rs));
			list.add(prod);
		}
		productionMap.put(list.get(0).getIDProdukcja(), list.get(0));
		return list.get(0);
	}
	
	public List<Production> getByBatch(Batch batch, Operation operation) throws SQLException {
		String sql = "select produkcja.IDProdukcja, produkcja.Partia, produkcja.Operacja, produkcja.Stanowisko, produkcja.PreTime, produkcja.PostTime, produkcja.Opis, "
				+ "station.IDStanowisko, station.Nazwa as stationName, station.Opis as stationDesc, station.PostTime as stationPostTime, station.PreTime as stationPreTime, "
				+ "from produkcja "
				+ "inner join station on Stanowisko = IDStanowisko "+
				"where Partia = " + batch.getIDPartia() + " and Operacja = "
				+ operation.getId();
		List<Production> list = new ArrayList<Production>();
		ResultSet rs = query.makeSimpleQuery(sql);
		while(rs.next()){
			Production prod = makeSingleProduction(rs);
			prod.setBatchObject(batch);
			prod.setOperationObject(operation);
			prod.setStationObject(makeSingleStationObject(rs));
			list.add(prod);
		}
		addToMap(list);
		return list;
	}
	
	private void addToMap(List<Production> p){
		for(Production prod : p)
			productionMap.put(prod.getIDProdukcja(), prod);
		
	}

	public List<Timing> getTimingsForOrder(Batch batch) throws SQLException {
		List<Timing> timings = new ArrayList<Timing>();
		List<Production> byBatch = getByBatch(batch);
		for (Production actualProduction : byBatch) {
			List<Timing> timingList = timingService.getById(actualProduction);
			timings.addAll(timingList);
		}
		
		return timings;
	}

	public List<Production> findByStation(Station station) throws SQLException {
		String sql = "select produkcja.IDProdukcja, produkcja.Partia, produkcja.Operacja, produkcja.Stanowisko, produkcja.PreTime, produkcja.PostTime, produkcja.Opis, "
				+ "operacja.IDOperacja, operacja.Nazwa, operacja.Kolejna, operacja.PreTime as operacjaPreTime, operacja.PostTime as operacjaPostTime, operacja.Duration, operacja.Opis as operacjaOpis, operacja.StationType, "
				+ "partia.IDPartia, partia.Ilosc, partia.NrPartii, partia.Grupa, partia.Opis as batchDesc, partia.Zlecenie "
				+ "from produkcja "
				+ "inner join partia on Partia = IDPartia "
				+ "inner join operacja on Operacja = IDOperacja "
				+"where Stanowisko = " + station.getStationId();

		ResultSet rs = query.makeSimpleQuery(sql);
		List<Production> list = new ArrayList<Production>();

		while(rs.next()){
			Production prod = makeSingleProduction(rs);
			prod.setStationObject(station);
			prod.setOperationObject(makeSingleOperation(rs));
			prod.setBatchObject(makeSingleBatch(rs));
			list.add(prod);
		}
		
		return list;
	}

	public List<Timing> findTimingsByStation(Station station) throws SQLException {

		List<Timing> timingList = new ArrayList<Timing>();
		List<Production> listOfProduction = findByStation(station);
		for (Production p : listOfProduction) {
			List<Timing> timings = timingService.getById(p);
			for (Timing t : timings) {
				t.setProduction(p);
				timingList.add(t);
			}
		}
		return timingList;
	}

	public List<Timing> findTimingsByStation(Station station, Date startDate) throws SQLException {
		List<Timing> timingList = timingService.getByStartDateAndStation(station, startDate);
		return timingList;
	}

	public Production save(Production entity) throws SQLException {
		saveProduction(entity);
		return entity;
	}

	public void save(List<Production> entity) throws SQLException {
		for (Production prod : entity)
			saveProduction(prod);
	}

	private void saveProduction(Production prod) throws SQLException {
		String sql = "insert into produkcja (Partia, Operacja, Stanowisko, PreTime, PostTime, Opis) values (?, ?, ?, ?, ?, ?)";

		PreparedStatement statement = query.makeQueryWithArgumentsAndReturnGeneratedKeys(sql);
		statement.setLong(1, prod.getBatchObject().getIDPartia());
		statement.setLong(2, prod.getOperationObject().getId());
		statement.setLong(3, prod.getStationObject().getStationId());
		statement.setString(4, prod.getPreTime());
		statement.setString(5, prod.getPostTime());
		statement.setString(6, prod.getDescription());

		int executeUpdate = statement.executeUpdate();

		if (executeUpdate > 0) {
			ResultSet generatedKeys = statement.getGeneratedKeys();
			if (generatedKeys.next())
				prod.setIDProdukcja(generatedKeys.getLong(1));
		}
	}
	
	private List<Production> makeListOfProductions(ResultSet rs) throws SQLException {
		List<Production> prod = new ArrayList<Production>();
		if(rs != null)
			while (rs.next()){
				Production singleProduction = makeSingleProduction(rs);
				singleProduction.setOperationObject(makeSingleOperation(rs));
				singleProduction.setStationObject(makeSingleStationObject(rs));
				singleProduction.setBatchObject(makeSingleBatch(rs));
				prod.add(singleProduction);
				
			}

		return prod;
	}
	
	public List<Production> getByBatch(Batch batch) throws SQLException {
		String sql = "select produkcja.IDProdukcja, produkcja.Partia, produkcja.Operacja, produkcja.Stanowisko, produkcja.PreTime, produkcja.PostTime, produkcja.Opis, "
				+ "operacja.IDOperacja, operacja.Nazwa, operacja.Kolejna, operacja.PreTime as operacjaPreTime, operacja.PostTime as operacjaPostTime, operacja.Duration, operacja.Opis as operacjaOpis, operacja.StationType "
				+ "from produkcja "
				+ "inner join operacja on Operacja = IDOperacja "
				+ "where produkcja.Partia = "+batch.getIDPartia();

		ResultSet rs = query.makeSimpleQuery(sql);
		List<Production> list = new ArrayList<Production>();//makeListOfProductions(rs);
		
		while(rs.next()){
			Operation operation = makeSingleOperation(rs);
			Production prod = makeSingleProduction(rs);
			
			prod.setOperationObject(operation);
			prod.setBatchObject(batch);
			prod.setStationObject(StationDefinitionController.getMapOfStations().get(prod.getStation()));
			
			list.add(prod);
		}
			
		return list;
	}
	
	private Batch makeSingleBatch(ResultSet rs) throws SQLException{
		Batch b = new Batch();

			b.setIDPartia(rs.getLong("IDPartia"));
			b.setIlosc(rs.getShort("Ilosc"));
			b.setNrPartii(rs.getInt("NrPartii"));
			b.setGrupa(rs.getString("Grupa"));
			b.setOpis(rs.getString("batchDesc"));
			b.setZlecenie(rs.getInt("Zlecenie"));
	
		return b;		
	}

	SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	private Production makeSingleProduction(ResultSet rs) throws SQLException {
		Production p = new Production();

		p.setIDProdukcja(rs.getLong("IDProdukcja"));
		p.setBatch(rs.getLong("Partia"));
		p.setOperation(rs.getLong("Operacja"));
		p.setStation(rs.getLong("Stanowisko"));
		p.setPreTime(timeFormat.format(rs.getTime("PreTime")));
		p.setPostTime(timeFormat.format(rs.getTime("PostTime")));
		p.setDescription(rs.getString("Opis"));

		return p;
	}
		
	protected Station makeSingleStationObject(ResultSet rs){
		Station station = new Station();
		
		try {
			station.setStationId(rs.getLong("IDStanowisko"));
			station.setName(rs.getString("stationName"));
			station.setDescription(rs.getString("stationDesc"));
			station.setPostTime(rs.getTime("stationPostTime"));
			station.setPreTime(rs.getTime("stationPreTime"));
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return station;
	}
	
	protected Operation makeSingleOperation(ResultSet result) throws SQLException{
		Operation operation = new Operation();
		
		if(result != null){
			operation.setId(result.getLong("IDOperacja"));
			operation.setName(result.getString("Nazwa"));
			operation.setNextOperationsId(result.getLong("Kolejna"));
//			operation.setProces(result.getLong("Proces"));
			operation.setPreTime(result.getTime("operacjaPreTime"));
			operation.setPostTime(result.getTime("operacjaPostTime"));
			operation.setDuration(result.getTime("Duration"));
			operation.setDescription(result.getString("operacjaOpis"));
			operation.setStationTypeId(result.getLong("StationType"));
		}
		return operation;
	}

}
