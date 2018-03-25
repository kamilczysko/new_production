package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnector {

	static final String JDBC_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	static final String DB_URL = "jdbc:sqlserver://localhost:1433;database=magazyn;";



	private final String login = "mag", password = "mag";

	private Connection connect = null;
	private Statement statement = null;

	public DatabaseConnector() {
		connectToDb();
	}

	public void connectToDb() {
		try {
			loadDriver();
			makeConnection(this.login, this.password);
			createStatement();
		} catch (ClassNotFoundException | SQLException e) {
			System.out.println("Nie polaczono do bazy");
			e.printStackTrace();
		}
		System.out.println("Polaczono do bazy");
	}

	private void loadDriver() throws ClassNotFoundException {
		Class.forName(JDBC_DRIVER);
	}

	private void makeConnection(String login, String password) throws SQLException {

		this.connect = DriverManager.getConnection(DB_URL + "user=" + login + ";" + "password=" + password);

	}

	private void createStatement() throws SQLException {
		this.statement = this.connect.createStatement();
	}

	protected Connection getConnection() {
		return this.connect;
	}

	protected Statement getStatement() {
		return this.statement;
	}

}
