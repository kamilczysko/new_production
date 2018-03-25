package application;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.stereotype.Service;

@Service
public class QueryCreator extends DatabaseConnector{

	
	public ResultSet makeSimpleQuery(String query){
		
		try {
			return getConnection().createStatement().executeQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public void makeQueryWithNoResult(String query) throws SQLException{
			
		getConnection().createStatement().execute(query);
	}
	
	public PreparedStatement makeQueryWithArguments(String query){
		PreparedStatement stat = null;
		try {
			stat = getConnection().prepareStatement(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return stat;
	}
	
	public PreparedStatement makeQueryWithArgumentsAndReturnGeneratedKeys(String query){
		PreparedStatement stat = null;
		try {
			stat = getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return stat;
	}
	
}
