package mobile.application.footcardz_ui.model;

import java.util.List;

public class PlayerResponse {
    private final List<Player> content;
    private final PageInfo page;

    public PlayerResponse(List<Player> content, PageInfo page) {
        this.content = content;
        this.page = page;
    }

    public List<Player> getContent() {
        return content;
    }

    public PageInfo getPage() {
        return page;
    }

    public static class PageInfo {
        private final int size;
        private final int number;
        private final int totalElements;
        private final int totalPages;

        public PageInfo(int size, int number, int totalElements, int totalPages) {
            this.size = size;
            this.number = number;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
        }

        public int getTotalPages() {
            return totalPages;
        }
    }
}
