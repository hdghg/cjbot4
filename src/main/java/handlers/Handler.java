package handlers;

import com.gitlab.hdghg.cjbot4.service.BuildInfoService;
import com.gitlab.hdghg.cjbot4.service.MessageService;
import ru.sber.platformv.faas.api.HttpFunction;
import ru.sber.platformv.faas.api.HttpRequest;
import ru.sber.platformv.faas.api.HttpResponse;

import java.io.IOException;
import java.util.logging.Logger;

public class Handler implements HttpFunction {

    // Метод service. Данный метод будет обрабатывать HTTP запросы поступающие к функции
    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        if ("".equals(request.getPath())) {
            health(request, response);
            return;
        }

        // Логирование входящего запроса
        String requestBody = new String(request.getInputStream().readAllBytes());
        Logger.getGlobal().info("Request received: " + requestBody + "\nMethod: " + request.getMethod());

        // Подготовка и возврат ответа на вызов
        response.setContentType("text/plain; charset=utf-8");
        response.getWriter().write("Hello from Java11 function from github!\nYou said: " + requestBody);
    }

    private void health(HttpRequest request, HttpResponse response) throws IOException {
        String token = System.getenv("bot.key");
        String botIsOk;
        try {
            MessageService messageService = new MessageService(token, 0, null);
            messageService.getMe();
            botIsOk = "OK";
        } catch (Exception e) {
            botIsOk = "ERROR: " + e.getMessage();
        }
        String botBingKey = System.getenv("bot.bing.key");
        var sb = new StringBuilder("Bot status:\nbot.key: ");
        sb.append(null != token ? "OK" : "Not set (mandatory)").append("\nBot Test getMe(): ");
        sb.append(botIsOk).append("\nbot.bing.key: ");
        sb.append(null == botBingKey ? "NOT SET" : "OK");
        sb.append("\npom.properties:\n").append(new BuildInfoService().mavenBuildInfo());

        response.setContentType("text/plain; charset=utf-8");
        response.setStatusCode(200);
        response.getWriter().write(sb.toString());
    }
}