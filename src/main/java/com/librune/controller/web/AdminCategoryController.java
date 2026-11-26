package com.librune.controller.web;

import com.librune.entity.Category;
import com.librune.service.CategoryService;
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
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + PaginationSupport.DEFAULT_PAGE_SIZE) int size,
            Model model) {
        Page<Category> categoryPage = categoryService.searchPage(keyword, page, size);
        model.addAttribute("activePage", "categories");
        model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("keyword", keyword);
        return "admin/categories/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("activePage", "categories");
        model.addAttribute("category", new Category());
        return "admin/categories/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("activePage", "categories");
            model.addAttribute("category", categoryService.findById(id));
            return "admin/categories/form";
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", Objects.toString(ex.getMessage(), "Không tìm thấy danh mục"));
            return "redirect:/admin/categories";
        }
    }

    @PostMapping("/save")
    public String save(
            @Valid @ModelAttribute("category") Category category,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes ra) {
        if (category.getName() != null && category.getName().trim().length() > 100) {
            bindingResult.rejectValue("name", "size.name", "Tên danh mục tối đa 100 ký tự");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("activePage", "categories");
            model.addAttribute("errorMessage", "Vui lòng kiểm tra lại tên danh mục.");
            return "admin/categories/form";
        }

        try {
            categoryService.save(category);
            ra.addFlashAttribute("successMessage", "Lưu danh mục thành công");
            return "redirect:/admin/categories";
        } catch (Exception ex) {
            model.addAttribute("activePage", "categories");
            model.addAttribute("errorMessage", Objects.toString(ex.getMessage(), "Không thể lưu danh mục"));
            return "admin/categories/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            categoryService.deleteById(id);
            ra.addFlashAttribute("successMessage", "Đã xóa danh mục #" + id);
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", Objects.toString(ex.getMessage(), "Không thể xóa danh mục"));
        }
        return "redirect:/admin/categories";
    }
}
