package com.SchedularApp.Repositories;

import com.SchedularApp.Entities.TableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TableRepository extends JpaRepository<TableEntity, Long> {
    Optional<TableEntity> findByName(String tableName);
}
