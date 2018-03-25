package service;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import application.QueryCreator;
import model.Container;

@Service
public class ContainerService {

	@Autowired
	private QueryCreator query;
	
	public List<Container> getAllContainers(){
		System.out.println("geting containders from db.");
		String sql = "select * from kuwety where dataKasacji is null order by oznaczenie asc";
		
		ResultSet rs = query.makeSimpleQuery(sql);
		List<Container> list = makeListOfContainers(rs);
		System.out.println("done.");
		return list;
	}
	
	public List<Container> getAllDeletedContainers(){
		System.out.println("geting deleted containders from db.");
		String sql = "select * from kuwety where dataKasacji is not null order by oznaczenie asc";
		
		ResultSet rs = query.makeSimpleQuery(sql);
		List<Container> list = makeListOfContainers(rs);
		System.out.println("done.");
		return list;
	}
		
	public void save(List<Container> list) throws SQLException{
		for(Container c : list){
			if(c.getId() == -1)
				saveContainersToBase(c);
			else
				updateContainersInBase(c);
		}
	}
	
	private void saveContainersToBase(Container container) throws SQLException{
		String sql = "insert into kuwety (dataUtworzenia, oznaczenie, r, g, b) values (?, ?, ?, ?, ?)";
		
		PreparedStatement statement = query.makeQueryWithArgumentsAndReturnGeneratedKeys(sql);
		statement.setDate(1, new Date(System.currentTimeMillis()));
		statement.setShort(2, container.getOznaczenie());
		statement.setDouble(3, container.getR());
		statement.setDouble(4, container.getG());
		statement.setDouble(5, container.getB());
		
		int executeUpdate = statement.executeUpdate();
		
		if(executeUpdate > 0){
			ResultSet generatedKeys = statement.getGeneratedKeys();
			if(generatedKeys.next())
				container.setId(generatedKeys.getInt(1));
		}
		
	}
	
	private void updateContainersInBase(Container container) throws SQLException{
		String sql = "update kuwety set r = ?, g = ?, b = ? where id = "+container.getId();
		
		PreparedStatement statement = query.makeQueryWithArguments(sql);
		statement.setDouble(1, container.getR());
		statement.setDouble(2, container.getG());
		statement.setDouble(3, container.getB());
		statement.executeUpdate();
	}
	
	public void removeListOfContainers(List<Container> list) throws SQLException{
		for(Container c : list)
			removeContainer(c);
	}
	
	private void removeContainer(Container container) throws SQLException{
		String sql = "update kuwety set dataKasacji = ? where id = "+container.getId();
		
		PreparedStatement statement = query.makeQueryWithArguments(sql);
		statement.setDate(1, new Date(System.currentTimeMillis()));
		statement.executeUpdate();
	}
	
		
	private List<Container> makeListOfContainers(ResultSet rs){
		List<Container> list = new ArrayList<Container>();
		
		try {
			while(rs.next())
				list.add(makeSingleContainer(rs));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	private Container makeSingleContainer(ResultSet rs) throws SQLException{
		Container c = new Container();

			c.setId(rs.getInt(1));
			c.setDataUtworzenia(rs.getDate(2));
			c.setDataKasacji(rs.getDate(3));
			c.setOznaczenie(rs.getShort(4));
			c.setR(rs.getFloat(5));
			c.setG(rs.getFloat(6));
			c.setB(rs.getFloat(7));

		return c;		
	}
	
}
