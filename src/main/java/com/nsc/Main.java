package com.nsc;

import com.jcraft.jsch.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.jcr2dav.Jcr2davRepositoryFactory;

import javax.jcr.*;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws Exception {
        //username,port,password를 입력받기(실행할때 arguments받아서 실행)
        Jcr2davRepositoryFactory repoFactory = new Jcr2davRepositoryFactory();

        Map<String, String> params = new HashMap<>();
        params.put("org.apache.jackrabbit.repository.uri", "자산 가져올 URL");
        Repository repository = repoFactory.getRepository(params);
        Session session = repository.login(new SimpleCredentials("mig-test", "password".toCharArray()));


        //자산 조회
        QueryManager qm = session.getWorkspace().getQueryManager();

        String stmt = "select * from [dam:Asset] where isdescendantnode('/content/dam/01-tv-av')";
        Query q = qm.createQuery(stmt, Query.JCR_SQL2);

        NodeIterator nodeIterator = q.execute().getNodes();

        //조회된 자산을 로컬로 다운 -> nas로 다운받게끔
        try(SFTPClient client = new SFTPClient("원격 포트번호","원격 사용자이름","원격 비밀번호")) {
            while (nodeIterator.hasNext()) {
                Node node = nodeIterator.nextNode();
                System.out.println("=====================================");
                System.out.println("Asset Path:" + node.getPath());
                System.out.println("=====================================");
                Node jcrContent = node.getNode("jcr:content/renditions/original/jcr:content");
                InputStream is = jcrContent.getProperty(org.apache.jackrabbit.JcrConstants.JCR_DATA).getBinary().getStream();

                String destPath = "/lg-dam/GMC/";
                client.upload(is, destPath + node.getPath());
            }
        }

    }
}