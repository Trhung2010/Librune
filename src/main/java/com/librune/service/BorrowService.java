package com.librune.service;

import com.librune.entity.Book;
import com.librune.entity.BorrowDetail;
import com.librune.entity.BorrowRequest;
import com.librune.entity.User;
import com.librune.repository.BookRepository;
import com.librune.repository.BorrowDetailRepository;
import com.librune.repository.BorrowRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BorrowService {

    private static final int DEFAULT_BORROW_DAYS = 14;

    private final BorrowRequestRepository borrowRequestRepository;
    private final BookRepository bookRepository;
    private final BorrowDetailRepository borrowDetailRepository;
    private final NotificationService notificationService;

    public BorrowService(
            BorrowRequestRepository borrowRequestRepository,
            BookRepository bookRepository,
            BorrowDetailRepository borrowDetailRepository,
            NotificationService notificationService) {
        this.borrowRequestRepository = borrowRequestRepository;
        this.bookRepository = bookRepository;
        this.borrowDetailRepository = borrowDetailRepository;
        this.notificationService = notificationService;
    }

    public List<BorrowRequest> findAll(String status) {
        if (status != null && !status.isBlank()) {
            String upper = status.trim().toUpperCase();
            if (!BorrowRequest.VALID_STATUSES.contains(upper)) {
                throw new IllegalArgumentException("Trạng thái phiếu mượn không hợp lệ: " + status);
            }
            return borrowRequestRepository.findByStatusOrderByRequestDateDesc(upper);
        }
        return borrowRequestRepository.findAllByOrderByRequestDateDesc();
    }

    public Page<BorrowRequest> searchPage(String status, String keyword, int page, int size) {
        Pageable pageable = PaginationSupport.of(page, size, Sort.by(Sort.Direction.DESC, "requestDate"));
        String cleanStatus = null;
        if (status != null && !status.isBlank()) {
            String upper = status.trim().toUpperCase();
            if (!BorrowRequest.VALID_STATUSES.contains(upper)) {
                throw new IllegalArgumentException("Trạng thái phiếu mượn không hợp lệ: " + status);
            }
            cleanStatus = upper;
        }
        String cleanKeyword = keyword != null ? keyword.trim() : null;
        return borrowRequestRepository.searchBorrows(cleanStatus, cleanKeyword, pageable);
    }

    public List<BorrowRequest> findRecent() {
        return borrowRequestRepository.findTop8ByOrderByRequestDateDesc();
    }

    public List<BorrowRequest> findByUser(Long userId, String status) {
        if (status != null && !status.isBlank()) {
            String upper = status.trim().toUpperCase();
            if (!BorrowRequest.VALID_STATUSES.contains(upper)) {
                throw new IllegalArgumentException("Trạng thái phiếu mượn không hợp lệ: " + status);
            }
            return borrowRequestRepository.findByUserIdOrderByRequestDateDesc(userId).stream()
                    .filter(r -> upper.equalsIgnoreCase(r.getStatus()))
                    .toList();
        }
        return borrowRequestRepository.findByUserIdOrderByRequestDateDesc(userId);
    }

    public BorrowRequest findById(Long id) {
        return borrowRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu mượn #" + id));
    }

    public long countByStatus(String status) {
        return borrowRequestRepository.countByStatus(status);
    }

    public long countAll() {
        return borrowRequestRepository.count();
    }

    @Transactional
    public BorrowRequest createRequest(User reader, List<Map<String, Object>> rawItems, String note) {
        if (rawItems == null || rawItems.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất 1 cuốn sách để mượn");
        }

        // Merge duplicate bookIds if submitted in the same request
        Map<Long, Integer> merged = new LinkedHashMap<>();
        for (Map<String, Object> item : rawItems) {
            Object bookIdObj = item.get("bookId");
            if (bookIdObj == null) {
                throw new IllegalArgumentException("Thiếu mã sách (bookId)");
            }
            Long bookId = ((Number) bookIdObj).longValue();
            int qty = item.get("quantity") instanceof Number n ? n.intValue() : 1;
            if (qty <= 0) {
                throw new IllegalArgumentException("Số lượng mượn phải lớn hơn 0");
            }
            merged.merge(bookId, qty, Integer::sum);
        }

        BorrowRequest request = new BorrowRequest();
        request.setUser(reader);
        request.setStatus(BorrowRequest.STATUS_PENDING);
        request.setNote(note != null && !note.isBlank() ? note.trim() : null);

        for (Map.Entry<Long, Integer> entry : merged.entrySet()) {
            Book book = bookRepository.findById(entry.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách #" + entry.getKey()));
            if (book.getAvailableQuantity() < entry.getValue()) {
                throw new IllegalStateException("Sách '" + book.getTitle() + "' chỉ còn " + book.getAvailableQuantity() + " cuốn");
            }
            BorrowDetail detail = new BorrowDetail();
            detail.setBorrowRequest(request);
            detail.setBook(book);
            detail.setQuantity(entry.getValue());
            request.getDetails().add(detail);
        }

        BorrowRequest saved = borrowRequestRepository.save(request);
        notificationService.create(
                reader,
                saved,
                "Đã gửi yêu cầu mượn #" + saved.getId(),
                "Yêu cầu mượn sách của bạn đang chờ thủ thư duyệt."
        );
        return saved;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public BorrowRequest approveRequest(Long requestId, User admin, LocalDateTime dueDate, String note) {
        BorrowRequest request = findById(requestId);
        if (!BorrowRequest.STATUS_PENDING.equals(request.getStatus())) {
            throw new IllegalStateException("Chỉ có thể duyệt phiếu đang ở trạng thái PENDING");
        }

        for (BorrowDetail detail : request.getDetails()) {
            Book book = bookRepository.findByIdWithPessimisticLock(detail.getBook().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách #" + detail.getBook().getId()));
            int activeLoans = borrowDetailRepository.countActiveLoansByBookId(book.getId());
            int total = book.getTotalQuantity() != null ? book.getTotalQuantity() : 0;
            int maxPossibleAvailable = Math.max(0, total - activeLoans);
            if (book.getAvailableQuantity() <= 0
                    || book.getAvailableQuantity() < detail.getQuantity()
                    || maxPossibleAvailable < detail.getQuantity()) {
                throw new IllegalStateException("Sách '" + book.getTitle() + "' không đủ tồn kho để duyệt");
            }
            book.setAvailableQuantity(book.getAvailableQuantity() - detail.getQuantity());
            bookRepository.save(book);
        }

        LocalDateTime now = LocalDateTime.now();
        request.setStatus(BorrowRequest.STATUS_APPROVED);
        request.setApprovedBy(admin);
        request.setBorrowDate(now);
        request.setDueDate(dueDate != null ? dueDate : now.plusDays(DEFAULT_BORROW_DAYS));
        if (note != null && !note.isBlank()) {
            request.setNote(note.trim());
        }

        BorrowRequest saved = borrowRequestRepository.save(request);
        notificationService.create(
                request.getUser(),
                saved,
                "Yêu cầu mượn #" + saved.getId() + " đã được duyệt",
                "Thủ thư đã duyệt phiếu mượn của bạn. Vui lòng nhận và trả sách đúng hạn."
        );
        return saved;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public BorrowRequest rejectRequest(Long requestId, User admin, String note) {
        BorrowRequest request = findById(requestId);
        if (!BorrowRequest.STATUS_PENDING.equals(request.getStatus())) {
            throw new IllegalStateException("Chỉ có thể từ chối phiếu đang ở trạng thái PENDING");
        }
        request.setStatus(BorrowRequest.STATUS_REJECTED);
        request.setApprovedBy(admin);
        if (note != null && !note.isBlank()) {
            request.setNote(note.trim());
        }

        BorrowRequest saved = borrowRequestRepository.save(request);
        notificationService.create(
                request.getUser(),
                saved,
                "Yêu cầu mượn #" + saved.getId() + " bị từ chối",
                note != null && !note.isBlank()
                        ? "Lý do: " + note.trim()
                        : "Yêu cầu mượn sách của bạn chưa được chấp thuận."
        );
        return saved;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public BorrowRequest confirmReturn(Long requestId, User admin, String note) {
        BorrowRequest request = findById(requestId);
        if (!BorrowRequest.STATUS_APPROVED.equals(request.getStatus())) {
            throw new IllegalStateException("Chỉ có thể xác nhận trả cho phiếu đang ở trạng thái APPROVED");
        }

        for (BorrowDetail detail : request.getDetails()) {
            Book book = detail.getBook();
            int nextAvailable = Math.min(book.getTotalQuantity(), book.getAvailableQuantity() + detail.getQuantity());
            book.setAvailableQuantity(nextAvailable);
            bookRepository.save(book);
        }

        request.setStatus(BorrowRequest.STATUS_RETURNED);
        request.setReturnDate(LocalDateTime.now());
        request.setApprovedBy(admin);
        if (note != null && !note.isBlank()) {
            request.setNote(note.trim());
        }

        BorrowRequest saved = borrowRequestRepository.save(request);
        notificationService.create(
                request.getUser(),
                saved,
                "Đã xác nhận trả sách #" + saved.getId(),
                "Thủ thư đã xác nhận hoàn trả sách và cập nhật tồn kho."
        );
        return saved;
    }

    @Transactional
    public BorrowRequest cancelByReader(Long requestId, Long readerId) {
        BorrowRequest request = findById(requestId);
        if (!request.getUser().getId().equals(readerId)) {
            throw new AccessDeniedException("Bạn không có quyền hủy phiếu mượn này");
        }
        if (!BorrowRequest.STATUS_PENDING.equals(request.getStatus())) {
            throw new IllegalStateException("Chỉ có thể hủy phiếu khi đang ở trạng thái PENDING");
        }
        request.setStatus(BorrowRequest.STATUS_CANCELLED);
        BorrowRequest saved = borrowRequestRepository.save(request);
        notificationService.create(
                request.getUser(),
                saved,
                "Đã hủy yêu cầu mượn #" + saved.getId(),
                "Bạn đã hủy yêu cầu mượn sách này."
        );
        return saved;
    }
}
