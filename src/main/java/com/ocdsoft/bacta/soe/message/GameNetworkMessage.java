package com.ocdsoft.bacta.soe.message;

import com.ocdsoft.bacta.engine.buffer.ByteBufferWritable;

import java.nio.ByteBuffer;

/**
 * Created by Kyle on 3/26/14.
 */
public abstract class GameNetworkMessage implements ByteBufferWritable {

    protected static short priority;
    protected static int messageType;

    public short getPriority() {
        return priority;
    }
    public int getMessageType() {
        return messageType;
    }
}
