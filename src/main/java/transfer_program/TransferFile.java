package transfer_program;

import javax.swing.*;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

class TransferFile extends SwingWorker<String,Object> {

    private final DataOutputStream os;
    private final JTextArea console;
    private final JTextField monitor;
    private final String PATH;
    private final int CHUNK_SIZE;
    private final int FILE_SIZE;
    private final int DELAY = 30;
    private final boolean IS_CONSOLE_UPDATED;

    private int progress = 0;
    private Thread printerThread;
    volatile private boolean running = true;

    protected TransferFile(DataOutputStream os, JTextArea console, JTextField monitor, boolean IS_CONSOLE_UPDATED, String PATH, int CHUNK_SIZE, int FILE_SIZE){
        this.os=os;
        this.console=console;
        this.monitor = monitor;
        this.PATH = PATH;
        this.CHUNK_SIZE = CHUNK_SIZE;
        this.FILE_SIZE = FILE_SIZE;
        this.IS_CONSOLE_UPDATED = IS_CONSOLE_UPDATED;

        if (this.IS_CONSOLE_UPDATED) {
            printerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (running) {
                        printProgress(progress, FILE_SIZE);
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
    protected String doInBackground() throws Exception {
        console.append(Main.getTime() + "Reading bytes from file");
        byte[] fileInfoArr = Files.readAllBytes(Paths.get(PATH));
        console.append(Main.getTime() + "Uploading ...");
        long start = System.currentTimeMillis();
        while (true) {
            if (progress + CHUNK_SIZE > fileInfoArr.length) {
                os.write(fileInfoArr, progress, fileInfoArr.length - progress);
                break;
            }
            os.write(fileInfoArr, progress, CHUNK_SIZE);
            progress += CHUNK_SIZE;
        }
        long end = System.currentTimeMillis();
        long time = end-start;
        console.append(Main.getTime() + "Finished transfer\n Time taken: " + time + " ms");
        console.append("\nAverage speed: " + (int)(((FILE_SIZE /1048576) / (time / 1000D))*100)/100D + " MB/s");
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