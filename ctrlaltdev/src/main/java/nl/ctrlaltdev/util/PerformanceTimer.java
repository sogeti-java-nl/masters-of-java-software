package nl.ctrlaltdev.util;

/**
 * Performance measurement utility. For each call to tick() it will return the
 * number of milliseconds passed since the last call to tick.
 * 
 * @author E.Hooijmeijer
 */

public class PerformanceTimer {

	private static ThreadLocal<Long> tl = new ThreadLocal<>();

	/**
	 * resets the timer. This should be your first call.
	 */
	public static void reset() {
		Long now = new Long(System.currentTimeMillis());
		tl.set(now);
	}

	/**
	 * @return the time in milliseconds between this call to tick and the
	 *         previous one.
	 */

	public static long tick() {
		Long now = new Long(System.currentTimeMillis());
		Long then = tl.get();
		tl.set(now);
		if (then == null)
			return 0;
		return now.longValue() - then.longValue();
	}

}
