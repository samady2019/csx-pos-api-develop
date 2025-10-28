package kh.com.csx.posapi.dto;

import kh.com.csx.posapi.exception.ApiException;
import org.springframework.http.HttpStatus;
import java.util.List;

public class ID {
    private final Long id;
    private final List<Long> ids;

    public ID(Long id) {
        this.id = id;
        this.ids = null;
    }

    public ID(List<Long> ids) {
        this.id = null;
        this.ids = ids;
    }

    public Long id() {
        return id;
    }

    public List<Long> ids() {
        return ids;
    }

    public boolean isSingle() {
        return id != null;
    }

    public static ID id(Object rawId) {
        if (rawId instanceof Integer i) {
            return new ID(i.longValue());
        }
        if (rawId instanceof Long l) {
            return new ID(l);
        }
        if (rawId instanceof List<?> list) {
            if (!list.isEmpty() && list.stream().allMatch(e -> e instanceof Long || e instanceof Integer)) {
                List<Long> ids = list.stream().map(e -> (e instanceof Integer i) ? i.longValue() : (Long) e).toList();
                return new ID(ids);
            }
        }
        throw new ApiException("ID must be either a Long or a non-empty List of Longs.", HttpStatus.BAD_REQUEST);
    }
}
