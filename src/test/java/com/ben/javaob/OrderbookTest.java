package com.ben.javaob;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.ArrayList;


public class OrderbookTest 
    extends TestCase
{
    public OrderbookTest( String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( OrderbookTest.class );
    }

    public void testFullFillSellAgainstBid()
    {
        Order.Pool pool = new Order.Pool(5);
        Order ord = pool.acquire(100l, 100l, Order.type.BUY);
        Orderbook book = new Orderbook();
        book.bids.computeIfAbsent(ord.price, k -> new ArrayList<Order>()).add(ord);
        assertTrue(book.IOC(pool.acquire(100l, 100l, Order.type.SELL)) == 0);
        assertTrue(book.pool.pool[book.pool.index - 1].amount == 100l);
    }

    public void testNotFullFillSellAgainstBid()
    {
        Order.Pool pool = new Order.Pool(5);
        Order ord = pool.acquire(100l, 100l, Order.type.BUY);
        Orderbook book = new Orderbook();
        book.bids.computeIfAbsent(ord.price, k -> new ArrayList<Order>()).add(ord);
        assertTrue(book.GOC(pool.acquire(100l, 105l, Order.type.SELL)) == -2);
        assertTrue(book.pool.pool[book.pool.index - 1].amount == 100l);
        assertTrue(book.asks.get(100l).getFirst().amount == 5l);
    }

    public void testNoMatchPriceMismatch()
    {
        Order.Pool pool = new Order.Pool(5);
        Order ord = pool.acquire(100l, 100l, Order.type.BUY);
        Orderbook book = new Orderbook();
        book.bids.computeIfAbsent(ord.price, k -> new ArrayList<Order>()).add(ord);
        assertTrue(book.IOC(pool.acquire(101l, 105l, Order.type.SELL)) == -2);
        assertTrue(book.pool.index == 0);
    }

    public void testMarketOrderFills()
    {
        Order.Pool pool = new Order.Pool(5);
        Order ord = pool.acquire(100l, 100l, Order.type.SELL);
        Orderbook book = new Orderbook();
        book.asks.computeIfAbsent(ord.price, k -> new ArrayList<Order>()).add(ord);
        assertTrue(book.IOC(pool.acquire(100l, 100l, Order.type.BUY)) == 0);
        assertTrue(book.pool.index == 1);
    }

    public void testFOKRejectsInsufficientLiquidity()
    {
        Order.Pool pool = new Order.Pool(5);
        Order ord = pool.acquire(100l, 50l, Order.type.SELL);
        Orderbook book = new Orderbook();
        book.asks.computeIfAbsent(ord.price, k -> new ArrayList<Order>()).add(ord);
        assertTrue(book.FOK(pool.acquire(100l, 100l, Order.type.BUY)) == -2);
        assertTrue(book.pool.index == 0);
    }

    public void testFOKFillsWhenSufficientLiquidity()
    {
        Order.Pool pool = new Order.Pool(5);
        Orderbook book = new Orderbook();
        book.GOC(pool.acquire(100l, 150l, Order.type.BUY));
        assertTrue(book.FOK(pool.acquire(100l, 100l, Order.type.SELL)) == 0);
        assertTrue(book.pool.index == 1);
    }

    public void testCancel()
    {
        Order.Pool pool = new Order.Pool(5);
        Orderbook book = new Orderbook();
        Order ord = pool.acquire(100l, 150l, Order.type.BUY);
        book.GOC(ord);
        assertTrue(book.Cancel(ord) == 0);
        assertTrue(book.bids.get(100l).isEmpty());
    }

    public void testPricePriority()
    {
        Order.Pool pool = new Order.Pool(5);
        Orderbook book = new Orderbook();
        book.GOC(pool.acquire(90l, 150l, Order.type.SELL));
        book.GOC(pool.acquire(100l, 150l, Order.type.SELL));
        assertTrue(book.Market_order(pool.acquire(0l, 150l, Order.type.BUY)) == 0);
        assertTrue(book.pool.pool[book.pool.index - 1].price == 90l);
    }


    public void testReplace()
    {
        Order.Pool pool = new Order.Pool(5);
        Orderbook book = new Orderbook();
        Order ord =pool.acquire(100l, 150l, Order.type.SELL);
        book.GOC(ord);
        Order replacement =pool.acquire(90l, 150l, Order.type.SELL);
        book.Replace(ord, replacement);
        assertTrue(book.asks.get(100l).isEmpty());
        assertTrue(book.asks.get(90l).getFirst() == replacement);
    }
}
