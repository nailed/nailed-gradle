package jk_5.nailed.gradle.common;

import java.io.IOException;
import java.io.OutputStream;

/**
 * No description given
 *
 * @author jk-5
 */
public class StringOutputStream extends OutputStream {

    private StringBuffer buffer = new StringBuffer();

    @Override
    public void write(byte[] b) throws IOException{
        this.buffer.append(new String(b));
    }

    @Override
    public void write(byte[] b, int offset, int length) throws IOException{
        this.buffer.append(new String(b, offset, length));
    }

    @Override
    public void write(int b) throws IOException{
        byte[] bytes = new byte[1];
        bytes[0] = ((byte) b);
        this.buffer.append(new String(bytes));
    }

    @Override
    public String toString(){
        return this.buffer.toString();
    }
}
