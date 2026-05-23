package com.calt.buroxz.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.calt.buroxz.domain.Scope;
import com.calt.buroxz.repository.ScopeRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Scope} entity.
 */
public interface ScopeSearchRepository extends ElasticsearchRepository<Scope, Long>, ScopeSearchRepositoryInternal {}

interface ScopeSearchRepositoryInternal {
    Page<Scope> search(String query, Pageable pageable);

    Page<Scope> search(Query query);

    @Async
    void index(Scope entity);

    @Async
    void deleteFromIndexById(Long id);
}

class ScopeSearchRepositoryInternalImpl implements ScopeSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final ScopeRepository repository;

    ScopeSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, ScopeRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Page<Scope> search(String query, Pageable pageable) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery.setPageable(pageable));
    }

    @Override
    public Page<Scope> search(Query query) {
        SearchHits<Scope> searchHits = elasticsearchTemplate.search(query, Scope.class);
        List<Scope> hits = searchHits.map(SearchHit::getContent).stream().toList();
        return new PageImpl<>(hits, query.getPageable(), searchHits.getTotalHits());
    }

    @Override
    public void index(Scope entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Scope.class);
    }
}
