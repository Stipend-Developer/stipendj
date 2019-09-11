/**
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.stipendj.core;

import com.google.common.net.InetAddresses;
import org.stipendj.params.MainNetParams;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A PeerAddress holds an IP address and port number representing the network location of
 * a peer in the Bitcoin P2P network. It exists primarily for serialization purposes.
 */
public class MasternodeAddress extends NetAddress {
    private static final long serialVersionUID = 7501293709324197411L;
    static final int MESSAGE_SIZE = 18;

    private int port;

    /**
     * Construct a peer address from a serialized payload.
     */
    public MasternodeAddress(NetworkParameters params, byte[] payload, int offset, int protocolVersion) throws ProtocolException {
        super(params, payload, offset, protocolVersion);
    }

    /**
     * Construct a peer address from a serialized payload.
     * @param params NetworkParameters object.
     * @param payload Bitcoin protocol formatted byte array containing message content.
     * @param offset The location of the first payload byte within the array.
     * @param protocolVersion Bitcoin protocol version.
     * @param serializer the serializer to use for this message.
     * @throws ProtocolException
     */
    public MasternodeAddress(NetworkParameters params, byte[] payload, int offset, int protocolVersion, Message parent, MessageSerializer serializer) throws ProtocolException {
        super(params, payload, offset, protocolVersion, parent, serializer);
    }


    /**
     * Construct a peer address from a memorized or hardcoded address.
     */
    public MasternodeAddress(InetAddress addr, int port) {
        super(addr);
        this.port = port;
        length = MESSAGE_SIZE;
    }

    /**
     * Constructs a peer address from the given IP address. Port and protocol version are default for the mainnet.
     */
    public MasternodeAddress(InetAddress addr) {
        this(addr, MainNetParams.get().getPort());
    }

    public MasternodeAddress(InetSocketAddress addr) {
        this(addr.getAddress(), addr.getPort());
    }

    public static MasternodeAddress localhost(NetworkParameters params) {
        return new MasternodeAddress(InetAddresses.forString("127.0.0.1"), params.getPort());
    }

    @Override
    protected void bitcoinSerializeToStream(OutputStream stream) throws IOException {
        // Java does not provide any utility to map an IPv4 address into IPv6 space, so we have to do it by hand.
        super.bitcoinSerializeToStream(stream);
        // And write out the port. Unlike the rest of the protocol, address and port is in big endian byte order.
        stream.write((byte) (0xFF & port >> 8));
        stream.write((byte) (0xFF & port));
    }

    @Override
    protected void parse() throws ProtocolException {
        super.parse();
        port = ((0xFF & payload[cursor++]) << 8) | (0xFF & payload[cursor++]);
        length = MESSAGE_SIZE;
    }

    public int calculateMessageSizeInBytes()
    {
        return getMessageSize();
    }

    public InetSocketAddress getSocketAddress() {
        return new InetSocketAddress(getAddr(), getPort());
    }

    public int getPort() {
        return port;
    }


    public void setPort(int port) {
        unCache();
        this.port = port;
    }


    @Override
    public String toString() {
        return super.toString() + ":" + port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MasternodeAddress other = (MasternodeAddress) o;
        return other.getAddr().equals(getAddr()) &&
                other.port == port;
        //TODO: including services and time could cause same peer to be added multiple times in collections
    }

    @Override
    public int hashCode() {
        return getAddr().hashCode() ^ port;
    }
    
    public InetSocketAddress toSocketAddress() {
        return new InetSocketAddress(getAddr(), port);
    }


    public MasternodeAddress duplicate() { return new MasternodeAddress(getAddr(), getPort()); }
}
