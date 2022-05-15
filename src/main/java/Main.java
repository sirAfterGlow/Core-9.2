import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;

public class Main {

    public static final String NASA_API_REQUEST = "https://api.nasa.gov/planetary/apod?api_key=";
    public static final String API_KEY = "xP3dmCKmmCud5OlPbAv7oVsC6D4HFQhfsVYdbRYq";

    public static CloseableHttpClient getHttpClient() throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5_000)
                        .setSocketTimeout(30_000)
                        .setRedirectsEnabled(false)
                        .build())
                .build();
        return httpClient;
    }

    public static NasaApiResponse serialiseNasaResponse(CloseableHttpResponse response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        NasaApiResponse nasaApiResponse = mapper.readValue(response.getEntity().getContent(),
                new TypeReference<NasaApiResponse>() {});
        return nasaApiResponse;
    }

    public static boolean savePicture(InputStream in) {

        try (FileOutputStream out = new FileOutputStream("pic.jpg");
             BufferedOutputStream bos = new BufferedOutputStream(out)){
            byte[] bytes = in.readAllBytes();

            bos.write(bytes, 0, bytes.length);
            return true;
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        HttpGet request = new HttpGet(NASA_API_REQUEST + API_KEY);

        try {
            CloseableHttpResponse response = getHttpClient().execute(request);
            NasaApiResponse nasaApiResponse = serialiseNasaResponse(response);

            if(nasaApiResponse.getMediaType().equals("image")) {
                String pictureUrl = nasaApiResponse.getUrl();
                request = new HttpGet(pictureUrl);
                response = getHttpClient().execute(request);

                InputStream in = response.getEntity().getContent();
                savePicture(in);
            }

        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }

    }
}
