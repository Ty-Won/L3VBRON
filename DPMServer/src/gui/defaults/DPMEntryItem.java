package gui.defaults;

import java.awt.*;

public class DPMEntryItem {
	
	public String key;
	public TextField entry;
	public String type;
	
	public DPMEntryItem(String key, String type, TextField entry){
		this.key = key;
		this.entry = entry;
		this.type = type;
	}

}
