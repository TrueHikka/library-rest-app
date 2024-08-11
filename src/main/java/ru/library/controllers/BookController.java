package ru.library.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.library.models.Book;
import ru.library.models.Person;
import ru.library.services.book_service.BookServiceInf;
import ru.library.services.people_service.PeopleServiceInf;

import java.util.List;

@RestController
@RequestMapping("api/books")
public class BookController {
    private final BookServiceInf bookServiceInf;
    private final PeopleServiceInf peopleServiceinf;
    private final String redirectAllBooks = "redirect:/books";

    @Autowired
    public BookController(BookServiceInf bookServiceInf, PeopleServiceInf peopleServiceinf) {
        this.bookServiceInf = bookServiceInf;
        this.peopleServiceinf = peopleServiceinf;
    }

    @GetMapping()
    public String getAllBooks(Model model) {
        List<Book>  books = bookServiceInf.getAllBooks();
        model.addAttribute("allBooks", books);
        return "books/view-all-books";
    }

    @GetMapping("/{id}")
    public String getBookById(@PathVariable("id") Long bookId, Model model) {
        Book book = bookServiceInf.findBookById(bookId);
        List<Person> people = peopleServiceinf.getAllPeople();
        model.addAttribute("book", book);
        model.addAttribute("allPeople", people);
        return "books/view-book";
    }

    @GetMapping("/new")
    public String getPageToCreateNewBook(Model model) {
        model.addAttribute("newBook", new Book());
        return "books/view-to-create-new-book";
    }

    @PostMapping()
    public String createBook(@ModelAttribute("newBook") @Valid Book book, BindingResult result) {
        if(result.hasErrors()) {
            return "books/view-to-create-new-book";
        }
        bookServiceInf.save(book);
        return redirectAllBooks;
    }

    @GetMapping("/{id}/edit")
    public String getPageToEditBook(@PathVariable("id") Long bookId, Model model) {
        Book book = bookServiceInf.findBookById(bookId);
        model.addAttribute("editedBook", book);
        return "books/view-to-edit-book";
    }

    @PostMapping("/{id}")
    public String updateBook(@ModelAttribute("bookById") @Valid Book book, BindingResult result, @PathVariable("id") Long bookId) {
        if(result.hasErrors()) {
            return "books/view-to-edit-book";
        }
        bookServiceInf.update(book, bookId);
        return redirectAllBooks;
    }

    @PostMapping("/{id}/delete")
    public String deleteBook(@PathVariable("id") Long bookId) {
        bookServiceInf.delete(bookId);
        return redirectAllBooks;
    }

    @PostMapping("/{id}/assign")
    public String assignBook(@PathVariable("id") Long bookId, @RequestParam("personId") Long personId) {
        bookServiceInf.assignBookToPerson(bookId, personId);
        return "redirect:/books/" + bookId;
    }

    @PostMapping("/{id}/free")
    public String freeBook(@PathVariable("id") Long bookId) {
        bookServiceInf.freeBook(bookId);
        return "redirect:/books/" + bookId;
    }
}
