package com.kaituo.comparison.back.core.whitelist;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import expect4j.Expect4j;
import expect4j.matches.EofMatch;
import expect4j.matches.Match;
import expect4j.matches.RegExpMatch;
import expect4j.matches.TimeoutMatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.oro.text.regex.MalformedPatternException;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;

/**
 * @Author: tanhl
 * @Date: 2019/9/10 15:27
 */
@Slf4j
public class SSHClient {
    private static final int COMMAND_EXECUTION_SUCCESS_OPCODE = -2;
    private static String ENTER_CHARACTER = "\r";
    //    private static final int Integer.valueOf(port) = 22222;
    private List<String> lstCmds = new ArrayList<>();
    //    private static String[] linuxPromptRegEx = new String[]{"~]#", "~#","#", ">",  "\\$", "~$","~]$",":~$", "Password for kaituo_user1@HADOOP.COM:"};
//    private static String[] linuxPromptRegEx = new String[]{"~$", "\\$", "~]$",":~$","Password for kaituo_user1@HADOOP.COM:"};
    private static String[] linuxPromptRegEx = new String[]{"~$", "\\$", "~]$", ":~$", "Password for kaituo_user1@HADOOP.COM:"};
    //    private static String[] linuxPromptRegEx = new String[]{"~]#", "~#", "\\#","#", ">", "$", "\\$", "~$", "/$", "Password for kaituo_user1@HADOOP.COM:"};
    private static final long defaultTimeOut = 2000;

    private Expect4j expect = null;
    private StringBuilder buffer = new StringBuilder();
    private String userName;
    private String password;
    private String host;
    private String port;


    public SSHClient(String host, String userName, String password, String port) {
        this.host = host;
        this.userName = userName;
        this.password = password;
        this.port = port;
    }

    public String execute(List<String> cmdsToExecute) {

        SSH();

        this.lstCmds = cmdsToExecute;

        List<Match> lstPattern = new ArrayList<>();

        String[] regEx = linuxPromptRegEx;
        synchronized (regEx)

        {
            for (String regexElement : regEx) {
                try {
                    Match mat = new RegExpMatch(regexElement, expectState -> {
                        buffer.append(expectState.getBuffer());
//                        expectState.exp_continue();
                    });
                    lstPattern.add(mat);
                } catch (MalformedPatternException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            lstPattern.add(new EofMatch(expectState -> {

            }));
            lstPattern.add(new TimeoutMatch(defaultTimeOut, expectState -> {

            }));
        }


        try {

            if (Objects.isNull(expect)) {
                log.info("expect is null ");
                return null;
            }
            boolean isSuccess;
            for (String strCmd : lstCmds) {
                isSuccess = isSuccess(lstPattern, strCmd);
                if (!isSuccess) {
                    isSuccess(lstPattern, strCmd);
                }
            }


//            Thread.sleep(800000);

            checkResult(expect.expect(lstPattern));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            closeConnection();
        }
        return buffer.toString();
    }

    private boolean isSuccess(List<Match> objPattern, String strCommandPattern) {
        try {
            boolean isFailed = checkResult(expect.expect(objPattern));

            if (!isFailed) {
                expect.send(strCommandPattern);
                expect.send(ENTER_CHARACTER);
                return true;
            }
            return false;
        } catch (MalformedPatternException ex) {
            ex.printStackTrace();
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private Expect4j SSH() {
        JSch jsch = new JSch();
        Session session;
        try {
            session = jsch.getSession(userName, host, Integer.valueOf(port));
            if (password != null) {
                session.setPassword(password);
            }
            Hashtable<String, String> config = new Hashtable<>();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect(60000);
            ChannelShell channel = (ChannelShell) session.openChannel("shell");
            expect = new Expect4j(channel.getInputStream(), channel.getOutputStream());
            channel.connect();
        } catch (Exception e) {
            log.info("Connect to {},{} failed", host, Integer.valueOf(port));
            e.printStackTrace();
        }

        log.info("Connect to {},{} success", host, Integer.valueOf(port));
        return expect;
    }

    private boolean checkResult(int intRetVal) {
        System.out.println(intRetVal);
        if (intRetVal == COMMAND_EXECUTION_SUCCESS_OPCODE) {
            return true;
        }
        return false;
    }

    private void closeConnection() {
        if (expect != null) {
            expect.close();
        }
    }

}
