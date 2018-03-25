package service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import application.QueryCreator;
import model.Process;

@Service
public class ProcessService {

	@Autowired
	QueryCreator query;

	public MultiValueMap<Integer, Process> getAll() throws SQLException {

		System.out.println("getting all processes form ds.");
		MultiValueMap<Integer, Process> processMap = new LinkedMultiValueMap<Integer, Process>();
		String sql = "select * from proces";
		ResultSet simpleQuery = query.makeSimpleQuery(sql);

		while (simpleQuery.next()) {
			Process process = makeProcess(simpleQuery);
			processMap.add(process.getArticle(), process);
		}
		System.out.println("done.");
		return processMap;
	}

	public Process getById(long id) {
		return null;
	}

	public List<Process> getByArticle(int art) {

		System.out.println("Geting process for art: " + art);
		String sql = "select * from proces where Artykul = " + art;
		List<Process> list = new ArrayList<Process>();

		ResultSet results = query.makeSimpleQuery(sql);
		if (results != null)
			list = makeListOfProcessesFromResultSet(results);
		System.out.println("Done.");
		return list;
	}

	public List<Process> getByName(String name) {
		return null;
	}

	public List<Process> getProcessForOrderInProgress(long order) {
		System.out.println("Geting process for order: " + order);
		String sql = "select * from proces " + "where IDProces in (select Proces from operacja "
				+ "where IDOperacja in (select Operacja from Produkcja "
				+ "where (partia in (select IDPartia from partia where Zlecenie = " + order + "))))";
		List<Process> list = new ArrayList<Process>();

		ResultSet result = query.makeSimpleQuery(sql);
		list = makeListOfProcessesFromResultSet(result);
		System.out.println("done.");
		return list;
	}

	public Process saveProcess(Process process) throws SQLException {
		insertProcessToBase(process);
		return process;
	}

	private void insertProcessToBase(Process proc) throws SQLException {
		String sql = "insert into proces (Artykul, Nazwa, Opis, Partia) values (?, ?, ?, ?)";

		PreparedStatement statement = query.makeQueryWithArgumentsAndReturnGeneratedKeys(sql);
		statement.setInt(1, proc.getArticle());
		statement.setString(2, proc.getName());
		statement.setString(3, proc.getDescription());
		statement.setInt(4, proc.getBatch());

		int executeUpdate = statement.executeUpdate();
		if (executeUpdate > 0) {
			ResultSet generatedKeys = statement.getGeneratedKeys();
			if (generatedKeys.next())
				proc.setIDProces(generatedKeys.getLong(1));
		}

	}

	private List<Process> makeListOfProcessesFromResultSet(ResultSet rs) {
		List<Process> list = new ArrayList<Process>();

		try {

			while (rs.next())
				list.add(makeProcess(rs));

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return list;
	}

	private Process makeProcess(ResultSet rs) throws SQLException {
		Process p = new Process();

		// if(rs == null)
		// return null;

		p.setArticle(rs.getInt("Artykul"));
		p.setIDProces(rs.getLong("IDProces"));
		p.setDescription(rs.getString("Opis"));
		p.setName(rs.getString("Nazwa"));
		p.setBatch(rs.getShort("Partia"));

		return p;
	}

}
