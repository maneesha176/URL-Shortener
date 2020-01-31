package com.urlshortener.service;

import com.urlshortener.repository.UrlRepository;
import com.urlshortener.common.ShorteningUtil;
import com.urlshortener.model.UrlEntity;
import com.urlshortener.dto.FullUrl;
import com.urlshortener.dto.ShortUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UrlService {

    private static final Logger logger = LoggerFactory.getLogger(UrlService.class);

    private final UrlRepository urlRepository;

    @Autowired
    public UrlService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    public FullUrl getFullUrl(String shortenString) {
        Long id = ShorteningUtil.strToId(shortenString);
        logger.info("Resolved short code '{}' to id {}", shortenString, id);
        return new FullUrl(urlRepository.findById(id).get().getFullUrl());
    }

    public ShortUrl getShortUrl(FullUrl fullUrl) {
        UrlEntity entity = urlRepository.findUrlByFullUrl(fullUrl.getFullUrl())
                .orElseGet(() -> {
                    logger.info("Saving new URL: {}", fullUrl.getFullUrl());
                    return urlRepository.save(new UrlEntity(fullUrl.getFullUrl()));
                });

        String shortCode = ShorteningUtil.idToStr(entity.getId());
        logger.info("Short code for '{}' is '{}'", fullUrl.getFullUrl(), shortCode);

        return new ShortUrl(shortCode);
    }
}
