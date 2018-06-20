package notepad;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

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
