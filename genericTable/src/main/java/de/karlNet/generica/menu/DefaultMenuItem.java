package de.karlNet.generica.menu;

public class DefaultMenuItem extends org.primefaces.model.menu.DefaultMenuItem{
    public DefaultMenuItem(Object value, String icon, String url) {
    	super(value, icon, url);
    	this.setUrl(url);
    }
}