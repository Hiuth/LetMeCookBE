package com.example.letmecookbe.repository;

import com.example.letmecookbe.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    boolean existsAccountByUsername(String username);
    boolean existsAccountByEmail(String email);
    Optional<Account> findAccountByEmail(String email);
    Optional<Account> findByUsername(String username);
    @Query("SELECT a FROM Account a WHERE a.email LIKE %:keyword%")
    List<Account> searchByEmail(String keyword);
    @Query("SELECT a FROM Account a JOIN a.roles r WHERE r.name = :roleName")
    List<Account> findAllByRoles_Name(String roleName);
}
