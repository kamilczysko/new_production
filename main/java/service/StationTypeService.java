package service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import application.QueryCreator;
import model.Station;
import model.StationType;

@Service
public class StationTypeService {
		
	@Autowired
	private QueryCreator query;
	@Autowired 
	private StationService stationService;
	
	
	
	public StationTypeService() {
		System.out.println("stationType Service");
	}

	public List<StationType> getStationTypes() {
		String sql = "select * from stationType";
		
		List<StationType> typesList = makeStationTypeList(query.makeSimpleQuery(sql));
		
		return typesList;
	}

	public StationType getById(long id) {
		String sql = "select * from stationType where IDTypu = "+id;
		
		StationType singleType = makeStationTypeList(query.makeSimpleQuery(sql)).get(0);
		
		return singleType;
	}
	
	public List<StationType> getTypesByStationId(long id){
		List<StationType> list = new ArrayList<StationType>();
		String sql = "select IDTypu, Nazwa, Opis from stationType "
		+ "inner join oznaczenieTypuStanowiska on IDTypu = TypStanowiska "
		+ "where Stanowisko = "+id;
		
		ResultSet types = query.makeSimpleQuery(sql);
		list = makeStationTypeList(types);
		
		return list;
	}
	
	
	public void updateStationType(StationType type) throws SQLException{
		String sql = "update stationType set Nazwa = '"+type.getName()+"', Opis = '"+type.getDescription()+"' where IDTypu = "+type.getIDTypu();
		
		query.makeQueryWithNoResult(sql);
	}
	
	public void insertStationType(StationType type) throws SQLException{
		String sql = "insert into stationType (Nazwa, Opis) values ('"+type.getName()+"','"+type.getDescription()+"')";
		
		query.makeQueryWithNoResult(sql);
	}
	
//	public void removeFromStationMark(long typeId){
//
//	}
//	
	public boolean deleteType(List<StationType> types) throws SQLException {
		for(StationType type : types){
			long idTypu = type.getIDTypu();
			String stationTypeDeleteQuery = "delete from stationType where IDTypu = "+idTypu;
			String joinTableDeleteQuery = "delete from oznaczenieTypuStanowiska where TypStanowiska = "+idTypu;
			
			query.makeQueryWithNoResult(joinTableDeleteQuery);
			query.makeQueryWithNoResult(stationTypeDeleteQuery);			
		}
		
		return true;
	}
	
	private List<StationType> makeStationTypeList(ResultSet rs){
		List<StationType> list = new ArrayList<StationType>();
		try {
			while(rs.next())
				list.add(makeSingleStationTypeObject(rs));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		makeListCompleted(list);
		return list;
	}
	
	private StationType makeSingleStationTypeObject(ResultSet rs){
		StationType stationType = new StationType();
		
		try {
			stationType.setIDTypu(rs.getLong("IDTypu"));
			stationType.setName(rs.getString("Nazwa"));
			stationType.setDescription(rs.getString("Opis"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return stationType;
	}
	
	private void makeListCompleted(List<StationType> list){
		
		for(StationType type : list){
			List<Station> stationList = stationService.getByType(type.getIDTypu());
			type.setStation(stationList);
		}
		
	}

}
