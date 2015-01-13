package com.ocdsoft.bacta.soe.controller;

import com.google.inject.Inject;
import com.ocdsoft.bacta.engine.utils.UnsignedUtil;
import com.ocdsoft.bacta.soe.SoeController;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.UdpPacketType;
import com.ocdsoft.bacta.soe.router.SoeMessageRouter;

import java.nio.ByteBuffer;

/**
 *
 *
 * Parsing the multi byte size
 unsigned int __cdecl UdpMisc::GetVariableValue(const void *buffer, unsigned int *value)
 {
     unsigned int result; // eax@4

     if ( *(_BYTE *)buffer == 255 )
     {
         if ( *((_BYTE *)buffer + 1) != 255 || *((_BYTE *)buffer + 2) != 255 )
         {
            *value = *((_BYTE *)buffer + 2) | (*((_BYTE *)buffer + 1) << 8);
            result = 3;
         }
         else
         {
            *value = *((_BYTE *)buffer + 6) | (*((_BYTE *)buffer + 5) << 8) | (*((_BYTE *)buffer + 4) << 16) | (*((_BYTE *)buffer + 3) << 24);
            result = 7;
         }
     }
     else
     {
         *value = *(_BYTE *)buffer;
         result = 1;
     }
     return result;
 }
 */

@SoeController(handles = {UdpPacketType.cUdpPacketMulti})
public class MultiController implements SoeMessageController {

    private final SoeMessageRouter soeMessageRouter;

    @Inject
    public MultiController(final SoeMessageRouter soeMessageRouter) {
        this.soeMessageRouter = soeMessageRouter;
    }

    @Override
    public void handleIncoming(byte zeroByte, UdpPacketType type, SoeUdpConnection connection, ByteBuffer buffer) {

        short length = UnsignedUtil.getUnsignedByte(buffer);

        while (buffer.remaining() >= length) {

            if (length == 0xFF) {

                short value1 = UnsignedUtil.getUnsignedByte(buffer);
                short value2 = UnsignedUtil.getUnsignedByte(buffer);

                length =  (short)(value2 | value1 << 8);
            }

            ByteBuffer slicedMessage = buffer.slice();
            slicedMessage.limit(length - 2);

            soeMessageRouter.routeMessage(connection, slicedMessage);

            if (!buffer.hasRemaining()) {
                break;
            }

            length = UnsignedUtil.getUnsignedByte(buffer);
        }
    }

}
