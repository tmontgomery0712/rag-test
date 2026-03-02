//package com.example.ragaitest.seeder;
//
//import com.example.ragaitest.entity.StreakCompletionEntity;
//import com.example.ragaitest.entity.StreakEntity;
//import com.example.ragaitest.entity.UserEntity;
//import com.example.ragaitest.repository.StreakCompletionRepository;
//import com.example.ragaitest.repository.StreakRepository;
//import com.example.ragaitest.repository.UserRepository;
//import net.datafaker.Faker;
//import org.jspecify.annotations.NonNull;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Profile;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDate;
//import java.time.ZoneId;
//import java.util.ArrayList;
//import java.util.List;
//
//@Component
//@Profile("!prod")
//public class DataSeeder implements CommandLineRunner {
//
//    private final StreakRepository streakRepository;
//    private final StreakCompletionRepository completionRepository;
//    private final UserRepository userRepository;
//    private final Faker faker = new Faker();
//
//    // Batch size control
//    private static final int BATCH_SIZE = 2000;
//
//    public DataSeeder(StreakRepository streakRepository, StreakCompletionRepository completionRepository, UserRepository userRepository) {
//        this.streakRepository = streakRepository;
//        this.completionRepository = completionRepository;
//        this.userRepository = userRepository;
//    }
//
//    @Override
//    public void run(String @NonNull ... args) {
//        // Prevent double seeding
//        //if (streakRepository.count() > 0) {
//            System.out.println("⚠️ Data exists, clearing for fresh seed...");
//            completionRepository.deleteAll();
//            streakRepository.deleteAll();
//            //userRepository.deleteAll();
//        //}
//
//        System.out.println("🌱 Seeding Database...");
//        UserEntity user = new UserEntity();
//        user.setName("Trevor Montgomery");
//        UserEntity userEntity = userRepository.save(user);
//
//
////            // 1. Create Streaks
////            List<StreakEntity> streaks = new ArrayList<>();
////            for (int i = 0; i < 10; i++) {
////                StreakEntity streak = new StreakEntity();
////                streak.setName(faker.hobby().activity() + " Streak");
////                streak.setUser(userEntity);
////                streaks.add(streak);
////            }
////            // Save streaks first so we have IDs for the completions
////            streaks = streakRepository.saveAll(streaks);
////
////            // 2. Generate History with Batching
////            List<StreakCompletionEntity> batch = new ArrayList<>();
////            LocalDate today = LocalDate.now();
////            int totalSaved = 0;
////
////            for (StreakEntity streak : streaks) {
////                System.out.println("   Generating history for: " + streak.getName());
////
////                // Iterate back ~90 years (32,850 days)
////                for (int daysAgo = 0; daysAgo < 32_850; daysAgo++) {
////                    // 98% chance to complete
////                    if (faker.random().nextInt(100) < 98) {
////                        LocalDate refDate = today.minusDays(daysAgo);
////
////                        StreakCompletionEntity completion = new StreakCompletionEntity();
////                        completion.setStreak(streak);
////                        completion.setReferenceDate(refDate);
////                        completion.setCompletionDate(refDate.atTime(8 + faker.random().nextInt(12), 30)
////                                .atZone(ZoneId.systemDefault()).toInstant());
////
////                        batch.add(completion);
////
////                        // BATCH FLUSH: Save if we hit the limit
////                        if (batch.size() >= BATCH_SIZE) {
////                            completionRepository.saveAll(batch);
////                            totalSaved += batch.size();
////                            batch.clear(); // Clear memory
////                        }
////                    }
////                }
////            }
////
////            // Save any remaining items in the list
////            if (!batch.isEmpty()) {
////                completionRepository.saveAll(batch);
////                totalSaved += batch.size();
////            }
////
////            System.out.println("✅ Database Seeded with " + totalSaved + " records.");
//    }
//}