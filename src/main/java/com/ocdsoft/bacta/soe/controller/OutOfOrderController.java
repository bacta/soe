package com.ocdsoft.bacta.soe.controller;

import com.ocdsoft.bacta.engine.buffer.BactaBuffer;
import com.ocdsoft.bacta.soe.SoeController;
import com.ocdsoft.bacta.soe.client.SoeUdpClient;
import com.ocdsoft.bacta.soe.message.OutOfOrderMessage;

@SoeController(opcode = 0x11, handles = OutOfOrderMessage.class)
public class OutOfOrderController extends SoeMessageController {

    @Override
    public void handleIncoming(SoeUdpClient client, BactaBuffer buffer) {

        short sequenceNum = buffer.readShortBE();
        //client.setClientSequenceNumber(sequenceNum);
        logger.info("Not fully implemented");
    }
}