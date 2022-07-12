package net.shadew.otp;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * A {@link TimeSource} using a <a href="https://en.wikipedia.org/wiki/Network_Time_Protocol">Network Time Protocol</a>
 * server to synchronize time. This time source instantiates a connection with a server which must be closed after use,
 * via {@link #close()}.
 */
public final class NtpTimeSource implements TimeSource, Closeable {
    /**
     * The default time-out for time requests: 3000 milliseconds (3 seconds).
     */
    public static final int DEFAULT_TIMEOUT = 3000;

    /**
     * The default NTP port, which is {@code 123}.
     */
    public static final int NTP_PORT = 123;

    private final InetAddress serverAddress;
    private final int timeout;
    private final int port;
    private DatagramSocket socket;

    /**
     * Create an NTP time source.
     *
     * @param serverAddress The server address
     * @param timeout       The time-out for time requests
     * @param port          The port number
     * @throws SocketException When it fails to connect to the NTP server
     */
    public NtpTimeSource(InetAddress serverAddress, int timeout, int port) throws SocketException {
        this.serverAddress = serverAddress;
        this.timeout = timeout;
        this.port = port;

        init();
    }

    /**
     * Create an NTP time source, using default port number 123.
     *
     * @param serverAddress The server address
     * @param timeout       The time-out for time requests
     * @throws SocketException When it fails to connect to the NTP server
     */
    public NtpTimeSource(InetAddress serverAddress, int timeout) throws SocketException {
        this(serverAddress, timeout, NTP_PORT);
    }

    /**
     * Create an NTP time source, using default port number 123 and a default timeout of 3000 milliseconds.
     *
     * @param serverAddress The server address
     * @throws SocketException When it fails to connect to the NTP server
     */
    public NtpTimeSource(InetAddress serverAddress) throws SocketException {
        this(serverAddress, DEFAULT_TIMEOUT, NTP_PORT);
    }

    private void init() throws SocketException {
        socket = new DatagramSocket();
        socket.setSoTimeout(timeout);
        socket.connect(serverAddress, port);
    }

    /**
     * Closes the connection with the time source. The time source cannot be used after a call to this method, and will
     * throw an exception upon attempting so.
     */
    @Override
    public void close() {
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }

    /**
     * {@inheritDoc}
     * @throws OtpGenException When requesting time from the NTP server fails.
     */
    @Override
    public long time() throws OtpGenException {
        if (socket == null)
            throw new OtpGenException("NTP socket closed");

        try {
            Message msg = new Message();
            msg.mode = 3;
            msg.xmitTime = timestamp();
            byte[] buf = msg.write();

            DatagramPacket message = new DatagramPacket(buf, buf.length);
            socket.send(message);

            DatagramPacket response = new DatagramPacket(buf, buf.length);
            socket.receive(response);

            double destTime = timestamp();
            msg.read(response.getData());

            int ioff = (int) ((msg.rcvTime - msg.origTime + (msg.xmitTime - destTime)) / 2);
            return System.currentTimeMillis() + ioff;
        } catch (IOException e) {
            throw new OtpGenException("Failed to obtain NTP time", e);
        }
    }

    private static double timestamp() {
        return System.currentTimeMillis() / 1000D + 2208988800D;
    }

    private static class Message {
        private byte leap = 0;
        private byte version = 3;
        private byte mode = 0;
        private short stratum = 0;
        private byte pollInterval = 0;
        private byte precision = 0;
        private double rootDelay = 0;
        private double rootDispersion = 0;
        private byte[] refIdent = new byte[4];
        private double refTime = 0;
        private double origTime = 0;
        private double rcvTime = 0;
        private double xmitTime = 0;

        private void read(byte[] msg) {
            leap = (byte) (msg[0] >> 6 & 0x3);
            version = (byte) (msg[0] >> 3 & 0x7);
            mode = (byte) (msg[0] & 0x7);
            stratum = (short) (msg[1] & 0xFF);
            pollInterval = msg[2];
            precision = msg[3];

            rootDelay = msg[4] * 256.0 + (msg[5] & 0xFF) + (msg[6] & 0xFF) / 256.0 + (msg[7] & 0xFF) / 65536.0;
            rootDispersion = (msg[8] & 0xFF) * 256.0 + (msg[9] & 0xFF) + (msg[10] & 0xFF) / 256.0 + (msg[11] & 0xFF) / 65536.0;

            refIdent[0] = msg[12];
            refIdent[1] = msg[13];
            refIdent[2] = msg[14];
            refIdent[3] = msg[15];

            refTime = readTime(msg, 16);
            origTime = readTime(msg, 24);
            rcvTime = readTime(msg, 32);
            xmitTime = readTime(msg, 40);
        }

        private byte[] write() {
            byte[] msg = new byte[48];

            msg[0] = (byte) (leap << 6 | version << 3 | mode);
            msg[1] = (byte) stratum;
            msg[2] = pollInterval;
            msg[3] = precision;

            int l = (int) (rootDelay * 65536);
            msg[4] = (byte) (l >> 24 & 0xFF);
            msg[5] = (byte) (l >> 16 & 0xFF);
            msg[6] = (byte) (l >> 8 & 0xFF);
            msg[7] = (byte) (l & 0xFF);

            long ul = (long) (rootDispersion * 65536.0);
            msg[8] = (byte) (ul >> 24 & 0xFF);
            msg[9] = (byte) (ul >> 16 & 0xFF);
            msg[10] = (byte) (ul >> 8 & 0xFF);
            msg[11] = (byte) (ul & 0xFF);

            msg[12] = refIdent[0];
            msg[13] = refIdent[1];
            msg[14] = refIdent[2];
            msg[15] = refIdent[3];

            writeTime(msg, 16, refTime);
            writeTime(msg, 24, origTime);
            writeTime(msg, 32, rcvTime);
            writeTime(msg, 40, xmitTime);

            return msg;
        }

        public static double readTime(byte[] msg, int pos) {
            double r = 0;
            for (int i = 0; i < 8; i++) {
                r += (msg[pos + i] & 0xFF) * Math.pow(2, (3 - i) * 8);
            }
            return r;
        }

        public static void writeTime(byte[] msg, int pos, double time) {
            for (int i = 0; i < 8; i++) {
                double b = Math.pow(2, (3 - i) * 8);
                byte t = msg[pos + i] = (byte) (time / b);
                time -= (t & 0xFF) * b;
            }

            msg[pos + 7] = (byte) (Math.random() * 255);
        }
    }
}
