package com.st;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface GeneratedDataRepository extends CrudRepository<GeneratedDataEntity, Long> {

    @Override
    List<GeneratedDataEntity> findAll();

    @Override
    @Transactional
    <S extends GeneratedDataEntity> Iterable<S> saveAll(Iterable<S> entities);
}
