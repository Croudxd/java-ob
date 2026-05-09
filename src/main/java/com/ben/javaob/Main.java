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

                    List<Long> bidPrices = new ArrayList<Long>(book.bids.keySet());
                    List<Long> askPrices = new ArrayList<Long>(book.asks.keySet());
                    int rows = Math.max(bidPrices.size(), askPrices.size());

                    for (int i = 0; i < rows; i++)
                    {
                        String bid = "";
                        String ask = "";
                        if (i < bidPrices.size())
                        {
                            Long p = bidPrices.get(i);
                            Long amt = book.bids.get(p).stream().mapToLong(o -> o.amount).sum();
                            if (amt == 0l) continue;
                            bid = String.format("%.4f  %.4f", p / 10000.0, amt / 10000.0);
                        }
                        if (i < askPrices.size())
                        {
                            Long p = askPrices.get(i);
                            Long amt = book.asks.get(p).stream().mapToLong(o -> o.amount).sum();
                            if (amt == 0l) continue;
                            ask = String.format("%.4f  %.4f", p / 10000.0, amt / 10000.0);
                        }
                        System.out.printf("%-30s %s%n", bid, ask);
                    }

                    System.out.println("RECEIPTS (last 10)");
                    int start = ((book.pool.index - 10) % 100 + 100) % 100;
                    for (int y = 0; y < 10; y++)
                    {
                        Receipt r = book.pool.pool[start % 100];
                        if (r.price != null) System.out.println(r);
                        start++;
                    }
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
            if (count >= 10) 
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
            Order.Pool pool = new Order.Pool(100);
            synchronized(book)
            {
                if (function == 1)
                {
                    book.Market_order(pool.acquire(Math.round(price * 10000l), Math.round(amount * 10000l), ty));
                }
                if (function == 2)
                {
                    book.IOC(pool.acquire(Math.round(price * 10000f), Math.round(amount * 10000f), ty));
                }
                if (function == 3)
                {
                    book.FOK(pool.acquire(Math.round(price * 10000f), Math.round(amount * 10000f), ty));
                }
                if (function == 4)
                {
                    book.GOC(pool.acquire(Math.round(price * 10000f), Math.round(amount * 10000f), ty));
                }
            }
            count++;
        }
    }
}
