package com.example.cdr.msbackend.Repository;

import com.example.cdr.msbackend.Model.Cdr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CdrRepository extends JpaRepository<Cdr, Long> {
}
