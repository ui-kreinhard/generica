package de.karlNet.generica.menu;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIViewRoot;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.primefaces.model.menu.MenuElement;
import org.primefaces.model.menu.MenuModel;

public class MenuMenuModel implements MenuModel, ActionListener, Serializable {
	private static final long serialVersionUID = -7716248816983755362L;
	protected List<MenuElement> contents = new ArrayList<MenuElement>();
	protected static UIViewRoot uiViewRoot = new UIViewRoot();
	private Menu menu;

	public List<MenuElement> getElements() {
		return this.contents;
	}

	public void addElement(MenuElement element) {
		this.contents.add(element);
	}

	public void generateUniqueIds() {
				
	}

	public void processAction(ActionEvent event)
			throws AbortProcessingException {
		
	}

}
