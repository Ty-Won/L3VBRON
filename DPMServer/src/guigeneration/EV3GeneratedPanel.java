package guigeneration;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gui.MainWindow;
import gui.defaults.*;
import transmission.ServerEV3;

public class EV3GeneratedPanel extends DPMPanel implements ActionListener {

	private static final long serialVersionUID = 2518725966535981673L;
	private ServerEV3 server;
	private Document contents;
	public DPMEntryItemList textBoxes, teamBoxes;
	private Button start, stop, clear, fill;

	private FileDialog fileSelect;

	public EV3GeneratedPanel(MainWindow mw, Document fields, int port) {
		super(mw);
		contents = fields;
		Node inputs = contents.getElementsByTagName("inputs").item(0);
		textBoxes = new DPMEntryItemList();
		teamBoxes = new DPMEntryItemList();
		layoutPanel(mw, inputs);
		server = new ServerEV3(port, mw);
	}

	private void layoutPanel(MainWindow mw, Node inputs) {
		GridBagConstraints gridConstraints = new GridBagConstraints();
		setFont(new Font("Helvetica", Font.PLAIN, 14));
		setLayout(new GridBagLayout());

		Node entry, tempNode;
		NodeList entries = inputs.getChildNodes();
		int entriesLength = entries.getLength();
		String labelString, key, xKey, yKey;
		Label textLabel;
		DPMEntryItem dpmEntry;
		TextField textInput, xInput, yInput;

		for (int i = 0; i < entriesLength; i++) {
			gridConstraints.gridx = 0;
			gridConstraints.gridy = i;
			entry = entries.item(i);
			switch (entry.getNodeName()) {
			// TODO: avoid repeat code - MS
			case "#text":
				break;
			case "team":
				tempNode = entry.getFirstChild();
				tempNode = XMLReader.getNextNonTextNode(tempNode, true);
				labelString = tempNode.getFirstChild().getNodeValue();
				textLabel = new Label(labelString, Label.RIGHT);
				gridConstraints.gridwidth = 1;
				this.add(textLabel, gridConstraints);
				textInput = new TextField(11);
				gridConstraints.gridx = 1;
				gridConstraints.gridwidth = 2;
				this.add(textInput, gridConstraints);
				key = entry.getAttributes().getNamedItem("key").getNodeValue();
				dpmEntry = new DPMEntryItem(key, "Integer", textInput);
				textBoxes.add(dpmEntry);
				teamBoxes.add(dpmEntry);
				break;
			case "int":
				tempNode = entry.getFirstChild();
				tempNode = XMLReader.getNextNonTextNode(tempNode, true);
				labelString = tempNode.getFirstChild().getNodeValue();
				textLabel = new Label(labelString, Label.RIGHT);
				gridConstraints.gridwidth = 1;
				this.add(textLabel, gridConstraints);
				textInput = new TextField(11);
				gridConstraints.gridx = 1;
				gridConstraints.gridwidth = 2;
				this.add(textInput, gridConstraints);
				key = entry.getAttributes().getNamedItem("key").getNodeValue();
				dpmEntry = new DPMEntryItem(key, "Integer", textInput);
				textBoxes.add(dpmEntry);
				break;
			case "coordinate":
				tempNode = entry.getFirstChild();
				tempNode = XMLReader.getNextNonTextNode(tempNode, true);
				labelString = tempNode.getFirstChild().getNodeValue();
				textLabel = new Label(labelString, Label.RIGHT);
				gridConstraints.gridwidth = 1;
				this.add(textLabel, gridConstraints);
				xInput = new TextField(4);
				gridConstraints.gridx = 1;
				this.add(xInput, gridConstraints);
				xKey = entry.getAttributes().getNamedItem("keyx").getNodeValue();
				textBoxes.add(new DPMEntryItem(xKey, "Integer", xInput));
				yInput = new TextField(4);
				gridConstraints.gridx = 2;
				this.add(yInput, gridConstraints);
				yKey = entry.getAttributes().getNamedItem("keyy").getNodeValue();
				textBoxes.add(new DPMEntryItem(yKey, "Integer", yInput));
				break;
			case "string":
				tempNode = entry.getFirstChild();
				tempNode = XMLReader.getNextNonTextNode(tempNode, true);
				labelString = tempNode.getFirstChild().getNodeValue();
				textLabel = new Label(labelString, Label.RIGHT);
				gridConstraints.gridwidth = 1;
				this.add(textLabel, gridConstraints);
				textInput = new TextField(11);
				gridConstraints.gridx = 1;
				gridConstraints.gridwidth = 2;
				this.add(textInput, gridConstraints);
				key = entry.getAttributes().getNamedItem("key").getNodeValue();
				dpmEntry = new DPMEntryItem(key, "String", textInput);
				textBoxes.add(dpmEntry);
				break;
			default:
				break;
			}
		}

		// Start, reset and clear buttons
		gridConstraints.gridx = 0;
		gridConstraints.gridy = entriesLength;
		gridConstraints.gridwidth = 1;
		this.start = new Button("Start");
		this.start.addActionListener(this);
		new DPMToolTip("Transmit to connected robot(s)", this.start);
		this.add(start, gridConstraints);

		gridConstraints.gridx = 1;
		this.stop = new Button("Reset");
		this.stop.addActionListener(this);
		new DPMToolTip("Reset the counter", this.stop);
		this.add(stop, gridConstraints);

		gridConstraints.gridx = 2;
		this.clear = new Button("Clear");
		this.clear.addActionListener(this);
		new DPMToolTip("Clear all entered values", this.clear);
		this.add(clear, gridConstraints);

		gridConstraints.gridx = 1;
		gridConstraints.gridy = entriesLength + 1;
		gridConstraints.gridwidth = 1;
		this.fill = new Button("Fill");
		this.fill.addActionListener(this);
		new DPMToolTip("Load data from XML", this.fill);
		this.add(fill, gridConstraints);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent e) {
		Button bt = (Button) e.getSource();
		JSONObject startData = new JSONObject();

		ArrayList<Integer> targetTeams = new ArrayList<Integer>();
		targetTeams.ensureCapacity(teamBoxes.size());

		if (bt == start) {
			for (DPMEntryItem field : textBoxes) {

				String entryValue = field.entry.getText().trim();

				switch (field.type) {
				default:
				case "Integer":
					Integer data = 0;
					try {
						data = Integer.valueOf(entryValue);
					} catch (NumberFormatException excep) {
						System.err.println("Warning: field " + field.key + " has non-integer value; assuming 0");
					}
					startData.put(field.key, data);
					break;
				case "String":
					startData.put(field.key, entryValue);
				}
			}

			for (DPMEntryItem team : teamBoxes) {
				Integer teamNum;
				try {
					teamNum = Integer.valueOf(team.entry.getText().trim());
				} catch (NumberFormatException nan) {
					teamNum = 0;
				}
				// If invalid team number or team = 0, the team is skipped
				// As such, if you want to send to only one robot leave one team
				// set to 0
				if (teamNum != 0) {
					targetTeams.add(teamNum);
				}
			}

			if (!targetTeams.isEmpty()) {

				try {
					boolean status = server.transmit(targetTeams, startData);
					this.mw.clearTimer();
					if (status) {
						this.mw.displayOutput("All teams received data successfully.", true);
						this.mw.startTimer();
					} else {
						this.mw.displayOutput("Disabling timer as one or more teams failed to receive data", true);
					}
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}

		} else if (bt == clear) {
			clearFields();
			mw.clearTimer();
			mw.clearWifiPanel();
		} else if (bt == stop) {
			// stop button pressed
			mw.clearTimer();
		} else if (bt == fill) {
			fillFields();
		}

	}

	private void clearFields() {
		for (DPMEntryItem field : textBoxes) {
			field.entry.setText("");
		}
	}

	// TODO be improved when searching DPMEntryItemList is improved.
	private void fillFields() {
		fileSelect = new FileDialog(mw, "File to fill from");
		fileSelect.setVisible(true);
		String file, directory, path;
		file = fileSelect.getFile();
		directory = fileSelect.getDirectory();
		if ((file != null) && (directory != null)) {
			path = directory + file;
		} else {
			return;
		}
		System.out.println(path);
		Document content = XMLReader.getContentsDoc(path);
		if (content != null) {
			clearFields();
			HashMap<String, String> values = new HashMap<String, String>();
			Node value = content.getFirstChild().getFirstChild();
			String entry;
			value = XMLReader.getNextNonTextNode(value, false);
			while (value != null) {
				values.put(value.getAttributes().getNamedItem("key").getNodeValue(),
						value.getFirstChild().getNodeValue());
				value = value.getNextSibling();
				value = XMLReader.getNextNonTextNode(value, false);
			}
			for (DPMEntryItem field : textBoxes) {
				entry = values.get(field.key);
				if (entry != null) {
					field.entry.setText(entry);
				}
			}
		}
	}

}
