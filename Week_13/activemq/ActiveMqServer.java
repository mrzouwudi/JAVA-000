package traincamp.mq.activemq;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.jmx.ManagementContext;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;

public class ActiveMqServer {
    public static void main(String[] args) {
        try {
            BrokerService brokerService = new BrokerService();
            brokerService.setBrokerName("EmbedMQ");
            brokerService.addConnector("tcp://localhost:62000");
            brokerService.setManagementContext(new ManagementContext());
            brokerService.start();

            String inputStr = null;
            do {
                Scanner scanner = new Scanner(System.in);
                inputStr = scanner.next();
            } while(!"end".equals(inputStr));
            brokerService.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
