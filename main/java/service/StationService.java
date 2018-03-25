package service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import application.QueryCreator;
import model.Station;
import model.StationType;

@Service
public class StationService {

	@Autowired
	private QueryCreator query;
	@Autowired
	private StationTypeService typeService;

	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

	public StationService() {
		System.out.println("station service");
	}

	public List<Station> getAll() {
		String sql = "select * from station";
		List<Station> list = new ArrayList<Station>();

		ResultSet rs = query.makeSimpleQuery(sql);
		list = makeStationList(rs);
		fillStationListWithTypesAttachedToStation(list);
		return list;
	}

	public Station getById(long id) {
		String sql = "select * from station where IDStanowisko = " + id;

		ResultSet rs = query.makeSimpleQuery(sql);
		Station singleStation = makeSingleStationObject(rs);

		return singleStation;
	}

	public List<Station> getByName(String name) {
		return null;
	}

	public List<Station> getByType(long id) {
		String sql = "select IDStanowisko, Nazwa, Opis, PreTime, PostTime from station "
				+ "inner join oznaczenieTypuStanowiska on IDStanowisko = Stanowisko where TypStanowiska = " + id;
		List<Station> list = new ArrayList<Station>();

		ResultSet rs = query.makeSimpleQuery(sql);
		list = makeStationList(rs);

		return list;
	}

	public int updateStationType(Station station) throws SQLException {
		String sql = "update station set Nazwa = ?, Opis = ?," + " PreTime = ?, PostTime = ?"
				+ " where IDStanowisko = ?";

		PreparedStatement stat = query.makeQueryWithArguments(sql);
		stat.setString(1, station.getName());
		stat.setString(2, station.getDescription());
		stat.setString(3, timeFormat.format(station.getPreTime()));
		stat.setString(4, timeFormat.format(station.getPostTime()));
		stat.setLong(5, station.getStationId());

		return stat.executeUpdate();
	}

	public void insertStation(Station station) throws SQLException {
		String sql = "insert into station (Nazwa, Opis, PreTime, PostTime) values (?,?,?,?)";

		PreparedStatement stat = query.makeQueryWithArgumentsAndReturnGeneratedKeys(sql);
		stat.setString(1, station.getName());
		stat.setString(2, station.getDescription());
		stat.setString(3, timeFormat.format(station.getPreTime()));
		stat.setString(4, timeFormat.format(station.getPostTime()));

		int executeUpdate = stat.executeUpdate();
		if(executeUpdate != 0) {
			ResultSet generatedKeys = stat.getGeneratedKeys();
			if(generatedKeys.next())
				station.setStationId(generatedKeys.getInt(1));
		}
	}

	public void setTypesForStationInJoinTable(Station stat) throws SQLException {
		List<StationType> stationTypeList = stat.getStationType();

		for (StationType type : stationTypeList)
			insertIntoJoinTable(stat, type);
	}

	public void removeDemandedRecordsFromJoinTable(List<StationType> typesToRemove, Station stat) throws SQLException {

		for (StationType toRemove : typesToRemove)
			deleteRecordFromJoinTable(stat, toRemove);
	}

	private void insertIntoJoinTable(Station stat, StationType type) throws SQLException {
		String sql = "insert into oznaczenieTypuStanowiska (Stanowisko, TypStanowiska) values (?,?)";

		PreparedStatement state = query.makeQueryWithArguments(sql);
		state.setLong(1, stat.getStationId());
		state.setLong(2, type.getIDTypu());
		state.executeUpdate();
	}

	private void deleteRecordFromJoinTable(Station stat, StationType type) {
		String joinTableDeleteQuery = "delete from oznaczenieTypuStanowiska where TypStanowiska = " + type.getIDTypu()
				+ " and Stanowisko = " + stat.getStationId();
		try {
			query.makeQueryWithNoResult(joinTableDeleteQuery);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean deleteStation(Station station) throws SQLException {
		long stationId = station.getStationId();
		String stationDeleteQuery = "delete from station where IDStanowisko = " + stationId;
		String joinTableDeleteQuery = "delete from oznaczenieTypuStanowiska where Stanowisko = " + stationId;

		query.makeQueryWithNoResult(joinTableDeleteQuery);
		query.makeQueryWithNoResult(stationDeleteQuery);

		return true;
	}

	private List<Station> makeStationList(ResultSet rs) {
		List<Station> list = new ArrayList<Station>();
		try {
			while (rs.next())
				list.add(makeSingleStationObject(rs));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	protected Station makeSingleStationObject(ResultSet rs) {
		Station station = new Station();

		try {
			station.setStationId(rs.getLong("IDStanowisko"));
			station.setName(rs.getString("Nazwa"));
			station.setDescription(rs.getString("Opis"));
			station.setPostTime(rs.getTime("PostTime"));
			station.setPreTime(rs.getTime("PreTime"));

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return station;
	}

	public void fillStationListWithTypesAttachedToStation(List<Station> list) {

		for (Station s : list)
			s.setStationType(typeService.getTypesByStationId(s.getStationId()));
	}

}
