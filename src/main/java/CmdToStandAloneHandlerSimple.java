import lombok.Value;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.serialization.json.JacksonSerializer;

/**
 * Trivial Axon command bus example - send a cmd to a handler over the simplecommandbus.
 */
public class CmdToStandAloneHandlerSimple {

    @Value
    public static class DummyCommand {
        String cmdParam;
    }

    @Value
    public static class CmdHandler {
        @CommandHandler
        public void handle(DummyCommand cmd) {
            System.out.println("Got DummyCommand " + cmd);
        }
    }

    public static void main(String[] args) {
        Configurer configurer = DefaultConfigurer.defaultConfiguration();
        configurer.registerCommandHandler(c -> { return new CmdHandler(); });

        Configuration config = configurer.buildConfiguration();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> { config.shutdown(); }));
        config.start();
        Utils.dumpAxonConfig(config);

        config.commandGateway().send(new DummyCommand("Hello World!"));
    }

}
