package nl.moj.client.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import nl.moj.client.anim.Anim;
import nl.moj.client.anim.LayeredAnim;

/**
 * 
 */
public class AnimationMessageImpl extends AbstractMessage implements Message.Animation {

	private int testNr;
	private Anim animation;

	public AnimationMessageImpl(int testNr,Anim a) {
		super(Message.MSG_ANIMATION);
		if (a==null) throw new NullPointerException("Cannot send a NULL animation.");
		this.testNr=testNr;
		this.animation=a;
	}

	public AnimationMessageImpl(int type,DataInput in) throws IOException {
		super(type,in); 
		if (type!=Message.MSG_ANIMATION) throw new IOException("Incorrect Type");
		testNr=in.readInt();
		// 
		animation=new LayeredAnim();
		animation.read(in);
		//
	}

	public void write(DataOutput out) throws IOException {
		super.write(out);
		out.writeInt(testNr);
		//
		animation.write(out);
		//
	}
		
    public Anim getAnimation() {
        return animation;
    }
    
	public int getTest() {
		return testNr;
	}

}
