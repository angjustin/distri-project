package server;

import client.RegisterRequest;

import java.net.InetAddress;
import java.time.Instant;

public class ClientInfo {
    InetAddress address;
    int port;
    Instant expiry;
    RegisterRequest registerRequest;

    public ClientInfo(InetAddress address, int port, RegisterRequest registerRequest) {
        this.address = address;
        this.port = port;
        this.registerRequest = registerRequest;
        // calculate expiry time
        this.expiry  = Instant.now().plusSeconds(registerRequest.getMonitorInterval());
    }

    public int getPort() {
        return port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public Instant getExpiry() {
        return expiry;
    }

    public RegisterRequest getRegisterRequest() { return registerRequest; }

    @Override
    public String toString() {
        return "ClientInfo{address=" + address + ", port=" + port;
    }

    public String printAddressPort() { return "ClientInfo{address=" + address + ", port=" + port + "}";}
}
