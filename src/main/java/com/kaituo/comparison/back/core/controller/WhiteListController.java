package com.kaituo.comparison.back.core.controller;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.kaituo.comparison.back.common.util.whiteList.SSHClient;
import com.kaituo.comparison.back.common.util.whiteList.SftpUtils;
import com.kaituo.comparison.back.common.util.whiteList.SshConfig;
import com.kaituo.comparison.back.common.util.whiteList.WhiteConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Description:
 * @Author: yedong
 * @Date: 2020/2/13 17:14
 * @Modified by:
 */
@RestController
@Slf4j
@RequestMapping(value = {"/whiteList"})
public class WhiteListController {

    static final String fileName = "sqlnet.ora";
    static final String localDir = "C:\\Users\\yedong\\Desktop\\test\\download\\";


    /**
     * 连接服务器  下载sqlnet.ora到本地  初始化白名单数据库
     *
     * @return
     */
    @GetMapping(value = "/initWhiteList")
    public String initWhiteList() {
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

            //创建文件
            File file = new File(localDir + fileName);
            //文件存在就删除
            if (file.exists()) {
                file.delete();
            }
            //创建新文件
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            sftp.get(fileName, fileOutputStream);

            //关闭流
            fileOutputStream.close();
            //退出sftp
            SftpUtils.logout();
        } catch (Exception e) {
            e.printStackTrace();
        }


        WhiteConfig whiteConfig = new WhiteConfig();


        File file = new File(localDir + fileName);

        try {
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
                    log.info(substring);


                    if (substring.equals("TCP.VALIDNODE_CHECKING")) {
                        String value = s.substring(s.indexOf("=") + 1, s.length());
                        if (value.contains("#")) {
                            value = value.substring(0, value.indexOf("#"));
                        }
                        log.info(value);
                        whiteConfig.setTCP_VALIDNODE_CHECKING(value);
                    }


                    if (substring.equals("TCP.INVITED_NODES")) {
                        String value = s.substring(s.indexOf("=") + 1, s.length()).replace("(", "").replace(")", "");
                        if (value.contains("#")) {
                            value = value.substring(0, value.indexOf("#")).replace("(", "").replace(")", "");
                        }
                        log.info(value);
                        whiteConfig.setTCP_INVITED_NODES(value);


                    }

                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        //TODO


        return whiteConfig.toString();
    }


    /**
     * 修改本地文件 上传本地文件到服务器  最后 lsnrctl reload
     *
     * @param ip
     * @return
     */
    @GetMapping(value = "/reload")
    public String reload(String ip) {


        File file = new File(localDir + fileName);
        InputStreamReader reader;
//
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
                        sb.append("TCP.INVITED_NODES=(" + ip + ")");
                        sb.append("\n");

                    } else {
                        sb.append(line);
                        sb.append("\n");
                    }
                } else {
                    sb.append(line);
                    sb.append("\n");
                }
            }

            System.out.println(sb.toString());


            //修改本地文件
            FileUtils.writeStringToFile(file, sb.toString(), "utf8");


            //上传本地文件到服务器
            SshConfig sshConfig = new SshConfig();
            sshConfig.setHost("192.168.50.220");
            sshConfig.setUsername("root");
            sshConfig.setPassword("Root1234");
            sshConfig.setPort("22");
            sshConfig.setDir("/u01/app/oracle/product/11.2.0/dbhome_1/network/admin");


            //连接stfp
            ChannelSftp sftp = SftpUtils.login(sshConfig.getUsername(), sshConfig.getPassword(), sshConfig.getHost(), sshConfig.getPort());

            sftp.cd(sshConfig.getDir());

            log.info(sftp.pwd());


            FileInputStream fileOutputStream = new FileInputStream(file);
            sftp.put(fileOutputStream, fileName);

            //关闭流
            fileOutputStream.close();
            //退出sftp
            SftpUtils.logout();


            //linux服务器 执行命令
            SSHClient ssh = new SSHClient(sshConfig.getHost(), sshConfig.getUsername(), sshConfig.getPassword(), sshConfig.getPort());
            List<String> cmdsToExecute = new ArrayList();
            cmdsToExecute.add("lsnrctl reload");
            cmdsToExecute.add("exit 1");
            ssh.execute(cmdsToExecute);
            log.info("ssh 执行成功");


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        }


        return "success";
    }


}
