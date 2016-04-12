package com.ocdsoft.bacta.soe.dispatch;

import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.engine.conf.BactaConfiguration;
import com.ocdsoft.bacta.engine.utils.BufferUtil;
import com.ocdsoft.bacta.soe.ServerState;
import com.ocdsoft.bacta.soe.SoeController;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.controller.SoeMessageController;
import com.ocdsoft.bacta.soe.message.UdpPacketType;
import com.ocdsoft.bacta.soe.util.SoeMessageUtil;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Controllers are required to exist in the com.ocdsoft.bacta.soe.controller package to 
 * be loaded.
 */
@Singleton
public final class SoeDevMessageDispatcher implements SoeMessageDispatcher {

    private final static Logger logger = LoggerFactory.getLogger(SoeDevMessageDispatcher.class);

    private Map<UdpPacketType, SoeMessageController> controllers = new HashMap<>();

    private final Injector injector;
    private final Collection<String> swgControllerClasspaths;

    public SoeDevMessageDispatcher(final Injector injector, final Collection<String> swgControllerClasspaths) {
        
        this.injector = injector;
        this.swgControllerClasspaths = swgControllerClasspaths;
    }

    @Override
    public void dispatch(SoeUdpConnection client, ByteBuffer buffer) {

        byte zeroByte = buffer.get();
        byte type = buffer.get();
        if(type < 0 || type > 0x1E) {
            throw new RuntimeException("Type out of range:" + type + " " + buffer.toString() + " " + SoeMessageUtil.bytesToHex(buffer));
        }

        UdpPacketType packetType = UdpPacketType.values()[type];

        SoeMessageController controller = controllers.get(packetType);

        if (controller == null) {
            logger.error("Unhandled SOE Opcode 0x" + Integer.toHexString(packetType.getValue()).toUpperCase());
            logger.error(SoeMessageUtil.bytesToHex(buffer));
            return;
        }

        try {

            logger.trace("Routing to " + controller.getClass().getSimpleName() + ": " + BufferUtil.bytesToHex(buffer));
            controller.handleIncoming(zeroByte, packetType, client, buffer);

        } catch (Exception e) {
            logger.error("SOE Routing", e);
        }
    }

    @Override
    public void load() {
        
        Reflections reflections = new Reflections();

        Set<Class<? extends SoeMessageController>> subTypes = reflections.getSubTypesOf(SoeMessageController.class);

        Iterator<Class<? extends SoeMessageController>> iter = subTypes.iterator();

        controllers.clear();

        ServerState serverState = injector.getInstance(ServerState.class);
        BactaConfiguration configuration = injector.getInstance(BactaConfiguration.class);

        GameNetworkMessageDispatcher gameNetworkMessageDispatcher = new GameNetworkDevMessageDispatcher(
                injector,
                serverState,
                swgControllerClasspaths,
                configuration.getBoolean("SharedNetwork", "generateControllers"));

        while (iter.hasNext()) {

            try {
                
                Class<? extends SoeMessageController> controllerClass = iter.next();
                
                if(Modifier.isAbstract(controllerClass.getModifiers())) {
                    continue;
                }
                
                SoeController controllerAnnotation = controllerClass.getAnnotation(SoeController.class);

                if (controllerAnnotation == null) {
                    logger.info("Missing @SoeController annotation, discarding: " + controllerClass.getName());
                    continue;
                }

                UdpPacketType[] types = controllerAnnotation.handles();
                logger.debug("Loading SoeMessageController: " + serverState.getServerType() + " " + controllerClass.getSimpleName());

                SoeMessageController controller = injector.getInstance(controllerClass);
                controller.setSoeMessageDispatcher(this);
                controller.setGameNetworkMessageDispatcher(gameNetworkMessageDispatcher);

                for(UdpPacketType udpPacketType : types) {

                    if (!controllers.containsKey(udpPacketType)) {
                        logger.trace("Adding SOE controller: " + controller.getClass().getSimpleName());
                        synchronized (controllers) {
                            controllers.put(udpPacketType, controller);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Unable to add controller", e);
            }
        }
    }
}
