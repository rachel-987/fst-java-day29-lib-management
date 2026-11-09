package fstr_java.day29.lib_management.service;

import fstr_java.day29.lib_management.dto.request.BorrowBookRequest;
import fstr_java.day29.lib_management.dto.response.LoanResponse;

public interface LoanService {
    LoanResponse borrowBook(BorrowBookRequest request);

    LoanResponse returnBook(Long loanId);
}
