import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.io.IOException;

public class ExceptionHandling {

    public static void main(String[] args) throws IOException {
        ActorSystem system = ActorSystem.create("exceptionSystem");
        ActorRef supervising = system.actorOf(Props.create(SupervisingActor.class), "supervising-actor");
        supervising.tell("failChild", ActorRef.noSender());

        System.out.println(">>> Press ENTER to exit <<<");
        try {
            System.in.read();
        } finally {
            system.terminate();
        }
    }
}
class SupervisingActor extends AbstractActor {
    ActorRef child = getContext().actorOf(Props.create(SupervisedActor.class), "supervised-actor");

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("failChild", f -> {
                    child.tell("fail", getSelf());
                })
                .build();
    }
}

class SupervisedActor extends AbstractActor {
    @Override
    public void preStart() {
        System.out.println("supervised actor started");
    }

    @Override
    public void postStop() {
        System.out.println("supervised actor stopped");
    }

    @Override
    public void postRestart(Throwable reason) throws Exception {
        super.postRestart(reason);
        System.out.println("supervised actor restarted");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("fail", f -> {
                    System.out.println("supervised actor fails now");
                    throw new Exception("I failed!");
                })
                .build();
    }
}