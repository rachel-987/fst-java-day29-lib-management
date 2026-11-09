package fstr_java.day29.lib_management.controller;

import fstr_java.day29.lib_management.dto.request.BorrowBookRequest;
import fstr_java.day29.lib_management.dto.response.LoanResponse;
import fstr_java.day29.lib_management.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/borrow")
    public ResponseEntity<LoanResponse> borrowBook(
            @Valid @RequestBody BorrowBookRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(loanService.borrowBook(request));
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<LoanResponse> returnBook(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.returnBook(id));
    }
}