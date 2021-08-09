package transfer_program;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.IOException;

class ReceiveFile extends SwingWorker<String,Object> {

    private final DataInputStream is;
    private final JTextArea console;
    private final JTextField monitor;
    private final boolean HAS_INFO;
    private final boolean IS_CONSOLE_UPDATED;
    private final int DELAY = 30;

    private String path;
    private int chunkSize;
    private int totalBytes = 0;
    private int fileSize = 0;
    volatile private boolean running = true;
    private Thread printerThread;

    protected ReceiveFile(DataInputStream inputStream, String path, JTextArea console, JTextField monitor, int preferredChunkSize, boolean isFileInfoSent, boolean IS_CONSOLE_UPDATED) {
        this.is = inputStream;
        this.path = path;
        this.console = console;
        this.monitor = monitor;
        this.chunkSize = preferredChunkSize;
        this.HAS_INFO = isFileInfoSent;
        this.IS_CONSOLE_UPDATED = IS_CONSOLE_UPDATED;

        if (this.IS_CONSOLE_UPDATED) {
            printerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (running) {
                        printProgress(totalBytes, fileSize);
                        sleep(DELAY);
                    }
                }

                public void sleep(int time) {
                    try {
                        Thread.sleep(time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            printerThread.start();
        }
    }

    @Override
    protected String doInBackground() {
        byte[] buffer;
        String fileName = null;

        try {
            if (HAS_INFO) {
                fileName = is.readUTF();
                this.fileSize = is.readInt();
                console.append("\nFile name: " + fileName);
                console.append("\nFile size: " + UtilityFunctions.getMegaBytes(fileSize) + " MBs");
                console.append("\nin bytes: " + fileSize);
            }
            buffer = new byte[fileSize];
            console.append(Main.getTime() + "Reading...\n");
            int currentBytes;
            long start = System.currentTimeMillis();
            while (fileSize != totalBytes) {
                if (totalBytes + chunkSize > fileSize) {
                    chunkSize = fileSize - totalBytes;
                }
                currentBytes = is.read(buffer, totalBytes, chunkSize);
                totalBytes += currentBytes;
            }
            long end = System.currentTimeMillis();
            long time = end-start;
            printProgress(1,1);
            console.append(Main.getTime() + "Transfer finished");
            console.append("\nTime taken: " + time + " ms");
            console.append("\nAverage speed: " + (int)(((fileSize/1048576) / (time / 1000D))*100)/100D + " MB/s");

            if (fileName != null) {
                path = path + '\\' + fileName;
            } else {
                path = path + "\\unknown.unk";
            }

            if (UtilityFunctions.createAndWriteByteArrayToFile(path, buffer)) {
                console.append(Main.getTime() + "File successfully created at\n" + path);
            } else {
                console.append(Main.getTime() + "Unable to create file at " + path);
            }
            return null;
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return null;
    }

    @Override
    public void done(){
        if(IS_CONSOLE_UPDATED) {
            running = false;
            printerThread.stop();
            printProgress(1,1);
        }
    }
    private void printProgress(int totalBytes,int fileSize){
        int progressPercentage = (int) Math.round((double) totalBytes / fileSize * 100);
        monitor.setText(progressPercentage + "%");
    }

}