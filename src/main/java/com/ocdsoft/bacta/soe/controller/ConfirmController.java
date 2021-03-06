package com.ocdsoft.bacta.soe.controller;

import com.ocdsoft.bacta.engine.utils.BufferUtil;
import com.ocdsoft.bacta.soe.connection.EncryptMethod;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.UdpPacketType;

import java.nio.ByteBuffer;

@SoeController(handles = {UdpPacketType.cUdpPacketConfirm})
public class ConfirmController extends BaseSoeController {

    @Override
    public void handleIncoming(byte zeroByte, UdpPacketType type, SoeUdpConnection connection, ByteBuffer buffer) {

        int connectionID = buffer.getInt();
        int encryptCode = buffer.getInt();
        byte crcBytes = buffer.get();
        boolean compression = BufferUtil.getBoolean(buffer);
        byte cryptMethod = buffer.get();
        int maxRawPacketSize = buffer.getInt();

        connection.setId(connectionID);
        
        connection.getConfiguration().setEncryptCode(encryptCode);
        connection.getConfiguration().setMaxRawPacketSize(maxRawPacketSize);
        connection.getConfiguration().setCrcBytes(crcBytes);
        connection.getConfiguration().setEncryptMethod(EncryptMethod.values()[cryptMethod]);
        connection.getConfiguration().setCompression(compression);
        
        connection.confirm();
    }
}
