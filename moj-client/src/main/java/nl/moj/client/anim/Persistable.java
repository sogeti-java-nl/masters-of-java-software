package nl.moj.client.anim;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * defines a something that can be read and written
 */
public interface Persistable {

	public void read(DataInput in) throws IOException;
	public void write(DataOutput out) throws IOException;

}
