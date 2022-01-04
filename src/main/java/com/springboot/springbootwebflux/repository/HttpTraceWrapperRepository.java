package com.springboot.springbootwebflux.repository;

import java.util.stream.Stream;

import org.springframework.data.repository.Repository;

import com.springboot.springbootwebflux.domain.HttpTraceWrapper;

public interface HttpTraceWrapperRepository extends Repository<HttpTraceWrapper, String> {

    Stream<HttpTraceWrapper> findAll();

    void save(HttpTraceWrapper trace);
}
