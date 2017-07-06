import lombok.Data;
import lombok.Value;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.TargetAggregateIdentifier;
import org.axonframework.commandhandling.model.AbstractRepository;
import org.axonframework.commandhandling.model.Aggregate;
import org.axonframework.commandhandling.model.inspection.AnnotatedAggregate;
import org.axonframework.config.AggregateConfigurer;
import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.eventhandling.EventBus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Sending a command to an Aggregate, which is stored in an in-memory repository without event sourcing.
 */
public class CmdToNoESInMemAgg {

    @Value
    public static class CreateCmd {
        @TargetAggregateIdentifier String id;
    }

    @Value
    public static class AddCmd {
        @TargetAggregateIdentifier String id;
        int n;
    }

    @Data
    public static class MyAggregate {
        String id;
        int count;

        @CommandHandler
        public MyAggregate(CreateCmd cmd) {
            System.out.println("MyAggregate ctor");
            this.id = cmd.getId();
        }

        @CommandHandler
        public void handle(AddCmd cmd) {
            this.count += cmd.getN();
            System.out.println("new total for " + id + ": " + count);
        }
    }

    public static class MyRepository extends AbstractRepository<MyAggregate, Aggregate<MyAggregate>> {

        private EventBus eventBus;
        private final Map<String, Aggregate<MyAggregate>> map = new HashMap<>();

        public MyRepository(EventBus eventBus) {
            super(MyAggregate.class);
            this.eventBus = eventBus;
        }

        @Override
        protected Aggregate<MyAggregate> doCreateNew(Callable<MyAggregate> callable) throws Exception {
            return AnnotatedAggregate.initialize(callable, aggregateModel(), eventBus);
        }

        @Override
        protected void doSave(Aggregate<MyAggregate> agg) {
            map.put(agg.invoke(a -> { return a.getId(); }), agg);
        }

        @Override
        protected Aggregate<MyAggregate> doLoad(String s, Long aLong) {
            return map.get(s);
        }

        @Override
        protected void doDelete(Aggregate<MyAggregate> agg) {
            map.remove(agg.identifierAsString());
        }
    }

    public static void main(String[] args) {
        Configurer configurer = DefaultConfigurer.defaultConfiguration();

        configurer.configureAggregate(
                AggregateConfigurer
                        .defaultConfiguration(MyAggregate.class)
                        .configureRepository(c -> { return new MyRepository(c.eventBus()); })
        );

        Configuration config = configurer.buildConfiguration();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> { config.shutdown(); }));
        config.start();
        Utils.dumpAxonConfig(config);

        System.out.println("##### Sending CreateCmd");
        config.commandGateway().send(new CreateCmd("1000"));
        System.out.println("##### Sending AddCmd");
        config.commandGateway().send(new AddCmd("1000", 5));
        System.out.println("##### Sending AddCmd");
        config.commandGateway().send(new AddCmd("1000", 5));
    }

}
