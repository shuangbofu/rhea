//package cn.shuangbofu.rhea.job.job.shell;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.sshd.client.SshClient;
//import org.apache.sshd.client.channel.ClientChannel;
//import org.apache.sshd.client.channel.ClientChannelEvent;
//import org.apache.sshd.client.session.ClientSession;
//import org.apache.sshd.client.subsystem.sftp.SftpClient;
//import org.apache.sshd.client.subsystem.sftp.SftpClientFactory;
//import org.apache.sshd.common.config.keys.FilePasswordProvider;
//import org.apache.sshd.common.future.SshFutureListener;
//import org.apache.sshd.common.io.IoInputStream;
//import org.apache.sshd.common.io.IoReadFuture;
//import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
//import org.apache.sshd.common.util.buffer.Buffer;
//import org.apache.sshd.common.util.buffer.ByteArrayBuffer;
//
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.nio.file.FileSystems;
//import java.time.Duration;
//import java.util.Collection;
//import java.util.EnumSet;
//import java.util.concurrent.TimeUnit;
//
///**
// * 通过nio线程池异步回调执行ssh
// */
//@Slf4j
//public class SshHelper {
//
//    private static final int SSH_DEFAULT_TIMEOUT = 5;
//    private static final int SSH_CONNECT_TIMEOUT = SSH_DEFAULT_TIMEOUT;
//    private static final int SSH_AUTH_TIMEOUT = SSH_DEFAULT_TIMEOUT;
//    private static final int SSH_COMMAND_TIMEOUT = SSH_DEFAULT_TIMEOUT;
//    private static SshHelper sshHelper;
//    private final SshClient client = SshClient.setUpDefaultClient();
//    private final String userName;
//    private final String host;
//    private final int port;
//    private final String privateKeyPath;
//    private final String privateKeyFilePwd;
//
//    public SshHelper(String userName, String host, int port, String privateKeyPath, String privateKeyFilePwd) {
//        this.userName = userName;
//        this.host = host;
//        this.port = port;
//        this.privateKeyPath = privateKeyPath;
//        this.privateKeyFilePwd = privateKeyFilePwd;
//        client.start();
//    }
//
//    public static SshHelper getInstance(String userName, String host, int port, String privateKeyPath, String privateKeyFilePwd) {
//        if (sshHelper == null) {
//            synchronized (SshHelper.class) {
//                sshHelper = new SshHelper(userName, host, port, privateKeyPath, privateKeyFilePwd);
//            }
//        }
//        return sshHelper;
//    }
//
//    public static void main(String[] args) {
//        SshHelper.getInstance("jimmy", "localhost", 22,
//                "/tmp/id_rsa", "123456").ssh("ls -l",
//                (line) -> {
//                }
//        );
//    }
//
//    /**
//     * 执行ssh命令
//     *
//     * @param command
//     * @return 执行后的exitCode
//     */
//    public void ssh(String command,
//                    LogCb logCb) {
//        ClientSession session = null;
//        ClientChannel channel = null;
//        try {
//            session = createClientSession();
//            channel = session.createExecChannel(command);
//            channel.setStreaming(ClientChannel.Streaming.Async);
//            channel.open().verify(SSH_COMMAND_TIMEOUT, TimeUnit.SECONDS);
//            setLogCb("info", channel, channel.getAsyncOut(), logCb);
//            setLogCb("error", channel, channel.getAsyncErr(), logCb);
//
//            long waitStart = System.currentTimeMillis();
//            Collection<ClientChannelEvent> result = channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), -1);
//            long waitEnd = System.currentTimeMillis();
//            Integer exitCode = channel.getExitStatus();
//            log.info("Exec ssh cmd:{} done after wait of:{}ms, result:{}, exitCode:{}",
//                    command,
//                    waitEnd - waitStart,
//                    result.contains(ClientChannelEvent.TIMEOUT),
//                    exitCode);
//            if (exitCode != 0) {
//                throw new RuntimeException(String.format("Run ssh cmd:%s failed, exitCode:%s", command, exitCode));
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        } finally {
//            closeChannel(channel);
//            if (session != null) {
//                session.close(true);
//            }
//        }
//    }
//
//    public void scp(String localPath, String remotePath) throws IOException {
//        ClientSession session = null;
//        SftpClient sftp = null;
//        FileInputStream in = null;
//        SftpClient.CloseableHandle handle = null;
//        SftpClientFactory factory = SftpClientFactory.instance();
//        try {
//            session = createClientSession();
//            sftp = factory.createSftpClient(session);
//            handle = sftp.open(remotePath,
//                    EnumSet.of(SftpClient.OpenMode.Write, SftpClient.OpenMode.Create, SftpClient.OpenMode.Truncate));
//            int buff_size = 10 * 1024;
//            byte[] src = new byte[buff_size];
//            int len;
//            long fileOffset = 0L;
//            in = new FileInputStream(localPath);
//            while ((len = in.read(src)) != -1) {
//                sftp.write(handle, fileOffset, src, 0, len);
//                fileOffset += len;
//            }
//        } finally {
//            if (in != null) {
//                in.close();
//            }
//            if (sftp != null) {
//                sftp.close(handle);
//            }
//            if (session != null) {
//                session.close(false);
//            }
//        }
//    }
//
//    private ClientSession createClientSession() throws IOException {
//        ClientSession session = client.connect(userName, host, port).verify(Duration.ofSeconds(SSH_CONNECT_TIMEOUT)).getSession();
//        FileKeyPairProvider provider = new FileKeyPairProvider(
//                FileSystems.getDefault().getPath(privateKeyPath));
//        provider.setPasswordFinder(FilePasswordProvider.of(privateKeyFilePwd));
//        session.setKeyIdentityProvider(provider);
//        session.auth().verify(Duration.ofSeconds(SSH_AUTH_TIMEOUT));
//        return session;
//    }
//
//    /**
//     * 日志输出的回调
//     *
//     * @param type
//     * @param channel
//     * @param asyncOut
//     * @param logCb
//     */
//    private void setLogCb(String type, ClientChannel channel, IoInputStream asyncOut, LogCb logCb) {
//        final int DEFAULT_TIMEOUT = 10;
//        asyncOut.read(new ByteArrayBuffer(4096))
//                .addListener(new SshFutureListener<IoReadFuture>() {
//                    @Override
//                    public void operationComplete(IoReadFuture future) {
//                        try {
//                            future.verify(DEFAULT_TIMEOUT);
//                            Buffer buffer = future.getBuffer();
//                            String message = new String(buffer.array(), buffer.rpos(), buffer.available());
//                            if (logCb != null) {
//                                if ("info".equals(type)) {
//                                    logCb.log(message);
//                                } else if ("error".equals(type)) {
//                                    logCb.log(message);
//                                }
//                            } else {
//                                System.out.println(message);
//                            }
//                            buffer.rpos(buffer.rpos() + buffer.available());
//                            buffer.compact();
//
//                            asyncOut.read(buffer).addListener(this);
//                        } catch (IOException e) {
//                            log.error("error", e);
//                            closeChannel(channel);
//                        }
//                    }
//                });
//    }
//
//    private void closeChannel(ClientChannel channel) {
//        if (channel == null) {
//            return;
//        }
//        if (!(channel.isClosing() || channel.isClosed())) {
//            channel.close(true);
//        }
//    }
//
//    @FunctionalInterface
//    public interface LogCb {
//        void log(String data);
//    }
//}
