package com.gitlab.hdghg.cjbot4;

import com.gitlab.hdghg.cjbot4.service.MessageService;

public class CjbotApp {

    public static void main(String[] args) {
        MessageService.forEnvironment()
                .startPolling();
    }
}
