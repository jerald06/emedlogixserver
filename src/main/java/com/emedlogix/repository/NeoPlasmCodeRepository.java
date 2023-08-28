package com.emedlogix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.emedlogix.entity.NeoPlasmCode;

import java.util.List;
import java.util.Map;

@Repository
public interface NeoPlasmCodeRepository extends JpaRepository<NeoPlasmCode, Integer> {



}
