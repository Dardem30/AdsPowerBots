package util;

import org.json.JSONObject;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonUtils {
    public static String getResponseForSeleniumDriver(final ChromeDriver driver, final String request) {
        final LogEntries logs = driver.manage().logs().get("performance");
        String result = null;
        for (final LogEntry entry : logs) {
            final JSONObject json = new JSONObject(entry.getMessage());
            final JSONObject message = json.getJSONObject("message");
            final String method = message.getString("method");

            if ("Network.responseReceived".equals(method)) {
                final JSONObject params = message.getJSONObject("params");

                final JSONObject response = params.getJSONObject("response");
                final String messageUrl = response.getString("url");

                if (messageUrl.contains(request)) {
                    result = response.toString();
                 //   break;
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {
        final String cookie1 = "remixlang=0; remixstlid=9056408439638536989_E51jqlCzUjnlcvPTpKUTF8aOZAeZjiHtCOHLwAZ61ZH; remixua=33%7C-1%7C194%7C2728520241; remixstid=1703571603_ptTjaFH7OeP9VeZRKzihzthqI01k5EVtiWYR6Ed6Zj0; remixnp=0; remixscreen_width=1920; remixscreen_height=1440; remixscreen_dpr=1; remixscreen_depth=24; remixscreen_orient=1; remixdt=0; remixdark_color_scheme=0; remixcolor_scheme_mode=auto; remixgp=6462aa9c50b7ffbafb8d31dcc9048389; tmr_lvid=c13c4aeb8598b09dcc77172103b47ff3; tmr_lvidTS=1675252594639; remixuas=YzVkMGFmNDllZmNkNWM2YmM2ZmY4NzVi; remixluas2=ODU3ODY2M2Q4OWY1MWExZWNkOGNlN2M1; remixrefkey=f49a1f80ca2a7eeb71; remixseenads=2; remixscreen_winzoom=1.42; remixbdr=0; remixsuc=1%3A; remixpuad=ObAlptjs9sgdsyXApK83rguOAGAIIA5ggI-8irnQTxE; remixnsid=vk1.a.DY-Zo-m_7eqcBI6mSNQfUpjW7hHsDLEPrlUAkjYgq2YNjBIGagOHupqzkFaLeiXbIbCdLDD4oVq7YF0cZKVNRHTZ7BRusikN3BPXIX-M-Kr8W4UkFZfTnz9UTVobbsW2-L_0zb_yoXTxOyvtKri3uYM3cQWf67Se2bNrU-U6QZr1vXoVDClKhdUWA2kZH8qp; remixsid=1_5xQsdob4YbuX9gjM6rwSjuqx7dm81jMeVprK1s7qpoSaTJJHpVqQDHhUQLuj27cgIadJuhv_pQXWcMnCKR8e-w; remixdmgr_tmp=58eef12e22a0d8679e5a3862d6ed6b5ab0514ad9f8adffee6748c1e4c7f0f46a; remixdmgr=f32e8e7f4c57d2077b304e9a04926ced6898259a361f445accc9fab0b0ea651d; tmr_detect=0%7C1675346169276";
        final String cookie2 = "remixlang=0; remixstlid=9056408439638536989_E51jqlCzUjnlcvPTpKUTF8aOZAeZjiHtCOHLwAZ61ZH; remixua=33%7C-1%7C194%7C2728520241; remixstid=1703571603_ptTjaFH7OeP9VeZRKzihzthqI01k5EVtiWYR6Ed6Zj0; remixnp=0; remixscreen_width=1920; remixscreen_height=1440; remixscreen_dpr=1; remixscreen_depth=24; remixscreen_orient=1; remixdt=0; remixdark_color_scheme=0; remixcolor_scheme_mode=auto; remixgp=6462aa9c50b7ffbafb8d31dcc9048389; tmr_lvid=c13c4aeb8598b09dcc77172103b47ff3; tmr_lvidTS=1675252594639; remixuas=YzVkMGFmNDllZmNkNWM2YmM2ZmY4NzVi; remixnsid=vk1.a.DY-Zo-m_7eqcBI6mSNQfUpjW7hHsDLEPrlUAkjYgq2YNjBIGagOHupqzkFaLeiXbIbCdLDD4oVq7YF0cZKVNRHTZ7BRusikN3BPXIX-M-Kr8W4UkFZfTnz9UTVobbsW2-L_0zb_yoXTxOyvtKri3uYM3cQWf67Se2bNrU-U6QZr1vXoVDClKhdUWA2kZH8qp; s=1; remixrefkey=f49a1f80ca2a7eeb71; remixseenads=2; remixscreen_winzoom=1.42; remixbdr=0; sui=722636568%2CRBBxe6v3UPBTZBP_IJA7Nj09melgZQDjXB4nOdi_mEQ; remixsuc=1%3A; p=vk1.a.YO0RKaeGHsGQuJxJIxyMFs5l5TNZfFPU2pJc_-aVTcRn4GT1xqhVKduXIemUEm6rcLRe-br8tiXuRIJyQU6qq9DKbeRKxO-WzlLe8tLKpifwhXVy11r0f3VCNNssKajVWQ4En0b7ErLG4JtnJJB1N1lkL09NrVdAWMXYqgZjCFE; remixpuad=ObAlptjs9sgdsyXApK83rguOAGAIIA5ggI-8irnQTxE; sua=gllGt0lk4UV-ebGjnujixpKM6BAygo6esViJjvWyTU0%23722636568%5Evk1.a.h1r1uR7cPWiTGFiJ-k3FRbXzXBMEg593KXxWip7kvw8DEZI2dd-ZYSHpWq0GICpWqDsIjz3vaRUETp2xbUwk7aBPWtu2rk4t34IsHSlUg7iTVltcRDL91ll4Voaf-pTtEIFoAf8_4BCSQOTU0PrOfFuMeH1BR3GDMs4w6wZGRl18ywT56YNrjGQNT0Cdi-G_%5E1675345435; remixsid=1_5xQsdob4YbuX9gjM6rwSjuqx7dm81jMeVprK1s7qpoSaTJJHpVqQDHhUQLuj27cgIadJuhv_pQXWcMnCKR8e-w; remixdmgr_tmp=58eef12e22a0d8679e5a3862d6ed6b5ab0514ad9f8adffee6748c1e4c7f0f46a; remixdmgr=f32e8e7f4c57d2077b304e9a04926ced6898259a361f445accc9fab0b0ea651d; remixsts=%7B%22data%22%3A%5B%5B1675346443%2C%22web_dark_theme%22%2C%22auto%22%2C%22vkcom_light%22%2C0%5D%2C%5B1675346443%2C%22feed_switch%22%2C0%2C%22top_news%22%2C3%5D%2C%5B1675346443%2C%22feed_init_video_autoplay%22%2C%22good_browser%22%5D%5D%2C%22uniqueId%22%3A424992445%7D";
        final Map<String, String> cookieNames = new HashMap<>();
        for (final String cookieRow: cookie2.split(";")) {
            String[] props = cookieRow.split("=");
            cookieNames.put(props[0].trim(), props[1].trim());
        }
        for (final String cookieRow: cookie1.split(";")) {
            final String cookieName = cookieRow.split("=")[0].trim();
            final String cookieValue = cookieRow.split("=")[1].trim();
            String s = cookieNames.get(cookieName);
            if (s == null) {
                System.out.println("Missing " + cookieName);
            } else if (!s.equals(cookieValue)){
                System.out.println("Different " + cookieName + " " + s + " " + cookieValue);
            }
        }

    }
}
