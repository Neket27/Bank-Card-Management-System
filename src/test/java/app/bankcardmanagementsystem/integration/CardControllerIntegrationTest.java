package app.bankcardmanagementsystem.integration;

import app.bankcardmanagementsystem.controller.dto.TransactionDto;
import app.bankcardmanagementsystem.controller.dto.TransferRequestDto;
import app.bankcardmanagementsystem.controller.dto.card.CreateCardDto;
import app.bankcardmanagementsystem.controller.dto.limitOnOperationByCard.CreateLimitOnOperationByCard;
import app.bankcardmanagementsystem.controller.dto.user.CreateUserDto;
import app.bankcardmanagementsystem.controller.dto.user.UserDto;
import app.bankcardmanagementsystem.entity.*;
import app.bankcardmanagementsystem.mapper.CardMapper;
import app.bankcardmanagementsystem.mapper.UserMapper;
import app.bankcardmanagementsystem.service.CardService;
import app.bankcardmanagementsystem.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CardService cardService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CardMapper cardMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private Long createdCardId = 1L;

    private User user;

    private static final String TEST_CARD_NUMBER = "1234567890123456";
    private static final String TEST_CARD_HOLDER = "Ivan Ivanov";
    private static final String TEST_USER_EMAIL = "user@example.com";
    private static final String TEST_USER_PASSWORD = "password";


    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("user")
            .withPassword("password");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        postgres.start();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    public void setup() {
        CreateCardDto createCardDto = new CreateCardDto(
                "1234567890123456", "Ivan Ivanov", LocalDate.now().plusYears(2),
                BigDecimal.valueOf(1000), CardStatus.ACTIVE
        );
        Card entity = cardMapper.toEntity(createCardDto);

        CreateUserDto createUserDto = CreateUserDto.builder()
                .email("user@example.com")
                .password("password")
                .roles(Set.of(Role.ROLE_USER))
                .build();

        UserDto userDto = userService.createUser(createUserDto);
        user = userMapper.toEntity(userDto);

        entity.setUser(user);
        entity = cardService.createCard(entity);
        createdCardId = entity.getId();
    }

    @Test
    @Order(1)
    @WithMockUser(roles = "ADMIN")
    void createCard_success() throws Exception {
        // Arrange: Подготовка данных для создания карты
        CreateCardDto createCardDto = new CreateCardDto(
                "6543210987654321", "Petr Petrov", LocalDate.now().plusYears(3),
                BigDecimal.valueOf(500), CardStatus.ACTIVE
        );

        // Act: Выполнение HTTP-запроса на создание карты
        String response = mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCardDto)))
                .andExpect(status().isOk()) // Assert: Проверка кода состояния
                .andExpect(jsonPath("$.cardHolder").value("Petr Petrov")) // Assert: Проверка значения в ответе
                .andReturn().getResponse().getContentAsString();

        assertThat(response).contains("Petr Petrov"); // Assert: Проверка, что ответ содержит имя держателя карты
    }

    @Test
    @Order(2)
    @WithMockUser(roles = "ADMIN", username = TEST_USER_EMAIL)
    void createCard_checkDatabase() {
        // Act: Проверка, что карта была добавлена в базу данных
        Card createdCard = cardService.getCard(createdCardId);

        // Assert: Проверка, что карта создана и принадлежит пользователю
        assertThat(createdCard).isNotNull();
        assertThat(createdCard.getCardHolder()).isEqualTo(TEST_CARD_HOLDER);
        assertThat(createdCard.getUser().getEmail()).isEqualTo(TEST_USER_EMAIL);
    }

    @Test
    @Order(3)
    @WithMockUser(roles = "ADMIN")
    void blockCard_success() throws Exception {
        // Act: Выполнение запроса на блокировку карты
        String response = mockMvc.perform(put("/api/v1/cards/" + createdCardId + "/block"))
                .andExpect(status().isOk()) // Assert: Проверка кода состояния
                .andExpect(jsonPath("$.status").value("BLOCKED")) // Assert: Проверка статуса карты
                .andReturn().getResponse().getContentAsString();

        // Дополнительная проверка статуса карты
        assertThat(response).contains("BLOCKED");
    }

    @Test
    @Order(4)
    @WithMockUser(roles = "ADMIN")
    void activateCard_success() throws Exception {
        // Act: Выполнение запроса на активацию карты
        String response = mockMvc.perform(put("/api/v1/cards/" + createdCardId + "/activate"))
                .andExpect(status().isOk()) // Assert: Проверка кода состояния
                .andExpect(jsonPath("$.status").value("ACTIVE")) // Assert: Проверка статуса карты
                .andReturn().getResponse().getContentAsString();

        // Дополнительная проверка статуса карты
        assertThat(response).contains("ACTIVE");
    }

    @Test
    @Order(5)
    @WithMockUser(roles = "ADMIN")
    void getAllCards_success() throws Exception {
        // Act: Выполнение запроса на получение всех карт
        mockMvc.perform(get("/api/v1/cards"))
                .andExpect(status().isOk()) // Assert: Проверка кода состояния
                .andExpect(jsonPath("$").isArray()); // Assert: Проверка, что ответ является массивом
    }

    @Test
    @Order(6)
    @WithMockUser(roles = "ADMIN")
    void getCardById_success() throws Exception {
        // Act: Выполнение запроса на получение карты по ID
        mockMvc.perform(get("/api/v1/cards/" + createdCardId))
                .andExpect(status().isOk()) // Assert: Проверка кода состояния
                .andExpect(jsonPath("$.id").value(createdCardId)); // Assert: Проверка, что ID карты совпадает
    }

    @Test
    @Order(7)
    @WithMockUser(roles = "ADMIN")
    void setLimit_success() throws Exception {
        // Arrange: Подготовка данных для лимита
        String json = """
                {
                    "amount": 500,
                    "limitCard": "DAY"
                }
                """;

        // Act: Выполнение запроса на установку лимита для карты
        mockMvc.perform(post("/api/v1/cards/" + createdCardId + "/limit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk()); // Assert: Проверка кода состояния
    }

    @Test
    @Order(8)
    @WithMockUser(roles = "USER")
    void addTransaction_success() throws Exception {
        // Arrange: Создание транзакции
        TransactionDto tx = new TransactionDto();
        tx.setAmount(BigDecimal.valueOf(100));
        tx.setDescription("Test payment");

        // Act: Выполнение запроса на добавление транзакции
        String response = mockMvc.perform(post("/api/v1/cards/" + createdCardId + "/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tx)))
                .andExpect(status().isOk()) // Assert: Проверка кода состояния
                .andExpect(jsonPath("$.description").value("Test payment")) // Assert: Проверка описания транзакции
                .andReturn().getResponse().getContentAsString();

        // Дополнительная проверка
        assertThat(response).contains("Test payment");
    }

    @Test
    @Order(9)
    @WithMockUser(roles = "USER", username = "user@example.com")
    void getTransactions_success() throws Exception {
        // Act: Выполнение запроса на получение всех транзакций
        mockMvc.perform(get("/api/v1/cards/" + createdCardId + "/transactions"))
                .andExpect(status().isOk()) // Assert: Проверка кода состояния
                .andExpect(jsonPath("$").isArray()); // Assert: Проверка, что ответ является массивом
    }

    @Test
    @Order(10)
    @WithMockUser(roles = "USER")
    void transferBetweenOwnCards_fail_notOwner() throws Exception {
        // Arrange: Создание запроса на перевод
        TransferRequestDto dto = new TransferRequestDto(
                createdCardId, createdCardId, BigDecimal.TEN, "Self transfer"
        );

        // Act: Попытка выполнения перевода между своими картами
        mockMvc.perform(post("/api/v1/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest()); // Assert: Проверка ошибки перевода
    }

    @Test
    @Order(11)
    @WithMockUser(username = "user@example.com", authorities = "ROLE_USER")
    void requestBlockCard_success() throws Exception {
        mockMvc.perform(post("/api/v1/cards/{id}/request/block", createdCardId))
                .andExpect(status().isOk()); // Assert: Проверка успешного выполнения запроса
    }

    @Test
    @Order(12)
    @WithMockUser(username = "user@example.com", authorities = "ROLE_USER")
    void requestBlockCard_No_success() throws Exception {
        mockMvc.perform(post("/api/v1/cards/{id}/request/block", createdCardId)
                        .param("massage", "Please block my card"))
                .andExpect(status().isOk()); // Assert: Проверка успешного выполнения запроса с сообщением
    }

    @Test
    @Order(13)
    @WithMockUser(username = "admin@example.com", authorities = "ROLE_ADMIN")
    void getAllCards_returnsListOfCards() throws Exception {
        mockMvc.perform(get("/api/v1/cards"))
                .andExpect(status().isOk()) // Assert: Проверка успешного ответа
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1)))); // Assert: Проверка размера списка карт
    }

    @Test
    @Order(14)
    @WithMockUser(username = "admin@example.com", authorities = "ROLE_ADMIN")
    void getCardById_existingCard_returnsCard() throws Exception {
        mockMvc.perform(get("/api/v1/cards/{id}", createdCardId))
                .andExpect(status().isOk()) // Assert: Проверка успешного ответа
                .andExpect(jsonPath("$.id").value(createdCardId)); // Assert: Проверка ID карты
    }

    @Test
    @Order(15)
    @WithMockUser(username = "admin@example.com", authorities = "ROLE_ADMIN")
    void setLimitOnCard_success() throws Exception {
        CreateLimitOnOperationByCard limitDto = new CreateLimitOnOperationByCard(
                LimitCard.DAY,
                new BigDecimal("3000")
        );

        mockMvc.perform(post("/api/v1/cards/{id}/limit", createdCardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(limitDto)))
                .andExpect(status().isOk()); // Assert: Проверка успешного ответа на установку лимита
    }

    @Test
    @Order(16)
    @WithMockUser(username = "user@example.com", authorities = "ROLE_USER")
    void getAllCardsForCurrentUser_success() throws Exception {
        mockMvc.perform(get("/api/v1/cards/user"))
                .andExpect(status().isOk()) // Assert: Проверка успешного ответа
                .andExpect(jsonPath("$", isA(JSONArray.class))); // Assert: Проверка, что ответ является массивом
    }

    @Test
    @Order(17)
    @WithMockUser(username = "user@example.com", authorities = "ROLE_USER")
    void transferBetweenOwnCards_success() throws Exception {
        CreateCardDto secondCardDto = new CreateCardDto(
                "1234567812345679",
                "Ivan Ivanov",
                LocalDate.now().plusYears(2),
                BigDecimal.valueOf(1000.11),
                CardStatus.ACTIVE
        );

        Card card = cardService.createCard(cardMapper.toEntity(secondCardDto));
        cardService.assignCardToUser(user.getUsername(), card.getId());

        TransferRequestDto transferDto = new TransferRequestDto(
                createdCardId,
                card.getId(),
                BigDecimal.valueOf(100.11),
                "Transfer Test"
        );

        mockMvc.perform(post("/api/v1/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDto)))
                .andExpect(status().isOk()) // Assert: Проверка успешного выполнения перевода
                .andExpect(jsonPath("$.description").value("Transfer Test")) // Assert: Проверка описания транзакции
                .andExpect(jsonPath("$.amount").value("-100.11")); // Assert: Проверка суммы перевода
    }

    @Test
    @Order(18)
    @WithMockUser(username = "user2@example.com", authorities = "ROLE_USER")
    void getCardById_notOwnedByUser() throws Exception {
        mockMvc.perform(get("/api/v1/cards/{id}", createdCardId))
                .andExpect(status().isBadRequest()); // Assert: Проверка, что запрос возвращает ошибку из-за отсутствия прав
    }

    @Test
    @Order(19)
    @WithMockUser(username = "user@example.com", authorities = "ROLE_ADMIN")
    void transferBetweenCards_notOwned() throws Exception {
        CreateCardDto createCardDto = new CreateCardDto(
                "1234567890123456", "Ivan Ivanov", LocalDate.now().plusYears(2),
                BigDecimal.valueOf(1000), CardStatus.ACTIVE
        );
        Card entity = cardMapper.toEntity(createCardDto);

        CreateUserDto createUserDto2 = CreateUserDto.builder()
                .email("user2@example.com")
                .password("password")
                .roles(Set.of(Role.ROLE_USER))
                .build();

        User user2 = userMapper.toEntity(userService.createUser(createUserDto2));
        entity.setUser(user2);
        Card card = cardService.createCard(entity);

        TransferRequestDto transferDto = new TransferRequestDto(
                createdCardId,
                card.getId(),
                new BigDecimal("50.00"),
                "Should fail"
        );

        mockMvc.perform(post("/api/v1/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDto)))
                .andExpect(status().isBadRequest()); // Assert: Проверка ошибки из-за перевода на чужую карту
    }

    @Test
    @Order(20)
    @WithMockUser(username = "user@example.com", authorities = "ROLE_ADMIN")
    void transferWithInsufficientBalance_shouldFail() throws Exception {
        CreateCardDto lowBalanceCard = new CreateCardDto(
                "1234567812340000",
                "user@example.com",
                LocalDate.now().plusYears(1),
                new BigDecimal("0.00"),
                CardStatus.ACTIVE
        );

        String response = mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lowBalanceCard)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long fromCardId = objectMapper.readTree(response).get("id").asLong();

        TransferRequestDto transferDto = new TransferRequestDto(
                fromCardId,
                createdCardId,
                new BigDecimal("500.00"),
                "Too much money"
        );

        mockMvc.perform(post("/api/v1/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDto)))
                .andExpect(status().isBadRequest()); // Assert: Проверка ошибки при недостаточном балансе
    }

    @Test
    @Order(21)
    @WithMockUser(username = "admin@example.com", authorities = "ROLE_ADMIN")
    void getCardById_notFound() throws Exception {
        mockMvc.perform(get("/api/v1/cards/{id}", 999999))
                .andExpect(status().isNotFound()); // Assert: Проверка, что карта не найдена
    }

    @Test
    @Order(22)
    @WithMockUser(username = "admin@example.com", authorities = "ROLE_ADMIN")
    void setLimitOnCard_cardNotFound() throws Exception {
        CreateLimitOnOperationByCard limitDto = new CreateLimitOnOperationByCard(
                LimitCard.MONTH,
                new BigDecimal("5000")
        );

        mockMvc.perform(post("/api/v1/cards/{id}/limit", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(limitDto)))
                .andExpect(status().isNotFound()); // Assert: Проверка, что карта не найдена
    }

}
