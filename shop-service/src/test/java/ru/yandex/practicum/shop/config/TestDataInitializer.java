//package ru.yandex.practicum.shop.config;
//
//import org.springframework.boot.context.event.ApplicationReadyEvent;
//import org.springframework.context.annotation.Profile;
//import org.springframework.context.event.EventListener;
//import org.springframework.stereotype.Component;
//import ru.yandex.practicum.shop.model.User;
//import ru.yandex.practicum.shop.repository.UserRepository;
//
//@Component
//@Profile("test")
//public class TestDataInitializer {
//    private final UserRepository userRepository;
//
//    public TestDataInitializer(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    @EventListener(ApplicationReadyEvent.class)
//    public void initializeTestData() {
//        User testUser = new User(1L, "testuser", "password", "USER");
//        userRepository.save(testUser).block();
//    }
//}