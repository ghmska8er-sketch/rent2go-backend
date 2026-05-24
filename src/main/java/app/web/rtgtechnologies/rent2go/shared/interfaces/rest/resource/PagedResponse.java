package app.web.rtgtechnologies.rent2go.shared.interfaces.rest.resource;

import java.util.List;

public record PagedResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
    public PagedResponse {
        content = content == null ? List.of() : List.copyOf(content);
    }
}