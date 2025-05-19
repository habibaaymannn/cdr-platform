package com.example.cdr.msloader.Repository;


import com.example.cdr.msloader.Model.CdrLoader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CdrLoaderRepository extends JpaRepository<CdrLoader,Long> {
}
