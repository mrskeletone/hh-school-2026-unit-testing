package ru.hh.school.unittesting.homework;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryManagerTest {
  @Mock
  private NotificationService notificationService;
  @Mock
  private UserService userService;
  @InjectMocks
  private LibraryManager libraryManager;

  @BeforeEach
  void setUp() {
    libraryManager.addBook("bookId1", 5);
  }

  @Test
  void borrowBooksShouldReturnFalseIfAccountIsNotActive() {
    when(userService.isUserActive("userId1")).thenReturn(false);
    doNothing().when(notificationService).notifyUser("userId1", "Your account is not active.");
    boolean bookIsBorrow = libraryManager.borrowBook("bookId1", "userId1");
    assertFalse(bookIsBorrow);
  }

  @Test
  void borrowBooksShouldReturnFalseIfAvailableCopiesIsZero() {
    when(userService.isUserActive("userId1")).thenReturn(true);
    libraryManager.addBook("bookId2", 0);
    boolean bookIsBorrow = libraryManager.borrowBook("bookId2", "userId1");
    assertFalse(bookIsBorrow);
  }

  @Test
  void testBorrowBooks() {
    when(userService.isUserActive("userId1")).thenReturn(true);
    doNothing().when(notificationService).notifyUser("userId1", "You have borrowed the book: bookId1");
    boolean bookIsBorrow = libraryManager.borrowBook("bookId1", "userId1");
    int availableCopies = libraryManager.getAvailableCopies("bookId1");
    assertTrue(bookIsBorrow);
    assertEquals(4, availableCopies);
  }

  @Test
  void returnBooksShouldReturnFalseIfBorrowsBooksNotContainsBook() {
    boolean returnBooks = libraryManager.returnBook("bookId1", "userId1");
    assertFalse(returnBooks);
  }

  @Test
  void returnBooksShouldReturnFalseIfBorrowsBooksAnotherUser() {
    when(userService.isUserActive(any())).thenReturn(true);
    libraryManager.borrowBook("bookId1", "userId2");
    boolean returnBooks = libraryManager.returnBook("bookId1", "userId1");
    assertFalse(returnBooks);
  }

  @Test
  void testReturnBooks() {
    libraryManager.addBook("bookId1", 1);
    doNothing().when(notificationService).notifyUser(any(), any());
    when(userService.isUserActive(any())).thenReturn(true);
    libraryManager.borrowBook("bookId1", "userId1");
    boolean returnBooks = libraryManager.returnBook("bookId1", "userId1");
    int availableCopies = libraryManager.getAvailableCopies("bookId1");
    assertTrue(returnBooks);
    assertEquals(6, availableCopies);
  }

  @Test
  void calculateDynamicLateFeeShouldThrowIllegalArgumentException() {
    var exception = assertThrows(
        IllegalArgumentException.class,
        () -> libraryManager.calculateDynamicLateFee(-1, true, false)
    );
    assertEquals("Overdue days cannot be negative.", exception.getMessage());
  }

  @ParameterizedTest
  @CsvSource({
      "10, false, false, 5",
      "10, true,false, 7.5",
      "10, false,true, 4",
      "10, true,true, 6",
  })
  void testCalculateDynamicLateFee(int overdueDays, boolean isBestseller,
                                   boolean isPremiumMember, double answer) {
    double answerMethod = libraryManager.calculateDynamicLateFee(overdueDays, isBestseller, isPremiumMember);
    assertEquals(answer, answerMethod, 0.0001);
  }
}