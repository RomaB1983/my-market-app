package ru.yandex.practicum.shop.service;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImageService {

    @Value("${app.image.dir:static}")
    private String dirProperty;

    private Path imageDirectory;

    @PostConstruct
    public void init() {
        this.imageDirectory = Paths.get(dirProperty);
    }

    public Mono<Resource> getImageAsResource(String filename) {
        try {
            Path filePath = imageDirectory.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            return Mono.fromCallable(() -> {
                        if (resource.exists() && resource.isReadable()) {
                            return resource;
                        } else {
                            throw new FileNotFoundException("Файл не найден: " + filename);
                        }
                    })
                    .onErrorResume(ex -> Mono.empty()); // Возвращаем пустой Mono, если файл не найден
        } catch (Exception e) {
            return Mono.empty();
        }
    }
}