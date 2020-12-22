package traincamp.dubbo.hmily.common.account.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Account implements Serializable {
    private static final long serialVersionUID = 8913301549573100970L;

    private Long id;

    private Long userId;

    private Long balance;

    private Date created;

    private Date updated;
}
