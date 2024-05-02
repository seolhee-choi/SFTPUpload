package com.nsc;

import com.jcraft.jsch.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.jcr2dav.Jcr2davRepositoryFactory;

import java.io.*;

public class SFTPClient implements RemoteStorage {
    Session session;
    ChannelSftp channelSftp;

    public SFTPClient(String host, String username, String password) throws JSchException {
        this(host,22,username,password);
    }
    public SFTPClient(String host, int port, String username, String password) throws JSchException {
        JSch jsch = new JSch();
        session = jsch.getSession(username, host, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();

//        java.util.Properties config = new java.util.Properties();
//        config.put("StrictHostKeyChecking", "no");
//        session.setConfig(config);
    }

    public void upload(String localFilePath, String destPath) throws Exception {
//        File localFile = new File(localFilePath);
        File localFile = FileUtils.getFile(localFilePath);
        upload(localFile, destPath);
    }
    public void upload(File localFile, String destPath) throws Exception {
//        InputStream ipStream = new FileInputStream(localFile);
//        FileUtils.openInputStream(localFile)//FileInputStream
        try(InputStream ipStream =  FileUtils.openInputStream(localFile)) {
            upload(ipStream, destPath);
        }

    }

    //로컬파일을 원격 디렉토리로 업로드
    public void upload(InputStream srcInputStream, String destPath) {
//        File f = new File(srcInputStream);
        // 디렉토리가 없는 경우, 부모 디렉토리 생성
        try{
            if (!isExist(destPath)) {
                mkdirs(getParentDirectoryPath(destPath));
            }
            channelSftp.put(srcInputStream, destPath);
            System.out.println("File uploaded successfully from - " + srcInputStream);
            System.out.println("File uploaded successfully to - " + destPath);
        } catch (SftpException e) {
            System.out.println("File uploaded failed to - " + destPath + e.getMessage());
            e.printStackTrace();
        }
    }

    //원격에 해당 디렉토리가 없으면 생성하는 함수
    public void mkdirs(String destPath) throws SftpException {
        // 디렉토리 생성을 위해 경로를 분할
        String[] directories = destPath.split("/");
        String currentPath = "";
        for (String directory : directories) {
            if (!directory.isEmpty()) {
                currentPath += "/" + directory;
                try {
                    channelSftp.cd(currentPath);
                } catch (SftpException e) {
                    channelSftp.mkdir(currentPath);
                    channelSftp.cd(currentPath);
                }
            }
        }
    }

    //원격경로의 부모 디렉토리 경로 추출 함수
    private String getParentDirectoryPath(String destPath) {
        //destPath의 마지막으로 나타나는 슬래시의 인덱스 찾기(aaa.txt전까지)
        int lastSlashIndex = destPath.lastIndexOf('/');
        //lastSlashIndex가 0보다크면(destPath가 /로 시작하지않으면)
        //substring을 통해 첫 번째 문자부터 마지막 슬래시 전까지의 부분 문자열을 반환함
        return lastSlashIndex > 0 ? destPath.substring(0, lastSlashIndex) : "";
    }

    //원격에 해당 디렉토리 있는지 확인 여부 함수
    public boolean isExist (String destPath) {
        try {
            channelSftp.lstat(destPath);
            return true;
        } catch (SftpException e) {
            return false;
        }
    }

    @Override
    public void close() throws IOException {
        channelSftp.disconnect();
        session.disconnect();
    }
}
