package com.librune.controller.web;

import com.librune.entity.Author;
import com.librune.service.AuthorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/authors")
public class AdminAuthorController {

    private final AuthorService authorService;

    public AdminAuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("activePage", "authors");
        model.addAttribute("authors", authorService.search(keyword));
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
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/authors";
        }
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Author author, RedirectAttributes ra) {
        try {
            authorService.save(author);
            ra.addFlashAttribute("successMessage", "Lưu tác giả thành công");
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/authors";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            authorService.deleteById(id);
            ra.addFlashAttribute("successMessage", "Đã xóa tác giả #" + id);
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/authors";
    }
}
