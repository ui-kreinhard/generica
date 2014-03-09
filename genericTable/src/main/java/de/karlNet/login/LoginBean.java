package de.karlNet.login;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;

import de.karlNet.dbhandler.DBHandler;

@Controller
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class LoginBean {
	@Autowired
	private DBHandler dbHandler;
	private String username;
	private boolean loginFailed;
	
	public boolean isLoginFailed() {
		return loginFailed;
	}


	public void setLoginFailed(boolean loginFailed) {
		this.loginFailed = loginFailed;
	}


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	private String password;
	
	public boolean isLoggedIn = false;
	
	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	public void login(String username, String password) throws NotLoggedInException, SQLException {
		this.username = username;
		this.password = password;
		this.login();
	}
	
	public String login() throws SQLException, NotLoggedInException {
		boolean loggedIn = this.dbHandler.login(username, password);
		this.isLoggedIn = loggedIn;
		this.username = "";
		this.password = "";
		if(!loggedIn) {
			throw new NotLoggedInException();
		}
		return "welcome.xhtml";
	}
}
