package transfer_program;

import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class AcceptConnection extends SwingWorker<String, Object> {
    volatile protected Socket acceptedSocket;
    private final ServerSocket serverSocket;
    private final JTextArea console;
    AcceptConnection(ServerSocket ss, JTextArea console){
        this.serverSocket = ss;
        this.console = console;
    }
    @Override
    protected String doInBackground() throws IOException{
        console.append("\nAwaiting client...");
        acceptedSocket = serverSocket.accept();
        return null;
    }
    @Override
    protected void done() {
        console.append(Main.getTime()+"Connection established \nwith " + acceptedSocket.getInetAddress());
        Main.acceptedSocket = this.acceptedSocket;
        Main.revealComponents();
    }


}




