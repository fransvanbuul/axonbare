import org.axonframework.commandhandling.CommandHandler;

/**
 * Created by Frans van Buul on 4-7-2017.
 */
public class AddSomeCmdHandler {

    @CommandHandler
    public void handle(AddSomeCmd cmd) {
        System.out.println("Got AddSomeCmd " + cmd + ", n = " + cmd.getN());
    }

}
