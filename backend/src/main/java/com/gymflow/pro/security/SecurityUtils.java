package com.gymflow.pro.security;

import com.gymflow.pro.entity.Student;
import com.gymflow.pro.entity.User;
import com.gymflow.pro.entity.Workout;
import com.gymflow.pro.repository.StudentRepository;
import com.gymflow.pro.repository.UserRepository;
import com.gymflow.pro.repository.WorkoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component("securityUtils")
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final WorkoutRepository workoutRepository;

    public boolean isStudentRole(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_STUDENT"::equals);
    }

    public void assertOwnStudentIfStudentRole(UUID studentId, Authentication authentication) {
        if (isStudentRole(authentication) && !isOwnStudent(studentId, authentication)) {
            throw new AccessDeniedException("You can only access your own data");
        }
    }

    public void assertOwnWorkoutIfStudentRole(UUID workoutId, Authentication authentication) {
        if (isStudentRole(authentication) && !isOwnWorkout(workoutId, authentication)) {
            throw new AccessDeniedException("You can only access your own data");
        }
    }

    public Optional<Student> currentStudent(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        return currentUser(authentication).flatMap(user -> studentRepository.findByUserId(user.getId()));
    }

    private boolean isOwnStudent(UUID studentId, Authentication authentication) {
        if (studentId == null) {
            return false;
        }
        return currentStudent(authentication)
                .map(Student::getId)
                .map(studentId::equals)
                .orElse(false);
    }

    private boolean isOwnWorkout(UUID workoutId, Authentication authentication) {
        if (workoutId == null) {
            return false;
        }
        Optional<Student> student = currentStudent(authentication);
        if (student.isEmpty()) {
            return false;
        }
        return workoutRepository.findById(workoutId)
                .map(Workout::getStudent)
                .map(Student::getId)
                .map(id -> id.equals(student.get().getId()))
                .orElse(false);
    }

    private Optional<User> currentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return Optional.empty();
        }
        return userRepository.findByEmail(authentication.getName());
    }
}
