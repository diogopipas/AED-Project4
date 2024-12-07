package aed.tables;

import java.util.ArrayList;
import java.util.Iterator;

public class LinearOpenAddressingHashTable<Key,Value> {

    private static int[] primes = {
            37, 79, 163, 331,
            673, 1361, 2729, 5471, 10949,
            21911, 43853, 87719, 175447, 350899,
            701819, 1403641, 2807303, 5614657,
            11229331, 22458671, 44917381, 89834777, 179669557
    };

    private int m;
    private int primeIndex;
    private int size;
    private float loadFactor;
    private Key[] keys;
    private Value[] values;
    private int deletedNotRemoved;

    @SuppressWarnings("unchecked")
    private LinearOpenAddressingHashTable(int primeIndex)
    {
        this.primeIndex = primeIndex;
        this.m = primes[primeIndex];
        this.size = 0; this.loadFactor = 0;
        this.keys = (Key[]) new Object[this.m];
        this.values = (Value[]) new Object[this.m];
        this.deletedNotRemoved = 0;
    }

    public LinearOpenAddressingHashTable(){this(0);}


    public int size()
    {
        return this.size;
    }

    public int getCapacity()
    {
        return this.m;
    }

    public float getLoadFactor()
    {
        return this.loadFactor;
    }

    public int getDeletedNotRemoved()
    {
        return this.deletedNotRemoved;
    }

    public boolean containsKey(Key k)
    {
        for(int i = hash(k); this.keys[i] != null; i = (i+1) % this.m){
            if(keys[i].equals(k)){
                return true;
            }
        }
        return false;
    }

    private int hash(Key k){
        return (k.hashCode() & 0x7fffffff) % this.m;
    }


    public Value get(Key k)
    {
        for(int i = hash(k); this.keys[i] != null; i = (i+1) % this.m){
            if(this.keys[i].equals(k)){
                return this.values[i];
            }
        }

        return null;
    }

    private void resize(int primeIndex)
    {
        if(primeIndex < 0 || primeIndex >= primes.length) return;

        this.primeIndex = primeIndex;
        LinearOpenAddressingHashTable<Key,Value> aux =
                new LinearOpenAddressingHashTable<>(this.primeIndex);

        for(int i = 0; i < this.m; i++)
        {
            if(keys[i] != null) aux.put(keys[i],values[i]);
        }


        this.keys = aux.keys;
        this.values = aux.values;
        this.m = aux.m;
        this.loadFactor = (float) this.size/this.m;
        this.deletedNotRemoved = 0;

    }

    public void put(Key k, Value v)
    {
        if(this.loadFactor >= 0.5f){
            resize(this.primeIndex + 1);
        }

        if(v == null){
            delete(k);
            return;
        }

        int i = hash(k);
        for(; this.keys[i] != null; i = (i+1) % this.m){
            if(this.keys[i].equals(k)){
                this.values[i] = v;
                return;
            }
        }


        this.keys[i] = k;
        this.values[i] = v;
        this.size++;
        this.loadFactor = (float) this.size / this.m;

    }

    public void delete(Key k) {
        int i = hash(k);
        while (true) {
            if (this.keys[i] == null) return;

            if (this.keys[i].equals(k)) break;
            i = (i+1)%this.m;
        }

        this.keys[i] = null;
        this.values[i] = null;
        this.size--;
        this.deletedNotRemoved++;

        i = (i+1) % this.m;

        while(this.keys[i] != null) {
            Key auxKey = this.keys[i];
            Value auxValue = this.values[i];
            //remove from previous position
            this.keys[i] = null;
            this.values[i] = null;
            //temporarily reduce size,
            //next put will increment it
            this.size--;
            //add the key and value again
            this.put(auxKey, auxValue);
            i = (i + 1) % this.m;
        }


        this.loadFactor = (float) this.size/this.m;
        if(this.loadFactor < 0.125f)
            resize(this.primeIndex-1);

        //Calculate percentage of deleted keys
        float deletedPercentage = (float) this.deletedNotRemoved / this.m;

        //call resize with the same prime index just to remove deleted keys
        if(deletedPercentage >= 0.2f){
            //same index because we just want to remove nulls, not alter the maximum capacity
            resize(this.primeIndex);
        }
    }

    @SuppressWarnings("unchecked")
    public Iterable<Key> keys() {
        ArrayList<Key> keysArr = new ArrayList<>();
        for(Key key : this.keys){
            if(key != null){
                keysArr.add(key);
            }
        }
        return keysArr;
    }

    public static void main(String[] args)
    {
        //implement tests here
    }
}
