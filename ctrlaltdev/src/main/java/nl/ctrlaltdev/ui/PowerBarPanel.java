package nl.ctrlaltdev.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

/**
 * Display Component that renders values as a series of consecutive bars
 * together with a label.
 */
public class PowerBarPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2347180249069542493L;

	/** Entry contains all data about one single bar */
	public interface Entry {
		/** returns the number of entries in this bar. */
		public int getLength();

		/** returns the value for the specified entry */
		public int getValue(int nr);

		/** returns the overall label for this bar */
		public String getLabel();

		/** returns the overall sub-label for this bar */
		public String getSubLabel();

		/** returns the value specific text */
		public String getTextLabel(int nr);

		/** returns the value as text. */
		public String getValueLabel(int nr);
	}

	/** simple array based implementation of the Entry interface */
	public static class SimpleEntry implements Entry {
		private String myLbl, mySub;
		private int[] myValues;
		private String[] myValueLabels, myTextLabels;

		public SimpleEntry(String lbl, String sub, int[] v,
				String[] textLabels, String[] valueLabels) {
			myLbl = lbl;
			mySub = sub;
			myValues = v;
			myValueLabels = valueLabels;
			myTextLabels = textLabels;
		}

		public String getLabel() {
			return myLbl;
		}

		public String getSubLabel() {
			return mySub;
		}

		public int getLength() {
			return myValues.length;
		}

		public int getValue(int nr) {
			return myValues[nr];
		}

		public String getValueLabel(int nr) {
			if (nr >= myValueLabels.length)
				return "";
			return myValueLabels[nr];
		}

		public String getTextLabel(int nr) {
			if (nr >= myTextLabels.length)
				return "";
			return myTextLabels[nr];
		}
	}

	private int entryLabelWidth;
	private int verticalBarWidth;
	private int entryHeight;
	private Font boldFont;
	private Font plainFont;
	private Color vericalBarColor;
	private Color entryLabelColor;
	private Color entryTextColor;
	private Color labelTextColor;
	private Color[] barColors;
	//
	private List<Entry> myEntries = new ArrayList<>();

	/** constructs a new PowerBarPanel with default settings. */
	public PowerBarPanel() {
		super();
		//
		entryHeight = 42;
		entryLabelWidth = 128;
		verticalBarWidth = 8;
		//
		boldFont = new Font("Verdana", Font.BOLD, 12);
		plainFont = new Font("Verdana", Font.PLAIN, 11);
		//
		vericalBarColor = new Color(64, 64, 128);
		entryLabelColor = new Color(128, 128, 192);
		entryTextColor = new Color(255, 255, 255);
		labelTextColor = new Color(64, 64, 64);
		barColors = new Color[] { new Color(192, 128, 128),
				new Color(192, 192, 128), new Color(128, 192, 128),
				new Color(128, 160, 192), new Color(128, 128, 192) };
		//
	}

	public void setFonts(Font bold, Font plain) {
		boldFont = bold;
		plainFont = plain;
	}

	public void setBarColors(Color[] col) {
		if (col == null)
			throw new NullPointerException("Cannot set NULL Colors.");
		barColors = col;
	}

	public void setSizes(int height, int labelWidth, int barWidth) {
		entryHeight = height;
		entryLabelWidth = labelWidth;
		verticalBarWidth = barWidth;
	}

	public void setColors(Color labelBackgroundColor, Color labelTextColor,
			Color barTextColor, Color verticalBarColor) {
		this.vericalBarColor = verticalBarColor;
		this.entryLabelColor = labelBackgroundColor;
		this.entryTextColor = labelTextColor;
		this.labelTextColor = barTextColor;
	}

	public void addEntry(Entry e) {
		if (e == null)
			throw new NullPointerException("Cannot add a NULL entry.");
		myEntries.add(e);
		this.setPreferredSize(new Dimension(0, myEntries.size() * (entryHeight)));
		this.invalidate();
	}

	public void removeEntry(Entry e) {
		int idx = myEntries.indexOf(e);
		if (idx < 0)
			throw new NullPointerException("Entry not found.");
		myEntries.remove(idx);
		this.setPreferredSize(new Dimension(0, myEntries.size() * (entryHeight)));
		this.invalidate();
	}

	public int getMax() {
		int mx = 0;
		for (int t = 0; t < myEntries.size(); t++) {
			Entry e = myEntries.get(t);
			int sum = 0;
			for (int y = 0; y < e.getLength(); y++)
				sum += e.getValue(y);
			if (sum > mx)
				mx = sum;
		}
		return mx;
	}

	public void paint(Graphics g) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		//
		double mx = getMax();
		if (mx == 0)
			mx = 1.0;
		double width = getWidth() - entryLabelWidth;
		double pixPerPoint = width / mx;
		//
		g.setColor(vericalBarColor);
		g.fillRect(entryLabelWidth + 1, 0, verticalBarWidth, getHeight());
		//
		for (int t = 0; t < myEntries.size(); t++) {
			Entry e = myEntries.get(t);
			g.setColor(entryLabelColor);
			g.fillRect(0, t * entryHeight, entryLabelWidth, entryHeight - 1);
			//
			// Draw Label
			//
			g.setFont(boldFont);
			g.setColor(entryTextColor);
			int w = g.getFontMetrics().stringWidth(e.getLabel());
			g.drawString(e.getLabel(), entryLabelWidth - 8 - w, t * entryHeight
					+ entryHeight / 3);
			g.setFont(plainFont);
			w = g.getFontMetrics().stringWidth(e.getSubLabel());
			g.drawString(e.getSubLabel(), entryLabelWidth - 8 - w, t
					* entryHeight + entryHeight * 2 / 3);
			//
			int cx = entryLabelWidth + 1 + verticalBarWidth + 1;
			for (int y = 0; y < e.getLength(); y++) {
				//
				g.setColor(y < barColors.length ? barColors[y] : Color.white);
				int nx = (int) (e.getValue(y) * pixPerPoint);
				g.fillRect(cx, t * entryHeight, nx, entryHeight - 1);
				//
				String vlbl = e.getValueLabel(y);
				g.setFont(plainFont);
				w = g.getFontMetrics().stringWidth(vlbl);
				if (w < nx) {
					g.setColor(labelTextColor);
					g.drawString(vlbl, cx + nx / 2 - w / 2, t * entryHeight
							+ entryHeight * 2 / 3);
				}
				//
				vlbl = e.getTextLabel(y);
				g.setFont(boldFont);
				w = g.getFontMetrics().stringWidth(vlbl);
				if (w < nx) {
					g.setColor(labelTextColor);
					g.drawString(vlbl, cx + nx / 2 - w / 2, t * entryHeight
							+ entryHeight * 1 / 3);
				}
				//
				cx = cx + nx + (nx > 0 ? 1 : 0);
			}
		}
	}

}
