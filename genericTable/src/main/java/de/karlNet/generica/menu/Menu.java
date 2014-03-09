package de.karlNet.generica.menu;

import java.util.ArrayList;
import java.util.List;

public class Menu {
	private Integer id;

	private String link;
	private Integer menu_idmenu;
	private String menulabel;
	public Integer getId() {
		return id;
	}

	public String getLink() {
		return link;
	}

	public String getMenulabel() {
		return menulabel;
	}

	public void setMenulabel(String menulabel) {
		this.menulabel = menulabel;
	}

	public Integer getMenu_idmenu() {
		return menu_idmenu;
	}


	public void setId(Integer id) {
		this.id = id;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public void setMenu_idmenu(Integer menu_idmenu) {
		this.menu_idmenu = menu_idmenu;
	}


	@Override
	public String toString() {
		return "Menu [id=" + id + ", link=" + link + ", menuLabel=" + menulabel
				+ ", menu_idmenu=" + menu_idmenu + "]";
	}
}
