package com.gitlab.hdghg.cjbot3.service;

import com.gitlab.hdghg.cjbot3.model.ChatMessage;
import com.gitlab.hdghg.cjbot3.module.Module;
import com.gitlab.hdghg.cjbot3.module.bing.BingSearchModule;
import com.gitlab.hdghg.cjbot3.module.bing.SearchClient;
import com.gitlab.hdghg.cjbot3.module.puk.PukModule;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.WebhookInfo;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.BaseResponse;

import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class MessageService {

    private final TelegramBot telegramBot;
    private final PukModule pukModule;
    private final BingSearchModule bingSearchModule;

    public MessageService(String token, long myId, String searchKey) {
        telegramBot = new TelegramBot.Builder(token).build();
        this.pukModule = new PukModule(myId);
        this.bingSearchModule = new BingSearchModule(new SearchClient(), searchKey);
    }

    public void processUpdate(Update update) {
        Logger.getGlobal().info("Processing update " + update.updateId());
        Optional.ofNullable(update.message())
                .ifPresent(this::processMessage);
    }

    public void startPolling() {
        telegramBot.setUpdatesListener(list -> {
            list.forEach(update -> Optional.ofNullable(update.message())
                    .ifPresent(MessageService.this::processMessage));
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void processMessage(Message message) {
        Stream<Module> modules = Stream.of(this.bingSearchModule, pukModule);
        modules.map(m -> m.processMessage(message))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .map(this::toSendMessage)
                .ifPresent(telegramBot::execute);
    }

    /**
     * Convert module-agnostic entity to a telegram response
     *
     * @param chatMessage Message abstraction
     * @return Telegram's entity containing message
     */
    SendMessage toSendMessage(ChatMessage chatMessage) {
        var result = new SendMessage(chatMessage.chat, chatMessage.message);
        result.parseMode(ParseMode.HTML);
        if (null != chatMessage.replyToMessageId) {
            result.replyToMessageId(chatMessage.replyToMessageId);
        }
        return result;
    }

    public User getMe() {
        return telegramBot.execute(new GetMe()).user();
    }

    public WebhookInfo getWebhook() {
        return telegramBot.execute(new GetWebhookInfo()).webhookInfo();
    }

    public BaseResponse deleteWebhook() {
        return telegramBot.execute(new DeleteWebhook());
    }

    public BaseResponse activateWebhook(String url) {
        return telegramBot.execute(new SetWebhook().url(url).maxConnections(1));
    }

    public static MessageService forEnvironment() {
        String key = System.getenv("bot.key");
        long id = Long.parseLong(key.split(":", 2)[0]);
        String searchKey = System.getenv("bot.bing.key");
        return new MessageService(key, id, searchKey);
    }

}
