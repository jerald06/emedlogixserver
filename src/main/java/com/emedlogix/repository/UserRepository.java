package com.emedlogix.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.emedlogix.entity.User;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {

	User findByUsername(String username);

}
