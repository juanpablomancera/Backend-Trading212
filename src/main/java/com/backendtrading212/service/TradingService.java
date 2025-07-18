package com.backendtrading212.service;

import com.backendtrading212.exception.InsufficientBalanceException;
import com.backendtrading212.model.Holding;
import com.backendtrading212.model.Transaction;
import com.backendtrading212.model.User;
import com.backendtrading212.repository.HoldingRepository;
import com.backendtrading212.repository.TransactionRepository;
import com.backendtrading212.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TradingService {
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private HoldingRepository holdingRepo;
    @Autowired
    private TransactionRepository transactionRepo;

    public void buy(String username, String symbol, BigDecimal price, BigDecimal quantity) {
        User user = userRepo.findByUsername(username);
        BigDecimal cost = price.multiply(quantity);

        if (user.getBalance().compareTo(cost) < 0) {
            throw new InsufficientBalanceException("Needed: $" + cost + ", Available: $" + user.getBalance());
        }

        userRepo.updateBalance(user.getId(), user.getBalance().subtract(cost));
        holdingRepo.upsertHolding(user.getId(), symbol, quantity, price);
        transactionRepo.insertTransaction(user.getId(), symbol, quantity, price, "BUY");
    }

    public void sell(String username, String symbol, BigDecimal price, BigDecimal quantity) {
        User user = userRepo.findByUsername(username);
        Long userId = user.getId();

        List<Holding> holdings = holdingRepo.getHoldingsByUserIdAndSymbolOrderedByPrice(userId, symbol);

        BigDecimal remainingToSell = quantity;
        BigDecimal totalProceeds = BigDecimal.ZERO;

        for (Holding holding : holdings) {
            if (remainingToSell.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal availableQty = holding.getQuantity();
            BigDecimal sellQty = remainingToSell.min(availableQty);

            totalProceeds = totalProceeds.add(sellQty.multiply(price));
            remainingToSell = remainingToSell.subtract(sellQty);

            BigDecimal newQty = availableQty.subtract(sellQty);

            if (newQty.compareTo(BigDecimal.ZERO) == 0) {
                holdingRepo.deleteHoldingById((long) holding.getId());
            } else {
                holdingRepo.updateHoldingQuantityById((long) holding.getId(), newQty);
            }

            transactionRepo.insertTransaction(userId, symbol, sellQty, price, "SELL");
        }

        if (remainingToSell.compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Not enough holdings to sell");
        }

        userRepo.updateBalance(userId, user.getBalance().add(totalProceeds));
    }

    public List<Map<String, Object>> getProfitAndLoss(String username) {
        User user = userRepo.findByUsername(username);
        Long userId = user.getId();

        List<Transaction> transactions = transactionRepo.getTransactionsByUserId(userId);

        Map<String, List<Transaction>> grouped = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getCryptoSymbol));

        List<Map<String, Object>> result = new ArrayList<>();

        for (String symbol : grouped.keySet()) {
            BigDecimal totalBoughtQty = BigDecimal.ZERO;
            BigDecimal totalBoughtAmount = BigDecimal.ZERO;

            BigDecimal totalSoldQty = BigDecimal.ZERO;
            BigDecimal totalSoldAmount = BigDecimal.ZERO;

            for (Transaction tx : grouped.get(symbol)) {
                if ("BUY".equalsIgnoreCase(tx.getType())) {
                    totalBoughtQty = totalBoughtQty.add(tx.getQuantity());
                    totalBoughtAmount = totalBoughtAmount.add(tx.getQuantity().multiply(tx.getPrice()));
                } else if ("SELL".equalsIgnoreCase(tx.getType())) {
                    totalSoldQty = totalSoldQty.add(tx.getQuantity());
                    totalSoldAmount = totalSoldAmount.add(tx.getQuantity().multiply(tx.getPrice()));
                }
            }

            BigDecimal avgBuyPrice = totalBoughtQty.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : totalBoughtAmount.divide(totalBoughtQty, 8, RoundingMode.HALF_UP);

            BigDecimal realizedQty = totalSoldQty.min(totalBoughtQty);
            BigDecimal realizedCost = avgBuyPrice.multiply(realizedQty);
            BigDecimal realizedProceeds = totalSoldAmount;

            BigDecimal realizedPnL = realizedProceeds.subtract(realizedCost);

            BigDecimal remainingQty = totalBoughtQty.subtract(totalSoldQty);

            BigDecimal latestPrice = transactionRepo.getLatestPriceForSymbol(symbol);
            BigDecimal unrealizedPnL = latestPrice != null
                    ? latestPrice.subtract(avgBuyPrice).multiply(remainingQty)
                    : BigDecimal.ZERO;

            BigDecimal totalPnL = realizedPnL.add(unrealizedPnL);

            Map<String, Object> pnlData = new HashMap<>();
            pnlData.put("symbol", symbol);
            pnlData.put("totalBought", totalBoughtQty);
            pnlData.put("totalSold", totalSoldQty);
            pnlData.put("remainingQty", remainingQty);
            pnlData.put("avgBuyPrice", avgBuyPrice);
            pnlData.put("currentPrice", latestPrice);
            pnlData.put("realizedPnL", realizedPnL);
            pnlData.put("unrealizedPnL", unrealizedPnL);
            pnlData.put("totalPnL", totalPnL);

            result.add(pnlData);
        }

        return result;
    }

}