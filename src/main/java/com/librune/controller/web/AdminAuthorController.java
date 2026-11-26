package com.librune.controller.web;

import com.librune.entity.Author;
import com.librune.service.AuthorService;
import com.librune.service.PaginationSupport;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;

@Controller
@RequestMapping("/admin/authors")
public class AdminAuthorController {

    private final AuthorService authorService;

    public AdminAuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + PaginationSupport.DEFAULT_PAGE_SIZE) int size,
            Model model) {
        Page<Author> authorPage = authorService.searchPage(keyword, page, size);
        model.addAttribute("activePage", "authors");
        model.addAttribute("authorPage", authorPage);
        model.addAttribute("authors", authorPage.getContent());
        model.addAttribute("keyword", keyword);
        return "admin/authors/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("activePage", "authors");
        model.addAttribute("author", new Author());
        return "admin/authors/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("activePage", "authors");
            model.addAttribute("author", authorService.findById(id));
            return "admin/authors/form";
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", Objects.toString(ex.getMessage(), "Không tìm thấy tác giả"));
            return "redirect:/admin/authors";
        }
    }

    @PostMapping("/save")
    public String save(
            @Valid @ModelAttribute("author") Author author,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes ra) {
        if (author.getName() != null && author.getName().trim().length() > 100) {
            bindingResult.rejectValue("name", "size.name", "Tên tác giả tối đa 100 ký tự");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("activePage", "authors");
            model.addAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin tác giả.");
            return "admin/authors/form";
        }

        try {
            authorService.save(author);
            ra.addFlashAttribute("successMessage", "Lưu tác giả thành công");
            return "redirect:/admin/authors";
        } catch (Exception ex) {
            model.addAttribute("activePage", "authors");
            model.addAttribute("errorMessage", Objects.toString(ex.getMessage(), "Không thể lưu tác giả"));
            return "admin/authors/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            authorService.deleteById(id);
            ra.addFlashAttribute("successMessage", "Đã xóa tác giả #" + id);
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", Objects.toString(ex.getMessage(), "Không thể xóa tác giả"));
        }
        return "redirect:/admin/authors";
    }
}
