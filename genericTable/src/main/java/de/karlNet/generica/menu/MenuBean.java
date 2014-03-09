package de.karlNet.generica.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;

import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuElement;
import org.primefaces.model.menu.MenuModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import de.karlNet.generica.genericTable.daos.DataDAO;

@Controller
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MenuBean {
	private MenuModel menuModel;
	@Autowired
	private DataDAO dataDAO;

	// Maps id to list of children
	private HashMap<Integer, List<Menu>> parentRelation = new HashMap<Integer, List<Menu>>();

	public MenuModel getMenuModel() {
		return menuModel;
	}

	public void reloadMenu() throws Exception {
		this.menuModel = new DefaultMenuModel();
		this.parentRelation.clear();
		List menuItems = this.dataDAO.readOutViewOrTableWithoutMapping("menu",
				Menu.class);
		for (Object object : menuItems) {
			Menu menuItem = (Menu) object;
			if (!this.parentRelation.containsKey(menuItem.getMenu_idmenu())) {
				this.parentRelation.put(menuItem.getMenu_idmenu(),
						new ArrayList<Menu>());
			}
			System.out.println("adding " + menuItem.getMenu_idmenu());
			List<Menu> parentList = this.parentRelation.get(menuItem
					.getMenu_idmenu());
			parentList.add(menuItem);
		}
		List<MenuElement> rootNodes = new ArrayList<MenuElement>();
		// extract root nodes
		for (Menu menu : this.parentRelation.get(0)) {
			if (this.parentRelation.containsKey(menu.getId())
					&& this.parentRelation.get(menu.getId()).size() > 0) {
				DefaultSubMenu rootMenuItem = new DefaultSubMenu(
						menu.getMenulabel(), null);
				this.createSubmenuItems(menu, rootMenuItem);
				this.menuModel.addElement(rootMenuItem);
			} else {
				MenuElement rootMenuItem = new DefaultMenuItem(
						menu.getMenulabel(), null, menu.getLink());
				this.menuModel.addElement(rootMenuItem);
			}
		}
		System.out.println("init done");
	}

	@PostConstruct
	public void init() throws Exception {
		if (this.menuModel != null) {
			return;
		}
		this.reloadMenu();
	}

	private void createSubmenuItems(Menu parentNode, DefaultSubMenu parentMenu) {
		List<Menu> menus = this.parentRelation.get(parentNode.getId());
		for (Menu menu : this.parentRelation.get(parentNode.getId())) {
			if (this.parentRelation.containsKey(menu.getId())
					&& this.parentRelation.get(menu.getMenu_idmenu()).size() > 0) {
				DefaultSubMenu subMenuItem = new DefaultSubMenu(
						menu.getMenulabel(), null);
				parentMenu.addElement(subMenuItem);
				this.createSubmenuItems(menu, subMenuItem);
			} else {
				DefaultMenuItem subMenuItem = new DefaultMenuItem(
						menu.getMenulabel(), null, menu.getLink());
				parentMenu.addElement(subMenuItem);
			}
		}
	}
}
