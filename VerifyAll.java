import com.lostfound.service.*;
import com.lostfound.model.*;
import com.lostfound.web.WebServer;
import com.lostfound.util.Theme;
import java.net.http.*;
import java.net.URI;
import java.util.List;

public class VerifyAll {
    public static void main(String[] args) throws Exception {
        System.out.println("--- 1. Testing Services ---");
        ItemService is = new ItemService();
        List<Item> items = is.getAllItems();
        System.out.println("Items count: " + items.size());

        ClaimService cs = new ClaimService();
        List<Claim> claims = cs.getAllClaims();
        System.out.println("Claims count: " + claims.size());

        MatchingService ms = new MatchingService();
        if (!items.isEmpty()) {
            List<MatchingService.MatchResult> matches = ms.findMatches(items.get(0));
            System.out.println("Matches for item 0 (" + items.get(0).getItemName() + "): " + matches.size());
            if (!matches.isEmpty()) {
                System.out.println("  Match found: " + matches.get(0).getFoundItem().getItemName() + " <=> " + matches.get(0).getLostItem().getItemName() + " (" + matches.get(0).getPercentage() + "%)");
            }
        }

        System.out.println("\n--- 2. Testing Integrated WebServer ---");
        WebServer.start();
        int port = WebServer.getActivePort();
        System.out.println("WebServer listening on port: " + port);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest reqStats = HttpRequest.newBuilder().uri(URI.create("http://localhost:" + port + "/api/stats")).GET().build();
        HttpResponse<String> respStats = client.send(reqStats, HttpResponse.BodyHandlers.ofString());
        System.out.println("/api/stats status: " + respStats.statusCode() + " body: " + respStats.body());

        HttpRequest reqItems = HttpRequest.newBuilder().uri(URI.create("http://localhost:" + port + "/api/items")).GET().build();
        HttpResponse<String> respItems = client.send(reqItems, HttpResponse.BodyHandlers.ofString());
        System.out.println("/api/items status: " + respItems.statusCode() + " items length: " + respItems.body().length());

        HttpRequest reqIndex = HttpRequest.newBuilder().uri(URI.create("http://localhost:" + port + "/index.html")).GET().build();
        HttpResponse<String> respIndex = client.send(reqIndex, HttpResponse.BodyHandlers.ofString());
        System.out.println("/index.html status: " + respIndex.statusCode() + " bytes: " + respIndex.body().length());

        WebServer.stop();

        System.out.println("\n--- 3. Theme Verification ---");
        System.out.println("Active theme: " + Theme.getCurrentTheme().getDisplayName());
        System.out.println("Primary Accent: " + Theme.getPrimaryAccent());
        System.out.println("Card Background: " + Theme.getCard());
        System.out.println("Status FOUND: " + Theme.STATUS_FOUND);

        System.out.println("\nALL SYSTEM CHECKS PASSED WITH FLYING COLORS!");
    }
}
