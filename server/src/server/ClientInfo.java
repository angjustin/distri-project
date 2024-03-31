package server;

import java.net.InetAddress;

public class ClientInfo {
    InetAddress address;
    int port;

    public ClientInfo(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public String getAddressString(){
        return address.toString();
    }

    @Override
    public String toString() {
        return "ClientInfo{" +
                "address=" + address +
                ", port=" + port +
                '}';
    }
}
