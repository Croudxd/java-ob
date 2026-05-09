package com.ben.javaob;

import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Collections;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.List;

import com.ben.javaob.Order;
import com.ben.javaob.Order.type;
import com.ben.javaob.Receipt;

public class Orderbook
{
    TreeMap</*Price*/Long, /*Order object*/ArrayList<Order>> bids;
    TreeMap<Long, ArrayList<Order>> asks;
    private TreeMap<Long, Long> askLiqudity;
    private TreeMap<Long, Long> bidLiqudity;
    Receipt.Pool pool; 

    public Orderbook()
    {
        bids = new TreeMap<>(Collections.reverseOrder());
        asks = new TreeMap<>();
        askLiqudity = new TreeMap<>();
        bidLiqudity = new TreeMap<>();
        pool = new Receipt.Pool(100);
    }

    /**
     * Function returns 0 for success, -2 if failed to match.
     **/
    public int match(Order ord)
    {

        if (ord._type == type.BUY)
        {
            // This sucks due to unboxing but i would have to move away from treemap
            for (long price : asks.keySet()) 
            {
                if (ord.price == 0) ord.price = price;
                if (price > ord.price ) break;
                ArrayList<Order> list = asks.get(price);
                while (ord.amount > 0 && !list.isEmpty())
                {
                    Order ask = list.getFirst();
                    if (ask.amount - ord.amount < 0l)
                    {
                        list.removeFirst();
                        pool.acquire(ask.id, ord.id, ask.price, ask.amount, System.nanoTime());
                        ord.amount -= ask.amount;
                        askLiqudity.merge(ask.price, -ask.amount, Long::sum);
                    }
                    else
                    {
                        ask.amount -= ord.amount;
                        pool.acquire(ask.id, ord.id, ask.price, ord.amount, System.nanoTime());
                        askLiqudity.merge(ask.price, -ord.amount, Long::sum);
                        ord.amount = 0l;
                    }
                }

            }
        }
        if (ord._type == type.SELL)
        {
            for (long price : bids.keySet()) 
            {
                if (ord.price == 0) ord.price = price;
                if (price < ord.price) break;
                ArrayList<Order> list = bids.get(price);
                while (ord.amount > 0 && !list.isEmpty())
                {
                    Order bid = list.getFirst();
                    if (bid.amount - ord.amount < 0.0)
                    {
                        list.removeFirst();
                        pool.acquire(ord.id, bid.id, bid.price, bid.amount, System.nanoTime());
                        ord.amount -= bid.amount;
                        bidLiqudity.merge(bid.price, -bid.amount, Long::sum);
                    }
                    else
                    {
                        bid.amount -= ord.amount;
                        pool.acquire(ord.id, bid.id, bid.price, ord.amount, System.nanoTime());
                        bidLiqudity.merge(bid.price, -ord.amount, Long::sum);
                        ord.amount = 0l;
                    }
                }
            }
        }

        return ord.amount > 0 ? -2 : 0;
    }

    public int IOC(Order ord)
    {
        return match(ord);
    }

    public int Market_order(Order ord)
    {
        ord.price = 0l;
        return match(ord);
    }

    public int FOK(Order ord)
    {
        if(ord._type == type.BUY)
        {
            long total = 0;
            // Unboxing again.
            for (long price : askLiqudity.keySet()) 
            {
                if (price > ord.price) break;
                total += askLiqudity.get(price);
                if(total >= ord.amount)
                {
                    return match(ord);
                }
            }
        }
        if(ord._type == type.SELL)
        {
            long total = 0;
            for (long price : bidLiqudity.keySet()) 
            {
                if (price < ord.price) break;
                total += bidLiqudity.get(price);
                if(total >= ord.amount)
                {
                    return match(ord);
                }
            }
        }
        return -2;
    }
    
    public int GOC(Order ord)
    {
        int rs = match(ord);
        if (rs == -2)
        {
            if(ord._type == type.BUY)
            {
                // Only allocates if missing, Needs warming up before production due to needing to allocate price levels.
                bids.computeIfAbsent(ord.price, k -> new ArrayList<Order>()).add(ord);
                return -2;
            }
            if(ord._type == type.SELL)
            {
                asks.computeIfAbsent(ord.price, k -> new ArrayList<Order>()).add(ord);
                return -2;
            }
        }
        return 0;
    }

    public void Cancel(Order ord)
    {
        if(ord._type == type.BUY)
        {
            bids.get(ord.price).remove(ord);
            bidLiqudity.merge(ord.price, ord.amount, Long::sum);
        }
        if(ord._type == type.SELL)
        {
            asks.get(ord.price).remove(ord);
            askLiqudity.merge(ord.price, ord.amount, Long::sum);
        }
    }

    public void Replace(Order ord, Order replacement)
    {
        Cancel(ord);
        Market_order(replacement);
    }
}
