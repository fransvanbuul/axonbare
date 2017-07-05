import org.axonframework.common.AxonConfigurationException;
import org.axonframework.config.Configuration;
import org.axonframework.config.ModuleConfiguration;
import org.axonframework.messaging.correlation.CorrelationDataProvider;

/**
 * Created by Frans van Buul on 5-7-2017.
 */
public class Utils {

    private Utils() {
        throw new Error("non-instantiable class");
    }

    public static void dumpAxonConfig(Configuration config) {
        System.out.println("parameterResolverFactory: " + config.parameterResolverFactory());
        System.out.println("commandBus: " + config.commandBus());
        System.out.println("commandGateway: " + config.commandGateway());
        System.out.println("eventBus: " + config.eventBus());
        try {
            System.out.println("eventStore: " + config.eventStore());
        } catch(AxonConfigurationException ex) {
            System.out.println("eventStore: not configured");
        }
        System.out.println("resourceInjector: " + config.resourceInjector());
        System.out.println("serializer: " + config.serializer());

        if(config.correlationDataProviders().isEmpty()) {
            System.out.println("correlationDataProviders: none");
        } else {
            int i = 1;
            for(CorrelationDataProvider cdp : config.correlationDataProviders()) {
                System.out.println("correlationDataProvider (" + i++ + "/" + config.correlationDataProviders().size()
                        + "): " + cdp);
            }
        }

        if(config.getModules().isEmpty()) {
            System.out.println("modules: none");
        } else {
            int i = 1;
            for(ModuleConfiguration mc: config.getModules()) {
                System.out.println("module (" + i++ + "/" + config.getModules().size()
                        + "): " + mc);
            }
        }

    }



}
