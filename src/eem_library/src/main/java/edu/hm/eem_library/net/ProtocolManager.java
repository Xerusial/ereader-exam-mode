package edu.hm.eem_library.net;

import android.app.Activity;
import androidx.annotation.StringRes;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;

import edu.hm.eem_library.R;

public abstract class ProtocolManager {
    protected final Activity context;
    private final Toast toast;
    private final LinkedList<ReceiverThread> threads;

    public ProtocolManager(Activity context) {
        this.context = context;
        toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        threads = new LinkedList<>();
    }

    public void quit(){
        for(ReceiverThread thread : threads){
            thread.interrupt();
        }
    }

    /* Protocol Receiver Thread
     * The server opens one thread for each socket, the client has only got one thread.
     */
    public abstract class ReceiverThread extends Thread {
        private Socket inputSocket;

        public ReceiverThread(Socket inputSocket) {
            this.inputSocket = inputSocket;
            threads.add(this);
        }

        @Override
        public void run() {
            InputStream is = null;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    is = inputSocket.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Object[] header = DataPacket.readHeader(is);
                if ((int) header[0] != DataPacket.PROTOCOL_VERSION) {
                    putToast(R.string.toast_protocol_too_new);
                }
                if(!handleMessage((DataPacket.Type) header[1], is, inputSocket))
                    interrupt();
            }
        }

        protected abstract boolean handleMessage(DataPacket.Type type, InputStream is, Socket socket);
    }

    /* This method prevents the receiverThreads from flooding the application with toasts.
     * Only one toast is shown at a time.
     */
    protected void putToast(@StringRes int resId){
        if(toast.getView() == null) {
            toast.setText(context.getString(resId));
            toast.show();
        }
    }
}
