package com.backendtrading212;

import com.backendtrading212.exception.InsufficientBalanceException;
import com.backendtrading212.model.Holding;
import com.backendtrading212.model.Transaction;
import com.backendtrading212.model.User;
import com.backendtrading212.repository.HoldingRepository;
import com.backendtrading212.repository.TransactionRepository;
import com.backendtrading212.repository.UserRepository;

import com.backendtrading212.service.TradingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TradingServiceTest {

    @InjectMocks
    private TradingService tradingService;

    @Mock
    private UserRepository userRepo;

    @Mock
    private HoldingRepository holdingRepo;

    @Mock
    private TransactionRepository transactionRepo;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testBuy_Successful() {
        User user = User.builder()
                .id(1L)
                .username("alice")
                .balance(BigDecimal.valueOf(1000))
                .build();

        when(userRepo.findByUsername("alice")).thenReturn(user);

        tradingService.buy("alice", "BTC", BigDecimal.valueOf(100), BigDecimal.valueOf(5));

        verify(userRepo).updateBalance(1L, BigDecimal.valueOf(500));
        verify(holdingRepo).upsertHolding(1L, "BTC", BigDecimal.valueOf(5), BigDecimal.valueOf(100));
        verify(transactionRepo).insertTransaction(1L, "BTC", BigDecimal.valueOf(5), BigDecimal.valueOf(100), "BUY");
    }

    @Test
    void testBuy_InsufficientBalance_ThrowsException() {
        User user = User.builder()
                .id(1L)
                .username("bob")
                .balance(BigDecimal.valueOf(200))
                .build();

        when(userRepo.findByUsername("bob")).thenReturn(user);

        assertThrows(InsufficientBalanceException.class, () ->
                tradingService.buy("bob", "ETH", BigDecimal.valueOf(100), BigDecimal.valueOf(5))
        );

        verify(userRepo, never()).updateBalance(any(), any());
        verify(holdingRepo, never()).upsertHolding(any(), any(), any(), any());
        verify(transactionRepo, never()).insertTransaction(any(), any(), any(), any(), any());
    }

    @Test
    void testSell_Successful() {
        User user = User.builder()
                .id(2L)
                .username("charlie")
                .balance(BigDecimal.valueOf(100))
                .build();

        Holding holding = Holding.builder()
                .id(10)
                .cryptoSymbol("BTC")
                .quantity(BigDecimal.valueOf(10))
                .price(BigDecimal.valueOf(40))
                .build();

        // Mocks
        when(userRepo.findByUsername("charlie")).thenReturn(user);
        when(holdingRepo.getHoldingsByUserIdAndSymbolOrderedByPrice(2L, "BTC"))
                .thenReturn(List.of(holding));

        // Call the method under test
        tradingService.sell("charlie", "BTC", BigDecimal.valueOf(50), BigDecimal.valueOf(4));

        // Verifications
        verify(holdingRepo).updateHoldingQuantityById(10L, BigDecimal.valueOf(6));
        verify(userRepo).updateBalance(2L, BigDecimal.valueOf(300));
        verify(transactionRepo).insertTransaction(2L, "BTC", BigDecimal.valueOf(4), BigDecimal.valueOf(50), "SELL");
    }


    @Test
    void testSell_ExactQuantity_DeletesHolding() {
        User user = User.builder()
                .id(2L)
                .username("dana")
                .balance(BigDecimal.valueOf(100))
                .build();

        Holding holding = Holding.builder()
                .id(20)
                .cryptoSymbol("BTC")
                .quantity(BigDecimal.valueOf(3))
                .price(BigDecimal.valueOf(40))
                .build();

        when(userRepo.findByUsername("dana")).thenReturn(user);
        when(holdingRepo.getHoldingsByUserIdAndSymbolOrderedByPrice(2L, "BTC"))
                .thenReturn(List.of(holding));

        tradingService.sell("dana", "BTC", BigDecimal.valueOf(50), BigDecimal.valueOf(3));

        verify(holdingRepo).deleteHoldingById(20L);
    }


    @Test
    void testSell_InsufficientHoldings_ThrowsException() {
        User user = User.builder()
                .id(3L)
                .username("eric")
                .balance(BigDecimal.valueOf(1000))
                .build();

        Holding holding = Holding.builder()
                .cryptoSymbol("ETH")
                .quantity(BigDecimal.valueOf(1))
                .build();

        when(userRepo.findByUsername("eric")).thenReturn(user);
        when(holdingRepo.getHoldingByUserIdAndSymbol(3L, "ETH")).thenReturn(holding);

        assertThrows(RuntimeException.class, () ->
                tradingService.sell("eric", "ETH", BigDecimal.valueOf(2000), BigDecimal.valueOf(3))
        );

        verify(userRepo, never()).updateBalance(any(), any());
    }

    @Test
    void testSell_HoldingNotFound_ThrowsException() {
        User user = User.builder()
                .id(3L)
                .username("frank")
                .balance(BigDecimal.valueOf(500))
                .build();

        when(userRepo.findByUsername("frank")).thenReturn(user);
        when(holdingRepo.getHoldingByUserIdAndSymbol(3L, "BTC")).thenReturn(null);

        assertThrows(RuntimeException.class, () ->
                tradingService.sell("frank", "BTC", BigDecimal.valueOf(100), BigDecimal.valueOf(2))
        );
    }

    @Test
    void testGetProfitAndLoss() {
        User user = User.builder()
                .id(5L)
                .username("sara")
                .balance(BigDecimal.valueOf(1000))
                .build();

        when(userRepo.findByUsername("sara")).thenReturn(user);

        List<Transaction> txs = List.of(
                Transaction.builder()
                        .userId(5)
                        .cryptoSymbol("BTC")
                        .quantity(BigDecimal.valueOf(2))
                        .price(BigDecimal.valueOf(10000))
                        .type("BUY")
                        .build(),
                Transaction.builder()
                        .userId(5)
                        .cryptoSymbol("BTC")
                        .quantity(BigDecimal.valueOf(1))
                        .price(BigDecimal.valueOf(15000))
                        .type("SELL")
                        .build()
        );

        when(transactionRepo.getTransactionsByUserId(5L)).thenReturn(txs);
        when(transactionRepo.getLatestPriceForSymbol("BTC")).thenReturn(BigDecimal.valueOf(20000));

        List<Map<String, Object>> pnlList = tradingService.getProfitAndLoss("sara");

        assertEquals(1, pnlList.size());

        Map<String, Object> pnl = pnlList.get(0);
        assertEquals("BTC", pnl.get("symbol"));
        assertEquals(BigDecimal.valueOf(2), pnl.get("totalBought"));
        assertEquals(BigDecimal.valueOf(1), pnl.get("totalSold"));
        assertEquals(BigDecimal.valueOf(1), pnl.get("remainingQty"));
        assertEquals(0, ((BigDecimal) pnl.get("realizedPnL")).compareTo(BigDecimal.valueOf(5000)));
        assertEquals(0, ((BigDecimal) pnl.get("unrealizedPnL")).compareTo(BigDecimal.valueOf(10000)));
    }
}

