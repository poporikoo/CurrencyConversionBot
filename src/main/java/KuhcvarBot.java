import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class KuhcvarBot extends TelegramLongPollingBot {

    private final CurrencyModeService currencyModeService = CurrencyModeService.getInstance();
    private final CurrencyConversionService currencyConversionService = CurrencyConversionService.getInstance();
    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        } else if (update.hasMessage()) {
            handleMessage(update.getMessage());
        }

    }
    @SneakyThrows
    private void handleCallback(CallbackQuery callbackQuery) {
        Message message = callbackQuery.getMessage();
        String[] param = callbackQuery.getData().split(":");
        String action = param[0];
        Currency newCurrency = Currency.valueOf(param[1]);
        switch (action){
            case "ORIGINAL":
                currencyModeService.setOriginalCurrency(message.getChatId(), newCurrency);
                break;
            case "TARGET":
                currencyModeService.setTargetCurrency(message.getChatId(), newCurrency);
                break;
        }
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        Currency originalCurrency = currencyModeService.getOriginalCurrency(message.getChatId());
        Currency targetCurrency = currencyModeService.getTargetCurrency(message.getChatId());
        for (Currency currency : Currency.values()){
            buttons.add(
                    Arrays.asList(
                            InlineKeyboardButton.builder()
                                    .text(getCurrencyButton(originalCurrency, currency))
                                    .callbackData("ORIGINAL:" + currency)
                                    .build(),
                            InlineKeyboardButton.builder()
                                    .text(getCurrencyButton(targetCurrency, currency))
                                    .callbackData("TARGET:" + currency)
                                    .build()
                    ));
        }

        execute(EditMessageReplyMarkup.builder()
                .chatId(message.getChatId().toString())
                .messageId(message.getMessageId())
                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build())
                .build());
    }

    @SneakyThrows
    private void handleMessage(Message message) {
        // handle command
        if (message.hasText() && message.hasEntities()){
            Optional<MessageEntity> commandEntity =
            message.getEntities().stream().filter(e -> "bot_command".equals(e.getType())).findFirst();
            if (commandEntity.isPresent()) {
                String command = message.getText().substring(commandEntity.get().getOffset(), commandEntity.get().getLength());
                switch (command){
                    case "/currency_converter":
                        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
                        Currency originalCurrency = currencyModeService.getOriginalCurrency(message.getChatId());
                        Currency targetCurrency = currencyModeService.getTargetCurrency(message.getChatId());
                        for (Currency currency : Currency.values()){
                            buttons.add(
                                    Arrays.asList(
                                    InlineKeyboardButton.builder()
                                            .text(getCurrencyButton(originalCurrency, currency))
                                            .callbackData("ORIGINAL:" + currency)
                                            .build(),
                                    InlineKeyboardButton.builder()
                                            .text(getCurrencyButton(targetCurrency, currency))
                                            .callbackData("TARGET:" + currency)
                                            .build()
                            ));
                        }
                        execute(
                                SendMessage.builder()
                                        .text("Choose original and target currencies")
                                        .chatId(message.getChatId().toString())
                                            .replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build())
                                        .build());
                        return;
                }
            }
        }
        if (message.hasText()) {
            String messageText = message.getText();
            Optional<Double> value = parseDouble(messageText);
            Currency originalCurrency = currencyModeService.getOriginalCurrency(message.getChatId());
            Currency targetCurrency = currencyModeService.getTargetCurrency(message.getChatId());
            double ratio = currencyConversionService.getConversionRatio(originalCurrency, targetCurrency);
            if(value.isPresent()){
                execute(SendMessage.builder()
                        .chatId(message.getChatId().toString())
                        .text(
                                String.format(
                                        "%4.2f %s is %4.2f %s",
                                        value.get(), originalCurrency,(value.get() * ratio), targetCurrency))
                        .build());
                return;
            }
        }
    }

    private Optional<Double> parseDouble(String messageText) {
        try{
            return Optional.of(Double.parseDouble(messageText));
        } catch (Exception e){
            return Optional.empty();
        }
    }

    private String getCurrencyButton(Currency saved, Currency current){
        return saved == current ? current + "✅" : current.name();
    }
    @Override
    public String getBotUsername() {
        return "@KuhcvarWeather_bot";
    }

    @Override
    public String getBotToken(){
        return "6694952987:AAGfqdeMkutTG0Y0rHdShoSZeT8FHiN9_JA";
    }

    @SneakyThrows
    public static void main(String[] args){
        KuhcvarBot bot = new KuhcvarBot();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(bot);
    }
}
