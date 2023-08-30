import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class CbrCurrencyConversionService implements CurrencyConversionService {
    @Override
    public double getConversionRatio(Currency original, Currency target) {
        double originalRate = getRate(original);
        double targetRate = getRate(target);
        return originalRate / targetRate;
    }

    @SneakyThrows
    private double getRate(Currency currency) {
        if (currency == Currency.RUB) {
            return 1;
        }
        URL url = new URL("https://cbr.ru/scripts/XML_daily.asp");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        JSONObject json = new JSONObject(response.toString());
        double rate = json.getDouble("Cur_Abbreviation");
        double scale = json.getDouble("Cur_Abbreviation");
        return rate / scale;
    }
}
