package com.ben.javaob;

import com.ben.javaob.Orderbook;
import com.ben.javaob.Order;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
/**
 * Hello world!
 *
 */
public class Main 
{
    public static void main( String[] args )
    {
        Double minPrice = 0.39;
        Double maxPrice = 0.42;
        Double minAmount = 0.0;
        Double maxAmount = 0.3;

        Orderbook book = new Orderbook();

        Thread reader = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted())
            {
                synchronized(book)
                {
                    System.out.print("\033[H\033[2J");
                    System.out.flush();
                    System.out.printf("%-30s %s%n", "BIDS", "ASKS");
                    System.out.printf("%-30s %s%n", "----", "----");

                    List<Double> bidPrices = new ArrayList<>(book.bids.keySet());
                    List<Double> askPrices = new ArrayList<>(book.asks.keySet());
                    int rows = Math.max(bidPrices.size(), askPrices.size());

                    for (int i = 0; i < rows; i++)
                    {
                        String bid = "";
                        String ask = "";
                        if (i < bidPrices.size())
                        {
                            Double p = bidPrices.get(i);
                            Double amt = book.bids.get(p).stream().mapToDouble(o -> o.amount).sum();
                            bid = String.format("%.4f  %.4f", p, amt);
                        }
                        if (i < askPrices.size())
                        {
                            Double p = askPrices.get(i);
                            Double amt = book.asks.get(p).stream().mapToDouble(o -> o.amount).sum();
                            ask = String.format("%.4f  %.4f", p, amt);
                        }
                        System.out.printf("%-30s %s%n", bid, ask);
                    }

                    System.out.println("RECEIPTS (last 10)");
                    book.receipts.stream().skip(Math.max(0, book.receipts.size() - 10)).forEach(System.out::println);
                    
                }
                try { Thread.sleep(500); } catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                }
            }
        });
        reader.start();
        int count = 0; 
        for (;;)
        {
            if (count >= 3) 
            {
                count = 0;
                try { Thread.sleep(500); } catch (InterruptedException e) { break; }
            }
            Random rand = new Random();
            Double price = rand.nextDouble(minPrice, maxPrice);
            Double amount = rand.nextDouble(minAmount, maxAmount);
            int function = rand.nextInt(1, 5);
            boolean type = rand.nextBoolean();
            Order.type ty = Order.type.BUY;
            if (type)
            {
                ty = Order.type.SELL;
            }
            Order ord = new Order(price, amount, ty);
            synchronized(book)
            {
                if (function == 1)
                {
                    book.Market_order(ord);
                }
                if (function == 2)
                {
                    book.IOC(ord);
                }
                if (function == 3)
                {
                    book.FOK(ord);
                }
                if (function == 4)
                {
                    book.GOC(ord);
                }
            }
            count++;
        }
    }
}
