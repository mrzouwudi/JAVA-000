package aopusingjavaproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class JavaProxyDemo {
    public static void main(String[] args) {
        IStudent student = new StudentImpl(1,"小张");
        InvocationHandler handler = new ProxyAOP(student);
        IStudent proxyStudent = (IStudent)Proxy.newProxyInstance(student.getClass().getClassLoader(), student.getClass().getInterfaces(), handler);
        proxyStudent.study();
    }
}
