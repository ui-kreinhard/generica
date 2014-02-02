package de.karlNet.genericTable.dbhandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

@Component
public class DBHandler {
	public Connection getConnection() {
		return connection;
	}

	public String getDatabase() {
		return database;
	}

	public String getUrl() {
		return url;
	}

	public String getUser() {
		return user;
	}

	private Connection connection;
	@Value("${postgres.database}")
	private String database = "";
	@Value("${postgres.url}")
	private String url = "";
	@Value("${postgres.username}")
	private String user = "";
	@Value("${postgres.password}")
	private String password  = "";
	
	public ResultSet executeQuery(String query) throws SQLException {
		return this.executeQuery(this.connection.prepareStatement(query));
	}

	public ResultSet executeQuery(PreparedStatement query) throws SQLException {
		ResultSet resultSet = query.executeQuery();
		return resultSet;
	}

	public void executeUpdate(PreparedStatement query) throws SQLException {
		query.execute();
	}

	public int getNextID(String tableName) throws SQLException {
		PreparedStatement statement = this.connection
				.prepareStatement("SELECT Auto_increment FROM information_schema.tables WHERE table_name=? and TABLE_SCHEMA=?");
		statement.setString(1, tableName);
		statement.setString(2, this.database);
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.next()) {
			return resultSet.getInt("Auto_increment");
		}
		return -1;

	}
	
	public PreparedStatement prepareStatement(String query) throws SQLException {
		return this.connection.prepareStatement(query);
	}

	@PostConstruct
	public void init() throws SQLException {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Class.forName("org.postgresql.Driver");
			this.connection = DriverManager.getConnection(url, user, password);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 
		
	}
}
