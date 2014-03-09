package de.karlNet.dbhandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;

@Controller
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
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

	public boolean login(String username, String password) throws SQLException {
		try {
			this.connection = DriverManager.getConnection(url, username, password);
		} catch (SQLException e) {
			if(e.getSQLState().equals("28P01")) {
				return false;
			}
			throw e;
		}
		return true;
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
