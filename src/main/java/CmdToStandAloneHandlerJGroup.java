import lombok.Value;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.TargetAggregateIdentifier;
import org.axonframework.commandhandling.distributed.AnnotationRoutingStrategy;
import org.axonframework.commandhandling.distributed.DistributedCommandBus;
import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.jgroups.commandhandling.JGroupsConnector;
import org.axonframework.serialization.json.JacksonSerializer;
import org.jgroups.JChannel;

/**
 * Simple example with Axon JGroups command bus
 */
public class CmdToStandAloneHandlerJGroup {

    @Value    // This doesn't work with the XStreamSerializer - using Json instead
    public static class DummyCommand {
        @TargetAggregateIdentifier String id;
        String cmdParam;
    }

    public static class CmdHandler {
        @CommandHandler
        public void handle(DummyCommand cmd) {
            System.out.println("GOT DUMMYCOMMAND  " + cmd);
        }
    }

    private static void doHandlerNode() throws Exception {
        JChannel channel = new JChannel();
        Configurer configurer = DefaultConfigurer.defaultConfiguration();
        configurer.configureSerializer(c -> { return new JacksonSerializer(); });
        configurer.registerCommandHandler(c -> { return new CmdHandler(); });
        configurer.configureCommandBus(c -> {
            try {
                CommandBus localSegment = new SimpleCommandBus();
                JGroupsConnector connector = new JGroupsConnector(localSegment, channel, "myCommandBus", c.serializer());
                DistributedCommandBus commandBus = new DistributedCommandBus(connector, connector);
                connector.connect();
                return commandBus;
            } catch(Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        Configuration config = configurer.buildConfiguration();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> { config.shutdown(); channel.close(); }));
        config.start();
        System.out.println("\r\n\r\nCONFIG OF COMMAND HANDLING NODE");
        Utils.dumpAxonConfig(config);
        Thread.sleep(500);
    }

    private static void doClientNode() throws Exception {
        JChannel channel = new JChannel();
        Configurer configurer = DefaultConfigurer.defaultConfiguration();
        configurer.configureSerializer(c -> { return new JacksonSerializer(); });
        configurer.configureCommandBus(c -> {
            try {
                CommandBus localSegment = new SimpleCommandBus();
                JGroupsConnector connector = new JGroupsConnector(localSegment, channel, "myCommandBus", c.serializer(), new AnnotationRoutingStrategy());
                DistributedCommandBus commandBus = new DistributedCommandBus(connector, connector);
                connector.connect();
                return commandBus;
            } catch(Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        Configuration config = configurer.buildConfiguration();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> { config.shutdown(); channel.close(); }));
        config.start();
        System.out.println("\r\n\r\nCONFIG OF CLIENT NODE");
        Utils.dumpAxonConfig(config);
        Thread.sleep(500);

        System.out.println("\r\n\r\nSENDING COMMAND");
        config.commandGateway().send(new DummyCommand("1", "Hello World!"));
    }


    public static void main(String[] args) throws Exception {
        doHandlerNode();
        doClientNode();
    }

}
