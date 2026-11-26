package com.librune.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Normalizes page index and page size bounds for admin list queries.
 */
public final class PaginationSupport {

    public static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MIN_PAGE_SIZE = 5;
    private static final int MAX_PAGE_SIZE = 100;

    private PaginationSupport() {
    }

    public static Pageable of(int page, int size, Sort sort) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(size, MIN_PAGE_SIZE), MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize, sort);
    }
}
