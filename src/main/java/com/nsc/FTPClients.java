package com.nsc;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FTPClients implements RemoteStorage {
    FTPClient client = new FTPClient();

    //접속
    public FTPClients(String server, String username, String password) throws IOException {
        this(server, 21, username, password);
    }
    public FTPClients(String server, int port, String username, String password) throws IOException {
        // 프로토콜 명령 수신시 로그를 출력하기 위해 PrintCommandListener 추가
        client.addProtocolCommandListener(new PrintCommandListener(System.out, true));

        client.connect(server, port);
        client.login(username, password);
    }

    @Override
    public void upload(InputStream srcInputStream, String destPath){
        File destFile = new File(destPath);
        try {
            mkdirs(destPath);

            // 파일을 원격 서버에 업로드
            boolean success = client.storeFile(destFile.getName(), srcInputStream);

            if (success) {
                System.out.println("File uploaded successfully to " + destPath);
            } else {
                System.out.println("Failed to upload file to " + destPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void mkdirs(String destPath) throws IOException {
        File destFile = new File(destPath);
        String uploadPath = destFile.getParent().replace("\\","/");

        // 원격 경로로 이동하여 해당 디렉토리가 없으면 생성
        for(String path : uploadPath.split("/")) {
            //path가 빈 문자열이거나, path로 경로를 변경했을 때
            if("".equals(path) || client.changeWorkingDirectory(path))
                continue;
            if(!client.makeDirectory(path))//path로 makeDirectory 실패하면
                System.out.println("FTP서버 upload directory생성 실패 - " + path);
            client.changeWorkingDirectory(path);
        }
    }



    @Override
    public void close() throws IOException {
        client.logout();
        client.disconnect();
    }
}
