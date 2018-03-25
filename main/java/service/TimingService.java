package service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import application.QueryCreator;
import model.Production;
import model.Station;
import model.Timing;

@Service
public class TimingService {

	@Autowired
	private ProductionService productionService;
	@Autowired
	private QueryCreator query;

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");

	public List<Timing> getAll() throws SQLException {
		List<Timing> timingList = getAllTimingFromBase();
		return timingList;
	}

	private List<Timing> getAllTimingFromBase() throws SQLException {
		String sql = "select * from timing";
		ResultSet rs = query.makeSimpleQuery(sql);
		List<Timing> list = makeTimingList(rs);
		return list;
	}

	public List<Timing> getById(Production prod) throws SQLException {
		String sql = "select * from timing where Produkcja  = " + prod.getIDProdukcja();
		ResultSet rs = query.makeSimpleQuery(sql);
		List<Timing> list = makeTimingList(rs);
		for (Timing t : list)
			t.setProduction(prod);
		return list;
	}

	public List<Timing> getByStartDateAndStation(Station station, Date start) throws SQLException {
		String sql = "select * from timing "
				+ "inner join produkcja on Produkcja = produkcja.IDProdukcja where Stanowisko = "+ station.getStationId() + " "
						+ "AND  Start >= convert(datetime, '" + dateFormat.format(start)+ "',120)"
								+ " order by Start Asc";

		ResultSet rs = query.makeSimpleQuery(sql);
		List<Timing> list = makeTimingList(rs);
		
		return list;

	}

	public List<Timing> getByDateRange(Date start, Date end) throws SQLException {
		String sql = "select * from timing where Koniec is not null AND (Start > convert(datetime, '" + dateFormat.format(start) + "',120) "
				+ "AND Koniec < convert(datetime, '" + dateFormat.format(end) + "',120))";

		ResultSet rs = query.makeSimpleQuery(sql);
		List<Timing> list = makeTimingList(rs);
		System.out.println(dateFormat.format(start)+" - "+dateFormat.format(end));
		for(Timing t : list)
			System.out.println(t.getAmount());

		return list;
	}

	public List<Timing> getById(Production p, Date startDate) throws SQLException {
		String sql = "select * from timing where Produkcja = '" + p.getIDProdukcja() + "' order by Start ASC";

		ResultSet rs = query.makeSimpleQuery(sql);
		List<Timing> list = makeTimingList(rs);
		//
		for (Timing t : list)
			t.setProduction(p);

		return list;
	}
	

	public void save(List<Timing> timings) throws SQLException{
		for(Timing t : timings)
			save(t);
	}

	public void save(Timing t) throws SQLException {
		String sql = "insert into timing (Produkcja, Start, Ilosc, Opis, Pracownik) values" + " (?, convert(datetime, '"
				+ dateFormat.format(t.getStart()) + "',120), ?, ?, '')";
		PreparedStatement statement = query.makeQueryWithArgumentsAndReturnGeneratedKeys(sql);
		statement.setLong(1, t.getId());
		statement.setInt(2, t.getAmount());
		statement.setString(3, t.getOpis());
		statement.executeUpdate();
	}

	private List<Timing> makeTimingList(ResultSet rs) throws SQLException {
		List<Timing> list = new ArrayList<Timing>();

		while (rs.next()){
			Timing singleTiming = makeSingleTiming(rs);
			singleTiming.setProduction(productionService.getById(singleTiming.getId()));//makeSingleProduction(rs));
			list.add(singleTiming);
		}

		return list;
	}

	private Timing makeSingleTiming(ResultSet rs) throws SQLException {
		Timing t = new Timing();

		t.setId(rs.getLong("Produkcja"));
		t.setLacks(rs.getShort("Braki"));
		t.setAmount(rs.getShort("Ilosc"));
		t.setOpis(rs.getString("Opis"));
		t.setStart(rs.getTimestamp("Start"));
		t.setWorker(rs.getString("Pracownik"));
		
		t.setEnd(rs.getTimestamp("Koniec"));

		return t;
	}

}
