package com.ocdsoft.bacta.soe.disruptor;

import com.google.inject.Inject;
import com.lmax.disruptor.EventHandler;
import com.ocdsoft.bacta.soe.ServerState;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.dispatch.SoeDevelopMessageDispatcher;
import com.ocdsoft.bacta.soe.dispatch.SwgMessageDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class LocalInputEventHandler<T extends SoeUdpConnection> implements EventHandler<SoeInputEvent<T>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final SoeDevelopMessageDispatcher soeRouter;
    private final SwgMessageDispatcher swgRouter;
    private final ServerState serverState;

    @Inject
    public LocalInputEventHandler(SoeDevelopMessageDispatcher soeMessageRouter, ServerState serverState) {

        soeRouter = soeMessageRouter;
        swgRouter = null;
        this.serverState = serverState;
    }

    @Override
    public void onEvent(SoeInputEvent<T> event, long sequence, boolean endOfBatch)
            throws Exception {

        ByteBuffer buffer = event.getBuffer();
        T client = event.getClient();

        if (event.isSwgMessage()) {

            int opcode = buffer.getInt();

            //swgRouter.dispatch(opcode, client, bactaBuffer);

        } else {

           // soeRouter.dispatch(buffer.getShort(), client, buffer);

        }
    }

}
