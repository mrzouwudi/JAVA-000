package client;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class OkHttpClientDemo {

    public static void main(String[] args) {
        OkHttpClientDemo client = new OkHttpClientDemo();
        try {
            String content = client.getHttpContnet("http://localhost:8801/");
            System.out.println(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getHttpContnet(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}
