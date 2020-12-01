package traincamp.datasource.readwrite.customization.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import traincamp.datasource.readwrite.customization.bean.DBContextHolder;

@Aspect
@Component
public class DataSourceAop {
    /**
     * 只读：
     * 不是Master注解的对象或方法  && select开头的方法  ||  get开头的方法
     */
    @Pointcut("!@annotation(traincamp.datasource.readwrite.customization.annotation.Master) " +
            "&& (execution(* traincamp.datasource.readwrite.customization.service..*.select*(..)) " +
            "|| execution(* traincamp.datasource.readwrite.customization.service..*.get*(..)))")
    public void readPointcut() {

    }

    /**
     * 写：
     * Master注解的对象或方法 || insert开头的方法  ||  add开头的方法 || update开头的方法
     * || edlt开头的方法 || delete开头的方法 || remove开头的方法
     */
    @Pointcut("@annotation(traincamp.datasource.readwrite.customization.annotation.Master) " +
            "|| execution(* traincamp.datasource.readwrite.customization.service..*.insert*(..)) " +
            "|| execution(* traincamp.datasource.readwrite.customization.service..*.add*(..)) " +
            "|| execution(* traincamp.datasource.readwrite.customization.service..*.update*(..)) " +
            "|| execution(* traincamp.datasource.readwrite.customization.service..*.edit*(..)) " +
            "|| execution(* traincamp.datasource.readwrite.customization.service..*.delete*(..)) " +
            "|| execution(* traincamp.datasource.readwrite.customization..*.remove*(..))")
    public void writePointcut() {

    }

    @Before("readPointcut()")
    public void read() {
        DBContextHolder.slave();
    }

    @Before("writePointcut()")
    public void write() {
        DBContextHolder.master();
    }
}
