package org.collegelabs.buildmonitor.buildmonitor2.logs;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
*/
public class BufferedRandomAccessFile {
    public final long length;
    private RandomByteBuffer previousBuffer;
    private RandomByteBuffer currentBuffer;
    private RandomAccessFile file;
    final int bufferSize = 4 * 1024;

    public BufferedRandomAccessFile(RandomAccessFile file) throws Exception {
        this.file = file;
        this.length = file.length();
        readNextBuffer();
    }

    public long getLength(){
        return length;
    }

    public char getChar(long index){
        if(index < currentBuffer.offset){

            if(previousBuffer != null && index > previousBuffer.offset){
                return previousBuffer.charAt(index);
            }

            throw new RuntimeException("can't go backwards more than a single buffer: "+index);
        }

        if(index > currentBuffer.offset + currentBuffer.usedBytes - 1){
            readNextBuffer();
        }

        return currentBuffer.charAt(index);
    }

    private void readNextBuffer() {
        try {
            previousBuffer = currentBuffer;

            int offset = (previousBuffer != null ? previousBuffer.offset + bufferSize : 0);
            file.seek(offset);

            byte[] buffer = new byte[bufferSize];
            int usedBytes = file.read(buffer, 0, bufferSize);
            currentBuffer = new RandomByteBuffer(offset, usedBytes, buffer);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public char[] getChars(int offset, int charCount) {
        char[] chars = new char[charCount];
        for(int i = 0; i < charCount; i++){
            chars[i] = getChar(offset + i);
        }
        return chars;
    }
}
