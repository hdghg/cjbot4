package com.gitlab.hdghg.cjbot4.model.bing;

public class SearchResult {

    private Error error;
    private WebPages webPages;

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public WebPages getWebPages() {
        return webPages;
    }

    public void setWebPages(WebPages webPages) {
        this.webPages = webPages;
    }
}
