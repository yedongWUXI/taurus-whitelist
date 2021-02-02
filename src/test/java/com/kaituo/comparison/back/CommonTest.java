package com.kaituo.comparison.back;


import com.jcraft.jsch.ChannelSftp;
import com.kaituo.comparison.back.core.whitelist.SSHClient;
import com.kaituo.comparison.back.core.whitelist.SftpUtils;
import com.kaituo.comparison.back.core.whitelist.SshConfig;
import com.kaituo.comparison.back.core.whitelist.WhiteConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 *
 */
@Slf4j
public class CommonTest {

    static final String fileName = "sqlnet.ora";
    static final String localDir = "C:\\Users\\yedong\\Desktop\\test\\download\\";



    /**
     * 读取 sqlnet.ora 文件 白名单
     *
     * @throws IOException
     */
    @Test
    public void test3() throws IOException {

        WhiteConfig whiteConfig = new WhiteConfig();


        File file = new File("C:\\Users\\yedong\\Desktop\\test\\sqlnet.ora");


        InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String s = line.replaceAll("\\s", "");
            if (s.startsWith("#")) {
                continue;
            }

            if (s.contains("=")) {
                String substring = s.substring(0, s.indexOf("="));
                System.out.println(substring);


                if (substring.equals("TCP.VALIDNODE_CHECKING")) {
                    String value = s.substring(s.indexOf("=") + 1, s.length());
                    if (value.contains("#")) {
                        value = value.substring(0, value.indexOf("#"));
                    }
                    System.out.println(value);
                    whiteConfig.setTCP_VALIDNODE_CHECKING(value);
                }


                if (substring.equals("TCP.INVITED_NODES")) {
                    String value = s.substring(s.indexOf("=") + 1, s.length()).replace("(", "").replace(")", "");
                    if (value.contains("#")) {
                        value = value.substring(0, value.indexOf("#")).replace("(", "").replace(")", "");
                    }
                    System.out.println(value);
                    whiteConfig.setTCP_INVITED_NODES(value);


                }

            }


        }

        if (!Objects.isNull(whiteConfig)) {
            System.out.println(whiteConfig);

        }
    }


    /**
     * linux服务器 执行命令
     */
    @Test
    public void ssh() {
        SshConfig sshConfig = new SshConfig();
        sshConfig.setHost("192.168.50.220");
        sshConfig.setUsername("root");
        sshConfig.setPassword("Root1234");
        sshConfig.setPort("22");


        SSHClient ssh = new SSHClient(sshConfig.getHost(), sshConfig.getUsername(), sshConfig.getPassword(), sshConfig.getPort());
        List<String> cmdsToExecute = new ArrayList();
        cmdsToExecute.add("lsnrctl reload");
        cmdsToExecute.add("exit 1");
        ssh.execute(cmdsToExecute);
        log.info("ssh 执行成功");


    }


    /**
     * 下载服务器文件 到本地
     */
    @Test
    public void downLoadFile() {
        SshConfig sshConfig = new SshConfig();
        sshConfig.setHost("192.168.50.220");
        sshConfig.setUsername("root");
        sshConfig.setPassword("Root1234");
        sshConfig.setPort("22");

        //sftp 文件名
        String downLoadFileName = "sqlnet.ora";


        try {
            //连接stfp
            ChannelSftp sftp = SftpUtils.login(sshConfig.getUsername(), sshConfig.getPassword(), sshConfig.getHost(), sshConfig.getPort());

            sftp.cd("/u01/app/oracle/product/11.2.0/dbhome_1/network/admin");

            log.info(sftp.pwd());

            //创建文件
            File file = new File("C:\\Users\\yedong\\Desktop\\test\\download\\sqlnet.ora");
            //文件存在就删除
            if (file.exists()) {
                file.delete();
            }
            //创建新文件
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            sftp.get(downLoadFileName, fileOutputStream);
            //关闭流
            fileOutputStream.close();
            //退出sftp
            SftpUtils.logout();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    /**
     * 上传文件到 linux服务器
     */
    @Test
    public void uploadFile() {

        SshConfig sshConfig = new SshConfig();
        sshConfig.setHost("192.168.50.220");
        sshConfig.setUsername("root");
        sshConfig.setPassword("Root1234");
        sshConfig.setPort("22");
        sshConfig.setDir("/u01/app/oracle/product/11.2.0/dbhome_1/network/admin");


        try {
            //连接stfp
            ChannelSftp sftp = SftpUtils.login(sshConfig.getUsername(), sshConfig.getPassword(), sshConfig.getHost(), sshConfig.getPort());

            sftp.cd(sshConfig.getDir());

            log.info(sftp.pwd());

            File file = new File(localDir + fileName);


            FileInputStream fileOutputStream = new FileInputStream(file);
            sftp.put( fileOutputStream,fileName);

            //关闭流
            fileOutputStream.close();
            //退出sftp
            SftpUtils.logout();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    /**
     * 读取文件内容到内存  修改内存内容  最后写入文件
     */
    @Test
    public void test2() {

        File file = new File(localDir + fileName);
        InputStreamReader reader;

        try {
            reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
            StringBuilder sb = new StringBuilder();

            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String s = line.replaceAll("\\s", "");

                if (s.contains("=")) {
                    String substring = s.substring(0, s.indexOf("="));


                    if (substring.equals("TCP.INVITED_NODES") && !s.startsWith("#")) {
                        sb.append("test");
                        sb.append("\n");

                    }else {
                        sb.append(line);
                        sb.append("\n");
                    }


                }else {
                    sb.append(line);
                    sb.append("\n");
                }
            }

            System.out.println(sb.toString());


            FileUtils.writeStringToFile(file,sb.toString(),"utf8");


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }





    }

//    /**
//     * 替换新字符
//     *
//     * @param fileName
//     * @param oldstr
//     * @param newStr
//     * @return
//     */
//
//    public Boolean updateStartBat(String fileName, String oldstr, String newStr) {
//        RandomAccessFile raf = null;
//        try {
//            raf = new RandomAccessFile(fileName, "rw");
//            String line = null;
//            long lastPoint = 0; //记住上一次的偏移量
//            while ((line = raf.readLine()) != null) {
//                final long ponit = raf.getFilePointer();
//                if (line.contains(oldstr)) {
//                    String str = line.replace(oldstr, newStr);//oldstr 和 newStr的长度必须一样
//                    raf.seek(lastPoint);
//                    raf.writeBytes(str);
//                    lastPoint = ponit;
//                }
//            }
//        } catch (FileNotFoundException e) {
//
//            e.printStackTrace();
//        } catch (IOException e) {
//
//            e.printStackTrace();
//        } finally {
//            try {
//                raf.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return true;
//    }
//
//
//    @Test
//    public void test4() {
//        updateStartBat(localDir + fileName, "zhangyedong", "weichen");
//    }


    @Test
    public void test5() {
        File file = new File(localDir + fileName);
        try {
            String utf8 = FileUtils.readFileToString(file, "utf8");
            System.out.println(utf8);
//            FileUtils.writeStringToFile(file,"xxxxxxxxxxxxx","utf8");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
