package transfer_program;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
/**
 * #1.Send/Receive file name with ext
 * #2.Send/Receive file size in bytes
 * #3.Write/Read [stream] bytes
 */
class Main {
    protected static final String OS = System.getProperty("os.name");
    protected final Dimension SCREEN_DIMENSIONS = Toolkit.getDefaultToolkit().getScreenSize();
    protected final int FRAME_WIDTH = 700;
    protected final int FRAME_HEIGHT = 700;
    private final int OFFSET = 18;
    protected final int TIMEOUT = 4000;

    protected JFrame frame;
    protected JTextArea consoleArea;
    protected JTextField monitor;
    protected JButton createButton;
    protected JButton connectButton;
    static ArrayList<Component> listOfComponents;

    protected Socket clientSocket;
    protected ServerSocket serverSocket;
    volatile protected static Socket acceptedSocket;

    public static void main(String[] args) {
        Main program = new Main();
        program.createFrame();
        program.addComponents();
        program.initConsoleLog();
        program.frame.setVisible(true);
        program.addSecondStageComponents();


    }
    protected static void revealComponents(){
        for(Component component : listOfComponents){
            component.setVisible(true);
        }
    }

    private void createFrame() {
        frame = new JFrame();
        frame.setLayout(null);
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Transfer");
        frame.setLocation((int) (SCREEN_DIMENSIONS.getWidth() - FRAME_WIDTH - OFFSET), OFFSET);
        frame.getContentPane().setBackground(Color.gray);
    }

    private void addComponents() {
        addLabel("Connect to server:", 3, 3);
        addLabel("Gateway/IPv4:", 10, 43);
        addLabel("0 < Port < 65536:", 10, 83);
        addLabel("Console logs", 370, 5);
        addLabel("Server IP:", 10, 150);

        addLabel("Create server:", 3, 120);
        addLabel("Port [0 = random]:", 10, 200);

        JPanel separatorPanel1 = new JPanel();
        separatorPanel1.setBounds(0,115,320,3);
        separatorPanel1.setBackground(new Color(23,152,199));
        frame.add(separatorPanel1);

        JPanel separatorPanel2 = new JPanel();
        separatorPanel2.setBounds(0,225,320,4);
        separatorPanel2.setBackground(new Color(23,152,199));
        frame.add(separatorPanel2);

        JPanel separatorPanel3 = new JPanel();
        separatorPanel3.setBounds(0,470,420,5);
        separatorPanel3.setBackground(new Color(23,152,199));
        frame.add(separatorPanel3);

        JCheckBox bindServerBox = new JCheckBox("bind to ip");
        bindServerBox.setSelected(false);
        bindServerBox.setBackground(Color.black);
        bindServerBox.setForeground(Color.white);
        bindServerBox.setBounds(5,176,(int)bindServerBox.getPreferredSize().getWidth(),(int)bindServerBox.getPreferredSize().getHeight());
        frame.add(bindServerBox);

        JTextField ipFieldToConnectTo = new JTextField("10.0.0.x");
        ipFieldToConnectTo.setForeground(new Color(216, 216, 210));
        ipFieldToConnectTo.setBackground(Color.black);
        ipFieldToConnectTo.setCaretColor(Color.green);
        ipFieldToConnectTo.setFont(new Font("Arial", Font.BOLD, 20));
        ipFieldToConnectTo.setBounds(150, 40, 160, (int) ipFieldToConnectTo.getPreferredSize().getHeight());
        frame.add(ipFieldToConnectTo);

        JTextField ipFieldServer = new JTextField("x.x.x.x");
        ipFieldServer.setForeground(new Color(216, 216, 210));
        ipFieldServer.setBackground(Color.black);
        ipFieldServer.setCaretColor(Color.green);
        ipFieldServer.setFont(new Font("Arial", Font.BOLD, 20));
        ipFieldServer.setBounds(120, 147, 150, (int) ipFieldServer.getPreferredSize().getHeight());
        frame.add(ipFieldServer);

        JTextField portFieldTop = new JTextField("port");
        portFieldTop.setHorizontalAlignment(JTextField.CENTER);
        portFieldTop.setForeground(new Color(216, 216, 210));
        portFieldTop.setBackground(Color.black);
        portFieldTop.setCaretColor(Color.green);
        portFieldTop.setFont(new Font("Arial", Font.BOLD, 20));
        portFieldTop.setBounds(175, 80, 100, (int) portFieldTop.getPreferredSize().getHeight());
        frame.add(portFieldTop);

        JTextField portFieldBot = new JTextField("0");
        portFieldBot.setHorizontalAlignment(JTextField.CENTER);
        portFieldBot.setForeground(new Color(216, 216, 210));
        portFieldBot.setBackground(Color.black);
        portFieldBot.setCaretColor(Color.green);
        portFieldBot.setFont(new Font("Arial", Font.BOLD, 20));
        portFieldBot.setBounds(200, 195, 90, (int) portFieldBot.getPreferredSize().getHeight());
        frame.add(portFieldBot);

        connectButton = new JButton("Connect");
        connectButton.setFocusable(false);
        connectButton.setFont(new Font("Arial", Font.BOLD, 20));
        connectButton.setBounds(185, 1, connectButton.getPreferredSize().width, connectButton.getPreferredSize().height);

        connectButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent pressed) {
                String strIP = ipFieldToConnectTo.getText();
                if (UtilityFunctions.validateIPv4(strIP) || UtilityFunctions.validateIPv6(strIP) || strIP.equals("localhost")) {
                    consoleArea.append("\nSuccessful IP validation");
                    String portStr = portFieldTop.getText();
                    if (portStr.matches("[0-9]+") && Long.parseLong(portStr) < 65536) {
                        consoleArea.append("\nSuccessful port validation" + getTime()+"Connecting...");
                        try {
                            clientSocket = new Socket();
                            //SocketAddress endpoint
                            int portTop = Integer.parseInt(portFieldTop.getText());
                            clientSocket.connect(new InetSocketAddress(strIP, portTop), TIMEOUT);
                        } catch (Exception anyExc) {
                            consoleArea.append(getTime()+"Failed to connect\n--------------");
                            anyExc.printStackTrace();
                            return;
                        }
                        revealComponents();
                        connectButton.setEnabled(false);
                        createButton.setEnabled(false);
                        consoleArea.append(getTime()+"Connected!");

                    } else {
                        consoleArea.append("\nInvalid port");
                    }

                } else {
                    consoleArea.append("\nInvalid IP");
                }

            }
        });
        frame.add(connectButton);

        createButton = new JButton("Create server");
        createButton.setFocusable(false);
        createButton.setFont(new Font("Arial", Font.BOLD, 20));
        createButton.setBounds(155, 117, createButton.getPreferredSize().width, 30);
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String portStr = portFieldBot.getText();
                if(portStr.equals("0")){
                    if(createServer(0,bindServerBox.isSelected(),ipFieldServer.getText())){
                        disableButtonsAndAwaitClient();
                    }
                }
                else if (portStr.matches("[0-9]+")){
                    int port=0;
                    try {
                        port = Integer.parseInt(portStr);
                    }catch(NumberFormatException numExc){
                        consoleArea.append(getTime() +"Port too large");
                    }
                    if(port<65536){
                        if(!UtilityFunctions.isPortInUse(port)){
                            if(createServer(port,bindServerBox.isSelected(),ipFieldServer.getText())){
                                disableButtonsAndAwaitClient();
                            }
                        }
                        else{
                            consoleArea.append(getTime() +"Port in use");
                        }
                    }
                    else{
                        consoleArea.append(getTime() +"Port number too large");
                    }

                }else{
                    consoleArea.append(getTime() +"Invalid port");
                }
            }

            private void disableButtonsAndAwaitClient() {
                connectButton.setEnabled(false);
                createButton.setEnabled(false);
                AcceptConnection connect = new AcceptConnection(serverSocket,consoleArea);
                connect.execute();
            }

        });
        frame.add(createButton);

    }

    private void addSecondStageComponents() {
        //listOfComponents.add(frame.add())
        final String defaultPath = System.getProperty("user.home");
        listOfComponents = new ArrayList<>();

        listOfComponents.add(frame.add(addAndReturnInvisibleLabel("Output directory",5,235)));
        listOfComponents.add(frame.add(addAndReturnInvisibleLabel("Buffer chunk [bytes]:",5,355)));
        listOfComponents.add(frame.add(addAndReturnInvisibleLabel("0 <",5,385)));
        listOfComponents.add(frame.add(addAndReturnInvisibleLabel("< 65536",130,385)));
        listOfComponents.add(frame.add(addAndReturnInvisibleLabel("Send file",10,480)));
        listOfComponents.add(frame.add(addAndReturnInvisibleLabel("Buffer chunk [bytes]:",5,555)));
        listOfComponents.add(frame.add(addAndReturnInvisibleLabel("0 <",5,585)));
        listOfComponents.add(frame.add(addAndReturnInvisibleLabel("< 65536",125,585)));

        JTextField outputPathField = new JTextField(defaultPath);
        outputPathField.setVisible(false);
        outputPathField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String outputPath = outputPathField.getText();
                try {
                    if (new File(outputPath).isDirectory()) {
                        consoleArea.append(getTime() + "Output Directory set to: \n" + outputPath);
                    }
                }catch(NullPointerException ignored){}
            }
        });
        outputPathField.setForeground(new Color(216, 216, 210));
        outputPathField.setBackground(Color.black);
        outputPathField.setCaretColor(Color.green);
        outputPathField.setFont(new Font("Arial", Font.BOLD, 20));
        outputPathField.setBounds(5,275,310,30);
        listOfComponents.add(frame.add(outputPathField));

        JTextField filePathField = new JTextField(defaultPath + "\\file.x");
        filePathField.setVisible(false);
        filePathField.setForeground(new Color(216, 216, 210));
        filePathField.setBackground(Color.black);
        filePathField.setCaretColor(Color.green);
        filePathField.setVisible(false);
        filePathField.setFont(new Font("Arial", Font.BOLD, 20));
        filePathField.setBounds(5,515,410,(int)filePathField.getPreferredSize().getHeight());
        listOfComponents.add(frame.add(filePathField));

        JCheckBox receiveFileInfoBox = new JCheckBox("receive file info");
        receiveFileInfoBox.setVisible(false);
        receiveFileInfoBox.setSelected(true);
        receiveFileInfoBox.setBackground(Color.black);
        receiveFileInfoBox.setForeground(Color.white);
        receiveFileInfoBox.setBounds(5,310,(int)receiveFileInfoBox.getPreferredSize().getWidth(),(int)receiveFileInfoBox.getPreferredSize().getHeight());
        listOfComponents.add(frame.add(receiveFileInfoBox));

        JCheckBox updateConsoleBox = new JCheckBox("update console(slower)");
        updateConsoleBox.setVisible(false);
        updateConsoleBox.setSelected(true);
        updateConsoleBox.setBackground(Color.black);
        updateConsoleBox.setForeground(Color.white);
        updateConsoleBox.setBounds(510,5,(int)updateConsoleBox.getPreferredSize().getWidth(),(int)updateConsoleBox.getPreferredSize().getHeight());
        listOfComponents.add(frame.add(updateConsoleBox));

        JCheckBox sendFileInfoBox = new JCheckBox("send file info");
        sendFileInfoBox.setVisible(false);
        sendFileInfoBox.setSelected(true);
        sendFileInfoBox.setBackground(Color.black);
        sendFileInfoBox.setForeground(Color.white);
        sendFileInfoBox.setBounds(250,550,(int)sendFileInfoBox.getPreferredSize().getWidth(),(int)sendFileInfoBox.getPreferredSize().getHeight());
        listOfComponents.add(frame.add(sendFileInfoBox));

        JTextField bufferChunkFieldTop = new JTextField("65000");
        bufferChunkFieldTop.setVisible(false);
        bufferChunkFieldTop.setHorizontalAlignment(JTextField.CENTER);
        bufferChunkFieldTop.setForeground(new Color(216, 216, 210));
        bufferChunkFieldTop.setBackground(Color.black);
        bufferChunkFieldTop.setCaretColor(Color.green);
        bufferChunkFieldTop.setFont(new Font("Arial", Font.BOLD, 20));
        bufferChunkFieldTop.setBounds(45,382,70,30);
        listOfComponents.add(frame.add(bufferChunkFieldTop));

        JTextField bufferChunkFieldBot = new JTextField("65000");
        bufferChunkFieldBot.setVisible(false);
        bufferChunkFieldBot.setHorizontalAlignment(JTextField.CENTER);
        bufferChunkFieldBot.setForeground(new Color(216, 216, 210));
        bufferChunkFieldBot.setBackground(Color.black);
        bufferChunkFieldBot.setCaretColor(Color.green);
        bufferChunkFieldBot.setFont(new Font("Arial", Font.BOLD, 20));
        bufferChunkFieldBot.setBounds(45,582,70,30);
        listOfComponents.add(frame.add(bufferChunkFieldBot));

        JFileChooser directoryChooser = new JFileChooser();
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        directoryChooser.setPreferredSize(new Dimension(700,500));
        //starts with detailed view selected
        Action detailsAction = directoryChooser.getActionMap().get("viewTypeDetails");
        detailsAction.actionPerformed(null);

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setPreferredSize(new Dimension(700,500));
        //starts with detailed view selected
        detailsAction = fileChooser.getActionMap().get("viewTypeDetails");
        detailsAction.actionPerformed(null);

        JButton selectDirectoryButton = new JButton("Select in UI");
        selectDirectoryButton.setVisible(false);
        selectDirectoryButton.setFocusable(false);
        selectDirectoryButton.setFont(new Font("Arial", Font.BOLD, 20));
        selectDirectoryButton.setBounds(170, 230, selectDirectoryButton.getPreferredSize().width, selectDirectoryButton.getPreferredSize().height);
        selectDirectoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = directoryChooser.showOpenDialog(frame);
                if(result == JFileChooser.APPROVE_OPTION){
                    String chosenPath = directoryChooser.getSelectedFile().toString();
                    consoleArea.append(getTime()+"Output Directory set to: \n" + chosenPath);
                    outputPathField.setText(chosenPath);
                }
            }
        });
        listOfComponents.add(frame.add(selectDirectoryButton));

        JButton selectFileButton = new JButton("Select in UI");
        selectFileButton.setVisible(false);
        selectFileButton.setFocusable(false);
        selectFileButton.setFont(new Font("Arial", Font.BOLD, 20));
        selectFileButton.setBounds(170, 476, selectFileButton.getPreferredSize().width, selectFileButton.getPreferredSize().height);
        selectFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = fileChooser.showOpenDialog(frame);
                if(result == JFileChooser.APPROVE_OPTION){
                    String chosenPath = fileChooser.getSelectedFile().toString();
                    consoleArea.append(getTime()+"File to transfer: \n" + chosenPath);
                    filePathField.setText(chosenPath);
                }
            }
        });
        listOfComponents.add(frame.add(selectFileButton));

        JButton receiveTransferButton = new JButton("Receive transfer");
        receiveTransferButton.setVisible(false);
        receiveTransferButton.setFocusable(false);
        receiveTransferButton.setFont(new Font("Arial", Font.BOLD, 20));
        receiveTransferButton.setBounds(15,430,(int )receiveTransferButton.getPreferredSize().getWidth(),(int)receiveTransferButton.getPreferredSize().getHeight());

        receiveTransferButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String bufferChunkStr = bufferChunkFieldTop.getText();
                if(acceptedSocket!=null || clientSocket!=null){
                    if (bufferChunkStr.matches("[0-9]+") && bufferChunkStr.length() > 0 && bufferChunkStr.length() < 6) {
                        int bufferChunkSize = Integer.parseInt(bufferChunkStr);
                        //byte arr max size is 2147483647
                        if (bufferChunkSize < 65536) {
                            String path = outputPathField.getText();
                            if (new File(path).isDirectory()) {
                                DataInputStream inputStream;
                                try {
                                    //if you're server
                                    if (serverSocket != null && !acceptedSocket.isInputShutdown() && !acceptedSocket.isClosed()) {
                                        inputStream = new DataInputStream(acceptedSocket.getInputStream());
                                    }
                                    //if you're client
                                    else if(clientSocket != null && !clientSocket.isInputShutdown()){
                                        inputStream = new DataInputStream(clientSocket.getInputStream());
                                    }
                                    else{
                                        consoleArea.append("\nCouldn't get input stream");
                                        return;
                                    }

                                } catch (Exception exc) {
                                    consoleArea.append("\nError getting input stream");
                                    return;
                                }

                                ReceiveFile receiver = new ReceiveFile(inputStream, path, consoleArea, monitor, bufferChunkSize, receiveFileInfoBox.isSelected(), updateConsoleBox.isSelected());
                                receiver.execute();

                            } else {
                                consoleArea.append("\nInvalid directory");
                            }
                        }
                    } else {
                        consoleArea.append("\nInvalid buffer settings");
                    }
                }else{
                    consoleArea.append("\nNot connected");
                }
            }
        });
        listOfComponents.add(frame.add(receiveTransferButton));

        JButton initTransferButton = new JButton("Transfer file");
        initTransferButton.setVisible(false);
        initTransferButton.setFocusable(false);
        initTransferButton.setFont(new Font("Arial", Font.BOLD, 20));
        initTransferButton.setBounds(15,620,(int )initTransferButton.getPreferredSize().getWidth(),(int)initTransferButton.getPreferredSize().getHeight());
        initTransferButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String bufferChunkStr = bufferChunkFieldBot.getText();
                if (acceptedSocket!=null || clientSocket!=null) {
                    if (bufferChunkStr.matches("[0-9]+") && bufferChunkStr.length() > 0 && bufferChunkStr.length() < 6) {
                        int chunkSize = Integer.parseInt(bufferChunkStr);
                        if (chunkSize < 65536) {
                            String path = filePathField.getText();
                            if (new File(path).isFile()) {
                                long fileSize = 0;
                                try {
                                    fileSize = Files.size(Paths.get(path));
                                } catch (IOException ignore) {}

                                if (fileSize > 2147483647) {
                                    consoleArea.append(getTime() + "File too large");
                                    return;
                                }
                                consoleArea.append("\nFile size: " + UtilityFunctions.getMegaBytes((int) fileSize) + " MBs");
                                consoleArea.append("\nin bytes: " + fileSize);
                                DataOutputStream outputStream = null;
                                try {
                                    //you're client
                                    if (clientSocket != null && clientSocket.isBound() && clientSocket.isConnected() && !clientSocket.isOutputShutdown()) {
                                        outputStream = new DataOutputStream(clientSocket.getOutputStream());
                                    }
                                    //you're server
                                    else if(serverSocket != null && serverSocket.isBound() && !acceptedSocket.isOutputShutdown()){
                                        outputStream = new DataOutputStream(acceptedSocket.getOutputStream());
                                    }
                                    else{
                                        consoleArea.append("\nCouldn't get output stream for transfer");
                                        return;
                                    }
                                }catch(IOException ioException) {
                                    consoleArea.append("\nError at output stream");
                                }
                                if (outputStream==null){
                                    return;
                                }
                                try{
                                    //stream file name and ext
                                    if (sendFileInfoBox.isSelected()) {
                                        consoleArea.append(getTime() + "Sending file info");
                                        outputStream.writeUTF(path.substring(path.lastIndexOf('\\') + 1));
                                        outputStream.writeInt((int) fileSize);
                                        outputStream.flush();
                                    }

                                    TransferFile transfer = new TransferFile(outputStream, consoleArea, monitor, updateConsoleBox.isSelected(), path, chunkSize, (int) fileSize);
                                    transfer.execute();

                                } catch (IOException ioException) {
                                    consoleArea.append(getTime() + "Error at writer");
                                }
                            } else {
                                consoleArea.append("\nInvalid file path");
                            }
                        } else {
                            consoleArea.append("\nBuffer chunk too large");
                        }
                    } else {
                        consoleArea.append("\nInvalid buffer");
                    }
                }else{
                    consoleArea.append("\nNot connected");
                }
            }
        });
        listOfComponents.add(frame.add(initTransferButton));
    }

    private boolean createServer(int port, boolean toBind, String IP) {
        consoleArea.append(getTime()+"Creating server...");
        try {
            if(!toBind) {
                serverSocket = new ServerSocket(port);
            }
            else{
                serverSocket = new ServerSocket(port,2,InetAddress.getByName(IP));
            }
        } catch (IOException ioException) {
            consoleArea.append(getTime()+"Error");
            return false;
        }
        consoleArea.append(getTime()+"\nServer made at port: " + serverSocket.getLocalPort());
        if(OS.startsWith("Windows")){
            consoleArea.append("\nServer address: " + retrieveAddress());
        }
        return true;
    }

    private String retrieveAddress() {
        final Runtime run = Runtime.getRuntime();
        final Process proc;
        try {
            proc = run.exec("cmd /c ipconfig");
        } catch (IOException ioException) {
            consoleArea.append("\nError");
            return null;
        }
        BufferedReader procOutputReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String line;
        try {
            while((line = procOutputReader.readLine())!=null){
                int index;
                if((index = line.indexOf("IPv4"))!=-1){
                    return line.substring(index, index+4) +": "+ line.substring(27);
                }
            }
        }catch (IOException ioException) {
            consoleArea.append("\nError");
            return null;
        }
        return null;
    }

    protected static String getTime(){
        Timestamp timestamp = new Timestamp(new Date().getTime());
        return "\n[" + String.valueOf(timestamp).substring(11,19) + "] ";
    }

    private void initConsoleLog() {
        consoleArea = new JTextArea();
        consoleArea.setLineWrap(true);
        consoleArea.setForeground(Color.green);
        consoleArea.setBackground(Color.black);
        consoleArea.setFont(new Font("Arial", Font.ITALIC, 18));
        consoleArea.setCaretColor(new Color(216, 216, 210));
        frame.add(consoleArea);

        JScrollPane verticalScroll = new JScrollPane(consoleArea);
        verticalScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        verticalScroll.setBounds(325, 35, 355, 350);
        frame.add(verticalScroll);

        monitor = new JTextField();
        monitor.setHorizontalAlignment(JTextField.CENTER);
        monitor.setForeground(new Color(216, 216, 210));
        monitor.setBackground(Color.black);
        monitor.setEditable(false);
        monitor.setFont(new Font("Arial", Font.BOLD, 20));
        monitor.setBounds(326,386,70,30);
        frame.add(monitor);
    }

    private void addLabel(String text, int x, int y) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        label.setBounds(x, y, label.getPreferredSize().width, label.getPreferredSize().height);
        frame.add(label);
    }
    private JLabel addAndReturnInvisibleLabel(String text, int x, int y) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        label.setBounds(x, y, label.getPreferredSize().width, label.getPreferredSize().height);
        label.setVisible(false);
        frame.add(label);
        return label;
    }
}