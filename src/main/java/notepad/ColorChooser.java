package notepad;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ColorChooser extends JDialog {
	public static void main(String[] args) {

		ColorChooser chooser = new ColorChooser();
		chooser.setVisible(true);
		System.out.println("over");
	}
	JSlider slider[] = new JSlider[3];
	int value[] = new int[3];
	JLabel[] leftLabel = new JLabel[3], rightLabel = new JLabel[3];
	String[] strLeft = "red green blue".split(" ");
	JPanel center, south, southWest, southEast, southCenter;
	private ChangeListener listenChange = new ChangeListener() {

		@Override
		public void stateChanged(ChangeEvent e) {
			for (int i = 0; i < 3; i++) {
				if (e.getSource() == slider[i]) {
					value[i] = slider[i].getValue();
					rightLabel[i].setText(value[i] + "");
					center.setBackground(
							new Color(value[0], value[1], value[2]));
				}
			}
		}
	};
	public ColorChooser() {
		setSize(400, 400);
		setLayout(new BorderLayout());
		center = new JPanel();
		south = new JPanel(new BorderLayout());
		southEast = new JPanel(new GridLayout(3, 1));
		southWest = new JPanel(new GridLayout(3, 1));
		southCenter = new JPanel(new GridLayout(3, 1));

		for (int i = 0; i < 3; i++) {
			leftLabel[i] = new JLabel(strLeft[i]);
			southWest.add(leftLabel[i]);
			rightLabel[i] = new JLabel("" + value[i]);
			southEast.add(rightLabel[i]);

			slider[i] = new JSlider(0, 255);
			slider[i].addChangeListener(listenChange);
			slider[i].setValue(value[i]);
			southCenter.add(slider[i]);
		}
		south.add(southWest,BorderLayout.WEST);
		south.add(southCenter,BorderLayout.CENTER);
		south.add(southEast,BorderLayout.EAST);
		add(center, BorderLayout.CENTER);
		add(south, BorderLayout.SOUTH);
	}
}
