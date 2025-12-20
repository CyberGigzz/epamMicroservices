package com.gym.crm.service;

import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.dto.auth.CredentialsDto; 
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder; 

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerDAO trainerDAO;
    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder; 

    @InjectMocks
    private TrainerService trainerService;

    private Trainer testTrainer;
    private TrainingType testSpecialization;
    private String encodedPassword = "encodedOldPassword"; 

    @BeforeEach
    void setUp() {
        testSpecialization = new TrainingType();
        testSpecialization.setId(1L);
        testSpecialization.setTrainingTypeName("Cardio");

        testTrainer = new Trainer();
        testTrainer.setId(1L);
        testTrainer.setUsername("test.trainer");
        testTrainer.setPassword(encodedPassword); 
        testTrainer.setFirstName("Test");
        testTrainer.setLastName("Trainer");
        testTrainer.setSpecialization(testSpecialization);
        testTrainer.setActive(true);
    }

    @Test
    void createTrainerProfile_ShouldGenerateUsernameAndPasswordAndSave() {
        String firstName = "New";
        String lastName = "Trainer";
        String expectedUsername = "new.trainer";
        String plainPassword = "randomPass"; 
        String expectedEncodedPassword = "encodedRandomPass";

        when(userService.generateUsername(firstName, lastName)).thenReturn(expectedUsername);
        when(userService.generatePlainPassword()).thenReturn(plainPassword);
        when(userService.encodePassword(plainPassword)).thenReturn(expectedEncodedPassword);

        ArgumentCaptor<Trainer> trainerCaptor = ArgumentCaptor.forClass(Trainer.class);
        when(trainerDAO.save(trainerCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        CredentialsDto credentials = trainerService.createTrainerProfile(firstName, lastName, testSpecialization);

        assertNotNull(credentials);
        assertEquals(expectedUsername, credentials.getUsername());
        assertEquals(plainPassword, credentials.getPassword()); 

        verify(userService, times(1)).generateUsername(firstName, lastName);
        verify(userService, times(1)).generatePlainPassword();
        verify(userService, times(1)).encodePassword(plainPassword);
        verify(trainerDAO, times(1)).save(any(Trainer.class));

        Trainer capturedTrainer = trainerCaptor.getValue();
        assertEquals(expectedUsername, capturedTrainer.getUsername());
        assertEquals(expectedEncodedPassword, capturedTrainer.getPassword());
        assertEquals(firstName, capturedTrainer.getFirstName());
        assertEquals(lastName, capturedTrainer.getLastName());
        assertEquals(testSpecialization, capturedTrainer.getSpecialization());
        assertTrue(capturedTrainer.isActive());
    }

    @Test
    void checkTrainerCredentials_ShouldReturnTrue_WhenCorrect() {
        String plainPassword = "oldPassword";
        when(trainerDAO.findByUsername("test.trainer")).thenReturn(Optional.of(testTrainer));
        when(passwordEncoder.matches(plainPassword, encodedPassword)).thenReturn(true); 

        assertTrue(trainerService.checkTrainerCredentials("test.trainer", plainPassword));
        verify(passwordEncoder, times(1)).matches(plainPassword, encodedPassword);
    }

    @Test
    void checkTrainerCredentials_ShouldReturnFalse_WhenPasswordIncorrect() {
        String wrongPlainPassword = "wrongPassword";
        when(trainerDAO.findByUsername("test.trainer")).thenReturn(Optional.of(testTrainer));
        when(passwordEncoder.matches(wrongPlainPassword, encodedPassword)).thenReturn(false); 

        assertFalse(trainerService.checkTrainerCredentials("test.trainer", wrongPlainPassword));
        verify(passwordEncoder, times(1)).matches(wrongPlainPassword, encodedPassword);
    }

    @Test
    void checkTrainerCredentials_ShouldReturnFalse_WhenTrainerNotFound() {
        String username = "nonexistent.trainer";
        when(trainerDAO.findByUsername(username)).thenReturn(Optional.empty());

        assertFalse(trainerService.checkTrainerCredentials(username, "anyPassword"));
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void changeTrainerPassword_ShouldSucceed_WhenCredentialsAreCorrect() {
        String username = "test.trainer";
        String oldPasswordPlain = "oldPassword";
        String newPasswordPlain = "newPassword";
        String newPasswordEncoded = "encodedNewPassword";

        when(trainerDAO.findByUsername(username)).thenReturn(Optional.of(testTrainer));
        when(passwordEncoder.matches(oldPasswordPlain, encodedPassword)).thenReturn(true);
        when(passwordEncoder.encode(newPasswordPlain)).thenReturn(newPasswordEncoded);

        boolean result = trainerService.changeTrainerPassword(username, oldPasswordPlain, newPasswordPlain);

        assertTrue(result);
        assertEquals(newPasswordEncoded, testTrainer.getPassword()); 

        verify(passwordEncoder, times(1)).matches(oldPasswordPlain, encodedPassword);
        verify(passwordEncoder, times(1)).encode(newPasswordPlain);
    }

    @Test
    void changeTrainerPassword_ShouldFail_WhenOldPasswordIsIncorrect() {
        String username = "test.trainer";
        String wrongOldPasswordPlain = "wrongPassword";
        String newPasswordPlain = "newPassword";

        when(trainerDAO.findByUsername(username)).thenReturn(Optional.of(testTrainer));
        when(passwordEncoder.matches(wrongOldPasswordPlain, encodedPassword)).thenReturn(false); 

        boolean result = trainerService.changeTrainerPassword(username, wrongOldPasswordPlain, newPasswordPlain);

        assertFalse(result);
        assertEquals(encodedPassword, testTrainer.getPassword()); 

        verify(passwordEncoder, times(1)).matches(wrongOldPasswordPlain, encodedPassword);
        verify(passwordEncoder, never()).encode(anyString());
        verify(trainerDAO, never()).save(any(Trainer.class));
    }

    @Test
    void changeTrainerPassword_ShouldFail_WhenTrainerNotFound() {
        String username = "nonexistent.trainer";
        String oldPasswordPlain = "oldPassword";
        String newPasswordPlain = "newPassword";
        when(trainerDAO.findByUsername(username)).thenReturn(Optional.empty());

        boolean result = trainerService.changeTrainerPassword(username, oldPasswordPlain, newPasswordPlain);

        assertFalse(result);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(trainerDAO, never()).save(any(Trainer.class));
    }

    @Test
    void updateTrainerProfile_ShouldUpdateFieldsAndReturnUpdatedTrainer() {
        String username = "test.trainer";
        String updatedFirstName = "UpdatedFirst";
        String updatedLastName = "UpdatedLast";
        TrainingType newSpecialization = new TrainingType();
        newSpecialization.setId(2L);
        newSpecialization.setTrainingTypeName("Yoga");
        boolean updatedIsActive = false;

        when(trainerDAO.findByUsername(username)).thenReturn(Optional.of(testTrainer));

        Optional<Trainer> updatedTrainerOpt = trainerService.updateTrainerProfile(
                username, updatedFirstName, updatedLastName, newSpecialization, updatedIsActive);

        assertTrue(updatedTrainerOpt.isPresent());
        Trainer updatedTrainer = updatedTrainerOpt.get();
        assertEquals(updatedFirstName, updatedTrainer.getFirstName());
        assertEquals(updatedLastName, updatedTrainer.getLastName());
        assertEquals(newSpecialization, updatedTrainer.getSpecialization());
        assertEquals(updatedIsActive, updatedTrainer.isActive());
    }

    @Test
    void updateTrainerProfile_ShouldReturnEmpty_WhenTrainerNotFound() {
        String username = "nonexistent.trainer";
        when(trainerDAO.findByUsername(username)).thenReturn(Optional.empty());

        Optional<Trainer> updatedTrainerOpt = trainerService.updateTrainerProfile(
                username, "Any", "Any", testSpecialization, true);

        assertTrue(updatedTrainerOpt.isEmpty());
    }


     @Test
    void activateDeactivateTrainer_ShouldChangeActiveStatusToFalse() {
        String username = "test.trainer";
        when(trainerDAO.findByUsername(username)).thenReturn(Optional.of(testTrainer));
        assertTrue(testTrainer.isActive());

        boolean result = trainerService.activateDeactivateTrainer(username, false);

        assertTrue(result);
        assertFalse(testTrainer.isActive());
    }

     @Test
    void activateDeactivateTrainer_ShouldChangeActiveStatusToTrue() {
        String username = "test.trainer";
        testTrainer.setActive(false);
        when(trainerDAO.findByUsername(username)).thenReturn(Optional.of(testTrainer));
        assertFalse(testTrainer.isActive());

        boolean result = trainerService.activateDeactivateTrainer(username, true);

        assertTrue(result);
        assertTrue(testTrainer.isActive());
    }

    @Test
    void activateDeactivateTrainer_ShouldFail_WhenTrainerNotFound() {
        String username = "nonexistent.trainer";
        when(trainerDAO.findByUsername(username)).thenReturn(Optional.empty());

        boolean result = trainerService.activateDeactivateTrainer(username, false);

        assertFalse(result);
    }

}