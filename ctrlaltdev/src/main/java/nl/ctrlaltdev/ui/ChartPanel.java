package nl.ctrlaltdev.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

/**
 * A simple Chart Component.
 */
public class ChartPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 165452585487956674L;

	/**
	 * Series holds the data of a chart.
	 */
	public interface Series {
		public int getTop();

		public int getBottom();

		public int getLength();

		public int getValue(int nr);
	}

	/**
	 * A chart renderer renders the points of a Series.
	 */
	public interface ChartRenderer {
		public void renderValue(Graphics g, int x, int px, int y, int py,
				int count, int z, int height);

		public void renderMarker(Graphics g, int x, int px, int y, int py,
				int count, int z, int height);
	}

	public interface AxisRenderer {
		public void renderXAxis(Graphics g, int ofs, int left, int right,
				double scale, int ypos, int height);

		public void renderYAxis(Graphics g, int ofs, int bottom, int top,
				double scale, int xpos, int width);
	}

	/** Series implementation based on an array */
	public static class ArraySeries implements Series {
		private int[] myValues;
		private int myTop;
		private int myBottom;

		public ArraySeries(int[] values) {
			myValues = values;
			myTop = Integer.MIN_VALUE;
			myBottom = Integer.MAX_VALUE;
			for (int t = 0; t < values.length; t++) {
				if (values[t] > myTop)
					myTop = values[t];
				if (values[t] < myBottom)
					myBottom = values[t];
			}
		}

		public int getLength() {
			return myValues.length;
		}

		public int getTop() {
			return myTop;
		}

		public int getBottom() {
			return myBottom;
		}

		public int getValue(int nr) {
			return myValues[nr];
		}
	}

	/** Simple Line Renderer */
	public static class LineRenderer implements ChartRenderer {
		private Color myColor;

		public LineRenderer() {
			myColor = Color.BLACK;
		}

		public LineRenderer(Color color) {
			myColor = color;
		}

		public void renderValue(Graphics g, int x, int px, int y, int py,
				int count, int z, int height) {
			g.setColor(myColor);
			g.drawLine(px, py, x, y);
		}

		public void renderMarker(Graphics g, int x, int px, int y, int py,
				int count, int z, int height) {
			g.setColor(myColor);
			g.drawRoundRect(x - 3, y - 3, 7, 7, 2, 2);
		}
	}

	/** Semi 3D bar chart. */
	public static class BarRenderer implements ChartRenderer {
		private final static int MARGIN = 8;
		private final static int ZEFFECT = 4;
		private Color myColor;

		public BarRenderer() {
			myColor = Color.BLACK;
		}

		public BarRenderer(Color color) {
			myColor = color;
		}

		public void renderValue(Graphics g, int x, int px, int y, int py,
				int count, int z, int zeroLine) {
			px = px + MARGIN;
			x = x - MARGIN;
			int[] xx = new int[] { px + z * ZEFFECT, px + z * ZEFFECT,
					x + z * ZEFFECT, x + z * ZEFFECT };
			int[] yy = new int[] { zeroLine + z * ZEFFECT, y + z * ZEFFECT,
					y + z * ZEFFECT, zeroLine + z * ZEFFECT };
			g.setColor(myColor);
			g.fillPolygon(xx, yy, xx.length);
			g.setColor(myColor.darker());
			g.drawPolygon(xx, yy, xx.length);
		}

		public void renderMarker(Graphics g, int x, int px, int y, int py,
				int count, int z, int height) {
			// Do Nothing.
		}
	}

	public static class SimpleAxisRenderer implements AxisRenderer {
		public void renderXAxis(Graphics g, int ofs, int left, int right,
				double scale, int ypos, int height) {
			//
			g.setColor(Color.gray);
			g.drawLine((int) (ofs + left * scale), ypos, (int) (ofs + right
					* scale), ypos);
			//
			for (int t = left + 1; t < right; t++) {
				g.setColor(Color.lightGray);
				g.drawLine((int) (ofs + t * scale), ypos - height,
						(int) (ofs + t * scale), ypos + 2);
				g.setColor(Color.gray);
				g.drawLine((int) (ofs + t * scale), ypos - 2, (int) (ofs + t
						* scale), ypos + 2);
			}
			//
		}

		public void renderYAxis(Graphics g, int ofs, int bottom, int top,
				double scale, int xpos, int width) {
			int tmp = top - bottom;
			int mul = 1;
			while ((tmp / 25) > 0) {
				tmp = tmp / 5;
				mul = mul * 5;
			}
			//
			bottom = (bottom / mul) * mul;
			//
			for (int p = bottom; p <= top; p += mul) {
				g.setColor(Color.gray);
				g.drawLine(xpos - 2, (int) (ofs - p * scale), xpos + 2,
						(int) (ofs - p * scale));
				g.setColor(Color.lightGray);
				g.drawLine(xpos + 3, (int) (ofs - p * scale), xpos + width,
						(int) (ofs - p * scale));
			}
			//
			g.setColor(Color.gray);
			g.drawLine(xpos, (int) (ofs - bottom * scale), xpos,
					(int) (ofs - top * scale));
		}
	}

	private List<Series> mySeries = new ArrayList<>();
	private List<ChartRenderer> myRenderers = new ArrayList<>();
	private int myLeft, myRight, myTop, myBottom;
	private boolean myIsFloating;
	private AxisRenderer myAxis = new SimpleAxisRenderer();

	public ChartPanel() {
		super();
		setBackground(Color.white);
	}

	public ChartPanel(Series[] sr) {
		super();
		addSeries(sr);
		setBackground(Color.white);
	}

	public void addSeries(Series[] sr) {
		for (int t = 0; t < sr.length; t++)
			addSeries(sr[t]);
	}

	public void addSeries(Series sr) {
		addSeries(sr, new LineRenderer());
	}

	public void addSeries(Series sr, ChartRenderer renderer) {
		if (sr == null)
			throw new NullPointerException("Cannot add a NULL Series.");
		if (renderer == null)
			throw new NullPointerException("Cannot add a NULL Renderer.");
		mySeries.add(sr);
		myRenderers.add(renderer);
	}

	public void removeSeries(Series sr) {
		int idx = mySeries.indexOf(sr);
		if (idx < 0)
			throw new NullPointerException("This series is not in this Chart.");
		mySeries.remove(idx);
		myRenderers.remove(idx);
	}

	public Series[] getSeries() {
		return mySeries.toArray(new Series[mySeries.size()]);
	}

	public int getBottom() {
		int min = Integer.MAX_VALUE;
		if (!myIsFloating)
			min = 0;
		for (int t = 0; t < mySeries.size(); t++) {
			Series s = mySeries.get(t);
			if (s.getBottom() < min)
				min = s.getBottom();
		}
		return min;
	}

	public int getTop() {
		int max = Integer.MIN_VALUE;
		for (int t = 0; t < mySeries.size(); t++) {
			Series s = mySeries.get(t);
			if (s.getTop() > max)
				max = s.getTop();
		}
		return max;
	}

	public int getLength() {
		int max = Integer.MIN_VALUE;
		for (int t = 0; t < mySeries.size(); t++) {
			Series s = mySeries.get(t);
			if (s.getLength() > max)
				max = s.getLength();
		}
		return max;
	}

	public void setInnerMargins(int left, int right, int top, int bottom) {
		myLeft = left;
		myRight = right;
		myTop = top;
		myBottom = bottom;
	}

	public void setFloating(boolean f) {
		myIsFloating = f;
	}

	public void paint(Graphics g) {
		if (mySeries.size() == 0)
			return;
		int bottom = getBottom();
		int top = getTop();
		int length = getLength();
		//
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		//
		int width = getWidth() - myLeft - myRight;
		int height = getHeight() - myTop - myBottom;
		//
		//
		double pixelX = width / (length == 0 ? 1.0 : (double) length);
		double pixelY = height
				/ (top - bottom == 0 ? 1.0 : ((double) (top - bottom)));
		//
		int zeroLine = myTop + height + (int) (bottom * pixelY);
		//
		myAxis.renderYAxis(g, zeroLine, bottom, top, pixelY, myLeft, width);
		myAxis.renderXAxis(g, myLeft, 0, length, pixelX, zeroLine, height);
		//
		Series[] series = getSeries();
		int[] lastValue = new int[series.length];
		for (int t = 0; t < lastValue.length; t++)
			lastValue[t] = zeroLine;
		int lastPos = myLeft;
		for (int t = 0; t < length; t++) {
			int pos = myLeft + translate(t + 1, pixelX);
			for (int s = 0; s < series.length; s++) {
				ChartRenderer renderer = myRenderers.get(s);
				if (t < series[s].getLength()) {
					int value = series[s].getValue(t);
					value = myTop + height - translate(value - bottom, pixelY);
					//
					renderer.renderValue(g, pos, lastPos, value, lastValue[s],
							t, s, zeroLine);
					//
					lastValue[s] = value;
				}
			}
			lastPos = pos;
		}
		//
		// Draw Markers
		//
		lastValue = new int[series.length];
		for (int t = 0; t < lastValue.length; t++)
			lastValue[t] = zeroLine;
		lastPos = myLeft;
		for (int t = 0; t < length; t++) {
			int pos = myLeft + translate(t + 1, pixelX);
			for (int s = 0; s < series.length; s++) {
				ChartRenderer renderer = myRenderers.get(s);
				//
				if (t < series[s].getLength()) {
					int value = series[s].getValue(t);
					value = myTop + height - translate(value - bottom, pixelY);
					//
					renderer.renderMarker(g, pos, lastPos, value, lastValue[s],
							t, s, zeroLine);
					//
					lastValue[s] = value;
				}
			}
			lastPos = pos;
		}
	}

	protected int translate(double value, double pixelY) {
		return (int) Math.round(value * pixelY);
	}

	/*
	 * public static void main(String[] args) { // ChartPanel chart=new
	 * ChartPanel(); chart.addSeries(new ArraySeries(new int[]
	 * {3600,7200,9900}),new LineRenderer(Color.RED)); chart.addSeries(new
	 * ArraySeries(new int[] {3600,3600,5400}),new LineRenderer(Color.GREEN));
	 * chart.addSeries(new ArraySeries(new int[] {1200,1800,2600}),new
	 * LineRenderer(Color.BLUE)); chart.addSeries(new ArraySeries(new int[]
	 * {2400,2810,5600}),new LineRenderer(Color.YELLOW)); chart.addSeries(new
	 * ArraySeries(new int[] {900,1910,2600}),new LineRenderer(Color.CYAN));
	 * chart.addSeries(new ArraySeries(new int[] {800,2321,6600}),new
	 * LineRenderer(Color.BLACK)); chart.addSeries(new ArraySeries(new int[]
	 * {100,8110,7600}),new LineRenderer(Color.DARK_GRAY));
	 * chart.setInnerMargins(16,16,64,64); chart.setFloating(false); // JFrame
	 * f=new JFrame(); f.setSize(640,480);
	 * f.getContentPane().add(chart,BorderLayout.CENTER); f.show();
	 * f.addWindowListener(new WindowAdapter() { public void
	 * windowClosing(WindowEvent e) { System.exit(0); } }); // }
	 */
}
