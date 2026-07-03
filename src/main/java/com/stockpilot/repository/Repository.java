package com.stockpilot.repository;

import com.stockpilot.exception.DataAccessException;
import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {

    void save(T entity) throws DataAccessException;

    Optional<T> findById(ID id) throws DataAccessException;

    List<T> findAll() throws DataAccessException;

    void update(T entity) throws DataAccessException;

    void deleteById(ID id) throws DataAccessException;
}
