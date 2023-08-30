
public interface CurrencyConversionService {

    static CurrencyConversionService getInstance() {
        return new CbrCurrencyConversionService();
    }
    double getConversionRatio(Currency original, Currency target);
}
