package loaddata;

import java.text.SimpleDateFormat;

public class OrderData2StringUtil {
    private OrderData2StringUtil(){}

    private final static String STRING_DATA_FORMATER = "yyyy-MM-dd HH:mm:ss";

    public static void fillOrderDataIntoStringBuilder(Order order, StringBuilder stringBuilder) {
        stringBuilder.append(order.getId()).append("\t");
        stringBuilder.append(order.getUserId()).append("\t");
        stringBuilder.append(order.getProductId()).append("\t");
        stringBuilder.append(order.getProductCode()).append("\t");
        stringBuilder.append(order.getProductName()).append("\t");
        stringBuilder.append(order.getProductPic()).append("\t");
        stringBuilder.append(order.getProcuctIntroduction()).append("\t");
        stringBuilder.append(order.getProductAmout()).append("\t");
        stringBuilder.append(order.getProductPrice()).append("\t");
        stringBuilder.append(order.getDiscount()).append("\t");
        stringBuilder.append(order.getTotalPrice()).append("\t");
        stringBuilder.append(order.getReceiverAddress()).append("\t");
        stringBuilder.append(order.getReceiverName()).append("\t");
        stringBuilder.append(order.getReceiverMobile()).append("\t");
        stringBuilder.append(order.getRemark()).append("\t");
        stringBuilder.append(order.getFreight()).append("\t");
        stringBuilder.append(order.getProcessState()).append("\t");
        SimpleDateFormat dateFormat = new SimpleDateFormat(STRING_DATA_FORMATER);
        stringBuilder.append(dateFormat.format(order.getCreatedTime()));
        stringBuilder.append("\n");
    }
}
