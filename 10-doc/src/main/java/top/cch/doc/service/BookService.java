package top.cch.doc.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.cch.doc.common.BusinessException;
import top.cch.doc.common.ErrorCode;
import top.cch.doc.common.PageResult;
import top.cch.doc.dto.BookCreateRequest;
import top.cch.doc.dto.BookQuery;
import top.cch.doc.entity.Book;
import top.cch.doc.entity.Category;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class BookService {

    private final Map<Long, Book> store = new ConcurrentHashMap<>();

    private final AtomicLong idGenerator = new AtomicLong(0);

    public BookService() {
        seed("深入理解 Java 虚拟机", "周志明", "9787111641247", Category.TECH, "129.00", 10, "2019-12-01");
        seed("Effective Java", "Joshua Bloch", "9787111213828", Category.TECH, "89.00", 12, "2008-01-01");
        seed("三体", "刘慈欣", "9787536692930", Category.LITERATURE, "23.00", 20, "2008-01-01");
        seed("万历十五年", "黄仁宇", "9787101052039", Category.HISTORY, "18.00", 6, "2007-01-01");
        seed("时间简史", "史蒂芬·霍金", "9787535732309", Category.SCIENCE, "45.00", 8, "2010-04-01");
    }

    public PageResult<Book> page(BookQuery query) {
        List<Book> all = store.values().stream()
                .filter(book -> matchesKeyword(book, query.getKeyword()))
                .filter(book -> matchesCategory(book, query.getCategory()))
                .sorted(Comparator.comparing(Book::getId))
                .toList();
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();
        int from = (pageNum - 1) * pageSize;
        int to = Math.min(from + pageSize, all.size());
        List<Book> pageList = from >= all.size() ? List.of() : all.subList(from, to);
        return new PageResult<>(pageList, all.size(), pageNum, pageSize);
    }

    public Book getById(Long id) {
        Book book = store.get(id);
        if (book == null) {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
        }
        return book;
    }

    public Book create(BookCreateRequest request) {
        boolean duplicated = store.values().stream()
                .anyMatch(book -> book.getIsbn().equals(request.getIsbn()));
        if (duplicated) {
            throw new BusinessException(ErrorCode.BOOK_ISBN_DUPLICATED);
        }
        Book book = new Book();
        book.setId(idGenerator.incrementAndGet());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setCategory(request.getCategory());
        book.setPrice(request.getPrice());
        book.setStock(request.getStock() == null ? 0 : request.getStock());
        book.setPublishDate(request.getPublishDate());
        book.setCreateTime(LocalDateTime.now());
        store.put(book.getId(), book);
        return book;
    }

    public void delete(Long id) {
        Book removed = store.remove(id);
        if (removed == null) {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
        }
    }

    public List<Category> categories() {
        return Arrays.asList(Category.values());
    }

    private void seed(String title, String author, String isbn, Category category,
                      String price, int stock, String publishDate) {
        Book book = new Book();
        book.setId(idGenerator.incrementAndGet());
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setCategory(category);
        book.setPrice(new BigDecimal(price));
        book.setStock(stock);
        book.setPublishDate(LocalDate.parse(publishDate));
        book.setCreateTime(LocalDateTime.now());
        store.put(book.getId(), book);
    }

    private boolean matchesKeyword(Book book, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String kw = keyword.toLowerCase();
        return book.getTitle().toLowerCase().contains(kw)
                || book.getAuthor().toLowerCase().contains(kw);
    }

    private boolean matchesCategory(Book book, Category category) {
        return category == null || book.getCategory() == category;
    }
}
