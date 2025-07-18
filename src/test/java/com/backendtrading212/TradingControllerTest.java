package com.backendtrading212;

import com.backendtrading212.controller.TradingController;
import com.backendtrading212.dto.TradeRequest;
import com.backendtrading212.exception.InsufficientBalanceException;
import com.backendtrading212.exception.InsufficientHoldingsException;
import com.backendtrading212.exception.InvalidTradeException;
import com.backendtrading212.model.Holding;
import com.backendtrading212.model.Transaction;
import com.backendtrading212.model.User;
import com.backendtrading212.repository.HoldingRepository;
import com.backendtrading212.repository.TransactionRepository;
import com.backendtrading212.repository.UserRepository;
import com.backendtrading212.service.TradingService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TradingController.class)
public class TradingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TradingService tradingService;

    @MockBean
    private UserRepository userRepo;

    @MockBean
    private TransactionRepository transactionRepo;

    @MockBean
    private HoldingRepository holdingRepo;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testBuy() throws Exception {
        TradeRequest request = TradeRequest.builder()
                .username("user1")
                .symbol("BTC")
                .price(BigDecimal.valueOf(10000))
                .quantity(BigDecimal.ONE)
                .build();

        mockMvc.perform(post("/api/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Bought successfully"));

        Mockito.verify(tradingService).buy("user1", "BTC", BigDecimal.valueOf(10000), BigDecimal.ONE);
    }

    @Test
    void testSell() throws Exception {
        TradeRequest request = TradeRequest.builder()
                .username("user1")
                .symbol("BTC")
                .price(BigDecimal.valueOf(10000))
                .quantity(BigDecimal.ONE)
                .build();

        mockMvc.perform(post("/api/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Sold successfully"));

        Mockito.verify(tradingService).sell("user1", "BTC", BigDecimal.valueOf(10000), BigDecimal.ONE);
    }

    @Test
    void testReset() throws Exception {
        mockMvc.perform(post("/api/reset/user1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Account reset"));

        Mockito.verify(userRepo).resetBalance("user1");
    }

    @Test
    void testBalance() throws Exception {
        User user = User.builder()
                .username("user1")
                .balance(BigDecimal.valueOf(1234.56))
                .build();

        Mockito.when(userRepo.findByUsername("user1")).thenReturn(user);

        mockMvc.perform(get("/api/balance/user1"))
                .andExpect(status().isOk())
                .andExpect(content().string("1234.56"));
    }

    @Test
    void testTransactions() throws Exception {
        Transaction tx = Transaction.builder()
                .cryptoSymbol("BTC")
                .price(BigDecimal.valueOf(10000))
                .quantity(BigDecimal.ONE)
                .type("BUY")
                .build();

        Mockito.when(transactionRepo.getTransactionsByUsername("user1")).thenReturn(List.of(tx));

        mockMvc.perform(get("/api/transactions/user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cryptoSymbol").value("BTC"));
    }

    @Test
    void testHoldings() throws Exception {
        User user = User.builder()
                .id(1L)
                .username("user1")
                .build();

        Holding holding = Holding.builder()
                .cryptoSymbol("ETH")
                .quantity(BigDecimal.valueOf(5))
                .build();

        Mockito.when(userRepo.findByUsername("user1")).thenReturn(user);
        Mockito.when(holdingRepo.getHoldingsByUserId(1L)).thenReturn(List.of(holding));

        mockMvc.perform(get("/api/holdings/user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cryptoSymbol").value("ETH"));
    }

    @Test
    void testGetPnL() throws Exception {
        Map<String, Object> pnl = Map.of("symbol", "BTC", "totalPnL", BigDecimal.valueOf(200.50));
        Mockito.when(tradingService.getProfitAndLoss("user1")).thenReturn(List.of(pnl));

        mockMvc.perform(get("/api/pnl/user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("BTC"))
                .andExpect(jsonPath("$[0].totalPnL").value(200.50));
    }

    @Test
    void testBuy_InsufficientBalance() throws Exception {
        TradeRequest request = TradeRequest.builder()
                .username("user1")
                .symbol("BTC")
                .price(BigDecimal.valueOf(50000))
                .quantity(BigDecimal.ONE)
                .build();

        Mockito.doThrow(new InsufficientBalanceException("Needed $50000 but only $1000 available"))
                .when(tradingService)
                .buy(anyString(), anyString(), any(), any());

        mockMvc.perform(post("/api/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Insufficient balance: Needed $50000 but only $1000 available"));
    }

    @Test
    void testSell_InsufficientHoldings() throws Exception {
        TradeRequest request = TradeRequest.builder()
                .username("user1")
                .symbol("ETH")
                .price(BigDecimal.valueOf(2000))
                .quantity(BigDecimal.TEN)
                .build();

        Mockito.doThrow(new InsufficientHoldingsException("Only 2 ETH available"))
                .when(tradingService)
                .sell(anyString(), anyString(), any(), any());

        mockMvc.perform(post("/api/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Insufficient holdings: Only 2 ETH available"));
    }

    @Test
    void testBuy_InvalidTrade() throws Exception {
        TradeRequest request = TradeRequest.builder()
                .username("user1")
                .symbol("BTC")
                .price(BigDecimal.valueOf(-5))
                .quantity(BigDecimal.ZERO)
                .build();

        Mockito.doThrow(new InvalidTradeException("Quantity must be positive"))
                .when(tradingService)
                .buy(anyString(), anyString(), any(), any());

        mockMvc.perform(post("/api/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid trade: Quantity must be positive"));
    }

    @Test
    void testBalance_UserNotFound_InternalServerError() throws Exception {
        Mockito.when(userRepo.findByUsername("ghost")).thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/balance/ghost"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Unexpected error: User not found"));
    }

    @Test
    void testBuy_UnexpectedError() throws Exception {
        TradeRequest request = TradeRequest.builder()
                .username("user1")
                .symbol("BTC")
                .price(BigDecimal.valueOf(10000))
                .quantity(BigDecimal.ONE)
                .build();

        Mockito.doThrow(new RuntimeException("Database connection failed"))
                .when(tradingService)
                .buy(anyString(), anyString(), any(), any());

        mockMvc.perform(post("/api/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Unexpected error: Database connection failed"));
    }

}

