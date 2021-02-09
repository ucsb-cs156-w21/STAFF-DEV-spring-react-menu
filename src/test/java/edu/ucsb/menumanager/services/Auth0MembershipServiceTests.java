package edu.ucsb.menumanager.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import edu.ucsb.menumanager.entities.Admin;
import edu.ucsb.menumanager.entities.AppUser;
import edu.ucsb.menumanager.repositories.AdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

@ExtendWith(SpringExtension.class)
public class Auth0MembershipServiceTests {

  @MockBean
  AdminRepository adminRepository;
  @InjectMocks
  Auth0MembershipService service = new Auth0MembershipService();

  private DecodedJWT guestJWT = JWT.decode(
      "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lc3BhY2UiOnsiZW1haWwiOiJ0ZXN0QGdtYWlsLmNvbSJ9LCJzdWIiOiIxMjM0NTYiLCJuYW1lIjoiSm9obiBEb2UiLCJpYXQiOjE1MTYyMzkwMjJ9.6kcYK01GnVjCMELcgUyFJBYe1DeQ9y4NngDgYdwYwqE");

  private DecodedJWT memberJWT = JWT.decode(
      "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lc3BhY2UiOnsiZW1haWwiOiJ0ZXN0QHVjc2IuZWR1In0sInN1YiI6IjEyMzQ1NiIsIm5hbWUiOiJKb2huIERvZSIsImlhdCI6MTUxNjIzOTAyMn0.RnoI4IH5bgp4uWd8VxKyVLrUTYu_JnUUhLpAaWc-0G4");

  private DecodedJWT defaultAdminJWT = JWT.decode(
      "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lc3BhY2UiOnsiZW1haWwiOiJhZG1pbkB1Y3NiLmVkdSJ9LCJzdWIiOiIxMjM0NTYiLCJuYW1lIjoiSm9obiBEb2UiLCJpYXQiOjE1MTYyMzkwMjJ9.DhnSeuqo6YCsdb6qYV5AKsY_xrCZzZ6RHxnVP8WsiC0");

  private DecodedJWT noCustomClaimJWT = JWT.decode(
      "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTYiLCJuYW1lIjoiSm9obiBEb2UiLCJpYXQiOjE1MTYyMzkwMjIsImp0aSI6Ijg2ZjI1N2RiLWZmNmMtNDU3MS1iODcxLWNmNmM2M2ExYzg4MCIsImV4cCI6MTYxMTM2NjE3MH0.TVfNrSJ2fX-qZAdCmdEjPLRH3WM4mqMOKhDw0noG5iM");

  private AppUser exampleUser = new AppUser(1L, "test@ucsb.edu", "Test", "User");

  @BeforeEach
  public void setUp() {
    ReflectionTestUtils.setField(service, "memberHostedDomain", "ucsb.edu");
    ReflectionTestUtils.setField(service, "adminRepository", adminRepository);
    ReflectionTestUtils.setField(service, "namespace", "namespace");
  }

  @Test
  public void testAuth0MembershipService_isNotMemberOrAdmin_ifJWTIsNull() {
    assertEquals(false, service.isAdmin((DecodedJWT) null));
    assertEquals(false, service.isMember((DecodedJWT) null));
  }

  @Test
  public void testAuth0MembershipService_isNotMemberOrAdmin_ifEmailNotInOrg() {
    assertEquals(false, service.isMember(guestJWT));
  }

  @Test
  public void testAuth0MembershipService_isMember_ifEmailInOrg() {
    assertEquals(true, service.isMember(memberJWT));
  }

  @Test
  public void testAuth0MembershipService_isAdmin_ifAdminExistsWithEmail() {
    List<Admin> admins = new ArrayList<Admin>();
    admins.add(new Admin());
    when(adminRepository.findByEmail(any())).thenReturn(admins);
    assertEquals(true, service.isAdmin(defaultAdminJWT));
  }

  @Test
  public void testAuth0MembershipService_isAdmin_ifEmailIsDefaultAdmin() {
    List<String> adminEmails = new ArrayList<String>();
    adminEmails.add("admin@ucsb.edu");
    ReflectionTestUtils.setField(service, "adminEmails", adminEmails);
    assertEquals(true, service.isAdmin(defaultAdminJWT));
  }

  @Test
  public void testAuth0MembershipService_getAdminEmails() {
    List<String> adminEmails = new ArrayList<String>();
    adminEmails.add("admin@ucsb.edu");
    ReflectionTestUtils.setField(service, "adminEmails", adminEmails);
    assertEquals(adminEmails, service.getDefaultAdminEmails());
  }

  @Test
  public void testAuth0MembershipService_isNotAdmin_ifDoesNotExistsWithEmail() {
    List<Admin> admins = new ArrayList<Admin>();
    when(adminRepository.findByEmail(any())).thenReturn(admins);
    assertEquals(false, service.isAdmin(memberJWT));
  }

  @Test
  public void testAuth0MembershipService_isMember_acceptsAppUser() {

    assertEquals(true, service.isMember(exampleUser));
  }

  @Test
  public void testAuth0MembershipService_isAdmin_acceptsAppUser() {
    List<Admin> admins = new ArrayList<Admin>();
    admins.add(new Admin(exampleUser.getEmail(), false));
    when(adminRepository.findByEmail(any())).thenReturn(admins);
    assertEquals(true, service.isAdmin(exampleUser));
  }

  @Test
  public void testAuth0MembershipService_hasRole_logsErrorOnNullCustomClaim() throws Exception {

    // See: https://stackoverflow.com/a/51812144 for
    // example on which this is based

    // get Logback Logger
    Logger theLogger = (Logger) LoggerFactory.getLogger(Auth0MembershipService.class);

    // create and start a ListAppender
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();

    // add the appender to the logger
    theLogger.addAppender(listAppender);

    // call method under test

    boolean result = service.isMember(noCustomClaimJWT);
    assertFalse(result, "should return false on JWT with no custom claim");

    // check log messages

    List<ILoggingEvent> logsList = listAppender.list;
    assertLogMessageEquals(logsList.get(0), Level.ERROR, "ERROR!  customClaims is null");
    assertLogMessageEquals(logsList.get(1), Level.ERROR, "namespace = namespace");
    theLogger.detachAppender(listAppender);
  }

  private void assertLogMessageEquals(ILoggingEvent e, Level level, String expected) {
    assertEquals(level, e.getLevel());
    String actualMessage = e.getFormattedMessage();
    assertEquals(expected,actualMessage);
  }

}
