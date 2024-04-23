import java.util.Arrays;
import java.util.List;

public class RandomResolutionGenerator {

    public static void main(String[] args) {
        final List<String> resolutions = Arrays.asList("2560x1440", "2304x1440", "2048x1536", "2560x1600", "1920x1440", "1920x1200");
        for (int index =0; index<40;index++) {
            System.out.println(resolutions.get(getRandomNumber(0, 6)));
        }
    }
    private static int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }
}
