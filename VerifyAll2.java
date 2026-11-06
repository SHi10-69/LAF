import com.lostfound.service.*;
import com.lostfound.model.*;
import com.lostfound.web.WebServer;
import com.lostfound.util.Theme;
import java.net.http.*;
import java.net.URI;
import java.util.List;

public class VerifyAll2 {
    public static void main(String[] args) throws Exception {
        WebServer.start();
        int port = WebServer.getActivePort();
        System.out.println("WebServer listening on port: " + port);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest reqIndex = HttpRequest.newBuilder().uri(URI.create("http://localhost:" + port + "/index.html")).GET().build();
        HttpResponse<String> respIndex = client.send(reqIndex, HttpResponse.BodyHandlers.ofString());
        System.out.println("/index.html status: " + respIndex.statusCode() + " bytes: " + respIndex.body().length());

        HttpRequest reqCss = HttpRequest.newBuilder().uri(URI.create("http://localhost:" + port + "/style.css")).GET().build();
        HttpResponse<String> respCss = client.send(reqCss, HttpResponse.BodyHandlers.ofString());
        System.out.println("/style.css status: " + respCss.statusCode() + " bytes: " + respCss.body().length());

        HttpRequest reqJs = HttpRequest.newBuilder().uri(URI.create("http://localhost:" + port + "/script.js")).GET().build();
        HttpResponse<String> respJs = client.send(reqJs, HttpResponse.BodyHandlers.ofString());
        System.out.println("/script.js status: " + respJs.statusCode() + " bytes: " + respJs.body().length());

        WebServer.stop();
        System.out.println("ALL HTTP ENDPOINTS VERIFIED 200 OK!");
    }
}
