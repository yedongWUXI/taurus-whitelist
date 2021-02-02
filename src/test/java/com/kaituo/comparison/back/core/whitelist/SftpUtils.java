package com.kaituo.comparison.back.core.whitelist;

import com.jcraft.jsch.*;

import java.util.Properties;

/**
 * sftp连接工具类
 */
public class SftpUtils {

    private static ChannelSftp sftp = null;
    private static Session session = null;

    // 登录
    public static ChannelSftp login(String username, String password, String host, String port) throws JSchException, SftpException {
        JSch jSch = new JSch();
        // 设置用户名和主机，端口号一般都是22
        session = jSch.getSession(username, host, Integer.valueOf(port));
        // 设置密码
        session.setPassword(password);
        Properties config = new Properties();
        //严格主机密钥检查
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        //开启sshSession链接
        session.connect();
        //获取sftp通道
        Channel channel = session.openChannel("sftp");
        channel.connect();
        sftp = (ChannelSftp) channel;
        return sftp;
    }

    // 退出登录
    public static void logout() {
        if (sftp != null) {
            if (sftp.isConnected()) {
                sftp.disconnect();
            }
        }
        if (session != null) {
            if (session.isConnected()) {
                session.disconnect();
            }
        }
    }


}
