package traincamp.redis;

public class DistributionCounterExam {

    private static  String DISTRIBUTION_INVENTORY = "inventory:skuid";
    public static void main(String[] args) {
        DistributionCounter inventory = DistributionCounterFactory.getCounter(DistributionCounterFactory.DCOUNTER_JEDIS,
                DISTRIBUTION_INVENTORY, 50);
//        DistributionCounter inventory = DistributionCounterFactory.getCounter(DistributionCounterFactory.DCOUNTER_LETTUCE,
//                DISTRIBUTION_INVENTORY, 50);
        Long number = inventory.get();
        System.out.println("initial inventory is " + number);
        number = inventory.increase(50);
        System.out.println("after increase 50, current inventory is " + number);
        number = inventory.decrease(10);
        System.out.println("after decrease 10, current inventory is " + number);
        number = inventory.decrease(110);
        if(number != null) {
            System.out.println("decrease 110, current inventory is " + number);
        } else {
            System.out.println("decrease 110, but inventory is not enough!!!!");
        }
    }
}
