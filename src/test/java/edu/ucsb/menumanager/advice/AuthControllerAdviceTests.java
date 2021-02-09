package edu.ucsb.menumanager.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.auth0.jwt.interfaces.DecodedJWT;
import edu.ucsb.menumanager.entities.Admin;
import edu.ucsb.menumanager.entities.AppUser;
import edu.ucsb.menumanager.repositories.AdminRepository;
import edu.ucsb.menumanager.repositories.AppUserRepository;
import edu.ucsb.menumanager.services.MembershipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class AuthControllerAdviceTests {

  @InjectMocks
  AuthControllerAdvice authControllerAdvice;

  @Mock
  MembershipService mockMembershipService;
  @Mock
  AppUserRepository mockAppUserRepository;
  @Mock
  AdminRepository mockAdminRepository;

  private String exampleAuthToken =
      "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJodHRwczovL3Rlc3QtYXBwLmNvbSI6eyJlbWFpbCI6InRlc3RAdWNzYi5lZHUiLCJnaXZlbl9uYW1lIjoiVGVzdCIsImZhbWlseV9uYW1lIjoiVXNlciJ9LCJzdWIiOiIxMjM0NTYiLCJuYW1lIjoiSm9obiBEb2UiLCJpYXQiOjE1MTYyMzkwMjJ9.s0eGBAgVvby7Y7Q34qI1E7HqqFbrneIhvzpC_MI-B30";

  private AppUser exampleUser = new AppUser(1L, "test@ucsb.edu", "Test", "User");

  @BeforeEach
  public void setUp() {
    ReflectionTestUtils.setField(authControllerAdvice, "namespace", "https://test-app.com");
  }

  @Test
  public void test_getJWT() {
    DecodedJWT jwt = authControllerAdvice.getJWT(exampleAuthToken);
    assertEquals("John Doe", jwt.getClaim("name").asString());
  }

  @Test
  public void test_getUser_alreadyExists() {
    List<AppUser> users = new ArrayList<AppUser>();
    users.add(exampleUser);
    when(mockAppUserRepository.findByEmail(exampleUser.getEmail())).thenReturn(users);
    assertEquals(exampleUser, authControllerAdvice.getUser(exampleAuthToken));
  }

  @Test
  public void test_getUser_createNewUser() {
    when(mockAppUserRepository.save(any(AppUser.class))).thenReturn(exampleUser);
    assertEquals(exampleUser, authControllerAdvice.getUser(exampleAuthToken));
  }

  @Test
  public void test_getUser_createNewUserAndAdmin() {
    when(mockAppUserRepository.save(any(AppUser.class))).thenReturn(exampleUser);
    when(mockMembershipService.isAdmin(any(DecodedJWT.class))).thenReturn(true);
    assertEquals(exampleUser, authControllerAdvice.getUser(exampleAuthToken));
    verify(mockAdminRepository, times(1)).save(new Admin(exampleUser.getEmail(), true));
  }

  @Test
  public void test_getUser_userExists_butCreatesNewDefaultAdmin() {
    List<AppUser> users = new ArrayList<AppUser>();
    users.add(exampleUser);
    when(mockAppUserRepository.findByEmail(any(String.class))).thenReturn(users);
    when(mockMembershipService.isAdmin(any(DecodedJWT.class))).thenReturn(true);
    when(mockMembershipService.getDefaultAdminEmails())
        .thenReturn(new ArrayList<String>(Arrays.asList(exampleUser.getEmail())));
    assertEquals(exampleUser, authControllerAdvice.getUser(exampleAuthToken));
    verify(mockAdminRepository, times(1)).save(new Admin(exampleUser.getEmail(), true));
  }

  @Test
  public void test_getUser_userExists_butCreatesNewAdmin() {
    List<AppUser> users = new ArrayList<AppUser>();
    users.add(exampleUser);
    when(mockAppUserRepository.findByEmail(any(String.class))).thenReturn(users);
    when(mockMembershipService.isAdmin(any(DecodedJWT.class))).thenReturn(true);
    when(mockMembershipService.getDefaultAdminEmails())
        .thenReturn(new ArrayList<String>(Arrays.asList(exampleUser.getEmail())));
    assertEquals(exampleUser, authControllerAdvice.getUser(exampleAuthToken));
    verify(mockAdminRepository, times(1)).save(new Admin(exampleUser.getEmail(), true));
  }

  @Test
  public void test_getUser_userAndAdminExists() {
    List<AppUser> users = new ArrayList<AppUser>();
    users.add(exampleUser);
    List<Admin> admins = new ArrayList<Admin>();
    admins.add(new Admin(exampleUser.getEmail()));
    when(mockAppUserRepository.findByEmail(any(String.class))).thenReturn(users);
    when(mockAdminRepository.findByEmail(any(String.class))).thenReturn(admins);
    when(mockMembershipService.isAdmin(any(DecodedJWT.class))).thenReturn(true);
    assertEquals(exampleUser, authControllerAdvice.getUser(exampleAuthToken));
    verify(mockAdminRepository, times(0)).save(any(Admin.class));
  }

  @Test
  public void test_getRole() {
    when(mockMembershipService.role(any(DecodedJWT.class))).thenReturn("Member");
    assertEquals("Member", authControllerAdvice.getRole(exampleAuthToken));
  }

  @Test
  public void test_getRole_withAppUser() {
    when(mockMembershipService.role(any(AppUser.class))).thenReturn("Member");
    assertEquals("Member", authControllerAdvice.getRole(exampleUser));
  }

  @Test
  public void test_getIsMember() {
    when(mockMembershipService.isMember(any(DecodedJWT.class))).thenReturn(true);
    assertTrue(authControllerAdvice.getIsMember(exampleAuthToken));
  }

  @Test
  public void test_getIsAdmin() {
    when(mockMembershipService.isAdmin(any(DecodedJWT.class))).thenReturn(true);
    assertTrue(authControllerAdvice.getIsAdmin(exampleAuthToken));
  }
}
