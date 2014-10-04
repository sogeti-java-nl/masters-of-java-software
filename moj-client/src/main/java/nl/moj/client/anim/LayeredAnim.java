package nl.moj.client.anim;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * LayeredAnim : An anim implementation which can be read from and written to an
 * stream.
 * 
 * Typical steps to create an animation : - create the LayeredAnim - add (all)
 * the resources you need. - create a new frame. - add all visible resources in
 * the order they need to be drawn. - create another frame. - add all visible
 * resources in the order they need to be drawn. - etc. TODO : Implement fully.
 * 
 * @author E.Hooijmeijer
 */

public class LayeredAnim implements Anim {

	/** constant for scaling 360 steps to radials */
	public static final double ROTATIONSCALE = 57.29578;

	public static final short RES_BMP = 0;
	public static final short RES_SPRITE = 1;
	public static final short RES_SHAPE = 2;
	public static final short RES_MAP = 3;
	public static final short LAYER = 10;
	public static final short FRAME = 20;

	public static final double AREA_WIDTH = 100;
	public static final double AREA_HEIGHT = 100;

	/** defines a drawable resource */
	public interface Resource {
		public short getId();

		public void draw(Graphics2D g, int x, int y, int rot);
	}

	/**
	 * contains a color indexed bitmap.
	 */
	public static final class BitMapResource implements Persistable, Resource {
		private short id;
		private Color[] colors;
		private byte[][] pixels;

		public BitMapResource(short id) {
			this.id = id;
		}

		public short getId() {
			return id;
		}

		public BitMapResource setImageData(Color[] c, byte[][] pixels) {
			this.colors = c;
			this.pixels = pixels;
			return this;
		}

		public void read(DataInput in) throws IOException {
			int csize = in.readInt();
			colors = new Color[csize];
			for (int t = 0; t < csize; t++) {
				int c = in.readInt();
				colors[t] = (c >= 0 ? new Color(c) : null);
			}
			int width = in.readInt();
			int height = in.readInt();
			pixels = new byte[height][width];
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					pixels[y][x] = in.readByte();
				}
			}
		}

		public void write(DataOutput out) throws IOException {
			out.writeShort(RES_BMP);
			out.writeShort(id);
			out.writeInt(colors.length);
			for (int t = 0; t < colors.length; t++)
				out.writeInt(colors[t] == null ? -1
						: colors[t].getRGB() & 0x00FFFFFF);
			out.writeInt(pixels[0].length);
			out.writeInt(pixels.length);
			for (int y = 0; y < pixels.length; y++) {
				for (int x = 0; x < pixels[0].length; x++) {
					out.writeByte(pixels[y][x]);
				}
			}
		}

		public int getHeight() {
			return pixels.length;
		}

		public int getWidth() {
			if (pixels.length == 0)
				return 0;
			return pixels[0].length;
		}

		public Color getPixelAt(int x, int y) {
			return colors[pixels[y][x]];
		}

		public void draw(Graphics2D g, int x, int y, int rot) {
			int h = getHeight();
			int h2 = h / 2;
			int w = getWidth();
			int w2 = w / 2;
			//
			Graphics2D local = (Graphics2D) g.create();
			local.translate(x + w / 2, y + h / 2);
			if (rot != 0) {
				AffineTransform at = local.getTransform();
				at.rotate((rot) / ROTATIONSCALE);
				local.setTransform(at);
			}
			//
			for (int cy = 0; cy < h; cy++) {
				for (int cx = 0; cx < w; cx++) {
					Color c = getPixelAt(cx, cy);
					if (c != null) {
						local.setColor(c);
						local.fillRect(cx - w2, cy - h2, 1, 1);
					}
				}
			}
			//
			local.dispose();
		}
	}

	public static final class ShapeResource implements Persistable, Resource {
		public static final short SHAPE_RECT = 0;
		public static final short SHAPE_CIRCLE = 1;
		public static final short SHAPE_LINE = 2;
		public static final short SHAPE_TRIANGLE = 3;
		private short shape;
		private short w;
		private short h;
		private short id;
		private Color col;

		public ShapeResource(short id) {
			this.id = id;
		}

		public ShapeResource set(short shape, Color color, short w, short h) {
			this.shape = shape;
			this.col = color;
			this.w = w;
			this.h = h;
			return this;
		}

		public void draw(Graphics2D g, int x, int y, int rot) {
			Graphics2D local = (Graphics2D) g.create();
			try {
				local.translate(x, y);
				if (rot != 0) {
					AffineTransform at = local.getTransform();
					at.rotate((rot) / ROTATIONSCALE);
					local.setTransform(at);
				}
				local.setColor(col);
				switch (shape) {
				case SHAPE_RECT:
					local.fillRect(-w / 2, -h / 2, w, h);
					break;
				case SHAPE_CIRCLE:
					local.fillOval(-w / 2, -h / 2, w, h);
					break;
				case SHAPE_LINE:
					local.drawLine(0, 0, w, h);
					break;
				case SHAPE_TRIANGLE:
					local.drawPolygon(new int[] { 0, w / 2, -w / 2 },
							new int[] { -h / 2, h / 2, h / 2 }, 3);
					break;
				default:
					throw new RuntimeException("Unknown shape.");
				}

			} finally {
				local.dispose();
			}
		}

		public short getId() {
			return id;
		}

		public void read(DataInput in) throws IOException {
			shape = in.readShort();
			col = new Color(in.readInt());
			w = in.readShort();
			h = in.readShort();
		}

		public void write(DataOutput out) throws IOException {
			out.writeShort(RES_SHAPE);
			out.writeShort(id);
			out.writeShort(shape);
			out.writeInt(col.getRGB());
			out.writeShort(w);
			out.writeShort(h);
		}
	}

	/**
	 * Sprite is actually a reference to another resource so that you can re-use
	 * the same shape or bitmap multiple times in an animation.
	 */
	public static final class SpriteResource implements Persistable, Resource {
		private short id;
		private short sid;
		private LayeredAnim owner;

		public SpriteResource(short id, LayeredAnim owner) {
			this.owner = owner;
			this.id = id;
		}

		public short getId() {
			return id;
		}

		public void setResource(short rid) {
			this.sid = rid;
		}

		public short getResource() {
			return sid;
		}

		public void draw(Graphics2D g, int x, int y, int rot) {
			Resource r = owner.getResource(sid);
			r.draw(g, x, y, rot);
		}

		public void read(DataInput in) throws IOException {
			sid = in.readShort();
		}

		public void write(DataOutput out) throws IOException {
			out.writeShort(RES_SPRITE);
			out.writeShort(id);
			out.writeShort(sid);
		}

	}

	public static final class AnimFrame implements Anim.Frame, Persistable {
		private static final int STATE_ID = 0;
		private static final int STATE_X = 1;
		private static final int STATE_Y = 2;
		private static final int STATE_ROT = 3;
		private LayeredAnim owner;
		private List<short[]> states;

		public AnimFrame(short id, LayeredAnim owner) {
			this.owner = owner;
			states = new ArrayList<>();
		}

		public void add(Resource r, int x, int y, int rot) {
			states.add(new short[] { r.getId(), (short) x, (short) y,
					(short) rot });
		}

		public void write(DataOutput out) throws IOException {
			out.writeShort(FRAME);
			out.writeShort(0);
			out.writeInt(states.size());
			for (int t = 0; t < states.size(); t++) {
				short[] state = states.get(t);
				out.writeShort(state[STATE_ID]);
				out.writeShort(state[STATE_X]);
				out.writeShort(state[STATE_Y]);
				out.writeShort(state[STATE_ROT]);
			}
		}

		public void read(DataInput in) throws IOException {
			int cnt = in.readInt();
			for (int t = 0; t < cnt; t++) {
				short[] state = new short[4];
				state[STATE_ID] = in.readShort();
				state[STATE_X] = in.readShort();
				state[STATE_Y] = in.readShort();
				state[STATE_ROT] = in.readShort();
				states.add(state);
			}
		}

		public void draw(Graphics g, int w, int h) {
			//
			AffineTransform tf = ((Graphics2D) g).getTransform();
			tf.scale(w / AREA_WIDTH, h / AREA_HEIGHT);
			((Graphics2D) g).setTransform(tf);
			//
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, (int) AREA_WIDTH, (int) AREA_HEIGHT);
			//
			for (int t = 0; t < states.size(); t++) {
				short[] state = states.get(t);
				Resource res = owner.getResource(state[STATE_ID]);
				res.draw((Graphics2D) g, state[STATE_X], state[STATE_Y],
						state[STATE_ROT]);
			}
		}
	}

	public static final class MapResource implements Persistable, Resource {
		private short id;
		private short tileSize;
		private String[] map;
		private Map<Character, Short> charToTiles = new HashMap<>();
		private LayeredAnim owner;

		public MapResource(short id, LayeredAnim owner) {
			this.owner = owner;
			this.id = id;
		}

		public short getId() {
			return id;
		}

		public void setMap(int tileSize, String[] map,
				Map<Character, Short> charToTiles) {
			this.tileSize = (short) tileSize;
			this.map = map;
			this.charToTiles = charToTiles;
		}

		public void draw(Graphics2D g, int x, int y, int rot) {
			for (int yy = 0; yy < map.length; yy++) {
				for (int xx = 0; xx < map[yy].length(); xx++) {
					int tx = x + xx * tileSize;
					int ty = y + yy * tileSize;
					Character c = new Character(map[yy].charAt(xx));
					if (charToTiles.containsKey(c)) {
						short id = (charToTiles.get(c)).shortValue();
						Resource r = owner.getResource(id);
						r.draw(g, tx, ty, rot);
					}
				}
			}
		}

		public void read(DataInput in) throws IOException {
			//
			tileSize = in.readShort();
			//
			int mapHeight = in.readShort();
			int mapWidth = in.readShort();
			map = new String[mapHeight];
			for (int y = 0; y < mapHeight; y++) {
				StringBuffer sb = new StringBuffer();
				for (int x = 0; x < mapWidth; x++) {
					//
					char c = (char) in.readByte();
					sb.append(c);
					//
				}
				map[y] = sb.toString();
			}
			//
			charToTiles = new HashMap<>();
			int tiles = in.readShort();
			for (int t = 0; t < tiles; t++) {
				char c = (char) in.readByte();
				short id = in.readShort();
				charToTiles.put(new Character(c), new Short(id));
			}
			//
		}

		public void write(DataOutput out) throws IOException {
			out.writeShort(RES_MAP);
			out.writeShort(id);
			//
			out.writeShort(tileSize);
			//
			out.writeShort(map.length);
			out.writeShort(map[0].length());
			for (int y = 0; y < map.length; y++) {
				for (int x = 0; x < map[0].length(); x++) {
					out.writeByte((byte) map[y].charAt(x));
				}
			}
			//
			out.writeShort(charToTiles.size());
			Iterator<Character> i = charToTiles.keySet().iterator();
			while (i.hasNext()) {
				Character c = i.next();
				Short id = charToTiles.get(c);
				out.writeByte((byte) c.charValue());
				out.writeShort(id.shortValue());
			}
			//
		}
	}

	private List<Persistable> frames = new ArrayList<>();
	private List<Persistable> resources = new ArrayList<>();
	private List<Persistable> layers = new ArrayList<>();
	private int current;
	private short idCnt;

	public Persistable instantiate(DataInput in) throws IOException {
		short type = in.readShort();
		short id = in.readShort();
		//
		switch (type) {
		case RES_BMP:
			return new BitMapResource(id);
		case RES_SPRITE:
			return new SpriteResource(id, this);
		case RES_SHAPE:
			return new ShapeResource(id);
		case RES_MAP:
			return new MapResource(id, this);
			// case LAYER : return new LayerResource(id);
		case FRAME:
			return new AnimFrame(id, this);
		default:
			throw new IOException("Unknown type " + type + " in Animation.");
		}
		//
	}

	public void read(DataInput in) throws IOException {
		//
		int rsize = in.readInt();
		for (int t = 0; t < rsize; t++) {
			Persistable p = instantiate(in);
			p.read(in);
			resources.add(p);
		}
		//
		int lsize = in.readInt();
		for (int t = 0; t < lsize; t++) {
			Persistable p = instantiate(in);
			p.read(in);
			layers.add(p);
		}
		//
		int fsize = in.readInt();
		for (int t = 0; t < fsize; t++) {
			Persistable p = instantiate(in);
			p.read(in);
			frames.add(p);
		}
		//
	}

	public void write(DataOutput out) throws IOException {
		//
		out.writeInt(resources.size());
		for (int t = 0; t < resources.size(); t++) {
			(resources.get(t)).write(out);
		}
		//
		out.writeInt(layers.size());
		for (int t = 0; t < layers.size(); t++) {
			(layers.get(t)).write(out);
		}
		//
		out.writeInt(frames.size());
		for (int t = 0; t < frames.size(); t++) {
			(frames.get(t)).write(out);
		}
		//
	}

	protected Resource getResource(short id) {
		for (int t = 0; t < resources.size(); t++) {
			if (((Resource) resources.get(t)).getId() == id)
				return (Resource) resources.get(t);
		}
		throw new NullPointerException("Missing resource with id '" + id + "'");
	}

	public synchronized BitMapResource createBitmapResource() {
		BitMapResource res = new BitMapResource(idCnt++);
		resources.add(res);
		return res;
	}

	public synchronized ShapeResource createShapeResource() {
		ShapeResource res = new ShapeResource(idCnt++);
		resources.add(res);
		return res;
	}

	public synchronized SpriteResource createSpriteResource(Resource target) {
		SpriteResource res = new SpriteResource(idCnt++, this);
		resources.add(res);
		res.setResource(target.getId());
		return res;
	}

	public synchronized MapResource createMapResource() {
		MapResource res = new MapResource(idCnt++, this);
		resources.add(res);
		return res;
	}

	public synchronized AnimFrame createNewFrame() {
		AnimFrame f = new AnimFrame(idCnt++, this);
		frames.add(f);
		return f;
	}

	//
	// Public Interface
	//

	public Anim.Frame current() {
		return (Anim.Frame) frames.get(current);
	}

	public boolean next() {
		if (current + 1 < getFrameCount())
			current++;
		return current < getFrameCount() - 1;
	}

	public void prev() {
		if (current - 1 >= 0)
			current--;
	}

	public void first() {
		current = 0;
	}

	public void last() {
		current = getFrameCount() - 1;
	}

	public int currentFrame() {
		return current;
	}

	public int getFrameCount() {
		return frames.size();
	}

	public void setCurrentFrame(int f) {
		if (f < 0)
			return;
		if (f >= getFrameCount())
			return;
		current = f;
	}

}
