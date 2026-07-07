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
import java.util.NoSuchElementException;

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

    UrlEntity entity = urlRepository.findById(id)
            .orElseThrow(() ->
                    new NoSuchElementException("URL not found"));

    return new FullUrl(entity.getFullUrl());
}

    public ShortUrl getShortUrl(FullUrl fullUrl) {
       String originalUrl = fullUrl.getFullUrl();

    UrlEntity entity = urlRepository.findByFullUrl(originalUrl)
                                    .orElseGet(() -> {
            logger.info("Saving new URL: {}", originalUrl);
            return urlRepository.save(new UrlEntity(originalUrl));
        });

        String shortCode = ShorteningUtil.idToStr(entity.getId());
        logger.info("Short code for '{}' is '{}'", originalUrl, shortCode);

        return new ShortUrl(shortCode);
    }
}
