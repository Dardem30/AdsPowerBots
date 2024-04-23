package service;

import com.twocaptcha.TwoCaptcha;
import com.twocaptcha.captcha.Coordinates;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.*;

public class RuCaptcha {
    private static RuCaptcha INSTANCE;
    private final static String API_KEY = "cbecbbf032c79503a28399a6567be755";
    private final static TwoCaptcha solver = new TwoCaptcha(API_KEY);

    static {
        solver.setHost("2captcha.com");
        solver.setDefaultTimeout(25);
        solver.setRecaptchaTimeout(600);
        solver.setPollingInterval(5);
    }

    public static RuCaptcha getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RuCaptcha();
        }
        return INSTANCE;
    }

    public static void main(String[] args) throws Exception {
        getInstance().solveCoordinatesCaptcha("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");
    }

    public CaptchaAnswer solveCoordinatesCaptcha(final String base64EncodedImage) throws Exception {
        final Date startTime = new Date();
        Coordinates captcha = new Coordinates();
        captcha.setBase64(base64EncodedImage);
        captcha.setLang("en");
        captcha.setHintText("Choose image according to the word");
        solver.solve(captcha);
        String code = captcha.getCode();
        final Date endTime = new Date();
        System.out.println(endTime + " Captcha has been solved[" + code + "] for " + ((endTime.getTime() - startTime.getTime()) / 1000) + "s ");
        final CaptchaAnswer answer = new CaptchaAnswer();
        answer.addCoordinates(code);
        answer.setAnswerId(captcha.getId());
        return answer;
    }
    public void reportBad(final CaptchaAnswer answer) {
        final RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> exchange = restTemplate.exchange("https://2captcha.com/res.php?key=" + API_KEY + "&action=reportbad&id=" + answer.getAnswerId(),
                HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                String.class);
        System.out.println("Report bad: " + exchange.getBody());
        answer.setReported(true);
    }
    public void reportGood(final CaptchaAnswer answer) {
        final RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> exchange = restTemplate.exchange("https://2captcha.com/res.php?key=" + API_KEY + "&action=reportgood&id=" + answer.getAnswerId(),
                HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                String.class);
        System.out.println("Report good: " + exchange.getBody());
        answer.setReported(true);
    }

    public static class CaptchaAnswer {
        private String answerId;
        private List<Map<String, Integer>> coordinates;
        private boolean reported;

        public boolean isReported() {
            return reported;
        }

        public void setReported(boolean reported) {
            this.reported = reported;
        }

        public String getAnswerId() {
            return answerId;
        }

        public void setAnswerId(String answerId) {
            this.answerId = answerId;
        }

        public List<Map<String, Integer>> getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(List<Map<String, Integer>> coordinates) {
            this.coordinates = coordinates;
        }

        public void addCoordinates(final String code) {
            if (coordinates == null) {
                coordinates = new ArrayList<>();
            }
            for (final String coordinate : code
                    .replaceAll("coordinates:", "")
                    .replaceAll("x=", "")
                    .replaceAll("y=", "")
                    .split(";")) {
                final String[] points = coordinate.split(",");
                final Map<String, Integer> item = new HashMap<>();
                item.put("x", Integer.valueOf(points[0]));
                item.put("y", Integer.valueOf(points[1]));
                coordinates.add(item);
            }
        }
    }
}
