package fstr_java.day29.lib_management.repository;

import fstr_java.day29.lib_management.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {
}
