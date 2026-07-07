package com.urlshortener.controller;

import com.urlshortener.common.UrlUtil;
import com.urlshortener.dto.ShortUrl;
import com.urlshortener.error.InvalidUrlError;
import com.urlshortener.dto.FullUrl;
import com.urlshortener.service.UrlService;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.NoSuchElementException;

@RestController
public class UrlController {

    private static final Logger logger = LoggerFactory.getLogger(UrlController.class);
    private static final UrlValidator URL_VALIDATOR = new UrlValidator(new String[]{"http", "https"});

    protected final UrlService urlService;

    @Autowired
    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/shorten")
    public ResponseEntity<?> saveUrl(@RequestBody FullUrl fullUrl, HttpServletRequest request) {
        if (!URL_VALIDATOR.isValid(fullUrl.getFullUrl())) {
            logger.error("Malformed URL provided: {}", fullUrl.getFullUrl());
            return ResponseEntity.badRequest().body(new InvalidUrlError("url", fullUrl.getFullUrl(), "Invalid URL"));
        }

        String baseUrl;
        try {
            baseUrl = UrlUtil.getBaseUrl(request.getRequestURL().toString());
        } catch (MalformedURLException e) {
            logger.error("Malformed request URL", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request url is invalid", e);
        }

        ShortUrl shortUrl = urlService.getShortUrl(fullUrl);
        shortUrl.setShortUrl(baseUrl + shortUrl.getShortUrl());

        logger.debug("ShortUrl for {} is {}", fullUrl.getFullUrl(), shortUrl.getShortUrl());

        return ResponseEntity.ok(shortUrl);
    }

    @GetMapping("/{shortenString}")
    public void redirectToFullUrl(HttpServletResponse response, @PathVariable String shortenString) {
        try {
            FullUrl fullUrl = urlService.getFullUrl(shortenString);
            logger.info("Redirecting to {}", fullUrl.getFullUrl());
            response.sendRedirect(fullUrl.getFullUrl());
        } catch (NoSuchElementException e) {
            logger.error("No URL found for '{}' in the database", shortenString);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Url not found", e);
        } catch (IOException e) {
            logger.error("Could not redirect to the full url");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not redirect to the full url", e);
        }
    }
}
