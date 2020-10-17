import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HelloClassloader extends ClassLoader {
    //类全名称中的分隔符
    private static final byte PACKAGE_SEPAREATOR = (byte)('.');
    //系统文件分隔符
    private static final String PATH_SEPAREATOR = File.separator;

    /**
     * 将HelloClassloader编译后的class文件和Hello.xlass文件放到同一个目录，运行
     * java HelloClassloader
     * 输出：Hello, classLoader!
     * @param args
     */
    public static void main(String[] args) {
        try {
            HelloClassloader classloader = new HelloClassloader();
            Class clz = classloader.loadClass("Hello"); //通过Hello.xlass获取Hello类
            Method method = clz.getDeclaredMethod("hello"); //用反射找到hello方法
            method.invoke(clz.newInstance()); //调用hello方法输出“Hello, classLoader!”
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = loadXlassFile(convertFileName(name));
        return defineClass(name, bytes, 0, bytes.length);
    }

    /**
     * 通过类名读取xlass文件，并获得相应包含字节码的byte数组
     * @param name 类名
     * @return 对应该类的字节码的byte数组
     * @throws ClassNotFoundException 如果找不到文件抛出该异常
     */
    private byte[] loadXlassFile(String name) throws ClassNotFoundException{
        byte[] result = new byte[0];
        try (InputStream is = getResourceAsStream(name)) {
            byte[] tmp = new byte[1000];
            int byteread = 0;
            while((byteread = is.read(tmp)) != -1) {
                byte[] newResult = new byte[result.length + byteread];
                System.arraycopy(result,0,newResult,0, result.length);
                System.arraycopy(tmp, 0, newResult, result.length, byteread);
                result = newResult;
            }
            convert(result);
        } catch (IOException e) {
            throw new ClassNotFoundException();
        }
        return result;
    }

    /**
     * 转换类名到本地相对的文件路径，类的全名称中的"."转换为当前系统的文件分隔符
     * @param name  类名
     * @return
     */
    private String convertFileName(String name) {
        StringBuilder builder = new StringBuilder();
        byte[] bytes = name.getBytes();
        for(int i=0; i < bytes.length; i++) {
            if(bytes[i] != PACKAGE_SEPAREATOR) {
                builder.append((char)bytes[i]);
            } else {
                builder.append(PATH_SEPAREATOR);
            }
        }
        builder.append(".xlass");
        return builder.toString();
    }

    /**
     * 逐字节进行原地转换，转换方式使用按位取反达到255-x的效果
     * @param bytes
     */
    private void convert(byte[] bytes) {
        for (int i=0; i< bytes.length; i++) {
            bytes[i] = (byte)~bytes[i]; //x=255 - x
        }
    }

}
