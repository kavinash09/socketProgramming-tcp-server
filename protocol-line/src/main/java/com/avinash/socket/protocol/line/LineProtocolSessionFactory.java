package com.avinash.socket.protocol.line;

import com.avinash.socket.protocol.spi.ProtocolSession;
import com.avinash.socket.protocol.spi.ProtocolSessionFactory;

public class LineProtocolSessionFactory implements ProtocolSessionFactory {
    @Override
    public ProtocolSession create() {
        return new LineProtocolSession();
    }
}
