package com.kaituo.comparison.back.core.whitelist;

import lombok.Data;

@Data
public class SshConfig {
    //ip
    private String host;
    //端口
    private String port;
    //连接sftp用户名
    private String username;
    //连接sftp密码
    private String password;
    //sqlnet.ora文件位置
    private String dir;
}
