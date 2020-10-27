package client;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpClientDemo {

    public static void main(String[] args) {
        HttpClientDemo client = new HttpClientDemo();
        try {
            String content = client.getHttpContnet("http://localhost:8801/");
            System.out.println(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getHttpContnet(String url) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(url);
        try (CloseableHttpResponse response = httpclient.execute(httpget)) {
            return EntityUtils.toString(response.getEntity());
        }
    }
}
