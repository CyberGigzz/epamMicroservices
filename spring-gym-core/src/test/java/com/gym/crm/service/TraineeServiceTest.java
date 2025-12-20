package com.gym.crm.service;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.dto.auth.CredentialsDto; 
import com.gym.crm.model.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder; 

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private TraineeDAO traineeDAO;
    @Mock
    private TrainerDAO trainerDAO;
    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder; 

    @InjectMocks
    private TraineeService traineeService;

    private Trainee testTrainee;
    private String encodedPassword = "encodedOldPassword"; 

    @BeforeEach
    void setUp() {
        testTrainee = new Trainee();
        testTrainee.setId(1L);
        testTrainee.setUsername("test.user");
        testTrainee.setPassword(encodedPassword); 
        testTrainee.setFirstName("Test");
        testTrainee.setLastName("User");
        testTrainee.setActive(true);
        testTrainee.setDateOfBirth(LocalDate.of(1995, 5, 15));
        testTrainee.setAddress("123 Test St");
    }

    @Test
    void createTraineeProfile_ShouldSucceedAndReturnCredentials() {
        String firstName = "New";
        String lastName = "User";
        LocalDate dob = LocalDate.of(2000, 1, 1);
        String address = "456 Main St";
        String expectedUsername = "new.user";
        String plainPassword = "randomPass123"; 
        String expectedEncodedPassword = "encodedRandomPass123"; 

        when(userService.generateUsername(firstName, lastName)).thenReturn(expectedUsername);
        when(userService.generatePlainPassword()).thenReturn(plainPassword); 
        when(userService.encodePassword(plainPassword)).thenReturn(expectedEncodedPassword); 
        ArgumentCaptor<Trainee> traineeCaptor = ArgumentCaptor.forClass(Trainee.class);
        when(traineeDAO.save(traineeCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        CredentialsDto credentials = traineeService.createTraineeProfile(firstName, lastName, dob, address);

        assertNotNull(credentials);
        assertEquals(expectedUsername, credentials.getUsername());
        assertEquals(plainPassword, credentials.getPassword()); 

        verify(userService, times(1)).generateUsername(firstName, lastName);
        verify(userService, times(1)).generatePlainPassword();
        verify(userService, times(1)).encodePassword(plainPassword);
        verify(traineeDAO, times(1)).save(any(Trainee.class));

        Trainee capturedTrainee = traineeCaptor.getValue();
        assertEquals(expectedUsername, capturedTrainee.getUsername());
        assertEquals(expectedEncodedPassword, capturedTrainee.getPassword()); 
        assertEquals(firstName, capturedTrainee.getFirstName());
        assertEquals(lastName, capturedTrainee.getLastName());
        assertEquals(dob, capturedTrainee.getDateOfBirth());
        assertEquals(address, capturedTrainee.getAddress());
        assertTrue(capturedTrainee.isActive());
    }

    @Test
    void changeTraineePassword_ShouldSucceed_WhenCredentialsAreCorrect() {
        String username = "test.user";
        String oldPasswordPlain = "oldPassword"; 
        String newPasswordPlain = "newPassword"; 
        String newPasswordEncoded = "encodedNewPassword"; 

        when(traineeDAO.findByUsername(username)).thenReturn(Optional.of(testTrainee));
        when(passwordEncoder.matches(oldPasswordPlain, encodedPassword)).thenReturn(true); 
        when(passwordEncoder.encode(newPasswordPlain)).thenReturn(newPasswordEncoded); 

        boolean result = traineeService.changeTraineePassword(username, oldPasswordPlain, newPasswordPlain);

        assertTrue(result);
        assertEquals(newPasswordEncoded, testTrainee.getPassword());

        verify(passwordEncoder, times(1)).matches(oldPasswordPlain, encodedPassword);
        verify(passwordEncoder, times(1)).encode(newPasswordPlain);
    }

    @Test
    void changeTraineePassword_ShouldFail_WhenOldPasswordIsIncorrect() {
        String username = "test.user";
        String wrongOldPasswordPlain = "wrongOldPassword";
        String newPasswordPlain = "newPassword";

        when(traineeDAO.findByUsername(username)).thenReturn(Optional.of(testTrainee));
        when(passwordEncoder.matches(wrongOldPasswordPlain, encodedPassword)).thenReturn(false);

        boolean result = traineeService.changeTraineePassword(username, wrongOldPasswordPlain, newPasswordPlain);

        assertFalse(result);
        assertEquals(encodedPassword, testTrainee.getPassword()); 

        verify(passwordEncoder, times(1)).matches(wrongOldPasswordPlain, encodedPassword);
        verify(passwordEncoder, never()).encode(anyString()); 
        verify(traineeDAO, never()).save(any(Trainee.class)); 
    }

    @Test
    void changeTraineePassword_ShouldFail_WhenTraineeNotFound() {
        String username = "nonexistent.user";
        String oldPasswordPlain = "oldPassword";
        String newPasswordPlain = "newPassword";
        when(traineeDAO.findByUsername(username)).thenReturn(Optional.empty());

        boolean result = traineeService.changeTraineePassword(username, oldPasswordPlain, newPasswordPlain);

        assertFalse(result);

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(traineeDAO, never()).save(any(Trainee.class));
    }

    @Test
    void activateDeactivateTrainee_ShouldChangeActiveStatusToFalse() {
        String username = "test.user";
        when(traineeDAO.findByUsername(username)).thenReturn(Optional.of(testTrainee));
        assertTrue(testTrainee.isActive());

        boolean result = traineeService.activateDeactivateTrainee(username, false);

        assertTrue(result);
        assertFalse(testTrainee.isActive());
    }

    @Test
    void activateDeactivateTrainee_ShouldChangeActiveStatusToTrue() {
        String username = "test.user";
        testTrainee.setActive(false); 
        when(traineeDAO.findByUsername(username)).thenReturn(Optional.of(testTrainee));
        assertFalse(testTrainee.isActive());

        boolean result = traineeService.activateDeactivateTrainee(username, true);

        assertTrue(result);
        assertTrue(testTrainee.isActive());
    }

    @Test
    void activateDeactivateTrainee_ShouldFail_WhenTraineeNotFound() {
        String username = "nonexistent.user";
        when(traineeDAO.findByUsername(username)).thenReturn(Optional.empty());

        boolean result = traineeService.activateDeactivateTrainee(username, false);

        assertFalse(result);
    }

    @Test
    void updateTraineeProfile_ShouldSucceedAndReturnUpdatedTrainee() {
        String username = "test.user";
        String updatedFirstName = "UpdatedJohn";
        String updatedLastName = "UpdatedDoe";
        LocalDate updatedDob = LocalDate.of(1992, 2, 2);
        String updatedAddress = "456 Updated St";
        boolean updatedIsActive = false;

        when(traineeDAO.findByUsername(username)).thenReturn(Optional.of(testTrainee));

        Optional<Trainee> resultOpt = traineeService.updateTraineeProfile(username, updatedFirstName, updatedLastName, updatedDob, updatedAddress, updatedIsActive);

        assertTrue(resultOpt.isPresent());
        Trainee updatedTrainee = resultOpt.get();
        assertEquals(updatedFirstName, updatedTrainee.getFirstName());
        assertEquals(updatedLastName, updatedTrainee.getLastName());
        assertEquals(updatedDob, updatedTrainee.getDateOfBirth());
        assertEquals(updatedAddress, updatedTrainee.getAddress());
        assertEquals(updatedIsActive, updatedTrainee.isActive());
    }

    @Test
    void updateTraineeProfile_ShouldReturnEmpty_WhenTraineeNotFound() {
        String username = "nonexistent.user";
        when(traineeDAO.findByUsername(username)).thenReturn(Optional.empty());

        Optional<Trainee> resultOpt = traineeService.updateTraineeProfile(username, "New", "Name", null, null, true);

        assertTrue(resultOpt.isEmpty());
    }

    @Test
    void deleteTraineeProfileByUsername_ShouldSucceed_WhenTraineeExists() {
        String username = "test.user";
        when(traineeDAO.findByUsername(username)).thenReturn(Optional.of(testTrainee));
        doNothing().when(traineeDAO).delete(testTrainee); 

        boolean result = traineeService.deleteTraineeProfileByUsername(username);

        assertTrue(result);

        verify(traineeDAO, times(1)).delete(testTrainee);
    }

    @Test
    void deleteTraineeProfileByUsername_ShouldFail_WhenTraineeNotFound() {
        String username = "nonexistent.user";
        when(traineeDAO.findByUsername(username)).thenReturn(Optional.empty());

        boolean result = traineeService.deleteTraineeProfileByUsername(username);

        assertFalse(result);

        verify(traineeDAO, never()).delete(any(Trainee.class));
    }

}