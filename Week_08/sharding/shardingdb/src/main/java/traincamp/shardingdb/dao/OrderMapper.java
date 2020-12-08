package traincamp.shardingdb.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import traincamp.shardingdb.entity.Order;

import java.util.Date;
import java.util.List;

@Mapper
public interface OrderMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);

    int insertOrderBatch(@Param("orders") List<Order> orders);

    @Select("select process_state from t_order where id = #{id} for update")
    Integer getProcessStateByIdForUpdate(@Param("id") Long id);

    @Update("update t_order set process_state = 1, paid_time = #{date}, updated_time = #{date} where id = #{id}")
    int updatePaidProcessState(@Param("id")Long id, @Param("date") Date date);

}