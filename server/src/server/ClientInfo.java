package server;

import java.net.InetAddress;
import java.time.Instant;

public class ClientInfo {
    InetAddress address;
    int port;
    Instant startTime;

    public ClientInfo(InetAddress address, int port) {
        this.address = address;
        this.port = port;
        this.startTime  = Instant.now();
    }

    public int getPort() {
        return port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public Instant getStartTime() {
        return startTime;
    }

    @Override
    public String toString() {
        return "ClientInfo{address=" + address + ", port=" + port;
    }
}
