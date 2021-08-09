package transfer_program;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class UtilityFunctions {

    protected static boolean createAndWriteByteArrayToFile(String filePath,byte [] bytes){
        File file = new File(filePath);
        try {
            if(file.createNewFile()){
                Files.write(Paths.get(filePath),bytes);
            }
        } catch (IOException ioException) {
            return false;
        }
        return true;
    }
    protected static boolean validateIPv4(String str){
        str=str.replace(" ","");
        for(int i = 0;i<str.length();i++){
            if(!(Character.isDigit(str.charAt(i)) || str.charAt(i)=='.')){
                return false;
            }
        }
        return true;
    }
    protected static boolean validateIPv6(String str){
        str=str.replace(" ","");
        int count = 0;
        for(int i = 0;i<str.length();i++){
            if(str.charAt(i)==':') count++;
        }
        return count>3;
    }
    protected static boolean isPortInUse(int port){
        ServerSocket testServer;
        try {
            testServer = new ServerSocket(port);
        } catch (IOException ioException ) {
            return true;
        }
        try {
            testServer.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return false;
    }
    protected static String getMegaBytes(int bytes){
        return String.valueOf(((int)((double)bytes/1048576 *100))/100D);
    }

}