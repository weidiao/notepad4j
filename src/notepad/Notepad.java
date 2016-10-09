package notepad;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;

class TextTab extends JTextArea {
	static Font myFont = new Font("serif", Font.BOLD, 20);
	File file;
	UndoManager manager = new UndoManager();

	public TextTab() {
		setFont(myFont);
		getDocument().addUndoableEditListener(new UndoableEditListener() {
			@Override
			public void undoableEditHappened(UndoableEditEvent e) {
				manager.addEdit(e.getEdit());
			}
		});
	}

	public TextTab(File file) {
		this();
		this.file = file;
		setText(readFile(file));
	}

	String readFile(File file) {
		try {
			FileInputStream cin = new FileInputStream(file);
			byte[] contents = new byte[(int) file.length()];
			cin.read(contents);
			cin.close();
			return new String(contents);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	boolean isDirty() {
		return file == null ? true : !readFile(file).equals(getText());
	}

	void onFindTextChange(String s) {
		if (getSelectedText() == null)
			findNext(s);
		else if (getText().substring(getSelectionStart()).startsWith(s))
			select(getSelectionStart(), s.length());
		else
			findNext(s);
	}

	void findNext(String s) {
		int start = (getSelectedText() == null ? getCaretPosition() : getSelectionEnd());
		int pos = getText().substring(start).indexOf(s);
		if (pos == -1) {
			pos = getText().substring(0, start).indexOf(s);
			if (pos == -1)
				return;
		} else
			pos += start;
		select(pos, pos + s.length());
	}

	void findPrevious(String s) {
		int start = (getSelectedText() == null ? getCaretPosition() : getSelectionStart());
		int pos = getText().substring(0, start).lastIndexOf(s);
		if (pos == -1) {
			pos = getText().substring(start).lastIndexOf(s);
			if (pos == -1)
				return;
			else
				pos += start;
		}
		select(pos, pos + s.length());
	}

	void replace(String now) {
		if (getSelectedText() == null)
			return;
		String s = getText();
		int pos = getSelectionStart();
		setText(s.substring(0, getSelectionStart()) + now + s.substring(getSelectionEnd()));
		select(pos, pos + now.length());
	}

	void replaceAll(String old, String now) {
		if (old.isEmpty())
			return;
		setText(getText().replaceAll(old, now));
	}

	void undo() {
		if (manager.canUndo())
			manager.undo();
	}

	void redo() {
		if (manager.canRedo())
			manager.redo();
	}

	boolean save() {
		if (file == null) {
			return saveAs();
		} else {
			writeOut();
			return true;
		}
	}

	void writeOut() {
		try {
			FileWriter cout = new FileWriter(file);
			cout.write(getText());
			cout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	boolean saveAs() {
		JFileChooser chooser = new JFileChooser();
		chooser.showSaveDialog(this);
		File file = chooser.getSelectedFile();
		if (file == null)
			return false;
		else {
			this.file = file;
			writeOut();
			return true;
		}
	}

	void foreColor() {
		setForeground(JColorChooser.showDialog(this, "foreColor", new Color(0)));
	}

	void backColor() {
		setBackground(JColorChooser.showDialog(this, "backColor", new Color(0)));
	}

	void delete() {
		replace("");
	}
}

public class Notepad extends JFrame {
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		new Notepad();
	}

	JTabbedPane tabbedPane;
	String[] fileMenu = { "file", "new |control N", "open | control O", "close | control W",
			"close all | control shift W", "save | control S", "save as | control shift S", "=", "exit | control Q" };
	String[] editMenu = { "edit", "undo | control Z", "redo | control Y", "=", "cut | control X", "copy |control C",
			"paste | control V", "delete | Del", "=", "find | control F", "replace | control R" };
	String[] formatMenu = { "format", "font", "fore color", "back color", "[line wrap | control L" };

	TextTab getSelectedTextTab() {
		JScrollPane pane = (JScrollPane) tabbedPane.getSelectedComponent();
		if (pane == null)
			return null;
		return (TextTab) pane.getViewport().getComponent(0);
	}

	boolean isFileOpened(String path) {
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			JScrollPane pane = (JScrollPane) tabbedPane.getComponentAt(i);
			TextTab tab = (TextTab) pane.getViewport().getComponent(0);
			if (tab.file != null && tab.file.getPath().equals(path)) {
				tabbedPane.setSelectedComponent(pane);
				return true;
			}
		}
		return false;
	}

	void open() {
		JFileChooser chooser = new JFileChooser();
		chooser.showOpenDialog(this);
		File file = chooser.getSelectedFile();
		if (file == null || isFileOpened(file.getPath()))
			return;
		addTextTab(file.getName(), new TextTab(file));
	}

	void addTextTab(String name, TextTab tab) {
		Component pane = new JScrollPane(tab);
		tabbedPane.addTab(name == null ? "file" + tabbedPane.getComponentCount() : name, pane);
		tabbedPane.setSelectedComponent(pane);
	}

	boolean close() {
		JScrollPane pane = (JScrollPane) tabbedPane.getSelectedComponent();
		TextTab it = getSelectedTextTab();
		if (it == null)
			return true;
		if (it.isDirty()) {
			int res = JOptionPane.showConfirmDialog(this, "Do you want to save this file?", "save?",
					JOptionPane.YES_NO_CANCEL_OPTION);
			switch (res) {
			case JOptionPane.OK_OPTION:
				if (it.save()) {
					tabbedPane.remove(pane);
					return true;
				}
				return false;
			case JOptionPane.CANCEL_OPTION:
				return false;
			case JOptionPane.NO_OPTION:
				tabbedPane.remove(pane);
				return true;
			default:
				return false;
			}
		} else {
			tabbedPane.remove(pane);
			return true;
		}
	}

	void lineWrap(boolean b) {
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			JScrollPane pane = (JScrollPane) tabbedPane.getComponentAt(i);
			TextTab tab = (TextTab) pane.getViewport().getComponent(0);
			tab.setLineWrap(b);
		}
	}

	void font() {
		MyFontChooser fontChooser = new MyFontChooser(this);
		fontChooser.setVisible(true);
		if (fontChooser.getSelectedFont() == null)
			return;
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			JScrollPane pane = (JScrollPane) tabbedPane.getComponentAt(i);
			TextTab tab = (TextTab) pane.getViewport().getComponent(0);
			tab.setFont(fontChooser.getSelectedFont());
		}
		TextTab.myFont = fontChooser.getSelectedFont();
	}

	boolean closeAll() {
		while (getSelectedTextTab() != null)
			if (close() == false)
				return false;
		return true;
	}

	void exit() {
		if (closeAll())
			System.exit(0);
	}

	void find() {
		FindDialog dlg = new FindDialog(this);
		dlg.setVisible(true);
	}

	void replace() {
		ReplaceDialog dlg = new ReplaceDialog(this);
		dlg.setVisible(true);
	}

	void refreshEditMenu() {
		setEditMenuEnabled(getSelectedTextTab() != null);
	}

	void setEditMenuEnabled(boolean enabled) {
		JMenu editMenu = getJMenuBar().getMenu(1);
		for (int i = 0; i < editMenu.getItemCount(); i++) {
			if (editMenu.getItem(i) != null)// if item is sepetator
				editMenu.getItem(i).setEnabled(enabled);
		}
	}

	ActionListener act = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			JScrollPane pane = (JScrollPane) tabbedPane.getSelectedComponent();
			TextTab tab = null;
			if (pane != null) {
				tab = (TextTab) pane.getViewport().getComponent(0);
			}
			switch (e.getActionCommand()) {
			case "exit":
				exit();
				break;
			case "new":
				addTextTab(null, new TextTab());
				refreshEditMenu();
				break;
			case "close":
				close();
				refreshEditMenu();
				break;
			case "close all":
				closeAll();
				break;
			case "open":
				open();
				refreshEditMenu();
				break;
			case "save":
				tab.save();
				break;
			case "save as":
				tab.saveAs();
			case "undo":
				tab.undo();
				break;
			case "redo":
				tab.redo();
				break;
			case "cut":
				tab.cut();
				break;
			case "copy":
				tab.copy();
				break;
			case "paste":
				tab.paste();
				break;
			case "delete":
				tab.delete();
				break;
			case "find":
				find();
				break;
			case "replace":
				replace();
				break;
			case "font":
				font();
				break;
			case "fore color":
				tab.foreColor();
				break;
			case "back color":
				tab.backColor();
				break;
			case "line wrap":
				lineWrap(((JCheckBoxMenuItem) e.getSource()).isSelected());
				break;
			default:
				System.out.println(e.getActionCommand() + " not handled");
			}
		}
	};

	JMenu getMenu(String[] s) {
		JMenu menu = new JMenu(s[0]);
		for (int i = 1; i < s.length; i++) {
			if (s[i].equals("=")) {
				menu.addSeparator();
			} else {
				JMenuItem item = null;
				int pos = s[i].indexOf('|');
				String name = null, key = null;
				if (pos == -1)
					name = s[i];
				else {
					name = s[i].substring(0, pos - 1);
					key = s[i].substring(pos + 1);
				}
				if (name.startsWith("["))
					item = new JCheckBoxMenuItem(name.substring(1));
				else
					item = new JMenuItem(name);
				item.setAccelerator(KeyStroke.getKeyStroke(key));
				item.addActionListener(act);
				menu.add(item);
			}
		}
		return menu;

	}

	Notepad() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});

		setLayout(new BorderLayout());
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(getMenu(fileMenu));
		menuBar.add(getMenu(editMenu));
		menuBar.add(getMenu(formatMenu));
		setJMenuBar(menuBar);
		tabbedPane = new JTabbedPane();
		tabbedPane.setFont(TextTab.myFont);
		add(tabbedPane, BorderLayout.CENTER);
		refreshEditMenu();
		setSize(600, 600);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}
}
