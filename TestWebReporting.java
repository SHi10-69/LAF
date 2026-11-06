import com.lostfound.service.*;
import com.lostfound.model.*;
import com.lostfound.web.WebServer;
import java.net.http.*;
import java.net.URI;
import java.util.List;

public class TestWebReporting {
    public static void main(String[] args) throws Exception {
        WebServer.start();
        int port = WebServer.getActivePort();
        System.out.println("WebServer running on port: " + port);

        HttpClient client = HttpClient.newHttpClient();

        // 1. Report a new item from web
        String itemPayload = "{"
                + "\"itemName\":\"Web Reported Apple iPad Air\","
                + "\"category\":\"Electronics\","
                + "\"itemType\":\"LOST\","
                + "\"location\":\"Science Hall 3rd Floor\","
                + "\"ownerName\":\"Taylor Web User\","
                + "\"contact\":\"taylor@example.com\","
                + "\"description\":\"Blue magnetic cover with stylus.\","
                + "\"imageBase64\":\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==\""
                + "}";

        HttpRequest reqReport = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/items"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(itemPayload))
                .build();

        HttpResponse<String> respReport = client.send(reqReport, HttpResponse.BodyHandlers.ofString());
        System.out.println("POST /api/items status: " + respReport.statusCode() + " body: " + respReport.body());

        // 2. Verify item in ItemService
        ItemService is = new ItemService();
        List<Item> allItems = is.getAllItems();
        System.out.println("Total items now in ItemService: " + allItems.size());
        boolean foundNewItem = false;
        for (Item item : allItems) {
            if ("Web Reported Apple iPad Air".equals(item.getItemName())) {
                foundNewItem = true;
                System.out.println("Found newly reported item! ID=" + item.getId() + " imagePath=" + item.getImagePath());
                break;
            }
        }
        assert foundNewItem : "Newly reported item should be in ItemService!";

        // 3. Submit a claim from web
        String claimPayload = "{"
                + "\"itemId\":1,"
                + "\"claimantName\":\"Jordan Web Claimant\","
                + "\"contact\":\"jordan@example.com\","
                + "\"proof\":\"Has receipt serial proof attached.\","
                + "\"proofBase64\":\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==\""
                + "}";

        HttpRequest reqClaim = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/claims"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(claimPayload))
                .build();

        HttpResponse<String> respClaim = client.send(reqClaim, HttpResponse.BodyHandlers.ofString());
        System.out.println("POST /api/claims status: " + respClaim.statusCode() + " body: " + respClaim.body());

        ClaimService cs = new ClaimService();
        List<Claim> allClaims = cs.getAllClaims();
        System.out.println("Total claims now in ClaimService: " + allClaims.size());
        boolean foundNewClaim = false;
        for (Claim c : allClaims) {
            if ("Jordan Web Claimant".equals(c.getClaimantName())) {
                foundNewClaim = true;
                System.out.println("Found newly submitted claim! ID=" + c.getId() + " proofImagePath=" + c.getImagePath());
                break;
            }
        }
        assert foundNewClaim : "Newly submitted claim should be in ClaimService!";

        WebServer.stop();
        System.out.println("\nWEB REPORTING & CLAIMING VERIFIED 100% OPERATIONAL!");
    }
}
