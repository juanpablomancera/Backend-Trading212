package com.backendtrading212.controller;

import com.backendtrading212.dto.TradeRequest;
import com.backendtrading212.model.Holding;
import com.backendtrading212.model.Transaction;
import com.backendtrading212.model.User;
import com.backendtrading212.repository.HoldingRepository;
import com.backendtrading212.repository.TransactionRepository;
import com.backendtrading212.repository.UserRepository;
import com.backendtrading212.service.TradingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class TradingController {

    @Autowired
    private TradingService tradingService;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private TransactionRepository transactionRepo;

    @Autowired
    private HoldingRepository holdingRepo;

    @PostMapping("/buy")
    public ResponseEntity<String> buy(@RequestBody TradeRequest req) {
            tradingService.buy(req.getUsername(), req.getSymbol(), req.getPrice(), req.getQuantity());
        return ResponseEntity.ok("Bought successfully");
    }

    @PostMapping("/sell")
    public ResponseEntity<String> sell(@RequestBody TradeRequest req) {
        tradingService.sell(req.getUsername(), req.getSymbol(), req.getPrice(), req.getQuantity());
        return ResponseEntity.ok("Sold successfully");
    }

    @PostMapping("/reset/{username}")
    public ResponseEntity<String> reset(@PathVariable String username) {
        userRepo.resetBalance(username);
        return ResponseEntity.ok("Account reset");
    }

    @GetMapping("/balance/{username}")
    public BigDecimal balance(@PathVariable String username) {
        return userRepo.findByUsername(username).getBalance();
    }

    @GetMapping("/transactions/{username}")
    public List<Transaction> transactions(@PathVariable String username) {
        return transactionRepo.getTransactionsByUsername(username);
    }

    @GetMapping("/holdings/{username}")
    public List<Holding> holdings(@PathVariable String username) {
        User user = userRepo.findByUsername(username);
        return holdingRepo.getHoldingsByUserId(user.getId());
    }

    @GetMapping("/pnl/{username}")
    public List<Map<String, Object>> getPnL(@PathVariable String username) {
        return tradingService.getProfitAndLoss(username);
    }
}
