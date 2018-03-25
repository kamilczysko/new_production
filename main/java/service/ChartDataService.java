package service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import application.QueryCreator;
import controller.functions.MyDate;
import model.ChartDataObject;

@Service
public class ChartDataService {

	@Autowired
	private QueryCreator query;
	
	public List<ChartDataObject> getDataForOrder(int order) throws SQLException {
		String sql = "select c.Start, c.Koniec, c.Ilosc, b.Opis, d.PreTime, d.PostTime, d.Duration, b.Operacja,"
				+ "(Select Nazwa from station where IDStanowisko = Stanowisko)+' ('+convert(varchar(3), Stanowisko)+')' as stanowisko,"
				+ "b.Stanowisko, a.NrPartii, d.Nazwa, b.IDProdukcja, d.StationType, Pracownik from partia a "
				+ "left outer join produkcja b on a.IDPartia = b.Partia "
				+ "left outer join timing c on b.IDProdukcja = c.Produkcja "
				+ "left outer join operacja d on b.Operacja = d.IDOperacja "
				+ "where Zlecenie = ?";// AND (Koniec is null or Koniec like '%1900%') order by Partia";
		List<ChartDataObject> list = new ArrayList<ChartDataObject>();
		
		PreparedStatement stat = query.makeQueryWithArguments(sql);
		stat.setInt(1, order);
		ResultSet rs = stat.executeQuery();
		list = makeList(rs);
		
		return list;
	}
	
	private List<ChartDataObject> makeList(ResultSet rs) throws SQLException{
		List<ChartDataObject> list = new ArrayList<ChartDataObject>();
		
		while(rs.next())
			list.add(makeSingleObject(rs));
		
		return list;
	}

	private ChartDataObject makeSingleObject(ResultSet rs) throws SQLException {
	
		ChartDataObject obj = new ChartDataObject.Builder().Amount(rs.getInt("Ilosc"))
				.BatchNumber(rs.getShort("NrPartii"))
				.Description(rs.getString("Opis"))
				.Duration(rs.getTime("Duration"))
				.PreTime(rs.getTime("PreTime"))
				.PostTime(rs.getTime("PostTime"))
				.Start(getMyDate(rs.getTimestamp("Start")))
				.End(getMyDate(rs.getTimestamp("Koniec")))
				.Worker(rs.getString("Pracownik"))
				.StationWithId(rs.getString("stanowisko"))
				.OperationName(rs.getString("Nazwa"))
				.OperationId(rs.getLong("Operacja")).build();
		
		doEndTime(obj);
		
		return obj;
	}
	
	private MyDate getMyDate(Timestamp stamp){
		MyDate date = null;
		
		if(stamp != null){
			date = new MyDate();
			date.setTime(stamp.getTime());
		}
		
		
		return date;	
	}
	
	private void doEndTime(ChartDataObject obj){
		MyDate endDate = obj.getEnd();
		
		if(endDate == null || endDate.getYear() == 1900){
			calculate(obj);
		}else
			obj.setDone(true);
	}
	
	private void calculate(ChartDataObject obj){
		
	}

}
