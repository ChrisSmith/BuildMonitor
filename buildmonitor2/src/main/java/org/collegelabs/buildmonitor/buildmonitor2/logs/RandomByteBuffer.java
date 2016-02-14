package org.collegelabs.buildmonitor.buildmonitor2.logs;

/**
*/
public class RandomByteBuffer {
    final int offset;
    final int usedBytes;
    private final byte[] buffer;

    public RandomByteBuffer(int offset, int usedBytes, byte[] buffer){
        this.offset = offset;
        this.usedBytes = usedBytes;
        this.buffer = buffer;
    }

    public char charAt(long index){
        return (char) (buffer[(int)(index - offset)] & 0xFF);
    }
}
