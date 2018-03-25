package service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import application.QueryCreator;
import model.Batch;

@Service
public class BatchService {
	
	
	
	public BatchService() {
		System.out.println("batch service");
	}

	@Autowired
	private QueryCreator query;
	
	public List<Batch> getAll(){
		String sql = "select * from partia";
		List<Batch> list = new ArrayList<Batch>();

		ResultSet rs = query.makeSimpleQuery(sql);
		list = makeListOfBatches(rs);

		return list;
		
	}
	
	public List<Batch> getByOrder(int id){
		String sql = "select * from partia where Zlecenie = "+id;
		List<Batch> list = new ArrayList<Batch>();
		
		ResultSet rs = query.makeSimpleQuery(sql);
		list = makeListOfBatches(rs);
		
		return list;
	}
	
	public Batch save(Batch batch) throws SQLException{
		
		saveBatchInBase(batch);
		return batch;
	}
	
	public List<Batch> save(List<Batch> batchList) throws SQLException{
		
		for(Batch b : batchList)
			saveBatchInBase(b);
		return batchList;
	}
	
	
	private void saveBatchInBase(Batch b) throws SQLException{
		String sql = "insert into partia (NrPartii, Zlecenie, Ilosc, Opis, Grupa) values (?, ?, ?, ?, ?)";
		
		PreparedStatement statement = query.makeQueryWithArgumentsAndReturnGeneratedKeys(sql);
		statement.setInt(1, b.getNrPartii());
		statement.setLong(2, b.getZlecenie());
		statement.setInt(3, b.getIlosc());
		statement.setString(4, b.getOpis());
		statement.setString(5, b.getGrupa());
		
		int executeUpdate = statement.executeUpdate();
		
		if(executeUpdate > 0){
			ResultSet generatedKeys = statement.getGeneratedKeys();
			if(generatedKeys.next())
				b.setIDPartia(generatedKeys.getLong(1));
		}
	}
	
	private List<Batch> makeListOfBatches(ResultSet rs){
		List<Batch> list = new ArrayList<Batch>();
		
		try {
			while(rs.next())
				list.add(makeSingleBatch(rs));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	private Batch makeSingleBatch(ResultSet rs) throws SQLException{
		Batch b = new Batch();

			b.setIDPartia(rs.getLong("IDPartia"));
			b.setIlosc(rs.getShort("Ilosc"));
			b.setNrPartii(rs.getInt("NrPartii"));
			b.setGrupa(rs.getString("Grupa"));
			b.setOpis(rs.getString("Opis"));
			b.setZlecenie(rs.getInt("Zlecenie"));
	
		return b;		
	}
	
}
