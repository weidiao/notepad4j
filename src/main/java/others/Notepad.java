package others;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

public class Notepad extends JFrame implements ActionListener, DocumentListener {
	JMenu fileMenu, editMenu, formatMenu, viewMenu, helpMenu;
	JPopupMenu popupMenu;
	JMenuItem popupMenu_Undo, popupMenu_Cut, popupMenu_Copy, popupMenu_Paste, popupMenu_Delete, popupMenu_SelectAll;
	JMenuItem fileMenu_New, fileMenu_Open, fileMenu_Save, fileMenu_SaveAs, fileMenu_PageSetUp, fileMenu_Print,
			fileMenu_Exit;
	JMenuItem editMenu_Undo, editMenu_Cut, editMenu_Copy, editMenu_Paste, editMenu_Delete, editMenu_Find,
			editMenu_FindNext, editMenu_Replace, editMenu_GoTo, editMenu_SelectAll, editMenu_TimeDate;
	JCheckBoxMenuItem formatMenu_LineWrap;
	JMenuItem formatMenu_Font;
	JCheckBoxMenuItem viewMenu_Status;
	JMenuItem helpMenu_HelpTopics, helpMenu_AboutNotepad;
	JTextArea editArea;
	JLabel statusLabel;
	Toolkit toolkit = Toolkit.getDefaultToolkit();
	Clipboard clipBoard = toolkit.getSystemClipboard();
	protected UndoManager undo = new UndoManager();
	protected UndoableEditListener undoHandler = new UndoHandler();
	String oldValue;
	boolean isNewFile = true;
	File currentFile;

	public Notepad() {
		super("Java记事本");
		Font font = new Font("Dialog", Font.PLAIN, 12);
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource) {
				UIManager.put(key, font);
			}
		}
		JMenuBar menuBar = new JMenuBar();
		fileMenu = new JMenu("File(F)");
		fileMenu.setMnemonic('F');

		fileMenu_New = new JMenuItem("new(N)");
		fileMenu_New.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		fileMenu_New.addActionListener(this);

		fileMenu_Open = new JMenuItem("open(O)...");
		fileMenu_Open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		fileMenu_Open.addActionListener(this);

		fileMenu_Save = new JMenuItem("save(S)");
		fileMenu_Save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		fileMenu_Save.addActionListener(this);

		fileMenu_SaveAs = new JMenuItem("save as(A)...");
		fileMenu_SaveAs.addActionListener(this);

		fileMenu_PageSetUp = new JMenuItem("page setup(U)...");
		fileMenu_PageSetUp.addActionListener(this);

		fileMenu_Print = new JMenuItem("print(P)...");
		fileMenu_Print.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK));
		fileMenu_Print.addActionListener(this);

		fileMenu_Exit = new JMenuItem("exit(X)");
		fileMenu_Exit.addActionListener(this);

		editMenu = new JMenu("edit(E)");
		editMenu.setMnemonic('E');
		editMenu.addMenuListener(new MenuListener() {
			public void menuCanceled(MenuEvent e) {
				checkMenuItemEnabled();
			}

			public void menuDeselected(MenuEvent e) {
				checkMenuItemEnabled();
			}

			public void menuSelected(MenuEvent e) {
				checkMenuItemEnabled();
			}
		});

		editMenu_Undo = new JMenuItem("undo(U)");
		editMenu_Undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
		editMenu_Undo.addActionListener(this);
		editMenu_Undo.setEnabled(false);

		editMenu_Cut = new JMenuItem("cut(T)");
		editMenu_Cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
		editMenu_Cut.addActionListener(this);

		editMenu_Copy = new JMenuItem("copy(C)");
		editMenu_Copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
		editMenu_Copy.addActionListener(this);

		editMenu_Paste = new JMenuItem("paste(P)");
		editMenu_Paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
		editMenu_Paste.addActionListener(this);

		editMenu_Delete = new JMenuItem("delete(D)");
		editMenu_Delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		editMenu_Delete.addActionListener(this);

		editMenu_Find = new JMenuItem("find(F)...");
		editMenu_Find.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
		editMenu_Find.addActionListener(this);

		editMenu_FindNext = new JMenuItem("find next(N)");
		editMenu_FindNext.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		editMenu_FindNext.addActionListener(this);

		editMenu_Replace = new JMenuItem("replace(R)", 'R');
		editMenu_Replace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_MASK));
		editMenu_Replace.addActionListener(this);

		editMenu_GoTo = new JMenuItem("goto(G)", 'G');
		editMenu_GoTo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK));
		editMenu_GoTo.addActionListener(this);

		editMenu_SelectAll = new JMenuItem("select all", 'A');
		editMenu_SelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
		editMenu_SelectAll.addActionListener(this);

		editMenu_TimeDate = new JMenuItem("time date(D)", 'D');
		editMenu_TimeDate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		editMenu_TimeDate.addActionListener(this);

		formatMenu = new JMenu("format(O)");
		formatMenu.setMnemonic('O');

		formatMenu_LineWrap = new JCheckBoxMenuItem("linewrap(W)");
		formatMenu_LineWrap.setMnemonic('W');
		formatMenu_LineWrap.setState(true);
		formatMenu_LineWrap.addActionListener(this);

		formatMenu_Font = new JMenuItem("format(F)...");
		formatMenu_Font.addActionListener(this);

		viewMenu = new JMenu("view(V)");
		viewMenu.setMnemonic('V');

		viewMenu_Status = new JCheckBoxMenuItem("status(S)");
		viewMenu_Status.setMnemonic('S');
		viewMenu_Status.setState(true);
		viewMenu_Status.addActionListener(this);

		helpMenu = new JMenu("help(H)");
		helpMenu.setMnemonic('H');

		helpMenu_HelpTopics = new JMenuItem("help topics(H)");
		helpMenu_HelpTopics.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		helpMenu_HelpTopics.addActionListener(this);

		helpMenu_AboutNotepad = new JMenuItem("about(A)");
		helpMenu_AboutNotepad.addActionListener(this);

		menuBar.add(fileMenu);
		fileMenu.add(fileMenu_New);
		fileMenu.add(fileMenu_Open);
		fileMenu.add(fileMenu_Save);
		fileMenu.add(fileMenu_SaveAs);
		fileMenu.addSeparator();
		fileMenu.add(fileMenu_PageSetUp);
		fileMenu.add(fileMenu_Print);
		fileMenu.addSeparator();
		fileMenu.add(fileMenu_Exit);

		menuBar.add(editMenu);
		editMenu.add(editMenu_Undo);
		editMenu.addSeparator();
		editMenu.add(editMenu_Cut);
		editMenu.add(editMenu_Copy);
		editMenu.add(editMenu_Paste);
		editMenu.add(editMenu_Delete);
		editMenu.addSeparator();
		editMenu.add(editMenu_Find);
		editMenu.add(editMenu_FindNext);
		editMenu.add(editMenu_Replace);
		editMenu.add(editMenu_GoTo);
		editMenu.addSeparator();
		editMenu.add(editMenu_SelectAll);
		editMenu.add(editMenu_TimeDate);

		menuBar.add(formatMenu);
		formatMenu.add(formatMenu_LineWrap);
		formatMenu.add(formatMenu_Font);

		menuBar.add(viewMenu);
		viewMenu.add(viewMenu_Status);

		menuBar.add(helpMenu);
		helpMenu.add(helpMenu_HelpTopics);
		helpMenu.addSeparator();
		helpMenu.add(helpMenu_AboutNotepad);

		this.setJMenuBar(menuBar);

		editArea = new JTextArea(20, 50);
		JScrollPane scroller = new JScrollPane(editArea);
		scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.add(scroller, BorderLayout.CENTER);
		editArea.setWrapStyleWord(true);
		editArea.setLineWrap(true);
		oldValue = editArea.getText();

		editArea.getDocument().addUndoableEditListener(undoHandler);
		editArea.getDocument().addDocumentListener(this);

		popupMenu = new JPopupMenu();
		popupMenu_Undo = new JMenuItem("undo(U)");
		popupMenu_Cut = new JMenuItem("cut(T)");
		popupMenu_Copy = new JMenuItem("copy(C)");
		popupMenu_Paste = new JMenuItem("paste(P)");
		popupMenu_Delete = new JMenuItem("delete(D)");
		popupMenu_SelectAll = new JMenuItem("select all(A)");

		popupMenu_Undo.setEnabled(false);

		popupMenu.add(popupMenu_Undo);
		popupMenu.addSeparator();
		popupMenu.add(popupMenu_Cut);
		popupMenu.add(popupMenu_Copy);
		popupMenu.add(popupMenu_Paste);
		popupMenu.add(popupMenu_Delete);
		popupMenu.addSeparator();
		popupMenu.add(popupMenu_SelectAll);

		popupMenu_Undo.addActionListener(this);
		popupMenu_Cut.addActionListener(this);
		popupMenu_Copy.addActionListener(this);
		popupMenu_Paste.addActionListener(this);
		popupMenu_Delete.addActionListener(this);
		popupMenu_SelectAll.addActionListener(this);

		editArea.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
				checkMenuItemEnabled();
				editArea.requestFocus();
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
				checkMenuItemEnabled();
				editArea.requestFocus();
			}
		});

		statusLabel = new JLabel("status");
		this.add(statusLabel, BorderLayout.SOUTH);
		this.setLocation(100, 100);
		this.setSize(650, 550);
		this.setVisible(true);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exitWindowChoose();
			}
		});

		checkMenuItemEnabled();
		editArea.requestFocus();
	}

	public void checkMenuItemEnabled() {
		String selectText = editArea.getSelectedText();
		if (selectText == null) {
			editMenu_Cut.setEnabled(false);
			popupMenu_Cut.setEnabled(false);
			editMenu_Copy.setEnabled(false);
			popupMenu_Copy.setEnabled(false);
			editMenu_Delete.setEnabled(false);
			popupMenu_Delete.setEnabled(false);
		} else {
			editMenu_Cut.setEnabled(true);
			popupMenu_Cut.setEnabled(true);
			editMenu_Copy.setEnabled(true);
			popupMenu_Copy.setEnabled(true);
			editMenu_Delete.setEnabled(true);
			popupMenu_Delete.setEnabled(true);
		}
		Transferable contents = clipBoard.getContents(this);
		if (contents == null) {
			editMenu_Paste.setEnabled(false);
			popupMenu_Paste.setEnabled(false);
		} else {
			editMenu_Paste.setEnabled(true);
			popupMenu_Paste.setEnabled(true);
		}
	}

	public void exitWindowChoose() {
		editArea.requestFocus();
		String currentValue = editArea.getText();
		if (currentValue.equals(oldValue) == true) {
			System.exit(0);
		} else {
			int exitChoose = JOptionPane.showConfirmDialog(this, "是否保存？", "哈哈", JOptionPane.YES_NO_CANCEL_OPTION);
			if (exitChoose == JOptionPane.YES_OPTION) { // boolean isSave=false;
				if (isNewFile) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					fileChooser.setApproveButtonText("确定");
					fileChooser.setDialogTitle("保存文件");

					int result = fileChooser.showSaveDialog(this);

					if (result == JFileChooser.CANCEL_OPTION) {
						statusLabel.setText("取消保存");
						return;
					}

					File saveFileName = fileChooser.getSelectedFile();

					if (saveFileName == null || saveFileName.getName().equals("")) {
						JOptionPane.showMessageDialog(this, "锟斤拷锟较凤拷锟斤拷锟侥硷拷锟斤拷", "锟斤拷锟较凤拷锟斤拷锟侥硷拷锟斤拷",
								JOptionPane.ERROR_MESSAGE);
					} else {
						try {
							FileWriter fw = new FileWriter(saveFileName);
							BufferedWriter bfw = new BufferedWriter(fw);
							bfw.write(editArea.getText(), 0, editArea.getText().length());
							bfw.flush();
							fw.close();

							isNewFile = false;
							currentFile = saveFileName;
							oldValue = editArea.getText();

							this.setTitle(saveFileName.getName() + "  - 锟斤拷锟铰憋拷");
							statusLabel.setText("锟斤拷锟斤拷前锟斤拷锟侥硷拷:" + saveFileName.getAbsoluteFile());
							// isSave=true;
						} catch (IOException ioException) {
						}
					}
				} else {
					try {
						FileWriter fw = new FileWriter(currentFile);
						BufferedWriter bfw = new BufferedWriter(fw);
						bfw.write(editArea.getText(), 0, editArea.getText().length());
						bfw.flush();
						fw.close();
						// isSave=true;
					} catch (IOException ioException) {
					}
				}
				System.exit(0);
				// if(isSave)System.exit(0);
				// else return;
			} else if (exitChoose == JOptionPane.NO_OPTION) {
				System.exit(0);
			} else {
				return;
			}
		}
	}

	public void find() {
		final JDialog findDialog = new JDialog(this, "锟斤拷锟斤拷", false);
		Container con = findDialog.getContentPane();// 锟斤拷锟截此对伙拷锟斤拷锟絚ontentPane锟斤拷锟斤拷
		con.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel findContentLabel = new JLabel("锟斤拷锟斤拷锟斤拷锟斤拷(N)锟斤拷");
		final JTextField findText = new JTextField(15);
		JButton findNextButton = new JButton("锟斤拷锟斤拷锟斤拷一锟斤拷(F)锟斤拷");
		final JCheckBox matchCheckBox = new JCheckBox("锟斤拷锟街达拷小写(C)");
		ButtonGroup bGroup = new ButtonGroup();
		final JRadioButton upButton = new JRadioButton("锟斤拷锟斤拷(U)");
		final JRadioButton downButton = new JRadioButton("锟斤拷锟斤拷(U)");
		downButton.setSelected(true);
		bGroup.add(upButton);
		bGroup.add(downButton);

		JButton cancel = new JButton("取消");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				findDialog.dispose();
			}
		});
		// "锟斤拷锟斤拷锟斤拷一锟斤拷"锟斤拷钮锟斤拷锟斤拷
		findNextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { // "锟斤拷锟街达拷小写(C)"锟斤拷JCheckBox锟角凤拷选锟斤拷
				int k = 0;
				final String str1, str2, str3, str4, strA, strB;
				str1 = editArea.getText();
				str2 = findText.getText();
				str3 = str1.toUpperCase();
				str4 = str2.toUpperCase();
				if (matchCheckBox.isSelected())// 锟斤拷锟街达拷小写
				{
					strA = str1;
					strB = str2;
				} else// 锟斤拷锟斤拷锟街达拷小写,锟斤拷时锟斤拷锟斤拷选锟斤拷锟斤拷全锟斤拷锟斤拷锟缴达拷写(锟斤拷小写)锟斤拷锟皆憋拷锟节诧拷锟斤拷
				{
					strA = str3;
					strB = str4;
				}
				if (upButton.isSelected()) { // k=strA.lastIndexOf(strB,editArea.getCaretPosition()-1);
					if (editArea.getSelectedText() == null)
						k = strA.lastIndexOf(strB, editArea.getCaretPosition() - 1);
					else
						k = strA.lastIndexOf(strB, editArea.getCaretPosition() - findText.getText().length() - 1);
					if (k > -1) { // String
									// strData=strA.subString(k,strB.getText().length()+1);
						editArea.setCaretPosition(k);
						editArea.select(k, k + strB.length());
					} else {
						JOptionPane.showMessageDialog(null, "锟揭诧拷锟斤拷锟斤拷锟斤拷锟揭碉拷锟斤拷锟捷ｏ拷", "锟斤拷锟斤拷",
								JOptionPane.INFORMATION_MESSAGE);
					}
				} else if (downButton.isSelected()) {
					if (editArea.getSelectedText() == null)
						k = strA.indexOf(strB, editArea.getCaretPosition() + 1);
					else
						k = strA.indexOf(strB, editArea.getCaretPosition() - findText.getText().length() + 1);
					if (k > -1) { // String
									// strData=strA.subString(k,strB.getText().length()+1);
						editArea.setCaretPosition(k);
						editArea.select(k, k + strB.length());
					} else {
						JOptionPane.showMessageDialog(null, "锟揭诧拷锟斤拷锟斤拷锟斤拷锟揭碉拷锟斤拷锟捷ｏ拷", "锟斤拷锟斤拷",
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
		});// "锟斤拷锟斤拷锟斤拷一锟斤拷"锟斤拷钮锟斤拷锟斤拷锟斤拷锟斤拷
			// 锟斤拷锟斤拷"锟斤拷锟斤拷"锟皆伙拷锟斤拷慕锟斤拷锟�
		JPanel panel1 = new JPanel();
		JPanel panel2 = new JPanel();
		JPanel panel3 = new JPanel();
		JPanel directionPanel = new JPanel();
		directionPanel.setBorder(BorderFactory.createTitledBorder("锟斤拷锟斤拷"));
		// 锟斤拷锟斤拷directionPanel锟斤拷锟斤拷谋呖锟�;
		// BorderFactory.createTitledBorder(String
		// title)锟斤拷锟斤拷一锟斤拷锟铰憋拷锟斤拷呖锟绞癸拷锟侥拷媳呖颍ǜ锟斤拷窕锟斤拷锟侥拷锟斤拷谋锟轿伙拷茫锟轿伙拷诙锟斤拷锟斤拷希锟斤拷锟侥拷系锟斤拷锟�
		// (leading)
		// 锟皆硷拷锟缴碉拷前锟斤拷锟饺凤拷锟斤拷锟侥拷锟斤拷锟斤拷锟斤拷锟侥憋拷锟斤拷色锟斤拷锟斤拷指锟斤拷锟剿憋拷锟斤拷锟侥憋拷锟斤拷
		directionPanel.add(upButton);
		directionPanel.add(downButton);
		panel1.setLayout(new GridLayout(2, 1));
		panel1.add(findNextButton);
		panel1.add(cancel);
		panel2.add(findContentLabel);
		panel2.add(findText);
		panel2.add(panel1);
		panel3.add(matchCheckBox);
		panel3.add(directionPanel);
		con.add(panel2);
		con.add(panel3);
		findDialog.setSize(410, 180);
		findDialog.setResizable(false);// 锟斤拷锟缴碉拷锟斤拷锟斤拷小
		findDialog.setLocation(230, 280);
		findDialog.setVisible(true);
	}// 锟斤拷锟揭凤拷锟斤拷锟斤拷锟斤拷

	// 锟芥换锟斤拷锟斤拷
	public void replace() {
		final JDialog replaceDialog = new JDialog(this, "锟芥换", false);// false时锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷同时锟斤拷锟节硷拷锟斤拷状态(锟斤拷锟斤拷模式)
		Container con = replaceDialog.getContentPane();// 锟斤拷锟截此对伙拷锟斤拷锟絚ontentPane锟斤拷锟斤拷
		con.setLayout(new FlowLayout(FlowLayout.CENTER));
		JLabel findContentLabel = new JLabel("锟斤拷锟斤拷锟斤拷锟斤拷(N)锟斤拷");
		final JTextField findText = new JTextField(15);
		JButton findNextButton = new JButton("锟斤拷锟斤拷锟斤拷一锟斤拷(F):");
		JLabel replaceLabel = new JLabel("锟芥换为(P)锟斤拷");
		final JTextField replaceText = new JTextField(15);
		JButton replaceButton = new JButton("锟芥换(R)");
		JButton replaceAllButton = new JButton("全锟斤拷锟芥换(A)");
		JButton cancel = new JButton("取锟斤拷");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				replaceDialog.dispose();
			}
		});
		final JCheckBox matchCheckBox = new JCheckBox("锟斤拷锟街达拷小写(C)");
		ButtonGroup bGroup = new ButtonGroup();
		final JRadioButton upButton = new JRadioButton("锟斤拷锟斤拷(U)");
		final JRadioButton downButton = new JRadioButton("锟斤拷锟斤拷(U)");
		downButton.setSelected(true);
		bGroup.add(upButton);
		bGroup.add(downButton);
		/*
		 * ButtonGroup锟斤拷锟斤拷锟斤拷锟斤拷为一锟介按钮锟斤拷锟斤拷一锟斤拷锟斤拷猓╩ultiple-
		 * exclusion锟斤拷锟斤拷锟斤拷锟斤拷 使锟斤拷锟斤拷同锟斤拷 ButtonGroup 锟斤拷锟襟创斤拷一锟介按钮锟斤拷味锟脚★
		 * 拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷一锟斤拷锟斤拷钮时锟斤拷锟斤拷锟截憋拷锟斤拷锟叫碉拷锟斤拷锟斤拷锟斤拷锟叫帮拷钮锟斤拷
		 */
		/*
		 * JRadioButton锟斤拷锟斤拷实锟斤拷一锟斤拷锟斤拷选锟斤拷钮锟斤拷锟剿帮拷钮锟斤拷杀锟窖★拷锟斤拷取锟斤拷选锟今，
		 * 诧拷锟斤拷为锟矫伙拷锟斤拷示锟斤拷状态锟斤拷 锟斤拷 ButtonGroup
		 * 锟斤拷锟斤拷锟斤拷锟绞癸拷每纱锟斤拷锟揭伙拷榘磁ワ拷锟揭伙拷锟街伙拷锟窖★拷锟斤拷锟斤拷械锟揭伙拷锟斤拷锟脚ワ拷锟�
		 * 锟斤拷锟斤拷锟斤拷一锟斤拷 ButtonGroup 锟斤拷锟斤拷锟斤拷锟斤拷 add 锟斤拷锟斤拷锟斤拷 JRadioButton
		 * 锟斤拷锟斤拷锟斤拷锟斤拷诖锟斤拷锟斤拷小锟斤拷锟�
		 */

		// "锟斤拷锟斤拷锟斤拷一锟斤拷"锟斤拷钮锟斤拷锟斤拷
		findNextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { // "锟斤拷锟街达拷小写(C)"锟斤拷JCheckBox锟角凤拷选锟斤拷
				int k = 0;
				final String str1, str2, str3, str4, strA, strB;
				str1 = editArea.getText();
				str2 = findText.getText();
				str3 = str1.toUpperCase();
				str4 = str2.toUpperCase();
				if (matchCheckBox.isSelected())// 锟斤拷锟街达拷小写
				{
					strA = str1;
					strB = str2;
				} else// 锟斤拷锟斤拷锟街达拷小写,锟斤拷时锟斤拷锟斤拷选锟斤拷锟斤拷全锟斤拷锟斤拷锟缴达拷写(锟斤拷小写)锟斤拷锟皆憋拷锟节诧拷锟斤拷
				{
					strA = str3;
					strB = str4;
				}
				if (upButton.isSelected()) { // k=strA.lastIndexOf(strB,editArea.getCaretPosition()-1);
					if (editArea.getSelectedText() == null)
						k = strA.lastIndexOf(strB, editArea.getCaretPosition() - 1);
					else
						k = strA.lastIndexOf(strB, editArea.getCaretPosition() - findText.getText().length() - 1);
					if (k > -1) { // String
									// strData=strA.subString(k,strB.getText().length()+1);
						editArea.setCaretPosition(k);
						editArea.select(k, k + strB.length());
					} else {
						JOptionPane.showMessageDialog(null, "锟揭诧拷锟斤拷锟斤拷锟斤拷锟揭碉拷锟斤拷锟捷ｏ拷", "锟斤拷锟斤拷",
								JOptionPane.INFORMATION_MESSAGE);
					}
				} else if (downButton.isSelected()) {
					if (editArea.getSelectedText() == null)
						k = strA.indexOf(strB, editArea.getCaretPosition() + 1);
					else
						k = strA.indexOf(strB, editArea.getCaretPosition() - findText.getText().length() + 1);
					if (k > -1) { // String
									// strData=strA.subString(k,strB.getText().length()+1);
						editArea.setCaretPosition(k);
						editArea.select(k, k + strB.length());
					} else {
						JOptionPane.showMessageDialog(null, "锟揭诧拷锟斤拷锟斤拷锟斤拷锟揭碉拷锟斤拷锟捷ｏ拷", "锟斤拷锟斤拷",
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
		});// "锟斤拷锟斤拷锟斤拷一锟斤拷"锟斤拷钮锟斤拷锟斤拷锟斤拷锟斤拷

		// "锟芥换"锟斤拷钮锟斤拷锟斤拷
		replaceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (replaceText.getText().length() == 0 && editArea.getSelectedText() != null)
					editArea.replaceSelection("");
				if (replaceText.getText().length() > 0 && editArea.getSelectedText() != null)
					editArea.replaceSelection(replaceText.getText());
			}
		});// "锟芥换"锟斤拷钮锟斤拷锟斤拷锟斤拷锟斤拷

		// "全锟斤拷锟芥换"锟斤拷钮锟斤拷锟斤拷
		replaceAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editArea.setCaretPosition(0); // 锟斤拷锟斤拷锟脚碉拷锟洁辑锟斤拷锟斤拷头
				int k = 0, replaceCount = 0;
				if (findText.getText().length() == 0) {
					JOptionPane.showMessageDialog(replaceDialog, "锟斤拷锟斤拷写锟斤拷锟斤拷锟斤拷锟斤拷!", "锟斤拷示",
							JOptionPane.WARNING_MESSAGE);
					findText.requestFocus(true);
					return;
				}
				while (k > -1)// 锟斤拷锟侥憋拷锟斤拷锟斤拷锟斤拷锟捷憋拷选锟斤拷时(k>-1锟斤拷选锟斤拷)锟斤拷锟斤拷锟芥换锟斤拷锟斤拷锟津不斤拷锟斤拷while循锟斤拷
				{ // "锟斤拷锟街达拷小写(C)"锟斤拷JCheckBox锟角凤拷选锟斤拷
					// int k=0,m=0;
					final String str1, str2, str3, str4, strA, strB;
					str1 = editArea.getText();
					str2 = findText.getText();
					str3 = str1.toUpperCase();
					str4 = str2.toUpperCase();
					if (matchCheckBox.isSelected())// 锟斤拷锟街达拷小写
					{
						strA = str1;
						strB = str2;
					} else// 锟斤拷锟斤拷锟街达拷小写,锟斤拷时锟斤拷锟斤拷选锟斤拷锟斤拷全锟斤拷锟斤拷锟缴达拷写(锟斤拷小写)锟斤拷锟皆憋拷锟节诧拷锟斤拷
					{
						strA = str3;
						strB = str4;
					}
					if (upButton.isSelected()) { // k=strA.lastIndexOf(strB,editArea.getCaretPosition()-1);
						if (editArea.getSelectedText() == null)
							k = strA.lastIndexOf(strB, editArea.getCaretPosition() - 1);
						else
							k = strA.lastIndexOf(strB, editArea.getCaretPosition() - findText.getText().length() - 1);
						if (k > -1) { // String
										// strData=strA.subString(k,strB.getText().length()+1);
							editArea.setCaretPosition(k);
							editArea.select(k, k + strB.length());
						} else {
							if (replaceCount == 0) {
								JOptionPane.showMessageDialog(replaceDialog, "锟揭诧拷锟斤拷锟斤拷锟斤拷锟揭碉拷锟斤拷锟斤拷!", "锟斤拷锟铰憋拷",
										JOptionPane.INFORMATION_MESSAGE);
							} else {
								JOptionPane.showMessageDialog(replaceDialog,
										"锟缴癸拷锟芥换" + replaceCount + "锟斤拷匹锟斤拷锟斤拷锟斤拷!", "锟芥换锟缴癸拷",
										JOptionPane.INFORMATION_MESSAGE);
							}
						}
					} else if (downButton.isSelected()) {
						if (editArea.getSelectedText() == null)
							k = strA.indexOf(strB, editArea.getCaretPosition() + 1);
						else
							k = strA.indexOf(strB, editArea.getCaretPosition() - findText.getText().length() + 1);
						if (k > -1) { // String
										// strData=strA.subString(k,strB.getText().length()+1);
							editArea.setCaretPosition(k);
							editArea.select(k, k + strB.length());
						} else {
							if (replaceCount == 0) {
								JOptionPane.showMessageDialog(replaceDialog, "锟揭诧拷锟斤拷锟斤拷锟斤拷锟揭碉拷锟斤拷锟斤拷!", "锟斤拷锟铰憋拷",
										JOptionPane.INFORMATION_MESSAGE);
							} else {
								JOptionPane.showMessageDialog(replaceDialog,
										"锟缴癸拷锟芥换" + replaceCount + "锟斤拷匹锟斤拷锟斤拷锟斤拷!", "锟芥换锟缴癸拷",
										JOptionPane.INFORMATION_MESSAGE);
							}
						}
					}
					if (replaceText.getText().length() == 0 && editArea.getSelectedText() != null) {
						editArea.replaceSelection("");
						replaceCount++;
					}

					if (replaceText.getText().length() > 0 && editArea.getSelectedText() != null) {
						editArea.replaceSelection(replaceText.getText());
						replaceCount++;
					}
				} // while循锟斤拷锟斤拷锟斤拷
			}
		});// "锟芥换全锟斤拷"锟斤拷锟斤拷锟斤拷锟斤拷

		// 锟斤拷锟斤拷"锟芥换"锟皆伙拷锟斤拷慕锟斤拷锟�
		JPanel directionPanel = new JPanel();
		directionPanel.setBorder(BorderFactory.createTitledBorder("锟斤拷锟斤拷"));
		// 锟斤拷锟斤拷directionPanel锟斤拷锟斤拷谋呖锟�;
		// BorderFactory.createTitledBorder(String
		// title)锟斤拷锟斤拷一锟斤拷锟铰憋拷锟斤拷呖锟绞癸拷锟侥拷媳呖颍ǜ锟斤拷窕锟斤拷锟侥拷锟斤拷谋锟轿伙拷茫锟轿伙拷诙锟斤拷锟斤拷希锟斤拷锟侥拷系锟斤拷锟�
		// (leading)
		// 锟皆硷拷锟缴碉拷前锟斤拷锟饺凤拷锟斤拷锟侥拷锟斤拷锟斤拷锟斤拷锟侥憋拷锟斤拷色锟斤拷锟斤拷指锟斤拷锟剿憋拷锟斤拷锟侥憋拷锟斤拷
		directionPanel.add(upButton);
		directionPanel.add(downButton);
		JPanel panel1 = new JPanel();
		JPanel panel2 = new JPanel();
		JPanel panel3 = new JPanel();
		JPanel panel4 = new JPanel();
		panel4.setLayout(new GridLayout(2, 1));
		panel1.add(findContentLabel);
		panel1.add(findText);
		panel1.add(findNextButton);
		panel4.add(replaceButton);
		panel4.add(replaceAllButton);
		panel2.add(replaceLabel);
		panel2.add(replaceText);
		panel2.add(panel4);
		panel3.add(matchCheckBox);
		panel3.add(directionPanel);
		panel3.add(cancel);
		con.add(panel1);
		con.add(panel2);
		con.add(panel3);
		replaceDialog.setSize(420, 220);
		replaceDialog.setResizable(false);// 锟斤拷锟缴碉拷锟斤拷锟斤拷小
		replaceDialog.setLocation(230, 280);
		replaceDialog.setVisible(true);
	}// "全锟斤拷锟芥换"锟斤拷钮锟斤拷锟斤拷锟斤拷锟斤拷

	// "锟斤拷锟斤拷"锟斤拷锟斤拷
	public void font() {
		final JDialog fontDialog = new JDialog(this, "锟斤拷锟斤拷锟斤拷锟斤拷", false);
		Container con = fontDialog.getContentPane();
		con.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel fontLabel = new JLabel("锟斤拷锟斤拷(F)锟斤拷");
		fontLabel.setPreferredSize(new Dimension(100, 20));// 锟斤拷锟斤拷一锟斤拷Dimension锟斤拷锟斤拷锟斤拷锟斤拷锟绞硷拷锟轿革拷锟斤拷锟饺和高讹拷
		JLabel styleLabel = new JLabel("锟斤拷锟斤拷(Y)锟斤拷");
		styleLabel.setPreferredSize(new Dimension(100, 20));
		JLabel sizeLabel = new JLabel("锟斤拷小(S)锟斤拷");
		sizeLabel.setPreferredSize(new Dimension(100, 20));
		final JLabel sample = new JLabel("锟斤拷选锟劫的硷拷锟铰憋拷-ZXZ's Notepad");
		// sample.setHorizontalAlignment(SwingConstants.CENTER);
		final JTextField fontText = new JTextField(9);
		fontText.setPreferredSize(new Dimension(200, 20));
		final JTextField styleText = new JTextField(8);
		styleText.setPreferredSize(new Dimension(200, 20));
		final int style[] = { Font.PLAIN, Font.BOLD, Font.ITALIC, Font.BOLD + Font.ITALIC };
		final JTextField sizeText = new JTextField(5);
		sizeText.setPreferredSize(new Dimension(200, 20));
		JButton okButton = new JButton("确锟斤拷");
		JButton cancel = new JButton("取锟斤拷");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fontDialog.dispose();
			}
		});
		Font currentFont = editArea.getFont();
		fontText.setText(currentFont.getFontName());
		fontText.selectAll();
		// styleText.setText(currentFont.getStyle());
		// styleText.selectAll();
		if (currentFont.getStyle() == Font.PLAIN)
			styleText.setText("锟斤拷锟斤拷");
		else if (currentFont.getStyle() == Font.BOLD)
			styleText.setText("锟斤拷锟斤拷");
		else if (currentFont.getStyle() == Font.ITALIC)
			styleText.setText("斜锟斤拷");
		else if (currentFont.getStyle() == (Font.BOLD + Font.ITALIC))
			styleText.setText("锟斤拷斜锟斤拷");
		styleText.selectAll();
		String str = String.valueOf(currentFont.getSize());
		sizeText.setText(str);
		sizeText.selectAll();
		final JList<String> fontList, styleList, sizeList;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final String fontName[] = ge.getAvailableFontFamilyNames();
		fontList = new JList<String>(fontName);
		fontList.setFixedCellWidth(86);
		fontList.setFixedCellHeight(20);
		fontList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		final String fontStyle[] = { "锟斤拷锟斤拷", "锟斤拷锟斤拷", "斜锟斤拷", "锟斤拷斜锟斤拷" };
		styleList = new JList<String>(fontStyle);
		styleList.setFixedCellWidth(86);
		styleList.setFixedCellHeight(20);
		styleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		if (currentFont.getStyle() == Font.PLAIN)
			styleList.setSelectedIndex(0);
		else if (currentFont.getStyle() == Font.BOLD)
			styleList.setSelectedIndex(1);
		else if (currentFont.getStyle() == Font.ITALIC)
			styleList.setSelectedIndex(2);
		else if (currentFont.getStyle() == (Font.BOLD + Font.ITALIC))
			styleList.setSelectedIndex(3);
		final String fontSize[] = { "8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36",
				"48", "72" };
		sizeList = new JList<String>(fontSize);
		sizeList.setFixedCellWidth(43);
		sizeList.setFixedCellHeight(20);
		sizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fontList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				fontText.setText(fontName[fontList.getSelectedIndex()]);
				fontText.selectAll();
				Font sampleFont1 = new Font(fontText.getText(), style[styleList.getSelectedIndex()],
						Integer.parseInt(sizeText.getText()));
				sample.setFont(sampleFont1);
			}
		});
		styleList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				int s = style[styleList.getSelectedIndex()];
				styleText.setText(fontStyle[s]);
				styleText.selectAll();
				Font sampleFont2 = new Font(fontText.getText(), style[styleList.getSelectedIndex()],
						Integer.parseInt(sizeText.getText()));
				sample.setFont(sampleFont2);
			}
		});
		sizeList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				sizeText.setText(fontSize[sizeList.getSelectedIndex()]);
				// sizeText.requestFocus();
				sizeText.selectAll();
				Font sampleFont3 = new Font(fontText.getText(), style[styleList.getSelectedIndex()],
						Integer.parseInt(sizeText.getText()));
				sample.setFont(sampleFont3);
			}
		});
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Font okFont = new Font(fontText.getText(), style[styleList.getSelectedIndex()],
						Integer.parseInt(sizeText.getText()));
				editArea.setFont(okFont);
				fontDialog.dispose();
			}
		});
		JPanel samplePanel = new JPanel();
		samplePanel.setBorder(BorderFactory.createTitledBorder("示锟斤拷"));
		// samplePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		samplePanel.add(sample);
		JPanel panel1 = new JPanel();
		JPanel panel2 = new JPanel();
		JPanel panel3 = new JPanel();
		// JPanel panel4=new JPanel();
		// JPanel panel5=new JPanel();
		// panel1.add(fontLabel);
		// panel1.add(styleLabel);
		// panel1.add(sizeLabel);
		// panel2.add(fontText);
		// panel2.add(new
		// JScrollPane(fontList));//JList锟斤拷支锟斤拷直锟接癸拷锟斤拷锟斤拷锟斤拷锟斤拷要锟斤拷JList锟斤拷为JScrollPane锟斤拷锟接匡拷锟斤拷图
		// panel2.setLayout(new GridLayout(2,1));
		// panel3.add(styleText);
		// panel3.add(new JScrollPane(styleList));
		// panel3.setLayout(new GridLayout(2,1));
		// panel4.add(sizeText);
		// panel4.add(new JScrollPane(sizeText));
		// panel4.setLayout(new GridLayout(2,1));
		// panel5.add(okButton);
		// panel5.add(cancel);
		// con.add(panel1);
		// con.add(panel2);
		// con.add(panel3);
		// con.add(panel4);
		// con.add(panel5);
		panel2.add(fontText);
		panel2.add(styleText);
		panel2.add(sizeText);
		panel2.add(okButton);
		panel3.add(new JScrollPane(fontList));// JList锟斤拷支锟斤拷直锟接癸拷锟斤拷锟斤拷锟斤拷锟斤拷要锟斤拷JList锟斤拷为JScrollPane锟斤拷锟接匡拷锟斤拷图
		panel3.add(new JScrollPane(styleList));
		panel3.add(new JScrollPane(sizeList));
		panel3.add(cancel);
		con.add(panel1);
		con.add(panel2);
		con.add(panel3);
		con.add(samplePanel);
		fontDialog.setSize(350, 340);
		fontDialog.setLocation(200, 200);
		fontDialog.setResizable(false);
		fontDialog.setVisible(true);
	}

	// public void menuPerformed(MenuEvent e)
	// { checkMenuItemEnabled();//锟斤拷锟矫硷拷锟叫★拷锟斤拷锟狡★拷粘锟斤拷锟斤拷删锟斤拷锟饺癸拷锟杰的匡拷锟斤拷锟斤拷
	// }

	public void actionPerformed(ActionEvent e) { // 锟铰斤拷
		if (e.getSource() == fileMenu_New) {
			editArea.requestFocus();
			String currentValue = editArea.getText();
			boolean isTextChange = (currentValue.equals(oldValue)) ? false : true;
			if (isTextChange) {
				int saveChoose = JOptionPane.showConfirmDialog(this, "锟斤拷锟斤拷锟侥硷拷锟斤拷未锟斤拷锟芥，锟角否保存？", "锟斤拷示",
						JOptionPane.YES_NO_CANCEL_OPTION);
				if (saveChoose == JOptionPane.YES_OPTION) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					// fileChooser.setApproveButtonText("确锟斤拷");
					fileChooser.setDialogTitle("锟斤拷锟轿�");
					int result = fileChooser.showSaveDialog(this);
					if (result == JFileChooser.CANCEL_OPTION) {
						statusLabel.setText("锟斤拷没锟斤拷选锟斤拷锟轿猴拷锟侥硷拷");
						return;
					}
					File saveFileName = fileChooser.getSelectedFile();
					if (saveFileName == null || saveFileName.getName().equals("")) {
						JOptionPane.showMessageDialog(this, "锟斤拷锟较凤拷锟斤拷锟侥硷拷锟斤拷", "锟斤拷锟较凤拷锟斤拷锟侥硷拷锟斤拷",
								JOptionPane.ERROR_MESSAGE);
					} else {
						try {
							FileWriter fw = new FileWriter(saveFileName);
							BufferedWriter bfw = new BufferedWriter(fw);
							bfw.write(editArea.getText(), 0, editArea.getText().length());
							bfw.flush();// 刷锟铰革拷锟斤拷锟侥伙拷锟斤拷
							bfw.close();
							isNewFile = false;
							currentFile = saveFileName;
							oldValue = editArea.getText();
							this.setTitle(saveFileName.getName() + " - 锟斤拷锟铰憋拷");
							statusLabel.setText("锟斤拷前锟斤拷锟侥硷拷锟斤拷" + saveFileName.getAbsoluteFile());
						} catch (IOException ioException) {
						}
					}
				} else if (saveChoose == JOptionPane.NO_OPTION) {
					editArea.replaceRange("", 0, editArea.getText().length());
					statusLabel.setText(" 锟铰斤拷锟侥硷拷");
					this.setTitle("锟睫憋拷锟斤拷 - 锟斤拷锟铰憋拷");
					isNewFile = true;
					undo.discardAllEdits(); // 锟斤拷锟斤拷锟斤拷锟叫碉拷"锟斤拷锟斤拷"锟斤拷锟斤拷
					editMenu_Undo.setEnabled(false);
					oldValue = editArea.getText();
				} else if (saveChoose == JOptionPane.CANCEL_OPTION) {
					return;
				}
			} else {
				editArea.replaceRange("", 0, editArea.getText().length());
				statusLabel.setText(" 锟铰斤拷锟侥硷拷");
				this.setTitle("锟睫憋拷锟斤拷 - 锟斤拷锟铰憋拷");
				isNewFile = true;
				undo.discardAllEdits();// 锟斤拷锟斤拷锟斤拷锟叫碉拷"锟斤拷锟斤拷"锟斤拷锟斤拷
				editMenu_Undo.setEnabled(false);
				oldValue = editArea.getText();
			}
		} // 锟铰斤拷锟斤拷锟斤拷
			// 锟斤拷
		else if (e.getSource() == fileMenu_Open) {
			editArea.requestFocus();
			String currentValue = editArea.getText();
			boolean isTextChange = (currentValue.equals(oldValue)) ? false : true;
			if (isTextChange) {
				int saveChoose = JOptionPane.showConfirmDialog(this, "锟斤拷锟斤拷锟侥硷拷锟斤拷未锟斤拷锟芥，锟角否保存？", "锟斤拷示",
						JOptionPane.YES_NO_CANCEL_OPTION);
				if (saveChoose == JOptionPane.YES_OPTION) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					// fileChooser.setApproveButtonText("确锟斤拷");
					fileChooser.setDialogTitle("锟斤拷锟轿�");
					int result = fileChooser.showSaveDialog(this);
					if (result == JFileChooser.CANCEL_OPTION) {
						statusLabel.setText("锟斤拷没锟斤拷选锟斤拷锟轿猴拷锟侥硷拷");
						return;
					}
					File saveFileName = fileChooser.getSelectedFile();
					if (saveFileName == null || saveFileName.getName().equals("")) {
						JOptionPane.showMessageDialog(this, "锟斤拷锟较凤拷锟斤拷锟侥硷拷锟斤拷", "锟斤拷锟较凤拷锟斤拷锟侥硷拷锟斤拷",
								JOptionPane.ERROR_MESSAGE);
					} else {
						try {
							FileWriter fw = new FileWriter(saveFileName);
							BufferedWriter bfw = new BufferedWriter(fw);
							bfw.write(editArea.getText(), 0, editArea.getText().length());
							bfw.flush();// 刷锟铰革拷锟斤拷锟侥伙拷锟斤拷
							bfw.close();
							isNewFile = false;
							currentFile = saveFileName;
							oldValue = editArea.getText();
							this.setTitle(saveFileName.getName() + " - 锟斤拷锟铰憋拷");
							statusLabel.setText("锟斤拷前锟斤拷锟侥硷拷锟斤拷" + saveFileName.getAbsoluteFile());
						} catch (IOException ioException) {
						}
					}
				} else if (saveChoose == JOptionPane.NO_OPTION) {
					String str = null;
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					// fileChooser.setApproveButtonText("确锟斤拷");
					fileChooser.setDialogTitle("锟斤拷锟侥硷拷");
					int result = fileChooser.showOpenDialog(this);
					if (result == JFileChooser.CANCEL_OPTION) {
						statusLabel.setText("锟斤拷没锟斤拷选锟斤拷锟轿猴拷锟侥硷拷");
						return;
					}
					File fileName = fileChooser.getSelectedFile();
					if (fileName == null || fileName.getName().equals("")) {
						JOptionPane.showMessageDialog(this, "锟斤拷锟较凤拷锟斤拷锟侥硷拷锟斤拷", "锟斤拷锟较凤拷锟斤拷锟侥硷拷锟斤拷",
								JOptionPane.ERROR_MESSAGE);
					} else {
						try {
							FileReader fr = new FileReader(fileName);
							BufferedReader bfr = new BufferedReader(fr);
							editArea.setText("");
							while ((str = bfr.readLine()) != null) {
								editArea.append(str);
							}
							this.setTitle(fileName.getName() + " - 锟斤拷锟铰憋拷");
							statusLabel.setText(" 锟斤拷前锟斤拷锟侥硷拷锟斤拷" + fileName.getAbsoluteFile());
							fr.close();
							isNewFile = false;
							currentFile = fileName;
							oldValue = editArea.getText();
						} catch (IOException ioException) {
						}
					}
				} else {
					return;
				}
			} else {
				String str = null;
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				// fileChooser.setApproveButtonText("确锟斤拷");
				fileChooser.setDialogTitle("锟斤拷锟侥硷拷");
				int result = fileChooser.showOpenDialog(this);
				if (result == JFileChooser.CANCEL_OPTION) {
					statusLabel.setText(" 锟斤拷没锟斤拷选锟斤拷锟轿猴拷锟侥硷拷 ");
					return;
				}
				File fileName = fileChooser.getSelectedFile();
				if (fileName == null || fileName.getName().equals("")) {
					JOptionPane.showMessageDialog(this, "锟斤拷锟较凤拷锟斤拷锟侥硷拷锟斤拷", "锟斤拷锟较凤拷锟斤拷锟侥硷拷锟斤拷",
							JOptionPane.ERROR_MESSAGE);
				} else {
					try {
						FileReader fr = new FileReader(fileName);
						BufferedReader bfr = new BufferedReader(fr);
						editArea.setText("");
						while ((str = bfr.readLine()) != null) {
							editArea.append(str);
						}
						this.setTitle(fileName.getName() + " - 锟斤拷锟铰憋拷");
						statusLabel.setText(" 锟斤拷前锟斤拷锟侥硷拷锟斤拷" + fileName.getAbsoluteFile());
						fr.close();
						isNewFile = false;
						currentFile = fileName;
						oldValue = editArea.getText();
					} catch (IOException ioException) {
					}
				}
			}
		} // 锟津开斤拷锟斤拷
			// 锟斤拷锟斤拷
		else if (e.getSource() == fileMenu_Save) {
			editArea.requestFocus();
			if (isNewFile) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				// fileChooser.setApproveButtonText("确锟斤拷");
				fileChooser.setDialogTitle("锟斤拷锟斤拷");
				int result = fileChooser.showSaveDialog(this);
				if (result == JFileChooser.CANCEL_OPTION) {
					statusLabel.setText("锟斤拷没锟斤拷选锟斤拷锟轿猴拷锟侥硷拷");
					return;
				}
				File saveFileName = fileChooser.getSelectedFile();
				if (saveFileName == null || saveFileName.getName().equals("")) {
					JOptionPane.showMessageDialog(this, "锟斤拷锟较凤拷锟斤拷锟侥硷拷锟斤拷", "锟斤拷锟较凤拷锟斤拷锟侥硷拷锟斤拷",
							JOptionPane.ERROR_MESSAGE);
				} else {
					try {
						FileWriter fw = new FileWriter(saveFileName);
						BufferedWriter bfw = new BufferedWriter(fw);
						bfw.write(editArea.getText(), 0, editArea.getText().length());
						bfw.flush();// 刷锟铰革拷锟斤拷锟侥伙拷锟斤拷
						bfw.close();
						isNewFile = false;
						currentFile = saveFileName;
						oldValue = editArea.getText();
						this.setTitle(saveFileName.getName() + " - 锟斤拷锟铰憋拷");
						statusLabel.setText("锟斤拷前锟斤拷锟侥硷拷锟斤拷" + saveFileName.getAbsoluteFile());
					} catch (IOException ioException) {
					}
				}
			} else {
				try {
					FileWriter fw = new FileWriter(currentFile);
					BufferedWriter bfw = new BufferedWriter(fw);
					bfw.write(editArea.getText(), 0, editArea.getText().length());
					bfw.flush();
					fw.close();
				} catch (IOException ioException) {
				}
			}
		} // 锟斤拷锟斤拷锟斤拷锟�
			// 锟斤拷锟轿�
		else if (e.getSource() == fileMenu_SaveAs) {
			editArea.requestFocus();
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			// fileChooser.setApproveButtonText("确锟斤拷");
			fileChooser.setDialogTitle("锟斤拷锟轿�");
			int result = fileChooser.showSaveDialog(this);
			if (result == JFileChooser.CANCEL_OPTION) {
				statusLabel.setText("锟斤拷锟斤拷没锟斤拷选锟斤拷锟轿猴拷锟侥硷拷");
				return;
			}
			File saveFileName = fileChooser.getSelectedFile();
			if (saveFileName == null || saveFileName.getName().equals("")) {
				JOptionPane.showMessageDialog(this, "锟斤拷锟较凤拷锟斤拷锟侥硷拷锟斤拷", "锟斤拷锟较凤拷锟斤拷锟侥硷拷锟斤拷",
						JOptionPane.ERROR_MESSAGE);
			} else {
				try {
					FileWriter fw = new FileWriter(saveFileName);
					BufferedWriter bfw = new BufferedWriter(fw);
					bfw.write(editArea.getText(), 0, editArea.getText().length());
					bfw.flush();
					fw.close();
					oldValue = editArea.getText();
					this.setTitle(saveFileName.getName() + "  - 锟斤拷锟铰憋拷");
					statusLabel.setText("锟斤拷锟斤拷前锟斤拷锟侥硷拷:" + saveFileName.getAbsoluteFile());
				} catch (IOException ioException) {
				}
			}
		} // 锟斤拷锟轿拷锟斤拷锟�
			// 页锟斤拷锟斤拷锟斤拷
		else if (e.getSource() == fileMenu_PageSetUp) {
			editArea.requestFocus();
			JOptionPane.showMessageDialog(this, "锟皆诧拷锟金，此癸拷锟斤拷锟斤拷未实锟街ｏ拷", "锟斤拷示", JOptionPane.WARNING_MESSAGE);
		} // 页锟斤拷锟斤拷锟矫斤拷锟斤拷
			// 锟斤拷印
		else if (e.getSource() == fileMenu_Print) {
			editArea.requestFocus();
			JOptionPane.showMessageDialog(this, "锟皆诧拷锟金，此癸拷锟斤拷锟斤拷未实锟街ｏ拷", "锟斤拷示", JOptionPane.WARNING_MESSAGE);
		} // 锟斤拷印锟斤拷锟斤拷
			// 锟剿筹拷
		else if (e.getSource() == fileMenu_Exit) {
			int exitChoose = JOptionPane.showConfirmDialog(this, "确锟斤拷要锟剿筹拷锟斤拷?", "锟剿筹拷锟斤拷示",
					JOptionPane.OK_CANCEL_OPTION);
			if (exitChoose == JOptionPane.OK_OPTION) {
				System.exit(0);
			} else {
				return;
			}
		} // 锟剿筹拷锟斤拷锟斤拷
			// 锟洁辑
			// else if(e.getSource()==editMenu)
			// {
			// checkMenuItemEnabled();//锟斤拷锟矫硷拷锟叫★拷锟斤拷锟狡★拷粘锟斤拷锟斤拷删锟斤拷锟饺癸拷锟杰的匡拷锟斤拷锟斤拷
			// }
			// 锟洁辑锟斤拷锟斤拷
			// 锟斤拷锟斤拷
		else if (e.getSource() == editMenu_Undo || e.getSource() == popupMenu_Undo) {
			editArea.requestFocus();
			if (undo.canUndo()) {
				try {
					undo.undo();
				} catch (CannotUndoException ex) {
					System.out.println("Unable to undo:" + ex);
					// ex.printStackTrace();
				}
			}
			if (!undo.canUndo()) {
				editMenu_Undo.setEnabled(false);
			}
		} // 锟斤拷锟斤拷锟斤拷锟斤拷
			// 锟斤拷锟斤拷
		else if (e.getSource() == editMenu_Cut || e.getSource() == popupMenu_Cut) {
			editArea.requestFocus();
			String text = editArea.getSelectedText();
			StringSelection selection = new StringSelection(text);
			clipBoard.setContents(selection, null);
			editArea.replaceRange("", editArea.getSelectionStart(), editArea.getSelectionEnd());
			checkMenuItemEnabled();// 锟斤拷锟矫硷拷锟叫ｏ拷锟斤拷锟狡ｏ拷粘锟斤拷锟斤拷删锟斤拷锟斤拷锟杰的匡拷锟斤拷锟斤拷
		} // 锟斤拷锟叫斤拷锟斤拷
			// 锟斤拷锟斤拷
		else if (e.getSource() == editMenu_Copy || e.getSource() == popupMenu_Copy) {
			editArea.requestFocus();
			String text = editArea.getSelectedText();
			StringSelection selection = new StringSelection(text);
			clipBoard.setContents(selection, null);
			checkMenuItemEnabled();// 锟斤拷锟矫硷拷锟叫ｏ拷锟斤拷锟狡ｏ拷粘锟斤拷锟斤拷删锟斤拷锟斤拷锟杰的匡拷锟斤拷锟斤拷
		} // 锟斤拷锟狡斤拷锟斤拷
			// 粘锟斤拷
		else if (e.getSource() == editMenu_Paste || e.getSource() == popupMenu_Paste) {
			editArea.requestFocus();
			Transferable contents = clipBoard.getContents(this);
			if (contents == null)
				return;
			String text = "";
			try {
				text = (String) contents.getTransferData(DataFlavor.stringFlavor);
			} catch (Exception exception) {
			}
			editArea.replaceRange(text, editArea.getSelectionStart(), editArea.getSelectionEnd());
			checkMenuItemEnabled();
		} // 粘锟斤拷锟斤拷锟斤拷
			// 删锟斤拷
		else if (e.getSource() == editMenu_Delete || e.getSource() == popupMenu_Delete) {
			editArea.requestFocus();
			editArea.replaceRange("", editArea.getSelectionStart(), editArea.getSelectionEnd());
			checkMenuItemEnabled(); // 锟斤拷锟矫硷拷锟叫★拷锟斤拷锟狡★拷粘锟斤拷锟斤拷删锟斤拷锟饺癸拷锟杰的匡拷锟斤拷锟斤拷
		} // 删锟斤拷锟斤拷锟斤拷
			// 锟斤拷锟斤拷
		else if (e.getSource() == editMenu_Find) {
			editArea.requestFocus();
			find();
		} // 锟斤拷锟揭斤拷锟斤拷
			// 锟斤拷锟斤拷锟斤拷一锟斤拷
		else if (e.getSource() == editMenu_FindNext) {
			editArea.requestFocus();
			find();
		} // 锟斤拷锟斤拷锟斤拷一锟斤拷锟斤拷锟斤拷
			// 锟芥换
		else if (e.getSource() == editMenu_Replace) {
			editArea.requestFocus();
			replace();
		} // 锟芥换锟斤拷锟斤拷
			// 转锟斤拷
		else if (e.getSource() == editMenu_GoTo) {
			editArea.requestFocus();
			JOptionPane.showMessageDialog(this, "锟皆诧拷锟金，此癸拷锟斤拷锟斤拷未实锟街ｏ拷", "锟斤拷示", JOptionPane.WARNING_MESSAGE);
		} // 转锟斤拷锟斤拷锟斤拷
			// 时锟斤拷锟斤拷锟斤拷
		else if (e.getSource() == editMenu_TimeDate) {
			editArea.requestFocus();
			// SimpleDateFormat currentDateTime=new
			// SimpleDateFormat("HH:mmyyyy-MM-dd");
			// editArea.insert(currentDateTime.format(new
			// Date()),editArea.getCaretPosition());
			Calendar rightNow = Calendar.getInstance();
			Date date = rightNow.getTime();
			editArea.insert(date.toString(), editArea.getCaretPosition());
		} // 时锟斤拷锟斤拷锟节斤拷锟斤拷
			// 全选
		else if (e.getSource() == editMenu_SelectAll || e.getSource() == popupMenu_SelectAll) {
			editArea.selectAll();
		} // 全选锟斤拷锟斤拷
			// 锟皆讹拷锟斤拷锟斤拷(锟斤拷锟斤拷前锟斤拷锟斤拷锟斤拷)
		else if (e.getSource() == formatMenu_LineWrap) {
			if (formatMenu_LineWrap.getState())
				editArea.setLineWrap(true);
			else
				editArea.setLineWrap(false);

		} // 锟皆讹拷锟斤拷锟叫斤拷锟斤拷
			// 锟斤拷锟斤拷锟斤拷锟斤拷
		else if (e.getSource() == formatMenu_Font) {
			editArea.requestFocus();
			font();
		} // 锟斤拷锟斤拷锟斤拷锟矫斤拷锟斤拷
			// 锟斤拷锟斤拷状态锟斤拷锟缴硷拷锟斤拷
		else if (e.getSource() == viewMenu_Status) {
			if (viewMenu_Status.getState())
				statusLabel.setVisible(true);
			else
				statusLabel.setVisible(false);
		} // 锟斤拷锟斤拷状态锟斤拷锟缴硷拷锟皆斤拷锟斤拷
			// 锟斤拷锟斤拷锟斤拷锟斤拷
		else if (e.getSource() == helpMenu_HelpTopics) {
			editArea.requestFocus();
			JOptionPane.showMessageDialog(this, "路锟斤拷锟斤拷锟斤拷锟斤拷远锟解，锟结将锟斤拷锟铰讹拷锟斤拷锟斤拷锟斤拷", "锟斤拷锟斤拷锟斤拷锟斤拷",
					JOptionPane.INFORMATION_MESSAGE);
		} // 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟�
			// 锟斤拷锟斤拷
		else if (e.getSource() == helpMenu_AboutNotepad) {
			editArea.requestFocus();
			JOptionPane.showMessageDialog(this,
					"&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&\n"
							+ " 锟斤拷写锟竭ｏ拷锟斤拷山学院锟斤拷息锟斤拷锟斤拷学院锟斤拷锟斤拷锟斤拷锟窖э拷爰硷拷锟阶ㄒ�06锟斤拷锟斤拷锟斤拷 锟斤拷选锟斤拷 \n"
							+ " 锟斤拷写时锟戒：锟斤拷锟斤拷锟斤拷锟斤拷锟节硷拷                          \n"
							+ " 锟斤拷锟斤拷QQ锟斤拷414644665                            \n"
							+ " e-mail锟斤拷zxz414644665@163.com                \n"
							+ " 锟斤拷学锟竭ｏ拷一些锟截凤拷锟斤拷锟斤拷锟斤拷耍锟斤拷锟斤拷锟街拷锟较ｏ拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷谢谢锟斤拷  \n"
							+ "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&\n",
					"锟斤拷锟铰憋拷", JOptionPane.INFORMATION_MESSAGE);
		} // 锟斤拷锟节斤拷锟斤拷
	}// 锟斤拷锟斤拷actionPerformed()锟斤拷锟斤拷

	// 实锟斤拷DocumentListener锟接匡拷锟叫的凤拷锟斤拷(锟诫撤锟斤拷锟斤拷锟斤拷锟叫癸拷)
	public void removeUpdate(DocumentEvent e) {
		editMenu_Undo.setEnabled(true);
	}

	public void insertUpdate(DocumentEvent e) {
		editMenu_Undo.setEnabled(true);
	}

	public void changedUpdate(DocumentEvent e) {
		editMenu_Undo.setEnabled(true);
	}// DocumentListener锟斤拷锟斤拷

	// 实锟街接匡拷UndoableEditListener锟斤拷锟斤拷UndoHandler(锟诫撤锟斤拷锟斤拷锟斤拷锟叫癸拷)
	class UndoHandler implements UndoableEditListener {
		public void undoableEditHappened(UndoableEditEvent uee) {
			undo.addEdit(uee.getEdit());
		}
	}

	// main锟斤拷锟斤拷锟斤拷始
	public static void main(String args[]) {
		Notepad notepad = new Notepad();
		notepad.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);// 使锟斤拷 System
																// exit
																// 锟斤拷锟斤拷锟剿筹拷应锟矫筹拷锟斤拷
	}// main锟斤拷锟斤拷锟斤拷锟斤拷
}
