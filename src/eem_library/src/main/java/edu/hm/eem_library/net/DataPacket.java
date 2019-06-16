package edu.hm.eem_library.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public abstract class DataPacket {
    /* Protocol specification: EEP - E-Reader TeacherExam Protocol
       [4 Byte: Version]
       [4 Byte: Type]
       [following Bytes: Data]
       Data specification can be found in the respective child-classes.
     */
    public enum Type {
        LOGIN, EXAMFILE, SIGNAL;

        private static Type[] values = null;

        public ByteBuffer insertInBytebuffer(ByteBuffer buf) {
            return buf.putInt(this.ordinal());
        }

        public static Type extractFromBytebuffer(ByteBuffer buf) {
            int idx = buf.getInt();
            if (Type.values == null) {
                Type.values = Type.values();
            }
            return Type.values[idx];
        }

    }

    private final Type type;
    // incremental protocol version
    static final int PROTOCOL_VERSION = 0;
    private static final int HEADER_FIELDS = 3;
    static final int INT_BYTES = 4;
    static final int LONG_BYTES = 8;
    private static final int HEADER_SIZE = 2*INT_BYTES + LONG_BYTES;

    DataPacket(Type type) {
        this.type = type;
    }

    private void writeHeader(OutputStream os){
        ByteBuffer bb = ByteBuffer.allocate(HEADER_SIZE);
        bb.putInt(PROTOCOL_VERSION);
        type.insertInBytebuffer(bb);
        try {
            os.write(bb.array());
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Object[] readHeader(InputStream is){
        Object[] ret = new Object[HEADER_FIELDS];
        byte[] bytes = new byte[HEADER_SIZE];
        try {
            //noinspection ResultOfMethodCallIgnored
            is.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        ret[0] = bb.getInt();
        ret[1] = Type.extractFromBytebuffer(bb);
        return ret;
    }

    protected abstract void writeData(OutputStream os);

    private void sendData(OutputStream os) {
        writeHeader(os);
        writeData(os);
    }

    public static final class SenderThread extends Thread{
        private final Socket socket;
        private final DataPacket dp;

        public SenderThread(Socket socket, DataPacket dp) {
            this.socket = socket;
            this.dp = dp;
        }

        @Override
        public void run() {
            super.run();
            try {
                OutputStream os = socket.getOutputStream();
                dp.sendData(os);
                //Do not close outputstream, as this will close the socket entirely
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
