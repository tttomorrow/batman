package org.opengauss.batman.common.utils;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.opengauss.batman.common.ErrorCode;
import org.opengauss.batman.common.exception.BatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

public class SSHUtils {
    private static final Logger LOG = LoggerFactory.getLogger(SSHUtils.class);

    private static final int TIMEOUT = 2 * 60 * 60 * 1000;

    /**
     * 获取session
     *
     * @param host     ip
     * @param port     端口
     * @param username 用户名
     * @param password 密码
     * @return Session
     */
    public static Session getSession(String host, int port, String username, String password) {
        Properties properties = new Properties();
        properties.put("StrictHostKeyChecking", "no");
        properties.put("HostKeyAlgorithms", "+ssh-rsa");
        JSch jSch = new JSch();
        Session session = null;
        try {
            session = jSch.getSession(username, host, port);
            session.setPassword(password);
            session.setTimeout(TIMEOUT);
            session.setConfig(properties);
            session.connect();
        } catch (JSchException e) {
            LOG.error("get session exception : {}", ExceptionUtils.getStackTrace(e));
            throw new BatException(ErrorCode.SERVER_CONNECT_ERROR);
        }
        return session;
    }

    /**
     * 同步执行,需要获取执行完的结果
     *
     * @param session Session
     * @param cmd 命令
     * @return 结果
     */
    public static String execRemoteCmd(Session session, String cmd, String lineSeparator) {
        ChannelExec channelExec = null;
        BufferedReader inputReader = null;
        BufferedReader errorReader = null;
        StringBuilder result = new StringBuilder();
        try {
            channelExec =  (ChannelExec) session.openChannel("exec");;
            channelExec.setCommand(cmd);
            channelExec.connect();

            inputReader = new BufferedReader(new InputStreamReader(channelExec.getInputStream()));
            errorReader = new BufferedReader(new InputStreamReader(channelExec.getErrStream()));
            String line;
            while ((line = inputReader.readLine()) != null) {
                result.append(line).append(lineSeparator);
            }
            while ((line = errorReader.readLine()) != null) {
                result.append(line).append(lineSeparator);
            }
        } catch (Exception e) {
            LOG.error("exec cmd exception: {}", ExceptionUtils.getStackTrace(e));
        } finally {
            IOUtils.closeQuietly(inputReader);
            IOUtils.closeQuietly(errorReader);
            if (channelExec != null) {
                channelExec.disconnect();
            }
        }
        return result.toString();
    }
}
