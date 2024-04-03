package server;

import client.RegisterRequest;

import java.net.InetAddress;
import java.time.Instant;

public class ClientInfo {
    InetAddress address;
    int port;
    Instant startTime;
    RegisterRequest registerRequest;

    public ClientInfo(InetAddress address, int port, RegisterRequest registerRequest) {
        this.address = address;
        this.port = port;
        this.registerRequest = registerRequest;
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

    public RegisterRequest getRegisterRequest() { return registerRequest; }

    @Override
    public String toString() {
        return "ClientInfo{address=" + address + ", port=" + port;
    }

    public String printAddressPort() { return "ClientInfo{address=" + address + ", port=" + port + "}";}
}
